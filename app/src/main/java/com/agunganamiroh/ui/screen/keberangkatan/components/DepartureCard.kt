package com.agunganamiroh.ui.screen.keberangkatan.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.agunganamiroh.motion.animateEntrance
import com.agunganamiroh.motion.bounceClick
import com.agunganamiroh.viewmodel.CountdownState
import com.agunganamiroh.viewmodel.DeparturePackage

@Composable
fun DepartureCard(
    departure: DeparturePackage,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedPercent by animateFloatAsState(
        targetValue = departure.readinessPercent,
        animationSpec = tween(durationMillis = 800),
        label = "readinessPercent"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateEntrance(index * 60)
            .bounceClick()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = departure.paket?.title ?: "Paket",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    val detailParts = listOfNotNull(
                        departure.paket?.durasi,
                        departure.paket?.maskapai?.takeIf { it.isNotBlank() }
                    )
                    if (detailParts.isNotEmpty()) {
                        Text(
                            text = detailParts.joinToString(" • "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (departure.nearestDepartureDate != null) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = departure.nearestDepartureDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Tanggal keberangkatan belum tersedia",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                if (departure.countdown !is CountdownState.Unknown) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CountdownBadge(countdown = departure.countdown)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${departure.totalJamaah}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " jamaah",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
                if (departure.attentionCount > 0) {
                    val attColor = if (departure.countdown is CountdownState.Upcoming) {
                        val days = (departure.countdown as CountdownState.Upcoming).days
                        if (days <= 7) MaterialTheme.colorScheme.error
                        else if (days <= 14) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    } else MaterialTheme.colorScheme.onSurfaceVariant
                    Text(
                        text = "$departure.attentionCount perlu perhatian",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = attColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LinearProgressIndicator(
                    progress = { animatedPercent },
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (animatedPercent >= 0.8f) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "${(animatedPercent * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (animatedPercent >= 0.8f) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.secondary
                )
                Icon(
                    Icons.Default.ChevronRight,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
