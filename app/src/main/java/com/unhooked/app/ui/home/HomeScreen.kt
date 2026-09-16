package com.unhooked.app.ui.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unhooked.app.ui.components.PastelStatCard
import com.unhooked.app.ui.components.SegmentedPill
import com.unhooked.app.ui.theme.CardShape
import com.unhooked.app.ui.theme.IconBoxShape
import com.unhooked.app.ui.theme.PastelCoral
import com.unhooked.app.ui.theme.PastelCyan
import com.unhooked.app.ui.theme.PastelGreen
import com.unhooked.app.ui.theme.PastelPurple
import com.unhooked.app.ui.theme.PastelYellow
import com.unhooked.app.ui.theme.PillShape
import com.unhooked.app.ui.theme.PrimaryPurple
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToFocus: () -> Unit,
    onNavigateToBlock: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formattedDate = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // --- Header (Date pill and Profile Avatar) ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date pill dropdown
                Row(
                    modifier = Modifier
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                            PillShape
                        )
                        .clip(PillShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { /* Date filter */ }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = "Date",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Dropdown",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Profile Avatar pill button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                            CircleShape
                        )
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onNavigateToSettings() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // --- Friendly Heading ---
        item {
            Column(modifier = Modifier.padding(top = 6.dp)) {
                Text(
                    text = "Greetings ${uiState.userName},",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Normal
                    )
                )
                Text(
                    text = "Ready to Focus Today?",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        // --- Quick Stat Horizontal Carousel (matching 125147.jpg) ---
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    MetricChip(
                        icon = Icons.Rounded.LocalFireDepartment,
                        iconBg = PastelYellow,
                        label = "Controlled:",
                        value = "${uiState.todayControlledMinutes}/${uiState.overallLimitMinutes}m"
                    )
                }
                item {
                    MetricChip(
                        icon = Icons.Rounded.HourglassBottom,
                        iconBg = PastelCyan,
                        label = "Focus Time:",
                        value = "${uiState.todayFocusMinutes}m / 120m"
                    )
                }
                item {
                    MetricChip(
                        icon = Icons.Rounded.Shield,
                        iconBg = PastelGreen,
                        label = "Blocked:",
                        value = "${uiState.todayBlockedAttempts} attempts"
                    )
                }
            }
        }

        // --- Segmented Toggle Row: Daily/Weekly and Layout ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SegmentedPill(
                    options = listOf("Daily", "Weekly"),
                    icons = listOf(Icons.Rounded.CalendarToday, Icons.Rounded.FilterList),
                    selectedIndex = uiState.selectedTimeframeIndex,
                    onOptionSelected = { viewModel.setTimeframeIndex(it) }
                )

                SegmentedPill(
                    options = listOf("Grid", "Compact"),
                    icons = listOf(Icons.Rounded.GridView, Icons.Rounded.ViewAgenda),
                    selectedIndex = uiState.selectedLayoutIndex,
                    onOptionSelected = { viewModel.setLayoutIndex(it) }
                )
            }
        }

        // --- Permission Banner if Usage Access not granted ---
        if (!uiState.hasUsagePermission) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = PastelCoral.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Usage Permission Needed",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Required to accurately track limits and screen time.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = onNavigateToPermissions,
                            shape = PillShape,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                        ) {
                            Text("Enable", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // --- 4 Pastel Stat Cards (2x2 Grid or Stack) ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    PastelStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Overall Timer",
                        subtitle = "Today's allowance",
                        value = "${(uiState.overallLimitMinutes - uiState.todayControlledMinutes).coerceAtLeast(0)}m left",
                        icon = Icons.Rounded.LocalFireDepartment,
                        iconBgColor = PastelYellow,
                        onClick = onNavigateToBlock
                    )

                    PastelStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Focus Time",
                        subtitle = "Productive work",
                        value = "${uiState.todayFocusMinutes} min",
                        icon = Icons.Rounded.HourglassBottom,
                        iconBgColor = PastelCyan,
                        onClick = onNavigateToFocus
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    PastelStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Blocked Trips",
                        subtitle = "Habits intercepted",
                        value = "${uiState.todayBlockedAttempts} times",
                        icon = Icons.Rounded.Shield,
                        iconBgColor = PastelGreen,
                        onClick = onNavigateToBlock
                    )

                    PastelStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Distractions",
                        subtitle = "Controlled apps",
                        value = "${uiState.topApps.size} active",
                        icon = Icons.Rounded.NotificationsOff,
                        iconBgColor = PastelCoral,
                        onClick = onNavigateToBlock
                    )
                }
            }
        }

        // --- Quick Actions Bar ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Instant 25m Focus",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Silences distraction rules immediately",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.startQuickFocus(25)
                            onNavigateToFocus()
                        },
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Start",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Start", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp)) // Bottom nav padding
        }
    }
}

@Composable
fun MetricChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    label: String,
    value: String
) {
    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconBg, shape = IconBoxShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFF1F1A24)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
