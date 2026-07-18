package com.agunganamiroh.ui.screen.agent

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agunganamiroh.data.model.Jamaah
import com.agunganamiroh.viewmodel.JamaahViewModel
import com.agunganamiroh.viewmodel.PaketViewModel
import java.text.NumberFormat
import java.util.*

// ============================================================
// PREMIUM ENTERPRISE COLORS
// ============================================================
private val BrandGold = Color(0xFFC89B3C)
private val AppBackground = Color(0xFFFAF8F5)
private val SurfaceWhite = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1F1F1F)
private val TextSecondary = Color(0xFF6B7280)
private val TextMuted = Color(0xFF9CA3AF)
private val SuccessGreen = Color(0xFF22C55E)
private val WarningAmber = Color(0xFFF59E0B)
private val ErrorRed = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailJamaahScreen(
    navController: NavController,
    jamaahId: String,
    viewModel: JamaahViewModel = viewModel(),
    paketViewModel: PaketViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showPaymentSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val jamaah = uiState.selectedJamaah
    val paket = uiState.selectedPaket

    LaunchedEffect(jamaahId) {
        viewModel.loadJamaahDetail(jamaahId)
    }

    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            snackbarHostState.showSnackbar("Berhasil memperbarui data")
            viewModel.resetUpdateStatus()
            showPaymentSheet = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        IslamicPatternOverlay()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Detail Jamaah", fontWeight = FontWeight.ExtraBold, color = BrandGold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = BrandGold)
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("input_jamaah?id=$jamaahId") }) {
                            Icon(Icons.Default.Edit, "Edit", tint = BrandGold)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            if (uiState.loading && jamaah == null) {
                LoadingState()
            } else if (uiState.error != null && jamaah == null) {
                ErrorState(error = uiState.error!!, onRetry = { viewModel.loadJamaahDetail(jamaahId) })
            } else if (jamaah != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 40.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item { HeroProfileCard(jamaah = jamaah) }
                    
                    item { StatusSection(jamaah = jamaah) }

                    item { 
                        PaymentCard(
                            jamaah = jamaah, 
                            hargaPaket = if (jamaah.hargaPaket > 0) jamaah.hargaPaket else (paket?.harga ?: 0L),
                            onAddPayment = { showPaymentSheet = true }
                        ) 
                    }

                    item { PersonalInfoCard(jamaah = jamaah) }

                    item { ProgramCard(jamaah = jamaah, paket = paket) }

                    item { DocumentCard(jamaah = jamaah) }

                    item { AdditionalRequestCard(request = jamaah.requestTambahan) }

                    item {
                        ActionSection(
                            onEdit = { navController.navigate("input_jamaah?id=$jamaahId") },
                            onDelete = { showDeleteDialog = true }
                        )
                    }
                }
            }
        }

        if (showPaymentSheet && jamaah != null) {
            PaymentBottomSheet(
                hargaPaket = if (jamaah.hargaPaket > 0) jamaah.hargaPaket else (paket?.harga ?: 0L),
                totalPaid = jamaah.dp,
                onDismiss = { showPaymentSheet = false },
                onConfirm = { amount, method, notes ->
                    viewModel.addPayment(amount, method, notes)
                }
            )
        }

        if (showDeleteDialog) {
            DeleteConfirmDialog(
                onDismiss = { showDeleteDialog = false },
                onConfirm = {
                    viewModel.deleteJamaah(jamaahId)
                    navController.popBackStack()
                }
            )
        }
    }
}

// ============================================================
// COMPONENTS
// ============================================================

