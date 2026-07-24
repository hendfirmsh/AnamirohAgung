package com.agunganamiroh.ui.screen.agent

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
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
import com.agunganamiroh.viewmodel.JamaahViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataJamaahScreen(
    navController: NavController,
    viewModel: JamaahViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        IslamicPatternOverlay()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text("Daftar Jamaah Saya", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary) 
                    },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Search and Filter Section
                SearchAndFilterSection(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    selectedFilter = uiState.selectedFilter,
                    onFilterChange = { viewModel.onFilterChange(it) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Content Area
                Box(modifier = Modifier.weight(1f)) {
                    if (uiState.loading) {
                        LoadingState()
                    } else if (uiState.jamaahs.isEmpty()) {
                        EmptyState()
                    } else {
                        JamaahList(
                            jamaahs = uiState.jamaahs,
                            onItemClick = { id ->
                                navController.navigate("detail_jamaah/$id")
                            },
                            onDeleteClick = { viewModel.deleteJamaah(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SearchAndFilterSection(
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
        // Search Bar
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

        // Filter Chips
        val filters = listOf("Semua", "Pending", "Approved", "Pelunasan")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterChange(filter) },
                    label = { Text(filter, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        enabled = true,
                        selected = selectedFilter == filter
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
private fun JamaahList(
    jamaahs: List<Jamaah>,
    onItemClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(jamaahs, key = { it.id }) { jamaah ->
            JamaahCard(
                jamaah = jamaah,
                onClick = { onItemClick(jamaah.id) },
                onDelete = { onDeleteClick(jamaah.id) }
            )
        }
    }
}

@Composable
internal fun JamaahCard(
    jamaah: Jamaah,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
                // Avatar/Icon
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

                // Status Badge
                JamaahStatusBadge(status = jamaah.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info Items
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(jamaah.noHp, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(jamaah.keberangkatan, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Payment Info
                val formattedDp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                    .format(jamaah.dp).replace(",00", "")
                
                Text(
                    text = "DP: $formattedDp",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (jamaah.pelunasan) {
                    Icon(Icons.Default.Verified, "Lunas", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
internal fun JamaahStatusBadge(status: String) {
    val color = when (status.lowercase()) {
        "pending" -> MaterialTheme.colorScheme.secondary
        "approved", "verified" -> MaterialTheme.colorScheme.tertiary
        "rejected" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Memuat data jamaah...", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Tidak Ada Data Jamaah",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Data jamaah yang Anda cari tidak ditemukan atau belum ada pendaftaran.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
