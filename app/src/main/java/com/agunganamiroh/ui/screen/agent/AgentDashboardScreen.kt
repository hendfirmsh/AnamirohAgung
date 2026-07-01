package com.agunganamiroh.ui.screen.agent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.agunganamiroh.R
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ============================================================
// THEME CONSTANTS - GOLD & WHITE PREMIUM ENTERPRISE THEME
// ============================================================
private val GoldPrimary = Color(0xFFD4AF37)
private val GoldDark = Color(0xFFB8860B)
private val GoldLight = Color(0xFFF0E68C)
private val GoldPale = Color(0xFFFFF8E1)
private val GoldGradientStart = Color(0xFFD4AF37)
private val GoldGradientMid = Color(0xFFE8C84A)
private val GoldGradientEnd = Color(0xFFB8860B)
private val BackgroundWhite = Color(0xFFFFFFFF)
private val SurfaceWhite = Color(0xFFFDFBF7)
private val GlassWhite = Color(0xF2FFFFFF)
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF4A4A4A)
private val TextMuted = Color(0xFF888888)
private val SuccessGreen = Color(0xFF2E7D32)
private val WarningOrange = Color(0xFFED6C02)
private val ErrorRed = Color(0xFFD32F2F)
private val InfoBlue = Color(0xFF1565C0)

// ============================================================
// DUMMY DATA MODELS (Siap diganti dengan Firestore)
// ============================================================
data class StatItem(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val tint: Color,
    val bgTint: Color
)

data class MenuItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val description: String,
    val badge: Int = 0
)

data class PaketUmroh(
    val id: String,
    val nama: String,
    val tanggal: String,
    val harga: Long,
    val sisaSeat: Int,
    val totalSeat: Int,
    val imageRes: Int = R.drawable.background_das
)

data class Aktivitas(
    val id: String,
    val title: String,
    val subtitle: String,
    val time: String,
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color
)

