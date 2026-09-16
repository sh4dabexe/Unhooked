package com.unhooked.app.ui.block

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unhooked.app.ui.components.SegmentedPill
import com.unhooked.app.ui.theme.CardShape
import com.unhooked.app.ui.theme.IconBoxShape
import com.unhooked.app.ui.theme.PastelCoral
import com.unhooked.app.ui.theme.PastelGreen
import com.unhooked.app.ui.theme.PastelYellow
import com.unhooked.app.ui.theme.PillShape
import com.unhooked.app.ui.theme.PrimaryPurple

@Composable
fun BlockScreen(
    viewModel: BlockViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedAppForEdit by remember { mutableStateOf<InstalledAppItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = "App Blocker",
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            placeholder = { Text("Search apps (e.g. Instagram, Games)...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            singleLine = true,
            shape = PillShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Tab selection
        SegmentedPill(
            options = listOf("Apps", "Schedules", "Protected Whitelist"),
            selectedIndex = uiState.selectedTab,
            onOptionSelected = { viewModel.setSelectedTab(it) },
            modifier = Modifier.fillMaxWidth()
        )

        // App list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.filteredApps, key = { it.packageName }) { appItem ->
                AppRuleRow(
                    item = appItem,
                    onToggleBlock = { viewModel.toggleAppBlock(appItem) },
                    onClick = {
                        if (!appItem.isCritical) {
                            selectedAppForEdit = appItem
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    // Daily Limit Edit Dialog
    selectedAppForEdit?.let { appItem ->
        var limitMinutes by remember {
            mutableStateOf(appItem.rule?.dailyLimitMinutes?.toFloat() ?: 30f)
        }

        AlertDialog(
            onDismissRequest = { selectedAppForEdit = null },
            title = { Text(text = "Limit for ${appItem.appName}") },
            text = {
                Column {
                    Text(
                        text = "Daily usage allowance: ${limitMinutes.toInt()} minutes",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Slider(
                        value = limitMinutes,
                        onValueChange = { limitMinutes = it },
                        valueRange = 0f..180f,
                        steps = 11 // 15 min increments
                    )
                    Text(
                        text = if (limitMinutes.toInt() == 0) "No daily limit" else "Blocks automatically when exceeded.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setAppDailyLimit(appItem, limitMinutes.toInt())
                    selectedAppForEdit = null
                }) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedAppForEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AppRuleRow(
    item: InstalledAppItem,
    onToggleBlock: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
                // Icon squircle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (item.isCritical) PastelGreen
                            else if (item.rule?.isHardBlocked == true) PastelCoral
                            else MaterialTheme.colorScheme.surfaceVariant,
                            shape = IconBoxShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.isCritical) Icons.Rounded.Shield
                        else if (item.rule?.isHardBlocked == true) Icons.Rounded.Block
                        else Icons.Rounded.Apps,
                        contentDescription = item.appName,
                        tint = Color(0xFF1F1A24),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = item.appName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (item.isCritical) {
                        Text(
                            text = "Protected (Financial / Gov / Core)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                            color = Color(0xFF15803D) // Dark green
                        )
                    } else if (item.rule?.dailyLimitMinutes ?: 0 > 0) {
                        Text(
                            text = "Daily limit: ${item.rule?.dailyLimitMinutes} min",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (item.rule?.isHardBlocked == true) {
                        Text(
                            text = "Instant Block active",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text(
                            text = item.packageName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                }
            }

            if (!item.isCritical) {
                Switch(
                    checked = item.rule?.isHardBlocked == true,
                    onCheckedChange = { onToggleBlock() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.error
                    )
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Safe",
                    tint = Color(0xFF15803D),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
