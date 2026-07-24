package com.agunganamiroh.ui.screen.invoice

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agunganamiroh.motion.*
import com.agunganamiroh.viewmodel.AdminInvoiceViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCreateInvoiceScreen(
    navController: NavController,
    jamaahId: String,
    viewModel: AdminInvoiceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val jamaah = uiState.selectedJamaah
    val form = uiState.invoiceForm
    val snackbarHostState = remember { SnackbarHostState() }
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    LaunchedEffect(jamaahId) {
        viewModel.selectJamaahById(jamaahId)
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            snackbarHostState.showSnackbar("Invoice berhasil diterbitkan!")
            viewModel.clearStatus()
            navController.popBackStack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar("Error: $error")
            viewModel.clearStatus()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        IslamicPatternOverlay()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Buat Invoice", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            if (uiState.loading && jamaah == null) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    DetailSkeleton()
                }
            } else if (jamaah != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 100.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item { JamaahSummaryCard(jamaah = jamaah, paket = uiState.selectedPaket, modifier = Modifier.animateEntrance(0)) }

                    if (uiState.existingInvoices.isNotEmpty()) {
                        item {
                            ExistingInvoiceWarning(count = uiState.existingInvoices.size)
                        }
                    }

                    item {
                        BiayaFormSection(
                            form = form,
                            onFieldChange = { field, value -> viewModel.updateFormField(field, value) },
                            onStringFieldChange = { field, value -> viewModel.updateFormField(field, value) },
                            format = format,
                            modifier = Modifier.animateEntrance(80)
                        )
                    }

                    item {
                        TotalCard(total = form.totalTagihan, format = format, modifier = Modifier.animateEntrance(160))
                    }

                    item {
                        Button(
                            onClick = { viewModel.createInvoice() },
                            modifier = Modifier.fillMaxWidth().height(56.dp).bounceClick(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            enabled = !uiState.loading
                        ) {
                            if (uiState.loading) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Icon(Icons.Default.Send, null, tint = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("TERBITKAN INVOICE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JamaahSummaryCard(
    jamaah: com.agunganamiroh.data.model.Jamaah,
    paket: com.agunganamiroh.data.model.Paket?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (jamaah.gender.contains("Perempuan", true)) Icons.Default.Woman else Icons.Default.Man,
                        null, tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(jamaah.nama, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("${jamaah.program} • ${jamaah.keberangkatan}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)) {
                    Text("APPROVED", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                }
            }
            if (paket != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Paket: ${paket.title} • ${paket.durasi} • ${paket.maskapai}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ExistingInvoiceWarning(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Jamaah ini memiliki $count invoice sebelumnya.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun BiayaFormSection(
    form: com.agunganamiroh.viewmodel.InvoiceFormData,
    onFieldChange: (String, Long) -> Unit,
    onStringFieldChange: (String, String) -> Unit,
    format: java.text.NumberFormat,
    modifier: Modifier = Modifier
) {
    SectionCard(title = "RINCIAN BIAYA", icon = Icons.Default.ReceiptLong, modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            BiayaField(
                label = "Subtotal (Harga Paket)",
                value = form.subtotal,
                onValueChange = { onFieldChange("subtotal", it) },
                format = format,
                readOnly = true
            )

            BiayaField(
                label = "Upgrade Double",
                value = form.upgradeDouble,
                onValueChange = { onFieldChange("upgradeDouble", it) },
                format = format
            )

            BiayaField(
                label = "Upgrade Triple",
                value = form.upgradeTriple,
                onValueChange = { onFieldChange("upgradeTriple", it) },
                format = format
            )

            BiayaField(
                label = "Koper",
                value = form.koper,
                onValueChange = { onFieldChange("koper", it) },
                format = format
            )

            BiayaField(
                label = "Paspor",
                value = form.paspor,
                onValueChange = { onFieldChange("paspor", it) },
                format = format
            )

            BiayaField(
                label = "Vaksin",
                value = form.vaksin,
                onValueChange = { onFieldChange("vaksin", it) },
                format = format
            )

            BiayaField(
                label = "Diskon",
                value = form.diskon,
                onValueChange = { onFieldChange("diskon", it) },
                format = format
            )

            BiayaField(
                label = "Biaya Lain",
                value = form.biayaLain,
                onValueChange = { onFieldChange("biayaLain", it) },
                format = format
            )

            OutlinedTextField(
                value = form.keteranganBiayaLain,
                onValueChange = { onStringFieldChange("keteranganBiayaLain", it) },
                label = { Text("Keterangan Biaya Lain") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            OutlinedTextField(
                value = form.tanggalJatuhTempo,
                onValueChange = { onStringFieldChange("tanggalJatuhTempo", it) },
                label = { Text("Tanggal Jatuh Tempo (DD/MM/YYYY)") },
                placeholder = { Text("Contoh: 31/12/2026") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

@Composable
private fun BiayaField(
    label: String,
    value: Long,
    onValueChange: (Long) -> Unit,
    format: java.text.NumberFormat,
    readOnly: Boolean = false
) {
    var textValue by remember(value) {
        mutableStateOf(if (value > 0) value.toString() else "")
    }

    OutlinedTextField(
        value = textValue,
        onValueChange = { input ->
            val filtered = input.filter { it.isDigit() }
            textValue = filtered
            onValueChange(filtered.toLongOrNull() ?: 0L)
        },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        prefix = { Text("Rp ") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        readOnly = readOnly,
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun TotalCard(
    total: Long,
    format: java.text.NumberFormat,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("TOTAL TAGIHAN", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                Text("Termasuk seluruh rincian biaya", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
            Text(
                format.format(total).replace(",00", ""),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
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
