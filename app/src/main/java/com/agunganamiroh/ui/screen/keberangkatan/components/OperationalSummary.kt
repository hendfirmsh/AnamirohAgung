package com.agunganamiroh.ui.screen.keberangkatan.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.agunganamiroh.motion.animateEntrance
import com.agunganamiroh.viewmodel.CountdownState
import com.agunganamiroh.viewmodel.DeparturePackage

@Composable
fun OperationalSummary(
    nearestDeparture: DeparturePackage?,
    totalUpcomingJamaah: Int,
    readyCount: Int,
    attentionCount: Int,
    modifier: Modifier = Modifier
) {
    val animatedPercent by animateFloatAsState(
        targetValue = if (totalUpcomingJamaah > 0) readyCount.toFloat() / totalUpcomingJamaah else 0f,
        animationSpec = tween(1000),
        label = "heroPercent"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .animateEntrance(0),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Kesiapan Keberangkatan",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$totalUpcomingJamaah",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Jamaah Akan Berangkat",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$readyCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "Siap",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$attentionCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (attentionCount > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Perlu Perhatian",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Readiness",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(animatedPercent * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (animatedPercent >= 0.8f) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { animatedPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (animatedPercent >= 0.8f) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            if (nearestDeparture != null && nearestDeparture.countdown !is CountdownState.Unknown) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = nearestDeparture.paket?.title ?: "Paket",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (nearestDeparture.nearestDepartureDate != null) {
                            Text(
                                text = nearestDeparture.nearestDepartureDate,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    CountdownBadge(countdown = nearestDeparture.countdown)
                }
            }
        }
    }
}

@Composable
fun CountdownBadge(
    countdown: CountdownState,
    large: Boolean = false,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, text) = countdownDisplay(countdown)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(if (large) 10.dp else 8.dp))
            .background(backgroundColor)
            .padding(
                horizontal = if (large) 12.dp else 8.dp,
                vertical = if (large) 5.dp else 3.dp
            )
    ) {
        Text(
            text = text,
            style = if (large) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false
        )
    }
}

@Composable
fun countdownDisplay(countdown: CountdownState): Triple<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color, String> {
    return when (countdown) {
        is CountdownState.Unknown -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            ""
        )
        is CountdownState.Today -> Triple(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.primary,
            "Hari Ini"
        )
        is CountdownState.Upcoming -> {
            val days = countdown.days
            val color = when {
                days <= 7 -> MaterialTheme.colorScheme.error
                days <= 14 -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.tertiary
            }
            Triple(color.copy(alpha = 0.15f), color, "H-$days")
        }
        is CountdownState.Departed -> Triple(
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Berangkat"
        )
    }
}
