package com.agunganamiroh.ui.screen.invoice

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agunganamiroh.data.model.Invoice
import com.agunganamiroh.motion.*
import com.agunganamiroh.viewmodel.AgentInvoiceViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentInvoiceDetailScreen(
    navController: NavController,
    invoiceId: String,
    viewModel: AgentInvoiceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val invoice = uiState.selectedInvoice
    val jamaah = uiState.jamaah

    LaunchedEffect(invoiceId) {
        viewModel.loadInvoiceDetail(invoiceId)
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        IslamicPatternOverlay()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Detail Invoice", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            if (uiState.loading && invoice == null) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    DetailSkeleton()
                }
            } else if (invoice != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 40.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item { InvoiceHeader(invoice = invoice, jamaahName = jamaah?.nama ?: "") }
                    item { StatusBadgeSection(invoice = invoice) }
                    item { PaymentProgressCard(invoice = invoice) }
                    item { RincianBiayaCard(invoice = invoice) }
                    item { JamaahInfoCard(invoice = invoice, jamaah = jamaah) }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { /* Print functionality */ },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Print, null, tint = MaterialTheme.colorScheme.onPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("PRINT INVOICE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }
            }
        }

        if (uiState.error != null) {
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text("Error") },
                text = { Text(uiState.error!!) },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearError() }) { Text("OK") }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
private fun InvoiceHeader(invoice: Invoice, jamaahName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape).background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Receipt, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(invoice.noInvoice, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Text(jamaahName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Tanggal: ${invoice.tanggalInvoice}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun StatusBadgeSection(invoice: Invoice) {
    val statusColor = when (invoice.status) {
        "paid" -> MaterialTheme.colorScheme.tertiary
        "partial" -> MaterialTheme.colorScheme.secondary
        "cancelled" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }
    val statusIcon = when (invoice.status) {
        "paid" -> Icons.Default.Verified
        "partial" -> Icons.Default.Schedule
        "cancelled" -> Icons.Default.Cancel
        else -> Icons.Default.HourglassTop
    }
    val statusLabel = when (invoice.status) {
        "paid" -> "LUNAS"
        "partial" -> "PARTIAL"
        "cancelled" -> "DIBATALKAN"
        else -> "PUBLISHED"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateEntrance(0),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.05f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(statusLabel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = statusColor)
            }
        }
        if (invoice.tanggalJatuhTempo.isNotBlank()) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.05f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Jatuh Tempo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(invoice.tanggalJatuhTempo, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun PaymentProgressCard(invoice: Invoice) {
    val progress = if (invoice.totalTagihan > 0) {
        (invoice.totalDibayar.toFloat() / invoice.totalTagihan.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(1000), label = "progress")
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    val remaining = (invoice.totalTagihan - invoice.totalDibayar).coerceAtLeast(0)

    SectionCard(title = "STATUS PEMBAYARAN", icon = Icons.Default.Payments) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Progress Pembayaran", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total Tagihan", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    Text(format.format(invoice.totalTagihan).replace(",00", ""), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Terbayar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    Text(format.format(invoice.totalDibayar).replace(",00", ""), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Sisa Tagihan", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    format.format(remaining).replace(",00", ""),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (remaining > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun RincianBiayaCard(invoice: Invoice) {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    SectionCard(title = "RINCIAN BIAYA", icon = Icons.Default.ReceiptLong, modifier = Modifier.animateEntrance(80)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BiayaRow("Paket Umroh", format.format(invoice.subtotal).replace(",00", ""))
            if (invoice.upgradeDouble > 0) BiayaRow("Upgrade Double", format.format(invoice.upgradeDouble).replace(",00", ""))
            if (invoice.upgradeTriple > 0) BiayaRow("Upgrade Triple", format.format(invoice.upgradeTriple).replace(",00", ""))
            if (invoice.koper > 0) BiayaRow("Koper", format.format(invoice.koper).replace(",00", ""))
            if (invoice.paspor > 0) BiayaRow("Paspor", format.format(invoice.paspor).replace(",00", ""))
            if (invoice.vaksin > 0) BiayaRow("Vaksin", format.format(invoice.vaksin).replace(",00", ""))
            if (invoice.diskon > 0) BiayaRow("Diskon", "-${format.format(invoice.diskon).replace(",00", "")}", color = MaterialTheme.colorScheme.tertiary)
            if (invoice.biayaLain > 0) {
                BiayaRow("Biaya Lain", format.format(invoice.biayaLain).replace(",00", ""))
                if (invoice.keteranganBiayaLain.isNotBlank()) {
                    Text(invoice.keteranganBiayaLain, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("TOTAL", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    format.format(invoice.totalTagihan).replace(",00", ""),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun BiayaRow(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun JamaahInfoCard(invoice: Invoice, jamaah: com.agunganamiroh.data.model.Jamaah?) {
    SectionCard(title = "DATA JAMAAH", icon = Icons.Default.Person, modifier = Modifier.animateEntrance(160)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DetailItem("Nama", invoice.jamaahName)
            if (jamaah != null) {
                DetailItem("Program", jamaah.program)
                DetailItem("Keberangkatan", jamaah.keberangkatan)
                DetailItem("No. HP", jamaah.noHp)
                DetailItem("No. Paspor", jamaah.noPaspor.ifBlank { "-" })
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SectionCard(title: String, icon: ImageVector, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Box(modifier = Modifier.padding(20.dp)) { content() }
        }
    }
}

@Composable
private fun IslamicPatternOverlay() {
    val patternColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val opacity = if (backgroundColor.luminance() < 0.5f) 0.06f else 0.04f
    Canvas(modifier = Modifier.fillMaxSize().alpha(opacity)) {
        val sizePx = 60.dp.toPx()
        for (x in 0..(size.width / sizePx).toInt()) {
            for (y in 0..(size.height / sizePx).toInt()) {
                val cx = x * sizePx
                val cy = y * sizePx
                drawPath(
                    path = Path().apply {
                        moveTo(cx, cy - sizePx * 0.4f)
                        lineTo(cx + sizePx * 0.12f, cy - sizePx * 0.12f)
                        lineTo(cx + sizePx * 0.4f, cy)
                        lineTo(cx + sizePx * 0.12f, cy + sizePx * 0.12f)
                        lineTo(cx, cy + sizePx * 0.4f)
                        lineTo(cx - sizePx * 0.12f, cy + sizePx * 0.12f)
                        lineTo(cx - sizePx * 0.4f, cy)
                        lineTo(cx - sizePx * 0.12f, cy - sizePx * 0.12f)
                        close()
                    },
                    color = patternColor,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
    }
}
