package com.unhooked.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unhooked.app.ui.theme.PastelYellow
import com.unhooked.app.ui.theme.PillShape

data class ChartBarData(
    val label: String, // e.g. "22 Mar" or "Mon"
    val percentage: Int, // 0 to 100
    val isBestDay: Boolean = false
)

@Composable
fun PastelPillChart(
    data: List<ChartBarData>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFFEBE4F0)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { bar ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.fillMaxHeight()
            ) {
                // Best day emoji badge
                if (bar.isBestDay) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(PillShape)
                            .background(PastelYellow),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "😊", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                } else {
                    Spacer(modifier = Modifier.height(38.dp))
                }

                // Vertical Pill Bar
                val heightFraction = (bar.percentage / 100f).coerceIn(0.25f, 0.88f)
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .fillMaxHeight(heightFraction)
                        .clip(PillShape)
                        .background(if (bar.isBestDay) MaterialTheme.colorScheme.surface else barColor)
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Percentage pill badge
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${bar.percentage}%",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = bar.label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
