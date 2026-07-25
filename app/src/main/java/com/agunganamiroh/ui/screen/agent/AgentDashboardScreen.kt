package com.agunganamiroh.ui.screen.agent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agunganamiroh.viewmodel.ActivityViewModel
import com.agunganamiroh.viewmodel.NotificationViewModel
import com.agunganamiroh.viewmodel.AuthViewModel
import com.agunganamiroh.viewmodel.JamaahViewModel
import com.agunganamiroh.viewmodel.PaketViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.agunganamiroh.motion.*

// ============================================================
// UI MODELS
// ============================================================
data class StatItem(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val color: Color,
    val growth: String = "+0%"
)

data class MenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String,
    val badge: Int = 0
)

data class QuickAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String
)

// ============================================================
// MAIN SCREEN
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentDashboardScreen(
    navController: NavController,
    nestedScrollConnection: androidx.compose.ui.input.nestedscroll.NestedScrollConnection? = null,
    authViewModel: AuthViewModel = viewModel(),
    paketViewModel: PaketViewModel = viewModel(),
    jamaahViewModel: JamaahViewModel = viewModel(),
    activityViewModel: ActivityViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = viewModel()
) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val paketState by paketViewModel.uiState.collectAsStateWithLifecycle()
    val jamaahState by jamaahViewModel.uiState.collectAsStateWithLifecycle()
    val activityState by activityViewModel.uiState.collectAsStateWithLifecycle()
    val notificationState by notificationViewModel.state.collectAsStateWithLifecycle()

    val agentName = authState.user?.companyName ?: "Agent Anamiroh"
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val warningColor = MaterialTheme.colorScheme.secondary
    val successColor = MaterialTheme.colorScheme.tertiary
    val infoColor = MaterialTheme.colorScheme.primary // Fallback for info blue if not defined
    
    LaunchedEffect(authState.user?.email) {
        authState.user?.email?.let { email ->
            jamaahViewModel.loadJamaahByAgent(email)
        }
    }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    var isRefreshing by remember { mutableStateOf(false) }
    val refreshScope = rememberCoroutineScope()
    val pullRefreshState = rememberPullToRefreshState()

    // Prepare Stats Data
    val stats = remember(jamaahState.jamaahs, primaryColor, warningColor, successColor) {
        val jamaahs = jamaahState.jamaahs
        val total = jamaahs.size
        val pending = jamaahs.count { it.status.lowercase() == "pending" }
        val approved = jamaahs.count { it.status.lowercase() == "approved" || it.status.lowercase() == "verified" }
        val omzet = jamaahs.sumOf { it.dp }
        
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val omzetFormatted = currencyFormat.format(omzet).replace(",00", "")

        listOf(
            StatItem("Jamaah Saya", total.toString(), Icons.Default.Group, primaryColor, "+5%"),
            StatItem("Pending", pending.toString(), Icons.Default.Schedule, warningColor, "-2%"),
            StatItem("Approved", approved.toString(), Icons.Default.Verified, successColor, "+8%"),
            StatItem("Total Omzet", omzetFormatted, Icons.AutoMirrored.Filled.TrendingUp, primaryColor, "+12%")
        )
    }

    val quickActions = remember {
        listOf(
            QuickAction("Tambah Jamaah Baru", "Pendaftaran data jamaah baru", Icons.Default.PersonAdd, "input_jamaah"),
            QuickAction("Pembayaran", "Input bukti bayar jamaah", Icons.Default.Payments, "pembayaran"),
            QuickAction("Invoice", "Lihat tagihan jamaah", Icons.AutoMirrored.Filled.ReceiptLong, "agent_invoice_list"),
            QuickAction("Data Jamaah", "Lihat daftar jamaah", Icons.Default.AssignmentInd, "data_jamaah")
        )
    }

    val menuItems = remember {
        listOf(
            MenuItem("Input Jamaah", "Pendaftaran jamaah baru", Icons.Default.PersonAdd, "input_jamaah"),
            MenuItem("Data Jamaah", "Kelola database jamaah", Icons.Default.AssignmentInd, "data_jamaah", 3),
            MenuItem("Pembayaran", "Verifikasi & riwayat bayar", Icons.Default.Payments, "pembayaran"),
            MenuItem("Invoice", "Cetak & kirim tagihan", Icons.Default.Receipt, "agent_invoice_list"),
            MenuItem("Laporan", "Analisis penjualan & kinerja", Icons.Default.BarChart, "laporan"),
            MenuItem("Keberangkatan", "Pantau kesiapan jamaah", Icons.Default.FlightTakeoff, "keberangkatan"),
            MenuItem("Riwayat", "Laporan aktivitas agent", Icons.Default.History, "riwayat"),
            MenuItem("Profil", "Pengaturan akun agent", Icons.Default.Person, "account_center")
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                drawCircle(
                    color = primaryColor.copy(alpha = 0.05f),
                    radius = 200.dp.toPx(),
                    center = Offset(size.width, 0f)
                )
                drawCircle(
                    color = primaryColor.copy(alpha = 0.03f),
                    radius = 150.dp.toPx(),
                    center = Offset(0f, size.height)
                )
            }
    ) {
        IslamicPatternOverlay()

        Scaffold(
            topBar = {
                DashboardTopBar(
                    agentName = agentName,
                    notificationState = notificationState,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("agent_dashboard") { inclusive = true }
                        }
                    },
                    onNavigate = { route -> navController.navigate(route) }
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 10 }
            ) {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        authState.user?.email?.let { email ->
                            refreshScope.launch {
                                isRefreshing = true
                                jamaahViewModel.loadJamaahByAgent(email)
                                paketViewModel.observePakets()
                                activityViewModel.observeActivities()
                                notificationViewModel.refresh()
                                delay(300)
                                isRefreshing = false
                            }
                        }
                    },
                    state = pullRefreshState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (nestedScrollConnection != null) Modifier.nestedScroll(nestedScrollConnection) else Modifier)
                            .padding(paddingValues),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        item {
                            DashboardHeroCard(
                                target = 30,
                                achievement = jamaahState.jamaahs.size
                            )
                        }

                        item {
                            QuickActionSection(
                                actions = quickActions,
                                onActionClick = { route -> navController.navigate(route) }
                            )
                        }

                        item {
                            StatisticsSection(stats = stats, isLoading = jamaahState.loading)
                        }

                        item {
                            UpcomingPackageSection(
                                paketList = paketState.pakets,
                                isLoading = paketState.loading,
                                onDetailClick = { paket ->
                                    navController.navigate("package_detail/${paket.id}")
                                },
                                onSeeAllClick = { navController.navigate("package_catalog") }
                            )
                        }

                        item {
                            ActivitySection(
                                activities = activityState.activities,
                                isLoading = activityState.loading,
                                onSeeAllClick = { navController.navigate("riwayat") }
                            )
                        }

                        item {
                            MenuSection(
                                menuItems = menuItems,
                                onMenuClick = { route -> navController.navigate(route) }
                            )
                        }
                    }
                }
            }
        }

    }
}

