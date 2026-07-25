package com.agunganamiroh.ui.screen.keberangkatan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agunganamiroh.motion.*
import com.agunganamiroh.ui.screen.keberangkatan.components.*
import com.agunganamiroh.viewmodel.DepartureViewModel
import com.agunganamiroh.viewmodel.JamaahReadiness
import com.agunganamiroh.viewmodel.PackageRelationState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeberangkatanScreen(
    navController: NavController,
    viewModel: DepartureViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()
    val bgPrimaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                drawCircle(
                    color = bgPrimaryColor.copy(alpha = 0.05f),
                    radius = 200.dp.toPx(),
                    center = Offset(size.width, 0f)
                )
            }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Keberangkatan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pantau kesiapan jamaah sebelum keberangkatan",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
            when {
                state.loading -> {
                    LoadingState(modifier = Modifier.padding(paddingValues))
                }
                state.error != null -> {
                    ErrorState(
                        message = state.error ?: "Terjadi kesalahan",
                        onRetry = { },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
                else -> {
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            isRefreshing = true
                            viewModel.setFilter(state.filter)
                            isRefreshing = false
                        },
                        state = pullRefreshState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentPadding = PaddingValues(top = 2.dp, bottom = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                OperationalSummary(
                                    nearestDeparture = state.nearestDeparture,
                                    totalUpcomingJamaah = state.totalUpcomingJamaah,
                                    readyCount = state.readyCount,
                                    attentionCount = state.attentionCount
                                )
                            }

                            item {
                                FilterChips(
                                    selectedFilter = state.filter,
                                    onFilterChange = { viewModel.setFilter(it) },
                                    attentionCount = state.attentionCount
                                )
                            }

                            if (state.urgentItems.isNotEmpty()) {
                                item {
                                    AttentionSection(
                                        items = state.urgentItems,
                                        onJamaahClick = { id ->
                                            navController.navigate("detail_jamaah/$id")
                                        }
                                    )
                                }
                            }

                            if (state.dataAttentionItems.isNotEmpty()) {
                                item {
                                    DataAttentionSection(
                                        items = state.dataAttentionItems,
                                        onJamaahClick = { id ->
                                            navController.navigate("detail_jamaah/$id")
                                        }
                                    )
                                }
                            }

                            if (state.totalUpcomingJamaah == 0 && state.dataAttentionItems.isEmpty()) {
                                item {
                                    EmptyState(modifier = Modifier.padding(horizontal = 20.dp))
                                }
                            } else if (state.packages.isEmpty() && state.totalUpcomingJamaah > 0 && state.urgentItems.isEmpty()) {
                                item {
                                    AllReadyState(
                                        totalJamaah = state.totalUpcomingJamaah,
                                        modifier = Modifier.padding(horizontal = 20.dp)
                                    )
                                }
                            }

                            state.packages.forEachIndexed { index, departure ->
                                item(key = departure.paket?.id ?: "no-paket-$index") {
                                    DepartureCard(
                                        departure = departure,
                                        index = index,
                                        onClick = {
                                            val paketId = departure.paket?.id ?: ""
                                            navController.navigate("departure_detail/$paketId")
                                        },
                                        modifier = Modifier.padding(horizontal = 20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChips(
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    attentionCount: Int,
    modifier: Modifier = Modifier
) {
    val filters = listOf(
        "Semua" to null,
        "Mendatang" to null,
        "Bulan Ini" to null,
        "Perlu Perhatian" to attentionCount,
        "Sudah Berangkat" to null
    )

    LazyRow(
        modifier = modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { (filter, badge) ->
            val isSelected = selectedFilter == filter
            FilterChip(
                selected = isSelected,
                onClick = { onFilterChange(filter) },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = filter,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false
                        )
                        if (badge != null && badge > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "$badge",
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                    enabled = true,
                    selected = isSelected
                )
            )
        }
    }
}

@Composable
private fun DataAttentionSection(
    items: List<JamaahReadiness>,
    onJamaahClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Data perlu dilengkapi",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = items.size.toString(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val chevronRotation by animateFloatAsState(
                    targetValue = if (expanded) 90f else 0f,
                    animationSpec = tween(200),
                    label = "chevron"
                )
                Icon(
                    Icons.Default.ChevronRight,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(chevronRotation)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Data ${items.size} jamaah berikut belum terhubung dengan paket keberangkatan. Perbaiki relasi data untuk pemantauan yang akurat.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    items.forEach { readiness ->
                        val reasonText = when (readiness.packageRelation) {
                            PackageRelationState.MISSING_PACKAGE -> "Paket belum terhubung"
                            PackageRelationState.LEGACY_DATE_ONLY -> "Relasi paket perlu dikonfirmasi"
                            PackageRelationState.MISSING_DATE -> "Jadwal paket belum tersedia"
                            else -> "Data perlu dilengkapi"
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onJamaahClick(readiness.jamaah.id) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = readiness.jamaah.nama,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = reasonText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(110.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(36.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(60.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(120.dp))
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Terjadi Kesalahan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp)
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Belum Ada Keberangkatan Mendatang",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Jadwal keberangkatan akan muncul ketika jamaah terdaftar pada paket yang memiliki jadwal.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AllReadyState(
    totalJamaah: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Semua Jamaah Siap",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "$totalJamaah dari $totalJamaah jamaah telah memenuhi persyaratan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
