package com.unhooked.app.ui.blocked

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unhooked.app.ui.theme.CardShape
import com.unhooked.app.ui.theme.HeroCardShape
import com.unhooked.app.ui.theme.IconBoxShape
import com.unhooked.app.ui.theme.PastelCoral
import com.unhooked.app.ui.theme.PastelPurple
import com.unhooked.app.ui.theme.PillShape
import com.unhooked.app.ui.theme.PrimaryPurple
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val motivationalMessages = listOf(
    "Your future self will thank you for this moment.",
    "Small steps lead to big changes. Keep going!",
    "You're stronger than the urge. Stay focused.",
    "Real freedom is choosing what deserves your attention.",
    "Every minute you resist builds discipline.",
    "Take a deep breath. You've got this.",
    "Your goals are worth more than a quick scroll.",
    "This moment of resistance is building your strength.",
    "Be present. The digital world can wait.",
    "You chose this boundary for a reason. Honor it."
)

@Composable
fun BlockedScreen(
    appName: String,
    reason: String,
    unlockTimeMs: Long,
    isStrictMode: Boolean = false,
    onGoHome: () -> Unit
) {
    val unlockTimeStr = if (unlockTimeMs > 0) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(unlockTimeMs))
    } else null

    // Live countdown
    var remainingMs by remember { mutableLongStateOf(
        if (unlockTimeMs > 0) (unlockTimeMs - System.currentTimeMillis()).coerceAtLeast(0)
        else 0L
    ) }

    // Rotating motivational message
    var messageIndex by remember { mutableStateOf((System.currentTimeMillis() % motivationalMessages.size).toInt()) }

    LaunchedEffect(unlockTimeMs) {
        while (unlockTimeMs > 0) {
            val now = System.currentTimeMillis()
            remainingMs = (unlockTimeMs - now).coerceAtLeast(0)
            if (remainingMs <= 0) break
            delay(1000L)
        }
    }

    // Rotate message every 8 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(8000L)
            messageIndex = (messageIndex + 1) % motivationalMessages.size
        }
    }

    val countdownStr = if (remainingMs > 0) {
        val hours = remainingMs / 3_600_000
        val minutes = (remainingMs % 3_600_000) / 60_000
        val seconds = (remainingMs % 60_000) / 1_000
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .background(
                        if (isStrictMode) PastelCoral else PastelPurple,
                        shape = IconBoxShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isStrictMode) Icons.Rounded.Lock else Icons.Rounded.Block,
                    contentDescription = "Unhooked Block",
                    tint = Color(0xFF1F1A24),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isStrictMode) "This Session is Locked." else "Stay Focused.",
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = motivationalMessages[messageIndex],
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Center card with app details, reason & countdown
        Card(
            shape = HeroCardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = reason,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Live countdown timer
                if (countdownStr != null) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = countdownStr,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        ),
                        color = PrimaryPurple
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "remaining",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (unlockTimeStr != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Available again at $unlockTimeStr",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isStrictMode) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(PastelCoral.copy(alpha = 0.3f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🔒 Strict Mode Active",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Bottom Safe Navigation Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onGoHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = PillShape,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Go to Home Screen",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
