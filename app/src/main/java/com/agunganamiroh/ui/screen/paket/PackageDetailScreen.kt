package com.agunganamiroh.ui.screen.paket

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agunganamiroh.data.model.Paket
import com.agunganamiroh.motion.bounceClick
import com.agunganamiroh.viewmodel.paket.PackageDetailViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageDetailScreen(
    navController: NavController,
    viewModel: PackageDetailViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val paketId = navController.currentBackStackEntry
        ?.arguments?.getString("paketId") ?: ""

    LaunchedEffect(paketId) {
        if (paketId.isNotBlank()) viewModel.loadPaket(paketId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Paket", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        when {
            state.loading -> LoadingDetail(modifier = Modifier.padding(padding))
            state.error != null -> ErrorDetail(
                message = state.error!!,
                onRetry = { viewModel.loadPaket(paketId) },
                onBack = { navController.popBackStack() },
                modifier = Modifier.padding(padding)
            )
            state.paket == null -> NotFoundDetail(
                onBack = { navController.popBackStack() },
                modifier = Modifier.padding(padding)
            )
            else -> PackageDetailContent(
                paket = state.paket!!,
                viewModel = viewModel,
                onDaftarkan = {
                    navController.navigate("input_jamaah?paketId=${state.paket!!.id}")
                },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun PackageDetailContent(
    paket: Paket,
    viewModel: PackageDetailViewModel,
    onDaftarkan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canRegister = viewModel.canRegister(paket)
    val availabilityLabel = viewModel.availabilityLabel(paket)
    val isAvailable = viewModel.isAvailable(paket)
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Image,
                null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                modifier = Modifier.size(64.dp)
            )
            if (paket.brosurName.isNotBlank()) {
                Text(
                    "Brochure: ${paket.brosurName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp)
                )
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            AvailabilityStatus(isAvailable = isAvailable, label = availabilityLabel)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                paket.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoBadge(icon = Icons.Default.Timer, text = paket.durasi)
                InfoBadge(icon = Icons.Default.Flight, text = paket.maskapai)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 20.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            InfoRow(label = "Tanggal Keberangkatan", value = paket.tanggal.ifBlank { "Tanggal belum tersedia" }, icon = Icons.Default.CalendarMonth)
            InfoRow(label = "Durasi", value = paket.durasi, icon = Icons.Default.Timer)
            InfoRow(label = "Maskapai", value = paket.maskapai, icon = Icons.Default.Flight)
            InfoRow(label = "Hotel Makkah", value = paket.hotelMakkah.ifBlank { "-" }, icon = Icons.Default.Hotel)
            InfoRow(label = "Hotel Madinah", value = paket.hotelMadinah.ifBlank { "-" }, icon = Icons.Default.Hotel)

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 20.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Harga Paket", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        format.format(paket.harga).replace(",00", ""),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sisa Seat", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${paket.sisaSeat}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (paket.sisaSeat < 5 && paket.sisaSeat > 0) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurface
                    )
                    Text("Kursi", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onDaftarkan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .bounceClick(),
                shape = RoundedCornerShape(16.dp),
                enabled = canRegister,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            ) {
                Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (canRegister) "Daftarkan Jamaah"
                    else availabilityLabel,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!canRegister) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    when {
                        paket.sisaSeat <= 0 -> "Paket ini sudah penuh. Silakan pilih paket lain."
                        viewModel.isExpired(paket) -> "Keberangkatan paket ini telah lewat."
                        else -> "Paket tidak tersedia untuk pendaftaran."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AvailabilityStatus(isAvailable: Boolean, label: String) {
    val (bg, textColor) = if (isAvailable)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) to MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.error.copy(alpha = 0.1f) to MaterialTheme.colorScheme.error

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.FlightTakeoff,
                null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = textColor)
        }
    }
}

@Composable
private fun InfoBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun LoadingDetail(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ErrorDetail(message: String, onRetry: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Gagal memuat paket", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(4.dp))
            Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry, shape = RoundedCornerShape(12.dp)) {
                Text("Coba Lagi")
            }
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.material3.TextButton(onClick = onBack) {
                Text("Kembali")
            }
        }
    }
}

@Composable
private fun NotFoundDetail(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Paket tidak ditemukan", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Paket mungkin sudah dihapus.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBack, shape = RoundedCornerShape(12.dp)) {
                Text("Kembali")
            }
        }
    }
}
