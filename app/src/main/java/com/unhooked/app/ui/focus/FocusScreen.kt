package com.unhooked.app.ui.focus

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unhooked.app.domain.engine.TimerEngine
import com.unhooked.app.domain.model.TimerStatus
import com.unhooked.app.domain.model.TimerType
import com.unhooked.app.ui.components.SegmentedPill
import com.unhooked.app.ui.theme.HeroCardShape
import com.unhooked.app.ui.theme.PillShape
import com.unhooked.app.ui.theme.PrimaryPurple

@Composable
fun FocusScreen(
    viewModel: FocusViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = "Deep Focus",
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Mode Selector: Countdown vs Pomodoro vs Stopwatch
        SegmentedPill(
            options = listOf("Countdown", "Pomodoro", "Stopwatch"),
            selectedIndex = uiState.timerType.ordinal,
            onOptionSelected = { index ->
                viewModel.setTimerType(TimerType.values()[index])
            }
        )

        // Large Hero Timer Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = HeroCardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular Ring with central time digits
                Box(
                    modifier = Modifier.size(230.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { uiState.progress },
                        modifier = Modifier.fillMaxSize(),
                        color = PrimaryPurple,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 14.dp
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = TimerEngine.formatDuration(uiState.remainingMs),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = if (uiState.timerStatus == TimerStatus.RUNNING) "Focusing..."
                            else if (uiState.timerStatus == TimerStatus.PAUSED) "Paused"
                            else "Ready",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Duration chips (when idle)
                if (uiState.timerStatus == TimerStatus.IDLE) {
                    val presets = listOf(15, 25, 45, 60, 90)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        items(presets) { minutes ->
                            FilterChip(
                                selected = uiState.durationMinutes == minutes,
                                onClick = { viewModel.setDuration(minutes) },
                                label = { Text("${minutes}m") },
                                shape = PillShape,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }

                // Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (uiState.timerStatus) {
                        TimerStatus.IDLE, TimerStatus.COMPLETED -> {
                            Button(
                                onClick = { viewModel.startTimer() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = PillShape,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = "Start")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start Focus Session", fontWeight = FontWeight.Bold)
                            }
                        }
                        TimerStatus.RUNNING -> {
                            if (!uiState.isStrict) {
                                OutlinedButton(
                                    onClick = { viewModel.pauseTimer() },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    shape = PillShape
                                ) {
                                    Icon(Icons.Rounded.Pause, contentDescription = "Pause")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pause")
                                }
                            }

                            Button(
                                onClick = { viewModel.stopTimer() },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = PillShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Rounded.Stop, contentDescription = "End")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("End")
                            }
                        }
                        TimerStatus.PAUSED -> {
                            Button(
                                onClick = { viewModel.resumeTimer() },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = PillShape,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = "Resume")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Resume")
                            }

                            OutlinedButton(
                                onClick = { viewModel.stopTimer() },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = PillShape
                            ) {
                                Icon(Icons.Rounded.Stop, contentDescription = "Stop")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Give Up")
                            }
                        }
                        else -> {}
                    }
                }
            }
        }

        // Strict Mode Notice if active
        if (uiState.isStrict) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = "Locked",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Strict Mode is active: Session cannot be paused or casually aborted.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
