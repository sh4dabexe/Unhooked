package com.unhooked.app.ui.block

import android.app.Application
import android.app.TimePickerDialog
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unhooked.app.UnhookedApp
import com.unhooked.app.domain.model.ProtectionMode
import com.unhooked.app.domain.model.ScheduleModel
import com.unhooked.app.domain.security.EmergencyUnlockManager
import com.unhooked.app.domain.security.SecurityUtil
import com.unhooked.app.domain.security.UnlockCredentials
import com.unhooked.app.ui.settings.EmergencyUnlockScreen
import com.unhooked.app.ui.theme.CardShape
import com.unhooked.app.ui.theme.IconBoxShape
import com.unhooked.app.ui.theme.PastelCoral
import com.unhooked.app.ui.theme.PastelCyan
import com.unhooked.app.ui.theme.PastelGreen
import com.unhooked.app.ui.theme.PastelYellow
import com.unhooked.app.ui.theme.PillShape
import com.unhooked.app.ui.theme.PrimaryPurple
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ScheduleUiState(
    val schedules: List<ScheduleModel> = emptyList()
)

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UnhookedApp.repository!!
    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allSchedulesFlow.collectLatest { schedules ->
                _uiState.value = _uiState.value.copy(schedules = schedules)
            }
        }
    }

    fun saveSchedule(schedule: ScheduleModel) {
        viewModelScope.launch {
            repository.saveSchedule(schedule)
        }
    }

    /**
     * Save a schedule and generate emergency unlock credentials if Admin mode.
     * Returns the schedule ID and credentials (if Admin).
     */
    suspend fun saveScheduleWithProtection(schedule: ScheduleModel): Pair<Long, UnlockCredentials?> {
        val id = repository.saveSchedule(schedule)
        var credentials: UnlockCredentials? = null
        if (schedule.protectionMode == ProtectionMode.ADMIN) {
            credentials = repository.emergencyUnlockManager.generateUnlockCredentials(id)
        }
        return Pair(id, credentials)
    }

    fun deleteSchedule(schedule: ScheduleModel) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
        }
    }

    /**
     * Delete with authentication check: Admin blocks cannot be deleted, Password blocks need PIN.
     */
    fun deleteScheduleWithAuth(
        schedule: ScheduleModel,
        pin: String? = null,
        onError: (String) -> Unit
    ) {
        when (schedule.protectionMode) {
            ProtectionMode.ADMIN -> {
                onError("Admin-mode blocks cannot be deleted. Use QR or passphrase to unlock first.")
            }
            ProtectionMode.PASSWORD -> {
                if (pin.isNullOrBlank()) {
                    onError("PIN required to delete this block.")
                    return
                }
                if (!SecurityUtil.verifyPassword(pin, schedule.pinHash, schedule.pinSalt)) {
                    onError("Incorrect PIN.")
                    return
                }
                deleteSchedule(schedule)
            }
            ProtectionMode.NORMAL -> {
                deleteSchedule(schedule)
            }
        }
    }

    fun toggleSchedule(schedule: ScheduleModel) {
        when (schedule.protectionMode) {
            ProtectionMode.ADMIN -> return // Cannot toggle admin blocks
            else -> {
                viewModelScope.launch {
                    repository.saveSchedule(schedule.copy(enabled = !schedule.enabled))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScheduleContent(
    viewModel: ScheduleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf<ScheduleModel?>(null) }

    // Emergency unlock reveal state
    var showEmergencyReveal by remember { mutableStateOf(false) }
    var emergencyCredentials by remember { mutableStateOf<UnlockCredentials?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Add schedule button
        Button(
            onClick = { showCreateDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = PillShape,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Schedule", fontWeight = FontWeight.Bold)
        }

        if (uiState.schedules.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "📅", fontSize = 40.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No schedules yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Create time-based blocking rules",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.schedules, key = { it.id }) { schedule ->
                ScheduleCard(
                    schedule = schedule,
                    onToggle = { viewModel.toggleSchedule(schedule) },
                    onDelete = {
                        when (schedule.protectionMode) {
                            ProtectionMode.ADMIN -> {
                                Toast.makeText(context,
                                    "Admin-mode blocks cannot be deleted. Use QR/passphrase to unlock first.",
                                    Toast.LENGTH_LONG).show()
                            }
                            ProtectionMode.PASSWORD -> {
                                showPinDialog = schedule
                            }
                            ProtectionMode.NORMAL -> {
                                viewModel.deleteSchedule(schedule)
                            }
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    if (showCreateDialog) {
        CreateScheduleDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { schedule ->
                scope.launch {
                    val (id, credentials) = viewModel.saveScheduleWithProtection(schedule)
                    showCreateDialog = false
                    if (credentials != null) {
                        emergencyCredentials = credentials
                        showEmergencyReveal = true
                    }
                }
            }
        )
    }

    // PIN verification dialog for Password-mode deletion
    if (showPinDialog != null) {
        var pinInput by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPinDialog = null },
            title = { Text("Enter PIN to Delete") },
            text = {
                Column {
                    Text("This block is password-protected. Enter the PIN to delete it.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it; pinError = "" },
                        placeholder = { Text("Enter PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(pinError, color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteScheduleWithAuth(showPinDialog!!, pinInput) { error ->
                            pinError = error
                        }
                        if (pinError.isEmpty()) showPinDialog = null
                    },
                    shape = PillShape
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = null }) { Text("Cancel") }
            }
        )
    }

    // Emergency unlock reveal (full-screen overlay)
    if (showEmergencyReveal && emergencyCredentials != null) {
        EmergencyUnlockScreen(
            qrBitmap = emergencyCredentials!!.qrBitmap,
            passphrase = emergencyCredentials!!.passphrase,
            onConfirmed = {
                showEmergencyReveal = false
                emergencyCredentials = null
            }
        )
    }
}

@Composable
fun ScheduleCard(
    schedule: ScheduleModel,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val dayNames = listOf("", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val daysText = schedule.daysOfWeek.mapNotNull { dayNames.getOrNull(it) }.joinToString(", ")
    val timeText = String.format(
        "%02d:%02d – %02d:%02d",
        schedule.startHour, schedule.startMinute,
        schedule.endHour, schedule.endMinute
    )

    val isAdminLocked = schedule.protectionMode == ProtectionMode.ADMIN

    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            when (schedule.protectionMode) {
                                ProtectionMode.ADMIN -> PastelCoral
                                ProtectionMode.PASSWORD -> PastelYellow
                                else -> if (schedule.enabled) PastelCyan else MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = IconBoxShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (schedule.protectionMode) {
                            ProtectionMode.ADMIN -> Icons.Rounded.AdminPanelSettings
                            ProtectionMode.PASSWORD -> Icons.Rounded.Lock
                            else -> Icons.Rounded.Schedule
                        },
                        contentDescription = "Schedule",
                        tint = Color(0xFF1F1A24),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = schedule.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (schedule.protectionMode != ProtectionMode.NORMAL) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isAdminLocked) "🔐" else "🔒",
                                fontSize = 14.sp
                            )
                        }
                    }
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = daysText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isAdminLocked) {
                    Switch(
                        checked = schedule.enabled,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryPurple
                        )
                    )
                }
                if (!isAdminLocked) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateScheduleDialog(
    onDismiss: () -> Unit,
    onSave: (ScheduleModel) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var startHour by remember { mutableIntStateOf(9) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(17) }
    var endMinute by remember { mutableIntStateOf(0) }
    val selectedDays = remember { mutableStateListOf(2, 3, 4, 5, 6) } // Mon–Fri
    var selectedMode by remember { mutableStateOf(ProtectionMode.NORMAL) }
    var pinInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    val dayLabels = listOf("Sun" to 1, "Mon" to 2, "Tue" to 3, "Wed" to 4, "Thu" to 5, "Fri" to 6, "Sat" to 7)
    val modeLabels = listOf(
        ProtectionMode.NORMAL to "Normal",
        ProtectionMode.PASSWORD to "Password",
        ProtectionMode.ADMIN to "Admin"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Schedule", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Schedule Name") },
                    placeholder = { Text("e.g. Work Hours") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Time pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Start", style = MaterialTheme.typography.labelMedium)
                        Card(
                            shape = CardShape,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    TimePickerDialog(
                                        context, { _, h, m ->
                                            startHour = h; startMinute = m
                                        }, startHour, startMinute, true
                                    ).show()
                                }
                        ) {
                            Text(
                                text = String.format("%02d:%02d", startHour, startMinute),
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("End", style = MaterialTheme.typography.labelMedium)
                        Card(
                            shape = CardShape,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    TimePickerDialog(
                                        context, { _, h, m ->
                                            endHour = h; endMinute = m
                                        }, endHour, endMinute, true
                                    ).show()
                                }
                        ) {
                            Text(
                                text = String.format("%02d:%02d", endHour, endMinute),
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Day selector
                Text("Repeat on", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    dayLabels.forEach { (label, dayValue) ->
                        FilterChip(
                            selected = dayValue in selectedDays,
                            onClick = {
                                if (dayValue in selectedDays) selectedDays.remove(dayValue)
                                else selectedDays.add(dayValue)
                            },
                            label = { Text(label, fontSize = 12.sp) },
                            shape = PillShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryPurple,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Protection Mode selector
                Text("Protection Mode", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    modeLabels.forEach { (mode, label) ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { selectedMode = mode },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (mode) {
                                            ProtectionMode.ADMIN -> Icons.Rounded.AdminPanelSettings
                                            ProtectionMode.PASSWORD -> Icons.Rounded.Lock
                                            else -> Icons.Rounded.LockOpen
                                        },
                                        contentDescription = label,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(label, fontSize = 12.sp)
                                }
                            },
                            shape = PillShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (mode) {
                                    ProtectionMode.ADMIN -> PastelCoral
                                    ProtectionMode.PASSWORD -> PastelYellow
                                    else -> PastelGreen
                                },
                                selectedLabelColor = Color(0xFF1F1A24)
                            )
                        )
                    }
                }

                // PIN input for Password mode
                if (selectedMode == ProtectionMode.PASSWORD) {
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it },
                        label = { Text("Set PIN (6+ digits)") },
                        placeholder = { Text("Enter PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Admin mode warning
                if (selectedMode == ProtectionMode.ADMIN) {
                    Card(
                        shape = CardShape,
                        colors = CardDefaults.cardColors(
                            containerColor = PastelCoral.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = "⚠️ Admin mode will lock this schedule. You'll receive a QR code and passphrase for emergency unlock.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && selectedDays.isNotEmpty()) {
                        val pinHash: String
                        val pinSalt: String
                        if (selectedMode == ProtectionMode.PASSWORD && pinInput.isNotBlank()) {
                            pinSalt = SecurityUtil.generateSalt()
                            pinHash = SecurityUtil.hashPassword(pinInput, pinSalt)
                        } else {
                            pinHash = ""
                            pinSalt = ""
                        }

                        onSave(
                            ScheduleModel(
                                name = name,
                                startHour = startHour,
                                startMinute = startMinute,
                                endHour = endHour,
                                endMinute = endMinute,
                                daysOfWeek = selectedDays.sorted(),
                                targetPackages = emptyList(), // Applies to all blocked apps
                                enabled = true,
                                protectionMode = selectedMode,
                                pinHash = pinHash,
                                pinSalt = pinSalt
                            )
                        )
                    }
                },
                shape = PillShape,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
