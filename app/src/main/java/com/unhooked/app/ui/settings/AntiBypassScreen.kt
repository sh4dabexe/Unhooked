package com.unhooked.app.ui.settings

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unhooked.app.domain.model.ProtectionMode
import com.unhooked.app.system.deviceadmin.UnhookedAdminReceiver
import com.unhooked.app.ui.theme.CardShape
import com.unhooked.app.ui.theme.IconBoxShape
import com.unhooked.app.ui.theme.PastelCoral
import com.unhooked.app.ui.theme.PastelCyan
import com.unhooked.app.ui.theme.PastelGreen
import com.unhooked.app.ui.theme.PastelPurple
import com.unhooked.app.ui.theme.PillShape
import com.unhooked.app.ui.theme.PrimaryPurple

@Composable
fun AntiBypassScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showAdminDialog by remember { mutableStateOf(false) }
    var inputPassword by remember { mutableStateOf("") }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Anti-Bypass Protection",
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose your level of commitment to prevent impulse bypassing of focus rules.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Mode 1: Normal
        item {
            ModeCard(
                title = "Normal Mode",
                subtitle = "Flexible • Edit and disable rules without restriction",
                icon = Icons.Rounded.LockOpen,
                iconBg = PastelCyan,
                isSelected = uiState.protectionMode == ProtectionMode.NORMAL,
                onClick = { viewModel.setProtectionMode(ProtectionMode.NORMAL) }
            )
        }

        // Mode 2: Password
        item {
            ModeCard(
                title = "Password Protected",
                subtitle = "Protected • Changing rules requires your security PIN/password",
                icon = Icons.Rounded.Password,
                iconBg = PastelPurple,
                isSelected = uiState.protectionMode == ProtectionMode.PASSWORD,
                onClick = {
                    if (!uiState.hasPasswordSet) {
                        showPasswordDialog = true
                    } else {
                        viewModel.setProtectionMode(ProtectionMode.PASSWORD)
                    }
                }
            )
        }

        // Mode 3: Admin Protection (replaces both old Admin and Strict)
        item {
            ModeCard(
                title = "Admin Protection",
                subtitle = "Unbreakable • Locks rules, prevents uninstall, requires QR/passphrase to exit",
                icon = Icons.Rounded.AdminPanelSettings,
                iconBg = PastelCoral,
                isSelected = uiState.protectionMode == ProtectionMode.ADMIN,
                onClick = {
                    if (!uiState.isDeviceAdminGranted) {
                        // Prompt to enable Device Admin first
                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                            putExtra(
                                DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                                ComponentName(context, UnhookedAdminReceiver::class.java)
                            )
                            putExtra(
                                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                "Admin Protection prevents bypassing focus rules by uninstalling Unhooked. You'll need a QR code or passphrase to disable it."
                            )
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) { }
                    } else {
                        showAdminDialog = true
                    }
                }
            )
        }

        // Admin mode info banner
        if (uiState.isAdminActive) {
            item {
                Card(
                    shape = CardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = PastelCoral.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "🔐 Admin Mode Active",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "All active Admin-mode blocks are frozen. Use QR code or passphrase to unlock individual blocks.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }

    // Set Password Dialog
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Set Protection PIN/Password") },
            text = {
                Column {
                    Text(
                        text = "Enter a password or PIN. Passwords are saved with a salted cryptographic hash.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputPassword,
                        onValueChange = { inputPassword = it },
                        placeholder = { Text("Enter PIN or Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputPassword.isNotBlank()) {
                            viewModel.setPassword(inputPassword)
                            viewModel.setProtectionMode(ProtectionMode.PASSWORD)
                            showPasswordDialog = false
                        }
                    },
                    shape = PillShape
                ) {
                    Text("Save & Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Admin Mode Confirmation Dialog
    if (showAdminDialog) {
        AlertDialog(
            onDismissRequest = { showAdminDialog = false },
            title = { Text("Enable Admin Protection?") },
            text = {
                Column {
                    Text(
                        text = "⚠️ Warning: Admin Protection will:",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Freeze all Admin-mode block schedules — no edits, no deletes\n" +
                                "• Block access to device Settings to prevent uninstalling Unhooked\n" +
                                "• The ONLY way out is a QR code or passphrase generated per block",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "You'll generate an emergency unlock key when you create each Admin-mode block.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setProtectionMode(ProtectionMode.ADMIN)
                        showAdminDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = PillShape
                ) {
                    Text("Enable Admin Protection")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ModeCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) PrimaryPurple else Color.Transparent,
                shape = CardShape
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
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
                        .background(iconBg, shape = IconBoxShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color(0xFF1F1A24),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Selected",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