// ============================================================
// MAIN SCREEN
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentDashboardScreen(
    navController: NavController,
    agentName: String = "Ahmad Fauzi",
    agentEmail: String = "agent@agung-anamiroh.com"
) {
    val currentDate = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(Date())
    }

    var showProfileMenu by remember { mutableStateOf(false) }
    var showNotificationBadge by remember { mutableStateOf(true) }
    var selectedStatIndex by remember { mutableIntStateOf(-1) }
    var isContentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isContentVisible = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    val stats = remember {
        listOf(
            StatItem("Jamaah Saya", "128", Icons.Default.Group, GoldPrimary, GoldPale),
            StatItem("Pending", "12", Icons.Default.Schedule, WarningOrange, Color(0xFFFFF3E0)),
            StatItem("Approved", "89", Icons.Default.Verified, SuccessGreen, Color(0xFFE8F5E9)),
            StatItem("Omzet", "Rp 2.4M", Icons.Default.TrendingUp, GoldDark, GoldPale)
        )
    }

    val menuItems = remember {
        listOf(
            MenuItem("Input Jamaah", Icons.Default.PersonAdd, "input_jamaah", "Tambah jamaah baru"),
            MenuItem("Data Jamaah", Icons.Default.AssignmentInd, "data_jamaah", "Kelola data jamaah", 3),
            MenuItem("Pembayaran", Icons.Default.Payment, "pembayaran", "Verifikasi pembayaran", 5),
            MenuItem("Invoice", Icons.Default.Receipt, "invoice", "Generate & kelola invoice"),
            MenuItem("Riwayat", Icons.Default.History, "riwayat_transaksi", "Histori transaksi"),
            MenuItem("Profil", Icons.Default.Person, "profil_agent", "Pengaturan profil")
        )
    }

    val paketList = remember {
        listOf(
            PaketUmroh("1", "Umroh Regular 12 Hari", "15 Jan 2027", 28500000, 8, 45),
            PaketUmroh("2", "Umroh Plus Turki 15 Hari", "22 Feb 2027", 38500000, 3, 30),
            PaketUmroh("3", "Umroh Ramadhan 10 Hari", "10 Mar 2027", 32500000, 15, 40),
            PaketUmroh("4", "Umroh Ekonomis 9 Hari", "05 Apr 2027", 21500000, 22, 50)
        )
    }

    val aktivitasList = remember {
        listOf(
            Aktivitas("1", "Jamaah baru ditambahkan", "Ahmad Rizky - Paket Regular", "2 menit lalu", Icons.Default.PersonAdd, GoldPale, GoldDark),
            Aktivitas("2", "Pembayaran diverifikasi", "Rp 15.000.000 - Ustad Haryono", "15 menit lalu", Icons.Default.CheckCircle, Color(0xFFE8F5E9), SuccessGreen),
            Aktivitas("3", "Invoice dibuat", "INV-2027-00189 - 4 Jamaah", "1 jam lalu", Icons.Default.Receipt, Color(0xFFFFF3E0), WarningOrange),
            Aktivitas("4", "Jamaah update dokumen", "Fatimah Azzahra - Paspor baru", "3 jam lalu", Icons.Default.Verified, Color(0xFFE3F2FD), InfoBlue)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ============================================================
        // BACKGROUND IMAGE LAYER
        // ============================================================
        Image(
            painter = painterResource(id = R.drawable.background_das),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark overlay gradient for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x40000000),
                            Color(0x80000000),
                            Color(0xB3000000)
                        )
                    )
                )
        )

        // Floating gold particles effect (subtle)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        color = GoldPrimary.copy(alpha = 0.08f),
                        radius = size.width * 0.6f,
                        center = Offset(size.width * 0.5f, -size.height * 0.1f)
                    )
                }
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = GoldPrimary,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "AA",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            shadow = Shadow(
                                                color = Color.Black.copy(alpha = 0.3f),
                                                offset = Offset(1f, 1f),
                                                blurRadius = 4f
                                            )
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "AGUNG ANAMIROH",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp,
                                        color = Color.White,
                                        shadow = Shadow(
                                            color = Color.Black.copy(alpha = 0.5f),
                                            offset = Offset(1f, 2f),
                                            blurRadius = 6f
                                        )
                                    )
                                )
                                Text(
                                    text = "Dashboard Agent",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = GoldLight.copy(alpha = 0.9f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    },
                    actions = {
                        BadgedBox(
                            badge = {
                                if (showNotificationBadge) {
                                    Badge(
                                        containerColor = ErrorRed,
                                        contentColor = Color.White
                                    ) {
                                        Text("3", fontSize = 10.sp)
                                    }
                                }
                            },
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            IconButton(
                                onClick = { showNotificationBadge = false }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        Box {
                            IconButton(
                                onClick = { showProfileMenu = true },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(GoldPrimary.copy(alpha = 0.2f))
                                        .shadow(6.dp, CircleShape)
                                        .border(BorderStroke(2.dp, GoldPrimary.copy(alpha = shimmerAlpha)), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile",
                                        tint = GoldLight,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showProfileMenu,
                                onDismissRequest = { showProfileMenu = false },
                                modifier = Modifier.width(200.dp),
                                containerColor = GlassWhite,
                                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(GoldPrimary.copy(alpha = 0.2f))
                                            .border(BorderStroke(2.dp, GoldPrimary), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        agentName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        agentEmail,
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                                HorizontalDivider(color = GoldPrimary.copy(alpha = 0.2f))
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Profil Saya",
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = GoldPrimary
                                        )
                                    },
                                    onClick = {
                                        showProfileMenu = false
                                        navController.navigate("profil_agent")
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Pengaturan",
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = GoldPrimary
                                        )
                                    },
                                    onClick = {
                                        showProfileMenu = false
                                    }
                                )
                                HorizontalDivider(color = GoldPrimary.copy(alpha = 0.2f))
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Logout",
                                            fontWeight = FontWeight.SemiBold,
                                            color = ErrorRed
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Logout,
                                            contentDescription = null,
                                            tint = ErrorRed
                                        )
                                    },
                                    onClick = {
                                        showProfileMenu = false
                                        navController.navigate("login") {
                                            popUpTo("dashboard") { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    AnimatedVisibility(
                        visible = isContentVisible,
                        enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 2 }
                    ) {
                        WelcomeCard(
                            agentName = agentName,
                            currentDate = currentDate
                        )
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = isContentVisible,
                        enter = fadeIn(tween(700, delayMillis = 100)) + slideInVertically(tween(700, delayMillis = 100)) { it / 2 }
                    ) {
                        SectionTitle("Statistik")
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = isContentVisible,
                        enter = fadeIn(tween(800, delayMillis = 200)) + slideInVertically(tween(800, delayMillis = 200)) { it / 2 }
                    ) {
                        StatGrid(
                            stats = stats,
                            onStatClick = { index ->
                                selectedStatIndex = index
                            }
                        )
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = isContentVisible,
                        enter = fadeIn(tween(900, delayMillis = 300)) + slideInVertically(tween(900, delayMillis = 300)) { it / 2 }
                    ) {
                        SectionTitle("Menu Utama")
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = isContentVisible,
                        enter = fadeIn(tween(1000, delayMillis = 400)) + slideInVertically(tween(1000, delayMillis = 400)) { it / 2 }
                    ) {
                        MenuGrid(
                            menuItems = menuItems,
                            onMenuClick = { route ->
                                navController.navigate(route)
                            }
                        )
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = isContentVisible,
                        enter = fadeIn(tween(1100, delayMillis = 500)) + slideInVertically(tween(1100, delayMillis = 500)) { it / 2 }
                    ) {
                        SectionTitle("Aksi Cepat")
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = isContentVisible,
                        enter = fadeIn(tween(1200, delayMillis = 600)) + slideInVertically(tween(1200, delayMillis = 600)) { it / 2 }
                    ) {
                        AksiCepatSection(
                            onTambahJamaah = {
                                navController.navigate("input_jamaah")
                            },
                            onBuatPembayaran = {
                                navController.navigate("pembayaran")
                            }
                        )
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = isContentVisible,
                        enter = fadeIn(tween(1300, delayMillis = 700)) + slideInVertically(tween(1300, delayMillis = 700)) { it / 2 }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionTitle("Paket Umroh")
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(GoldPrimary.copy(alpha = 0.2f))
                                    .clickable { }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Lihat Semua",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = GoldLight,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = isContentVisible,
                        enter = fadeIn(tween(1400, delayMillis = 800)) + slideInVertically(tween(1400, delayMillis = 800)) { it / 2 }
                    ) {
                        PaketHorizontalList(paketList = paketList)
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = isContentVisible,
                        enter = fadeIn(tween(1500, delayMillis = 900)) + slideInVertically(tween(1500, delayMillis = 900)) { it / 2 }
                    ) {
                        SectionTitle("Aktivitas Terbaru")
                    }
                }

                items(aktivitasList) { aktivitas ->
                    AnimatedVisibility(
                        visible = isContentVisible,
                        enter = fadeIn(tween(1600, delayMillis = 1000))
                    ) {
                        AktivitasItem(aktivitas = aktivitas)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// ============================================================
// SECTION TITLE COMPONENT
// ============================================================
@Composable
private fun SectionTitle(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .background(GoldPrimary, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    offset = Offset(0f, 1f),
                    blurRadius = 4f
                )
            )
        )
    }
}

// ============================================================
// WELCOME CARD - GOLD GLASSMORPHISM WITH ANIMATION
// ============================================================
@Composable
private fun WelcomeCard(
    agentName: String,
    currentDate: String
) {
    val scaleAnim = remember { Animatable(0.9f) }
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, tween(800))
        scaleAnim.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }

    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scaleAnim.value)
            .alpha(alphaAnim.value)
            .offset(y = floatOffset.dp)
            .shadow(12.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 12.dp,
            pressedElevation = 8.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GoldGradientStart.copy(alpha = 0.95f),
                            GoldGradientMid.copy(alpha = 0.9f),
                            GoldGradientEnd.copy(alpha = 0.95f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
                .padding(26.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .align(Alignment.TopEnd)
                        .offset(x = 20.dp, y = (-30).dp)
                )
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                        .align(Alignment.BottomStart)
                        .offset(x = (-10).dp, y = 20.dp)
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Assalamu'alaikum",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Selamat datang kembali,",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = agentName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.3f),
                            offset = Offset(1f, 2f),
                            blurRadius = 4f
                        )
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentDate,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// STATISTICS GRID COMPONENT
// ============================================================
@Composable
private fun StatGrid(
    stats: List<StatItem>,
    onStatClick: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        stats.chunked(2).forEach { rowStats ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowStats.forEach { stat ->
                    val actualIndex = stats.indexOf(stat)
                    StatCard(
                        stat = stat,
                        modifier = Modifier.weight(1f),
                        onClick = { onStatClick(actualIndex) }
                    )
                }
                if (rowStats.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    stat: StatItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "stat_scale"
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = GlassWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        ),
        interactionSource = interactionSource
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(stat.bgTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = stat.icon,
                    contentDescription = null,
                    tint = stat.tint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stat.value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stat.label,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 12.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ============================================================
// MENU GRID COMPONENT
// ============================================================
@Composable
private fun MenuGrid(
    menuItems: List<MenuItem>,
    onMenuClick: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        menuItems.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { menu ->
                    MenuCard(
                        menu = menu,
                        modifier = Modifier.weight(1f),
                        onClick = { onMenuClick(menu.route) }
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MenuCard(
    menu: MenuItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "menu_scale"
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = GlassWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        ),
        interactionSource = interactionSource
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BadgedBox(
                    badge = {
                        if (menu.badge > 0) {
                            Badge(
                                containerColor = ErrorRed,
                                contentColor = Color.White,
                                modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                            ) {
                                Text(
                                    if (menu.badge > 9) "9+" else menu.badge.toString(),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = menu.icon,
                            contentDescription = menu.title,
                            tint = GoldPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = menu.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = menu.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ============================================================
// AKSI CEPAT SECTION
// ============================================================
@Composable
private fun AksiCepatSection(
    onTambahJamaah: () -> Unit,
    onBuatPembayaran: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onTambahJamaah,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp,
                pressedElevation = 2.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Tambah Jamaah Baru",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }

        OutlinedButton(
            onClick = onBuatPembayaran,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White.copy(alpha = 0.15f),
                contentColor = GoldLight
            ),
            border = BorderStroke(1.5.dp, GoldPrimary.copy(alpha = 0.6f)),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 1.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.PointOfSale,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Buat Pembayaran",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

// ============================================================
// PAKET HORIZONTAL LIST
// ============================================================
@Composable
private fun PaketHorizontalList(paketList: List<PaketUmroh>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(paketList) { paket ->
            PaketCard(paket = paket)
        }
    }
}

@Composable
private fun PaketCard(paket: PaketUmroh) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "paket_scale"
    )

    val hargaFormatted = remember(paket.harga) {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(paket.harga)
    }

    val sisaSeatPercent = remember(paket.sisaSeat, paket.totalSeat) {
        (paket.sisaSeat.toFloat() / paket.totalSeat.toFloat())
    }

    val seatColor = when {
        paket.sisaSeat <= 5 -> ErrorRed
        paket.sisaSeat <= 10 -> WarningOrange
        else -> SuccessGreen
    }

    val urgencyText = when {
        paket.sisaSeat <= 3 -> "Segera!"
        paket.sisaSeat <= 8 -> "Terbatas"
        else -> "Tersedia"
    }

    Card(
        onClick = { },
        modifier = Modifier
            .width(290.dp)
            .scale(scale)
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = GlassWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        ),
        interactionSource = interactionSource
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                GoldGradientStart.copy(alpha = 0.3f),
                                GoldGradientEnd.copy(alpha = 0.6f)
                            )
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(GoldPrimary)
                            .align(Alignment.TopStart)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "UMROH",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(seatColor)
                            .align(Alignment.TopEnd)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = urgencyText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier
                            .size(60.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 10.dp, y = 10.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = paket.nama,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = paket.tanggal,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(color = GoldPrimary.copy(alpha = 0.15f))

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Harga",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = hargaFormatted,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = GoldDark
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Detail",
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF0F0F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(sisaSeatPercent)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldPrimary)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${paket.sisaSeat} dari ${paket.totalSeat} kursi",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = "${(sisaSeatPercent * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

// ============================================================
// AKTIVITAS ITEM - TIMELINE STYLE
// ============================================================
@Composable
private fun AktivitasItem(aktivitas: Aktivitas) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(aktivitas.iconBg)
                    .border(BorderStroke(2.dp, GoldPrimary.copy(alpha = 0.3f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = aktivitas.icon,
                    contentDescription = null,
                    tint = aktivitas.iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(36.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                GoldPrimary.copy(alpha = 0.3f),
                                GoldPrimary.copy(alpha = 0.05f)
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .shadow(4.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = GlassWhite
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = aktivitas.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = aktivitas.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = aktivitas.time,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}