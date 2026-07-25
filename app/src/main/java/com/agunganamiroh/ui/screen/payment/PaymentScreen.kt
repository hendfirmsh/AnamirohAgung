package com.agunganamiroh.ui.screen.payment

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agunganamiroh.data.model.Jamaah
import com.agunganamiroh.motion.*
import com.agunganamiroh.viewmodel.PembayaranViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavController,
    viewModel: PembayaranViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }
    val refreshScope = rememberCoroutineScope()
    val pullRefreshState = rememberPullToRefreshState()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        IslamicPatternOverlay()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Pembayaran Jamaah", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            Text("Kelola pembayaran jamaah", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                SearchAndFilterSection(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    selectedFilter = uiState.selectedFilter,
                    onFilterChange = { viewModel.onFilterChange(it) }
                )

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        refreshScope.launch {
                            isRefreshing = true
                            viewModel.loadJamaahs()
                            delay(400)
                            isRefreshing = false
                        }
                    },
                    state = pullRefreshState,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (uiState.loading) {
                            LoadingState()
                        } else if (uiState.filteredJamaahs.isEmpty()) {
                            EmptyState()
                        } else {
                            PaymentList(
                                jamaahs = uiState.filteredJamaahs,
                                onItemClick = { id ->
                                    navController.navigate("payment_detail/$id")
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

@Composable
private fun SearchAndFilterSection(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedFilter: String,
    onFilterChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Cari Nama / No. HP / Paspor...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        val filters = listOf("Semua", "Belum Lunas", "Lunas", "Pending", "Approved")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.take(3).forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterChange(filter) },
                    label = { Text(filter, fontSize = 12.sp) },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.drop(3).forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterChange(filter) },
                    label = { Text(filter, fontSize = 12.sp) },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun PaymentList(
    jamaahs: List<Jamaah>,
    onItemClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(jamaahs, key = { _, jamaah -> jamaah.id }) { index, jamaah ->
            PaymentCard(
                jamaah = jamaah,
                onClick = { onItemClick(jamaah.id) },
                modifier = Modifier.staggerItem(index, 60)
            )
        }
    }
}

@Composable
private fun PaymentCard(
    jamaah: Jamaah,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    val progress = if (jamaah.hargaPaket > 0) (jamaah.dp.toFloat() / jamaah.hargaPaket.toFloat()).coerceIn(0f, 1f) else 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .bounceClick()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (jamaah.gender.contains("Perempuan", true)) Icons.Default.Woman else Icons.Default.Man,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = jamaah.nama,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = jamaah.program,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoItem("Berangkat", jamaah.keberangkatan)
                InfoItem("Harga Paket", format.format(jamaah.hargaPaket).replace(",00", ""))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoItem("Telah Dibayar", format.format(jamaah.dp).replace(",00", ""), color = MaterialTheme.colorScheme.tertiary)
                val remaining = (jamaah.hargaPaket - jamaah.dp).coerceAtLeast(0)
                InfoItem("Sisa", format.format(remaining).replace(",00", ""), color = if (remaining > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(
                    text = if (jamaah.pelunasan) "LUNAS" else "BELUM LUNAS",
                    color = if (jamaah.pelunasan) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                )
                StatusBadge(
                    text = jamaah.status.uppercase(),
                    color = when (jamaah.status.lowercase()) {
                        "pending" -> MaterialTheme.colorScheme.secondary
                        "approved", "verified" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun StatusBadge(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize()) {
        ListCardSkeleton()
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Payment, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Tidak Ada Data Pembayaran", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Data jamaah yang Anda cari tidak ditemukan.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
