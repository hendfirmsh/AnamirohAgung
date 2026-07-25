package com.agunganamiroh.ui.screen.keberangkatan.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.agunganamiroh.motion.bounceClick
import com.agunganamiroh.viewmodel.CountdownState
import com.agunganamiroh.viewmodel.JamaahReadiness
import com.agunganamiroh.viewmodel.PackageRelationState
import com.agunganamiroh.viewmodel.PaymentStatus
import com.agunganamiroh.viewmodel.UrgencyLevel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AttentionSection(
    items: List<JamaahReadiness>,
    onJamaahClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Perlu Perhatian",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
            ) {
                Text(
                    text = items.size.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        items.forEach { readiness ->
            AttentionCard(
                readiness = readiness,
                onClick = { onJamaahClick(readiness.jamaah.id) }
            )
        }
    }
}

@Composable
private fun AttentionCard(
    readiness: JamaahReadiness,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when (readiness.urgency) {
        UrgencyLevel.CRITICAL -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
        UrgencyLevel.HIGH -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
        else -> androidx.compose.ui.graphics.Color.Transparent
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .bounceClick()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = if (borderColor != androidx.compose.ui.graphics.Color.Transparent)
            androidx.compose.foundation.BorderStroke(1.dp, borderColor) else null
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(urgencyColor(readiness.urgency).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = readiness.jamaah.nama.take(1).uppercase(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = urgencyColor(readiness.urgency)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = readiness.jamaah.nama,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val relationText = when (readiness.packageRelation) {
                        PackageRelationState.MISSING_PACKAGE -> "○ Paket perlu dikonfirmasi"
                        PackageRelationState.LEGACY_DATE_ONLY -> "○ Relasi paket perlu diperbaiki"
                        PackageRelationState.MISSING_DATE -> "○ Jadwal belum tersedia"
                        else -> readiness.jamaah.program.ifBlank { "Reguler" }
                    }
                    Text(
                        text = relationText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (readiness.countdown !is CountdownState.Unknown) {
                    CountdownBadge(countdown = readiness.countdown)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            StatusRows(readiness = readiness)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Lihat Jamaah",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        Icons.Default.ChevronRight,
                        null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusRows(readiness: JamaahReadiness) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        PaymentRow(readiness)
        DocumentRow(readiness)
        ApprovalRow(readiness)
    }
}

@Composable
private fun PaymentRow(readiness: JamaahReadiness) {
    when (readiness.paymentStatus) {
        PaymentStatus.LUNAS -> StatusRow(
            icon = "✓",
            iconColor = MaterialTheme.colorScheme.tertiary,
            label = "Lunas",
            labelColor = MaterialTheme.colorScheme.tertiary
        )
        PaymentStatus.BELUM_LUNAS -> {
            val fmt = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("id").setRegion("ID").build())
            val sisa = fmt.format(readiness.remainingPayment).replace(",00", "")
            StatusRow(
                icon = "●",
                iconColor = MaterialTheme.colorScheme.error,
                label = "Belum Lunas",
                labelColor = MaterialTheme.colorScheme.error,
                subtitle = "Sisa $sisa"
            )
        }
        PaymentStatus.BELUM_MEMBAYAR -> StatusRow(
            icon = "○",
            iconColor = MaterialTheme.colorScheme.secondary,
            label = "Belum melakukan pembayaran",
            labelColor = MaterialTheme.colorScheme.onSurface
        )
        PaymentStatus.DATA_TIDAK_TERSEDIA -> StatusRow(
            icon = "○",
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            label = "Data pembayaran belum tersedia",
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun DocumentRow(readiness: JamaahReadiness) {
    if (readiness.documentsComplete) {
        StatusRow(
            icon = "✓",
            iconColor = MaterialTheme.colorScheme.tertiary,
            label = "Dokumen Lengkap",
            labelColor = MaterialTheme.colorScheme.tertiary
        )
    } else {
        val missing = readiness.missingDocuments
        StatusRow(
            icon = "\u26A0",
            iconColor = MaterialTheme.colorScheme.secondary,
            label = "${missing.size} Dokumen Belum Lengkap",
            labelColor = MaterialTheme.colorScheme.onSurface,
            subtitle = missing.joinToString(", ")
        )
    }
}

@Composable
private fun ApprovalRow(readiness: JamaahReadiness) {
    if (readiness.approvalComplete) {
        StatusRow(
            icon = "✓",
            iconColor = MaterialTheme.colorScheme.tertiary,
            label = "Disetujui",
            labelColor = MaterialTheme.colorScheme.tertiary
        )
    } else {
        val statusLower = readiness.jamaah.status.lowercase()
        if (statusLower == "rejected" || statusLower == "ditolak") {
            StatusRow(
                icon = "!",
                iconColor = MaterialTheme.colorScheme.error,
                label = "Ditolak",
                labelColor = MaterialTheme.colorScheme.error
            )
        } else {
            StatusRow(
                icon = "○",
                iconColor = MaterialTheme.colorScheme.primary,
                label = "Menunggu Persetujuan",
                labelColor = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun StatusRow(
    icon: String,
    iconColor: androidx.compose.ui.graphics.Color,
    label: String,
    labelColor: androidx.compose.ui.graphics.Color,
    subtitle: String? = null
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = iconColor,
            modifier = Modifier.width(18.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = labelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun urgencyColor(urgency: UrgencyLevel): androidx.compose.ui.graphics.Color = when (urgency) {
    UrgencyLevel.CRITICAL -> MaterialTheme.colorScheme.error
    UrgencyLevel.HIGH -> MaterialTheme.colorScheme.secondary
    UrgencyLevel.ATTENTION -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.tertiary
}