@Composable
private fun HeroProfileCard(jamaah: Jamaah) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().background(
                brush = Brush.verticalGradient(listOf(BrandGold.copy(alpha = 0.08f), SurfaceWhite))
            ).padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Avatar
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(BrandGold.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = jamaah.nama.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandGold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = jamaah.nama,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = jamaah.program,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusBadge(status = jamaah.status)
                    if (jamaah.pelunasan) {
                        Surface(shape = RoundedCornerShape(8.dp), color = SuccessGreen.copy(alpha = 0.1f)) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, null, tint = SuccessGreen, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("LUNAS", style = MaterialTheme.typography.labelSmall, color = SuccessGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusSection(jamaah: Jamaah) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoMiniCard(
            label = "Status",
            value = jamaah.status.uppercase(),
            color = when(jamaah.status.lowercase()) {
                "pending" -> WarningAmber
                "approved" -> SuccessGreen
                else -> ErrorRed
            },
            modifier = Modifier.weight(1f)
        )
        InfoMiniCard(
            label = "Pelunasan",
            value = if (jamaah.pelunasan) "LUNAS" else "BELUM LUNAS",
            color = if (jamaah.pelunasan) SuccessGreen else ErrorRed,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun InfoMiniCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun PaymentCard(jamaah: Jamaah, hargaPaket: Long, onAddPayment: () -> Unit) {
    val remaining = (hargaPaket - jamaah.dp).coerceAtLeast(0)
    val progress = if (hargaPaket > 0) (jamaah.dp.toFloat() / hargaPaket.toFloat()).coerceIn(0f, 1f) else 0f
    
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    
    SectionCard(title = "RINGKASAN PEMBAYARAN", icon = Icons.Default.Payments) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Total Harga Paket", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(format.format(hargaPaket).replace(",00",""), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(BrandGold.copy(alpha = 0.1f)).clickable { onAddPayment() }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, null, tint = BrandGold)
                }
            }
            
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Sudah Dibayar", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = BrandGold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = BrandGold,
                    trackColor = BrandGold.copy(alpha = 0.1f)
                )
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Telah Dibayar (DP)", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(format.format(jamaah.dp).replace(",00",""), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = SuccessGreen)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Sisa Tagihan", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(format.format(remaining).replace(",00",""), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = if (remaining > 0) ErrorRed else SuccessGreen)
                }
            }
            
            Button(
                onClick = onAddPayment,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGold)
            ) {
                Icon(Icons.Default.AddCard, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tambah Pembayaran")
            }
        }
    }
}

@Composable
private fun PersonalInfoCard(jamaah: Jamaah) {
    SectionCard(title = "DATA PRIBADI", icon = Icons.Default.Person) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailItem("Nama Lengkap", jamaah.nama)
            DetailItem("Bin / Binti", jamaah.binBinti.ifBlank { "-" })
            DetailItem("Jenis Kelamin", jamaah.gender)
            DetailItem("Nomor HP", jamaah.noHp)
            DetailItem("Alamat", jamaah.alamat)
            DetailItem("Tempat Lahir", jamaah.tempatLahir)
            DetailItem("Tanggal Lahir", jamaah.tanggalLahir)
        }
    }
}

@Composable
private fun ProgramCard(jamaah: Jamaah, paket: com.agunganamiroh.data.model.Paket?) {
    SectionCard(title = "PROGRAM UMROH", icon = Icons.Default.FlightTakeoff) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailItem("Paket", jamaah.program)
            if (paket != null) {
                DetailItem("Durasi", paket.durasi)
                DetailItem("Maskapai", paket.maskapai)
                DetailItem("Hotel Makkah", paket.hotelMakkah)
                DetailItem("Hotel Madinah", paket.hotelMadinah)
                DetailItem("Tanggal Keberangkatan", paket.tanggal)
                DetailItem("Sisa Seat", "${paket.sisaSeat} Seat")
            } else {
                DetailItem("Keberangkatan", jamaah.keberangkatan)
                Text("Memuat informasi paket tambahan...", style = MaterialTheme.typography.labelSmall, color = BrandGold)
            }
            DetailItem("Nomor Paspor", jamaah.noPaspor.ifBlank { "Belum ada" })
        }
    }
}