// ============================================================
// COMPONENTS
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(
    agentName: String,
    notificationState: com.agunganamiroh.viewmodel.NotificationUiState,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .clickable { showMenu = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = agentName.take(1).uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Assalamu'alaikum", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "$agentName 👋", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TopBarIcon(
                    icon = Icons.Default.Notifications,
                    hasBadge = notificationState.unreadCount > 0,
                    onClick = { onNavigate("notification_center") }
                )
                TopBarIcon(icon = Icons.AutoMirrored.Filled.Chat)
                
                Box {
                    TopBarIcon(icon = Icons.Default.AccountCircle, onClick = { showMenu = true })
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                        DropdownMenuItem(
                            text = { Text("Profil Saya", color = MaterialTheme.colorScheme.onSurface) },
                            leadingIcon = { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = { showMenu = false; onNavigate("account_center") }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline)
                        DropdownMenuItem(
                            text = { Text("Logout", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { showMenu = false; onLogout() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBarIcon(icon: ImageVector, hasBadge: Boolean = false, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        if (hasBadge) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.error).align(Alignment.TopEnd).offset(x = (-8).dp, y = 8.dp))
        }
    }
}

@Composable
private fun DashboardHeroCard(target: Int, achievement: Int) {
    val progressValue = (achievement.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progressValue, animationSpec = tween(durationMillis = 1000), label = "progress")
    val remaining = (target - achievement).coerceAtLeast(0)
    val percentage = (progressValue * 100).toInt()

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(160.dp).animateEntrance(0),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f), MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))))) {
            Row(modifier = Modifier.fillMaxSize().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1.2f)) {
                    Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Target Bulanan", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    Text(text = "$achievement / $target", style = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, letterSpacing = (-1).sp))
                    Text(text = "Jamaah Terdaftar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }

                Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)), color = MaterialTheme.colorScheme.surface, modifier = Modifier.size(width = 54.dp, height = 32.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "$percentage%", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(progress = { animatedProgress }, modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))
                    val currentMonth = SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(Date())
                    Text(text = "1 - 31 $currentMonth", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 10.sp)
                }

                Box(modifier = Modifier.fillMaxHeight(0.7f).width(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)))

                Column(modifier = Modifier.weight(1f).padding(start = 16.dp), horizontalAlignment = Alignment.End) {
                    Text(text = "Sisa Target", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Group, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "$remaining", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.End)
                        Text(text = " Jamaah", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), textAlign = TextAlign.End)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Ayo capai target bulan ini.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium, lineHeight = 14.sp, fontSize = 11.sp, textAlign = TextAlign.End)
                }
            }
        }
    }
}

