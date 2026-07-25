package com.agunganamiroh.ui.screen.invoice

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agunganamiroh.data.model.Invoice
import com.agunganamiroh.motion.*
import com.agunganamiroh.viewmodel.AgentInvoiceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentInvoiceListScreen(
    navController: NavController,
    viewModel: AgentInvoiceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }
    val refreshScope = rememberCoroutineScope()
    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit) {
        viewModel.loadInvoices()
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        IslamicPatternOverlay()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Invoice", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            Text("Tagihan jamaah Anda", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
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
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
                SearchAndFilterSection(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    selectedFilter = uiState.selectedFilter,
                    onFilterChange = { viewModel.onFilterChange(it) },
                    sortOrder = uiState.sortOrder,
                    onSortChange = { viewModel.onSortChange(it) }
                )

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        refreshScope.launch {
                            isRefreshing = true
                            viewModel.loadInvoices()
                            delay(400)
                            isRefreshing = false
                        }
                    },
                    state = pullRefreshState,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (uiState.loading && uiState.invoices.isEmpty()) {
                            LoadingState()
                        } else if (uiState.filteredInvoices.isEmpty()) {
                            EmptyState()
                        } else {
                            InvoiceList(
                                invoices = uiState.filteredInvoices,
                                onItemClick = { id ->
                                    navController.navigate("agent_invoice_detail/$id")
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
private fun SearchAndFilterSection(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    sortOrder: String,
    onSortChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Cari No. Invoice / Nama...", fontSize = 14.sp) },
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val filters = listOf("Semua", "Paid", "Unpaid", "Partial")
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

            SortDropdown(sortOrder = sortOrder, onSortChange = onSortChange)
        }
    }
}

@Composable
private fun SortDropdown(sortOrder: String, onSortChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }) {
            Icon(Icons.Default.Sort, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (sortOrder == "terbaru") "Terbaru" else "Terlama",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(
                text = { Text("Terbaru", color = if (sortOrder == "terbaru") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) },
                onClick = { onSortChange("terbaru"); expanded = false }
            )
            DropdownMenuItem(
                text = { Text("Terlama", color = if (sortOrder == "terlama") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) },
                onClick = { onSortChange("terlama"); expanded = false }
            )
        }
    }
}

@Composable
private fun InvoiceList(
    invoices: List<Invoice>,
    onItemClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(invoices, key = { _, invoice -> invoice.id }) { index, invoice ->
            InvoiceCard(
                invoice = invoice,
                onClick = { onItemClick(invoice.id) },
                modifier = Modifier.staggerItem(index, 60)
            )
        }
    }
}

@Composable
private fun InvoiceCard(invoice: Invoice, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    val statusColor = when (invoice.status) {
        "paid" -> MaterialTheme.colorScheme.tertiary
        "partial" -> MaterialTheme.colorScheme.secondary
        "cancelled" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }
    val statusLabel = when (invoice.status) {
        "paid" -> "Lunas"
        "partial" -> "Partial"
        "cancelled" -> "Batal"
        else -> "Published"
    }

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
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(
                        statusColor.copy(alpha = 0.1f)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (invoice.status) {
                            "paid" -> Icons.Default.Verified
                            "cancelled" -> Icons.Default.Cancel
                            else -> Icons.Default.Receipt
                        },
                        contentDescription = null,
                        tint = statusColor
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = invoice.noInvoice,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = invoice.jamaahName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Tagihan", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        format.format(invoice.totalTagihan).replace(",00", ""),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Tanggal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        invoice.tanggalInvoice,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
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
            Icon(
                Icons.Default.ReceiptLong, null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Tidak Ada Invoice",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Belum ada tagihan yang diterbitkan untuk Anda.",
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
