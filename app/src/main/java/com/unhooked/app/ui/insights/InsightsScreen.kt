package com.unhooked.app.ui.insights

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unhooked.app.ui.components.PastelPillChart
import com.unhooked.app.ui.components.PastelStatCard
import com.unhooked.app.ui.components.SegmentedPill
import com.unhooked.app.ui.theme.CardShape
import com.unhooked.app.ui.theme.IconBoxShape
import com.unhooked.app.ui.theme.PastelCoral
import com.unhooked.app.ui.theme.PastelCyan
import com.unhooked.app.ui.theme.PastelGreen
import com.unhooked.app.ui.theme.PastelYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Insights & Activity",
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Timeframe selector (Day / Week / Month)
        item {
            SegmentedPill(
                options = listOf("Day", "Week", "Month"),
                selectedIndex = uiState.selectedTab,
                onOptionSelected = { viewModel.setSelectedTab(it) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Pastel Pill Bar Chart Card (inspired by 125147.jpg)
        item {
            Card(
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Focus Consistency",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Text(
                            text = "+18% this week",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF15803D)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    PastelPillChart(data = uiState.chartData)
                }
            }
        }

        // Streak & Time Saved Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PastelStatCard(
                    modifier = Modifier.weight(1f),
                    title = "🔥 Streak",
                    subtitle = "Consecutive days",
                    value = "${uiState.streakDays} days",
                    icon = Icons.Rounded.HourglassTop,
                    iconBgColor = PastelYellow
                )

                PastelStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Time Saved",
                    subtitle = "From blocked apps",
                    value = "${uiState.timeSavedMinutes} min",
                    icon = Icons.AutoMirrored.Rounded.TrendingUp,
                    iconBgColor = PastelGreen
                )
            }
        }

        // 2 Stat cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PastelStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Focus Time",
                    subtitle = "Productive work",
                    value = "${uiState.totalFocusTimeMinutes} min",
                    icon = Icons.Rounded.HourglassTop,
                    iconBgColor = PastelCyan
                )

                PastelStatCard(
                    modifier = Modifier.weight(1f),
                    title = "Distractions",
                    subtitle = "Intercepted",
                    value = "${uiState.blockedAttemptsCount} times",
                    icon = Icons.Rounded.Shield,
                    iconBgColor = PastelCoral
                )
            }
        }

        // Recent Blocked Events Section
        item {
            Text(
                text = "Recent Blocked Attempts",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (uiState.recentBlocks.isEmpty()) {
            item {
                Text(
                    text = "No distracting apps opened recently. Great discipline!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(uiState.recentBlocks) { block ->
                val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(block.timestamp))
                Card(
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(PastelCoral.copy(alpha = 0.5f), shape = IconBoxShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Block,
                                    contentDescription = "Blocked",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = block.appName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = block.reason.displayName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.labelMedium,
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
}
