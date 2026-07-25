package com.agunganamiroh.ui.screen.keberangkatan.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agunganamiroh.motion.*
import com.agunganamiroh.ui.screen.keberangkatan.components.CountdownBadge
import com.agunganamiroh.viewmodel.CountdownState
import com.agunganamiroh.viewmodel.DepartureViewModel
import com.agunganamiroh.viewmodel.JamaahReadiness
import com.agunganamiroh.viewmodel.ReadinessStatus
import com.agunganamiroh.viewmodel.UrgencyLevel
import com.agunganamiroh.viewmodel.PackageRelationState
import com.agunganamiroh.viewmodel.PaymentStatus
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartureDetailScreen(
    navController: NavController,
    paketId: String,
    viewModel: DepartureViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val paket = remember(state.packages, paketId) {
        state.packages.find { it.paket?.id == paketId }
    }

    val allJamaahs = remember(state.packages, paketId) {
        state.packages.flatMap { pkg -> pkg.jamaahs }
            .filter { it.jamaah.paketId == paketId }
    }

    val attentionItems = remember(allJamaahs) {
        allJamaahs.filter {
            it.overallStatus == ReadinessStatus.NEEDS_ATTENTION ||
                    it.overallStatus == ReadinessStatus.CRITICAL
        }
    }

    val readyItems = remember(allJamaahs) {
        allJamaahs.filter { it.overallStatus == ReadinessStatus.READY }
    }

    val departedItems = remember(allJamaahs) {
        allJamaahs.filter { it.overallStatus == ReadinessStatus.DEPARTED }
    }

    val bgPrimaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                drawCircle(
                    color = bgPrimaryColor.copy(alpha = 0.05f),
                    radius = 180.dp.toPx(),
                    center = Offset(size.width, 0f)
                )
            }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = paket?.paket?.title ?: "Detail Keberangkatan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                )
            },
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        ) { paddingValues ->
            if (paket == null && state.loading) {
                LoadingDetail(modifier = Modifier.padding(paddingValues))
            } else if (paket == null) {
                NoDataDetail(modifier = Modifier.padding(paddingValues))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(top = 2.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        ReadinessHeroCard(paket = paket)
                    }

                    item {
                        BreakdownSection(paket = paket)
                    }

                    if (attentionItems.isNotEmpty()) {
                        item {
                            JamaahGroupSection(
                                title = "Perlu Perhatian",
                                icon = Icons.Default.Warning,
                                tint = MaterialTheme.colorScheme.error,
                                items = attentionItems,
                                count = attentionItems.size,
                                onJamaahClick = { id ->
                                    navController.navigate("detail_jamaah/$id")
                                }
                            )
                        }
                    }

                    if (readyItems.isNotEmpty()) {
                        item {
                            JamaahGroupSection(
                                title = "Siap Berangkat",
                                icon = Icons.Default.CheckCircle,
                                tint = MaterialTheme.colorScheme.tertiary,
                                items = readyItems,
                                count = readyItems.size,
                                onJamaahClick = { id ->
                                    navController.navigate("detail_jamaah/$id")
                                }
                            )
                        }
                    }

                    if (departedItems.isNotEmpty()) {
                        item {
                            JamaahGroupSection(
                                title = "Sudah Berangkat",
                                icon = Icons.Default.FlightTakeoff,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                items = departedItems,
                                count = departedItems.size,
                                onJamaahClick = { id ->
                                    navController.navigate("detail_jamaah/$id")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadinessHeroCard(
    paket: com.agunganamiroh.viewmodel.DeparturePackage
) {
    val animatedProgress by animateFloatAsState(
        targetValue = paket.readinessPercent,
        animationSpec = tween(1000),
        label = "readiness"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = paket.paket?.title ?: "Paket",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (paket.paket != null) {
                        Text(
                            text = "${paket.paket.durasi} • ${paket.paket.maskapai}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (paket.nearestDepartureDate != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = paket.nearestDepartureDate,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                if (paket.countdown !is CountdownState.Unknown) {
                    CountdownBadge(countdown = paket.countdown, large = true)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                )
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (animatedProgress >= 0.8f) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatMini(
                    value = "${paket.readyCount}",
                    label = "Siap",
                    color = MaterialTheme.colorScheme.tertiary
                )
                StatMini(
                    value = "${paket.attentionCount}",
                    label = "Perhatian",
                    color = MaterialTheme.colorScheme.secondary
                )
                StatMini(
                    value = "${paket.totalJamaah}",
                    label = "Total",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatMini(value: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BreakdownSection(
    paket: com.agunganamiroh.viewmodel.DeparturePackage
) {
    val total = paket.totalJamaah.coerceAtLeast(1)
    val paymentReady = paket.jamaahs.count { it.paymentComplete }
    val docReady = paket.jamaahs.count { it.documentsComplete }
    val approvalReady = paket.jamaahs.count { it.approvalComplete }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Rincian Kesiapan",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            BreakdownBar(
                label = "Pembayaran",
                ready = paymentReady,
                total = total,
                icon = Icons.Default.Payments,
                color = MaterialTheme.colorScheme.primary
            )
            BreakdownBar(
                label = "Dokumen",
                ready = docReady,
                total = total,
                icon = Icons.Default.Description,
                color = MaterialTheme.colorScheme.secondary
            )
            BreakdownBar(
                label = "Persetujuan",
                ready = approvalReady,
                total = total,
                icon = Icons.Default.Verified,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun BreakdownBar(
    label: String,
    ready: Int,
    total: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    val fraction = ready.toFloat() / total
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(800),
        label = "breakdown"
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon, null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "$ready/$total",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = { animatedFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.1f)
        )
    }
}

@Composable
private fun JamaahGroupSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    items: List<JamaahReadiness>,
    count: Int,
    onJamaahClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = tint.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "$count",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = tint
                )
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                items.forEachIndexed { index, readiness ->
                    JamaahItem(
                        readiness = readiness,
                        onClick = { onJamaahClick(readiness.jamaah.id) }
                    )
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JamaahItem(
    readiness: JamaahReadiness,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    when (readiness.urgency) {
                        UrgencyLevel.CRITICAL -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        UrgencyLevel.HIGH -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        UrgencyLevel.ATTENTION -> MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        else -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when {
                    readiness.urgency == UrgencyLevel.CRITICAL -> Icons.Default.ErrorOutline
                    readiness.urgency == UrgencyLevel.HIGH -> Icons.Default.WarningAmber
                    readiness.overallStatus == ReadinessStatus.READY -> Icons.Default.CheckCircle
                    else -> Icons.Default.Person
                },
                contentDescription = null,
                tint = when (readiness.urgency) {
                    UrgencyLevel.CRITICAL -> MaterialTheme.colorScheme.error
                    UrgencyLevel.HIGH -> MaterialTheme.colorScheme.secondary
                    UrgencyLevel.READY -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = readiness.jamaah.nama,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            CompactStatusRow(readiness = readiness)
        }

        if (readiness.countdown !is CountdownState.Unknown) {
            CountdownBadge(countdown = readiness.countdown)
            Spacer(modifier = Modifier.width(2.dp))
        }
        Icon(
            Icons.Default.ChevronRight,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun CompactStatusRow(readiness: JamaahReadiness) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (readiness.overallStatus == ReadinessStatus.READY) {
            Text(
                text = "Siap berangkat",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
            return
        }
        if (readiness.overallStatus == ReadinessStatus.DEPARTED) {
            Text(
                text = "Sudah berangkat",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return
        }

        val missingCount = readiness.missingDocuments.size
        var issues = 0
        if (!readiness.paymentComplete) issues++
        if (!readiness.documentsComplete) issues++
        if (!readiness.approvalComplete) issues++

        if (issues > 2) {
            Text(
                text = "$issues masalah",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            if (!readiness.paymentComplete) {
                val paymentLabel = when (readiness.paymentStatus) {
                    PaymentStatus.BELUM_LUNAS -> "Sisa ${formatRupiah(readiness.remainingPayment)}"
                    PaymentStatus.BELUM_MEMBAYAR -> "Bayar"
                    PaymentStatus.DATA_TIDAK_TERSEDIA -> "Bayar"
                    else -> "Bayar"
                }
                IssueChip(text = paymentLabel, color = MaterialTheme.colorScheme.error)
            }
            if (!readiness.documentsComplete) {
                IssueChip(text = "Dok $missingCount", color = MaterialTheme.colorScheme.secondary)
            }
            if (!readiness.approvalComplete) {
                val statusLabel = when (readiness.jamaah.status.lowercase()) {
                    "rejected", "ditolak" -> "Ditolak"
                    else -> "Setuju"
                }
                IssueChip(text = statusLabel, color = if (statusLabel == "Ditolak") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun IssueChip(text: String, color: androidx.compose.ui.graphics.Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false
        )
    }
}

private fun formatRupiah(amount: Long): String {
    val fmt = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("id").setRegion("ID").build())
    return fmt.format(amount).replace(",00", "")
}

@Composable
private fun LoadingDetail(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(120.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(100.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(150.dp))
    }
}

@Composable
private fun NoDataDetail(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Data Tidak Ditemukan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Paket atau jamaah yang dimaksud tidak tersedia.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