@Composable
private fun QuickActionSection(actions: List<QuickAction>, onActionClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "Aksi Cepat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            actions.forEach { action ->
                QuickActionCard(action = action, onClick = { onActionClick(action.route) })
            }
        }
    }
}

@Composable
private fun QuickActionCard(action: QuickAction, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().bounceClick().clickable { onClick() }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) {
                Icon(imageVector = action.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = action.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = action.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun StatisticsSection(stats: List<StatItem>, isLoading: Boolean = false) {
    Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "Statistik Performa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(stat = stats[0], modifier = Modifier.weight(1f), isLoading = isLoading)
                StatCard(stat = stats[1], modifier = Modifier.weight(1f), isLoading = isLoading)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(stat = stats[2], modifier = Modifier.weight(1f), isLoading = isLoading)
                StatCard(stat = stats[3], modifier = Modifier.weight(1f), isLoading = isLoading)
            }
        }
    }
}

@Composable
private fun StatCard(stat: StatItem, modifier: Modifier = Modifier, isLoading: Boolean = false) {
    Card(modifier = modifier.animateEntrance(0), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(stat.color.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(stat.icon, null, tint = stat.color, modifier = Modifier.size(16.dp))
                }
                if (!isLoading) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(text = stat.growth, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (isLoading) {
                DashboardStatSkeleton()
            } else {
                Text(text = stat.value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            }
            Text(text = stat.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun UpcomingPackageSection(
    paketList: List<com.agunganamiroh.data.model.Paket>,
    isLoading: Boolean,
    onDetailClick: (com.agunganamiroh.data.model.Paket) -> Unit,
    onSeeAllClick: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Paket Umroh & Haji", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "Lihat Semua", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onSeeAllClick() })
        }
        if (isLoading) {
            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(3) { ShimmerPackageCard() }
            }
        } else if (paketList.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                Text("No available packages.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(paketList) { paket ->
                    PackageCard(paket = paket, onDetailClick = onDetailClick)
                }
            }
        }
    }
}

@Composable
private fun PackageCard(paket: com.agunganamiroh.data.model.Paket, onDetailClick: (com.agunganamiroh.data.model.Paket) -> Unit) {
    Card(modifier = Modifier.width(260.dp).bounceClick().clickable { onDetailClick(paket) }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            val isAvailable = paket.sisaSeat > 0
            val statusColor = if (isAvailable) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
            Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.1f)) {
                Text(text = if (isAvailable) "Available" else "Sold Out", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = paket.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = "${paket.durasi} • ${paket.maskapai}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(paket.tanggal, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                Text(text = format.format(paket.harga).replace(",00", ""), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                Text(text = "${paket.sisaSeat} Seat", style = MaterialTheme.typography.labelSmall, color = if (paket.sisaSeat < 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun ShimmerPackageCard() {
    Card(modifier = Modifier.width(260.dp).height(160.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))) {}
}

@Composable
private fun ActivitySection(activities: List<com.agunganamiroh.data.model.Activity>, isLoading: Boolean, onSeeAllClick: () -> Unit = {}) {
    Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Aktivitas Terbaru", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "See All", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onSeeAllClick() })
        }
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (isLoading) {
                    repeat(3) { ActivityShimmerItem() }
                } else if (activities.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                        Text("No recent activity.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    activities.take(5).forEachIndexed { index, activity ->
                        ActivityItem(activity = activity, isLast = index == minOf(activities.size - 1, 4))
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityItem(activity: com.agunganamiroh.data.model.Activity, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        val color = when(activity.type) {
            "JAMAAH_CREATED" -> MaterialTheme.colorScheme.primary
            "JAMAAH_UPDATED" -> MaterialTheme.colorScheme.secondary
            "JAMAAH_DELETED" -> MaterialTheme.colorScheme.error
            "PAYMENT_ADDED" -> MaterialTheme.colorScheme.tertiary
            "PAYMENT_COMPLETED" -> MaterialTheme.colorScheme.tertiary
            "INVOICE_CREATED" -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.primary
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
            if (!isLast) {
                Box(modifier = Modifier.width(2.dp).height(50.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = activity.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = getRelativeTime(activity.createdAt?.toDate()?.time ?: 0L), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
            Text(text = activity.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ActivityShimmerItem() {
    Spacer(modifier = Modifier.height(60.dp))
}

private fun getRelativeTime(timeMillis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timeMillis
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000} mins ago"
        diff < 86400_000 -> "${diff / 3600_000} hours ago"
        diff < 604800_000 -> "${diff / 86400_000} days ago"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timeMillis))
    }
}

@Composable
private fun MenuSection(menuItems: List<MenuItem>, onMenuClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "Menu Utama", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            menuItems.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MenuCard(item = row[0], modifier = Modifier.weight(1f), onClick = { onMenuClick(row[0].route) })
                    if (row.size > 1) {
                        MenuCard(item = row[1], modifier = Modifier.weight(1f), onClick = { onMenuClick(row[1].route) })
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuCard(item: MenuItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.bounceClick().clickable { onClick() }, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
                    Icon(item.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                if (item.badge > 0) {
                    Badge(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError) { Text(item.badge.toString()) }
                } else {
                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = item.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun IslamicPatternOverlay() {
    val patternColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val opacity = if (backgroundColor.luminance() < 0.5f) 0.05f else 0.03f
    
    Canvas(modifier = Modifier.fillMaxSize().alpha(opacity)) {
        val sizePx = 60.dp.toPx()
        val strokeWidth = 1.dp.toPx()
        for (x in 0..(size.width / sizePx).toInt()) {
            for (y in 0..(size.height / sizePx).toInt()) {
                val cx = x * sizePx
                val cy = y * sizePx
                drawPath(path = Path().apply {
                    moveTo(cx, cy - sizePx * 0.5f)
                    lineTo(cx + sizePx * 0.15f, cy - sizePx * 0.15f)
                    lineTo(cx + sizePx * 0.5f, cy)
                    lineTo(cx + sizePx * 0.15f, cy + sizePx * 0.15f)
                    lineTo(cx, cy + sizePx * 0.5f)
                    lineTo(cx - sizePx * 0.15f, cy + sizePx * 0.15f)
                    lineTo(cx - sizePx * 0.5f, cy)
                    lineTo(cx - sizePx * 0.15f, cy - sizePx * 0.15f)
                    close()
                }, color = patternColor, style = Stroke(width = strokeWidth))
            }
        }
    }
}


