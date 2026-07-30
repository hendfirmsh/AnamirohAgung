package com.agunganamiroh.ui.screen.riwayat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agunganamiroh.data.model.Activity
import com.agunganamiroh.data.model.ActivityType
import com.agunganamiroh.motion.ShimmerBox
import com.agunganamiroh.motion.animateEntrance
import com.agunganamiroh.viewmodel.ActivityFilter
import com.agunganamiroh.viewmodel.PeriodFilter
import com.agunganamiroh.viewmodel.RiwayatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatScreen(
    navController: NavController,
    viewModel: RiwayatViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()

    var isRefreshing by remember { mutableStateOf(false) }
    var showPeriodSheet by remember { mutableStateOf(false) }
    val refreshScope = rememberCoroutineScope()
    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Riwayat", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("Aktivitas Agent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.loading && state.activities.isEmpty()) {
            TimelineSkeleton(modifier = Modifier.padding(padding))
        } else {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    refreshScope.launch {
                        isRefreshing = true
                        viewModel.refresh()
                        delay(300)
                        isRefreshing = false
                    }
                },
                state = pullRefreshState,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { viewModel.setSearchQuery(it) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    item {
                        FilterChipRow(
                            currentFilter = selectedFilter,
                            currentPeriod = selectedPeriod,
                            onFilterSelected = { viewModel.setFilter(it) },
                            onPeriodClick = { showPeriodSheet = true }
                        )
                    }

                    if (state.error != null && state.groupedActivities.isEmpty()) {
                        item {
                            ErrorState(
                                message = state.error ?: "Gagal memuat riwayat",
                                onRetry = { viewModel.refresh() }
                            )
                        }
                    } else if (state.groupedActivities.isEmpty()) {
                        item {
                            EmptyState(
                                hasSearch = searchQuery.isNotBlank(),
                                hasFilter = selectedFilter != ActivityFilter.ALL
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = state.groupedActivities,
                            key = { _, pair -> pair.first }
                        ) { groupIndex, (dateLabel, activities) ->
                            DateSectionHeader(
                                label = dateLabel,
                                modifier = Modifier.animateEntrance(groupIndex * 50)
                            )
                            activities.forEachIndexed { index, activity ->
                                TimelineItem(
                                    activity = activity,
                                    isLast = index == activities.size - 1,
                                    onClick = { navigateToDetail(navController, activity) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPeriodSheet) {
        PeriodBottomSheet(
            currentPeriod = selectedPeriod,
            onPeriodSelected = { period ->
                viewModel.setPeriod(period)
                showPeriodSheet = false
            },
            onDismiss = { showPeriodSheet = false }
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text("Cari aktivitas...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Hapus pencarian",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun FilterChipRow(
    currentFilter: ActivityFilter,
    currentPeriod: PeriodFilter,
    onFilterSelected: (ActivityFilter) -> Unit,
    onPeriodClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ActivityFilter.entries.forEach { filter ->
            FilterChip(
                selected = currentFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(filter.displayName, style = MaterialTheme.typography.labelMedium)
                },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        FilterChip(
            selected = currentPeriod != PeriodFilter.ALL,
            onClick = onPeriodClick,
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(currentPeriod.displayName, style = MaterialTheme.typography.labelMedium)
                }
            },
            shape = RoundedCornerShape(10.dp),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                selectedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun DateSectionHeader(label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.width(12.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun TimelineItem(
    activity: Activity,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val config = getActivityVisual(activity.type)
    val hasNavigation = activity.jamaahId.isNotBlank()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .then(
                if (hasNavigation) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .heightIn(min = 56.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(config.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = config.icon,
                    contentDescription = null,
                    tint = config.color,
                    modifier = Modifier.size(14.dp)
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(56.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 8.dp else 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatTimestamp(activity.timestamp.toDate().time),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            if (activity.jamaahName.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = activity.jamaahName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (activity.description.isNotBlank() && activity.description != activity.jamaahName) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = formatSecondaryInfo(activity),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (hasNavigation) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Ketuk untuk detail",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
            }
        }

        if (hasNavigation) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier
                    .size(16.dp)
                    .padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodBottomSheet(
    currentPeriod: PeriodFilter,
    onPeriodSelected: (PeriodFilter) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Filter Periode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            PeriodFilter.entries.forEach { period ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onPeriodSelected(period) },
                    color = if (currentPeriod == period)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    else
                        Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentPeriod == period,
                            onClick = { onPeriodSelected(period) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            period.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(hasSearch: Boolean, hasFilter: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (hasSearch) Icons.Default.SearchOff else Icons.Default.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = if (hasSearch) "Tidak ada aktivitas yang cocok"
                   else "Belum ada aktivitas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (hasSearch)
                       "Coba gunakan kata kunci lain."
                   else
                       "Setiap aktivitas penting Anda akan muncul di sini.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Gagal memuat riwayat",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedButton(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Coba Lagi")
        }
    }
}

@Composable
private fun TimelineSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        repeat(6) {
            Row(modifier = Modifier.fillMaxWidth()) {
                ShimmerBox(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerBox(
                        modifier = Modifier.fillMaxWidth(0.8f).height(14.dp),
                        shape = RoundedCornerShape(6.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ShimmerBox(
                        modifier = Modifier.fillMaxWidth(0.5f).height(12.dp),
                        shape = RoundedCornerShape(6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun getActivityVisual(type: ActivityType): ActivityVisual {
    return when (type) {
        ActivityType.REGISTRATION -> ActivityVisual(
            Icons.Default.PersonAdd, MaterialTheme.colorScheme.primary
        )
        ActivityType.STATUS_UPDATE -> ActivityVisual(
            Icons.Default.Edit, MaterialTheme.colorScheme.primary
        )
        ActivityType.PAYMENT -> ActivityVisual(
            Icons.Default.Payments, MaterialTheme.colorScheme.tertiary
        )
        ActivityType.INVOICE -> ActivityVisual(
            Icons.Default.Receipt, MaterialTheme.colorScheme.primary
        )
        ActivityType.PROFILE_UPDATE -> ActivityVisual(
            Icons.Default.Edit, MaterialTheme.colorScheme.secondary
        )
        ActivityType.OTHER -> ActivityVisual(
            Icons.Default.Info, MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatTimestamp(timeMillis: Long): String {
    if (timeMillis == 0L) return ""
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timeMillis))
}

private fun formatSecondaryInfo(activity: Activity): String {
    return when (activity.type) {
        ActivityType.PAYMENT -> {
            if (activity.amount > 0) {
                val fmt = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                    maximumFractionDigits = 0
                }
                "+${fmt.format(activity.amount).replace(",00", "")}"
            } else activity.description
        }
        else -> if (activity.description.length > 60) activity.description.take(60) + "..."
                else activity.description
    }
}

private fun navigateToDetail(navController: NavController, activity: Activity) {
    if (activity.jamaahId.isBlank()) return
    when (activity.type) {
        ActivityType.PAYMENT, ActivityType.REGISTRATION, ActivityType.STATUS_UPDATE ->
            navController.navigate("detail_jamaah/${activity.jamaahId}")
        ActivityType.INVOICE -> {
            if (activity.invoiceId.isNotBlank())
                navController.navigate("agent_invoice_detail/${activity.invoiceId}")
            else
                navController.navigate("detail_jamaah/${activity.jamaahId}")
        }
        else ->
            navController.navigate("detail_jamaah/${activity.jamaahId}")
    }
}

private data class ActivityVisual(
    val icon: ImageVector,
    val color: Color
)