@Composable
private fun DocumentCard(jamaah: Jamaah) {
    SectionCard(title = "DOKUMEN OPERASIONAL", icon = Icons.Default.Description) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DocItem("KTP (Kartu Tanda Penduduk)", jamaah.ktp)
            DocItem("Kartu Keluarga (KK)", jamaah.kk)
            DocItem("Paspor Asli", jamaah.paspor)
            DocItem("Akte Kelahiran / Ijazah", jamaah.akte)
            DocItem("Buku Kuning / Meningitis", jamaah.meningitis)
        }
    }
}

@Composable
private fun DocItem(label: String, isUploaded: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isUploaded) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isUploaded) SuccessGreen else TextMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = if (isUploaded) TextPrimary else TextMuted)
    }
}

@Composable
private fun AdditionalRequestCard(request: String) {
    SectionCard(title = "PERMINTAAN TAMBAHAN", icon = Icons.AutoMirrored.Filled.Notes) {
        Text(
            text = request.ifBlank { "Tidak ada permintaan tambahan." },
            style = MaterialTheme.typography.bodyMedium,
            color = if (request.isBlank()) TextMuted else TextPrimary,
            fontStyle = if (request.isBlank()) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
        )
    }
}

@Composable
private fun ActionSection(onEdit: () -> Unit, onDelete: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandGold)
        ) {
            Icon(Icons.Default.Edit, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("EDIT DATA JAMAAH", fontWeight = FontWeight.Bold)
        }
        
        OutlinedButton(
            onClick = onDelete,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
            border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.Delete, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("HAPUS JAMAAH", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SectionCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = BrandGold, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = BrandGold, letterSpacing = 1.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, Color(0xFFF3F4F6))
        ) {
            Box(modifier = Modifier.padding(20.dp)) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentBottomSheet(
    hargaPaket: Long,
    totalPaid: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long, String, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("Transfer") }
    val methods = listOf("Cash", "Transfer", "QRIS")
    
    val remaining = (hargaPaket - totalPaid).coerceAtLeast(0)
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        dragHandle = { BottomSheetDefaults.DragHandle(color = BrandGold.copy(alpha = 0.3f)) }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("Tambah Pembayaran", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = BrandGold)
            
            Card(colors = CardDefaults.cardColors(containerColor = AppBackground)) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Sisa Pembayaran", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(format.format(remaining).replace(",00",""), fontWeight = FontWeight.Bold, color = ErrorRed)
                    }
                }
            }
            
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                label = { Text("Nominal Pembayaran (IDR)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                prefix = { Text("Rp ") }
            )
            
            Column {
                Text("Metode Pembayaran", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    methods.forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            label = { Text(method) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
            
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan / No. Referensi") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            Button(
                onClick = { 
                    val amt = amount.toLongOrNull() ?: 0L
                    if (amt > 0) onConfirm(amt, selectedMethod, notes) 
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                enabled = amount.isNotEmpty()
            ) {
                Text("KONFIRMASI PEMBAYARAN", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DeleteConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hapus Data Jamaah?") },
        text = { Text("Tindakan ini tidak dapat dibatalkan. Semua data terkait jamaah ini akan dihapus secara permanen.") },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) { Text("Hapus") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        },
        containerColor = SurfaceWhite
    )
}

@Composable
private fun StatusBadge(status: String) {
    val color = when (status.lowercase()) {
        "pending" -> WarningAmber
        "approved" -> SuccessGreen
        else -> ErrorRed
    }
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.1f)) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = BrandGold)
    }
}

@Composable
private fun ErrorState(error: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(64.dp), tint = ErrorRed)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Terjadi Kesalahan", fontWeight = FontWeight.Bold)
            Text(error, textAlign = TextAlign.Center, color = TextSecondary)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) { Text("Coba Lagi") }
        }
    }
}

@Composable
private fun IslamicPatternOverlay() {
    Canvas(modifier = Modifier.fillMaxSize().alpha(0.04f)) {
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
                    color = BrandGold,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
    }
}
