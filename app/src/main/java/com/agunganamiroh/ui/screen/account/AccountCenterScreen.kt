package com.agunganamiroh.ui.screen.account

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agunganamiroh.data.preferences.NotificationPrefs
import com.agunganamiroh.motion.*
import com.agunganamiroh.ui.screen.account.components.*
import com.agunganamiroh.ui.theme.ThemeMode
import com.agunganamiroh.viewmodel.ProfileViewModel
import com.agunganamiroh.viewmodel.ThemeViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountCenterScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel(),
    themeViewModel: ThemeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val notifPrefs by viewModel.notificationPrefs.collectAsStateWithLifecycle(
        initialValue = NotificationPrefs()
    )
    val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }
    var showFaqDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatus()
        }
    }

    LaunchedEffect(state.updateSuccess) {
        if (state.updateSuccess) {
            snackbarHostState.showSnackbar("Profil berhasil diperbarui")
            viewModel.clearStatus()
            showEditDialog = false
        }
    }

    LaunchedEffect(state.passwordChangeSuccess) {
        if (state.passwordChangeSuccess) {
            snackbarHostState.showSnackbar("Password berhasil diubah")
            viewModel.clearStatus()
            showPasswordDialog = false
        }
    }

    LaunchedEffect(state.logoutSuccess) {
        if (state.logoutSuccess) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Account Center",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading && state.user == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                AccountSkeleton()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    AccountHeader(
                        user = state.user,
                        jamaahCount = state.jamaahCount,
                        invoiceCount = state.invoiceCount,
                        onEditPhotoClick = { showEditDialog = true }
                    )
                }

                item {
                    QuickActionGrid(
                        actions = listOf(
                            QuickAction(
                                icon = Icons.Default.Edit,
                                label = "Edit Profil",
                                onClick = { showEditDialog = true }
                            ),
                            QuickAction(
                                icon = Icons.Default.Receipt,
                                label = "Invoice",
                                onClick = { navController.navigate("agent_invoice_list") }
                            ),
                            QuickAction(
                                icon = Icons.Default.Lock,
                                label = "Keamanan",
                                onClick = { showPasswordDialog = true }
                            ),
                            QuickAction(
                                icon = Icons.Default.HeadsetMic,
                                label = "Bantuan",
                                onClick = { showFaqDialog = true }
                            )
                        )
                    )
                }

                item {
                    SectionCard(
                        icon = Icons.Default.Person,
                        title = "Informasi Akun",
                        subtitle = "Data pribadi dan perusahaan",
                        index = 1,
                        initiallyExpanded = true
                    ) {
                        AccountInfoContent(user = state.user, onEditClick = { showEditDialog = true })
                    }
                }

                item {
                    SectionCard(
                        icon = Icons.Default.Settings,
                        title = "Preferences",
                        subtitle = "Tema, notifikasi, bahasa",
                        index = 2
                    ) {
                        PreferencesContent(
                            themeMode = themeMode,
                            notifPrefs = notifPrefs,
                            onThemeClick = { showThemeDialog = true },
                            onPushToggle = { viewModel.setPushNotifications(it) },
                            onPaymentToggle = { viewModel.setPaymentNotifications(it) },
                            onInvoiceToggle = { viewModel.setInvoiceNotifications(it) },
                            onApprovalToggle = { viewModel.setApprovalNotifications(it) }
                        )
                    }
                }

                item {
                    SectionCard(
                        icon = Icons.Default.Lock,
                        title = "Keamanan",
                        subtitle = "Password, biometric, sesi",
                        index = 3
                    ) {
                        SecurityContent(onChangePassword = { showPasswordDialog = true })
                    }
                }

                item {
                    SectionCard(
                        icon = Icons.Default.Info,
                        title = "Aplikasi",
                        subtitle = "Tentang, kebijakan, lisensi",
                        index = 4
                    ) {
                        ApplicationContent(
                            onAboutClick = { showAboutDialog = true },
                            onPrivacyClick = { showPrivacyDialog = true },
                            onTermsClick = { showTermsDialog = true },
                            onLicensesClick = { showLicensesDialog = true }
                        )
                    }
                }

                item {
                    SectionCard(
                        icon = Icons.AutoMirrored.Filled.Help,
                        title = "Bantuan",
                        subtitle = "FAQ, kontak admin, laporan",
                        index = 5
                    ) {
                        SupportContent(
                            onFaqClick = { showFaqDialog = true },
                            onWhatsAppClick = {
                                val uri = Uri.parse("https://wa.me/628123456789")
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            },
                            onEmailClick = {
                                val uri = Uri.parse("mailto:support@anamiroh.com")
                                context.startActivity(Intent(Intent.ACTION_SENDTO, uri))
                            }
                        )
                    }
                }

                item {
                    SyncStatusCard()
                }

                item {
                    LogoutButton(onLogout = { showLogoutDialog = true })
                }

                item {
                    AccountFooter()
                }
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            user = state.user,
            onDismiss = { showEditDialog = false },
            onSave = { name, phone, address ->
                viewModel.updateProfile(name, phone, address)
            }
        )
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onSave = { old, new -> viewModel.changePassword(old, new) }
        )
    }

    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = { viewModel.logout() }
        )
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = themeMode,
            onDismiss = { showThemeDialog = false },
            onSelect = {
                themeViewModel.setThemeMode(it)
                showThemeDialog = false
            }
        )
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    if (showPrivacyDialog) {
        SimpleInfoDialog(
            title = "Kebijakan Privasi",
            content = "Kebijakan privasi Anamiroh Agung melindungi data pribadi Anda.\n\n" +
                    "Data yang dikumpulkan:\n• Nama lengkap\n• Nomor telepon\n• Alamat email\n• Data jamaah\n\n" +
                    "Data digunakan hanya untuk:\n• Manajemen perjalanan umroh\n• Verifikasi pembayaran\n• Komunikasi dengan agen\n\n" +
                    "Kami tidak membagikan data pribadi Anda kepada pihak ketiga tanpa persetujuan.",
            onDismiss = { showPrivacyDialog = false }
        )
    }

    if (showTermsDialog) {
        SimpleInfoDialog(
            title = "Syarat & Ketentuan",
            content = "Dengan menggunakan aplikasi Anamiroh Agung, Anda menyetujui:\n\n" +
                    "1. Data yang dimasukkan adalah benar dan akurat\n" +
                    "2. Pembayaran dilakukan sesuai ketentuan yang berlaku\n" +
                    "3. Anda bertanggung jawab atas keamanan akun Anda\n" +
                    "4. Perusahaan berhak mengubah ketentuan tanpa pemberitahuan\n\n" +
                    "Aplikasi ini menyediakan layanan manajemen data jamaah umroh.",
            onDismiss = { showTermsDialog = false }
        )
    }

    if (showLicensesDialog) {
        SimpleInfoDialog(
            title = "Lisensi Open Source",
            content = "Aplikasi ini menggunakan library open source berikut:\n\n" +
                    "• Jetpack Compose (Apache 2.0)\n" +
                    "• Firebase SDK (Apache 2.0)\n" +
                    "• Kotlin Coroutines (Apache 2.0)\n" +
                    "• Material3 (Apache 2.0)\n\n" +
                    "Terima kasih kepada seluruh kontributor open source.",
            onDismiss = { showLicensesDialog = false }
        )
    }

    if (showFaqDialog) {
        FaqDialog(onDismiss = { showFaqDialog = false })
    }
}

// ============================================================
// CONTENT SECTIONS
// ============================================================

@Composable
private fun AccountInfoContent(
    user: com.agunganamiroh.data.model.User?,
    onEditClick: () -> Unit
) {
    Column {
        InfoRow(icon = Icons.Default.Email, label = "Email", value = user?.email ?: "-")
        HorizontalDivider()
        InfoRow(icon = Icons.Default.Badge, label = "UID", value = user?.uid ?: "-")
        HorizontalDivider()
        InfoRow(icon = Icons.Default.Phone, label = "Nomor HP", value = user?.phoneNumber ?: "-")
        HorizontalDivider()
        InfoRow(icon = Icons.Default.LocationOn, label = "Alamat", value = user?.address ?: "-")
        HorizontalDivider()
        InfoRow(icon = Icons.Default.Store, label = "Perusahaan", value = user?.companyName ?: "-")
        HorizontalDivider()
        InfoRow(icon = Icons.Default.AccountTree, label = "Cabang", value = user?.branch ?: "-")
        HorizontalDivider()
        InfoRow(icon = Icons.Default.Event, label = "Bergabung", value = formatTimestamp(user?.createdAt))
        HorizontalDivider()
        InfoRow(icon = Icons.Default.History, label = "Terakhir Login", value = formatTimestamp(user?.lastLogin))

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = onEditClick,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit Profil")
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun PreferencesContent(
    themeMode: ThemeMode,
    notifPrefs: NotificationPrefs,
    onThemeClick: () -> Unit,
    onPushToggle: (Boolean) -> Unit,
    onPaymentToggle: (Boolean) -> Unit,
    onInvoiceToggle: (Boolean) -> Unit,
    onApprovalToggle: (Boolean) -> Unit
) {
    Column {
        SettingsRow(
            icon = Icons.Default.Palette,
            title = "Tema Aplikasi",
            subtitle = when (themeMode) {
                ThemeMode.LIGHT -> "Terang"
                ThemeMode.DARK -> "Gelap"
                ThemeMode.SYSTEM -> "Ikuti Sistem"
            },
            onClick = onThemeClick
        )
        HorizontalDivider()
        SettingsSwitch(
            icon = Icons.Default.Notifications,
            title = "Notifikasi Push",
            subtitle = "Pemberitahuan umum",
            checked = notifPrefs.pushEnabled,
            onCheckedChange = onPushToggle
        )
        HorizontalDivider()
        SettingsSwitch(
            icon = Icons.Default.Payments,
            title = "Notifikasi Pembayaran",
            subtitle = "Info pembayaran jamaah",
            checked = notifPrefs.paymentEnabled,
            onCheckedChange = onPaymentToggle
        )
        HorizontalDivider()
        SettingsSwitch(
            icon = Icons.Default.Receipt,
            title = "Notifikasi Invoice",
            subtitle = "Info tagihan jamaah",
            checked = notifPrefs.invoiceEnabled,
            onCheckedChange = onInvoiceToggle
        )
        HorizontalDivider()
        SettingsSwitch(
            icon = Icons.Default.VerifiedUser,
            title = "Notifikasi Approval",
            subtitle = "Info persetujuan pendaftaran",
            checked = notifPrefs.approvalEnabled,
            onCheckedChange = onApprovalToggle
        )
        HorizontalDivider()
        SettingsRow(
            icon = Icons.Default.Language,
            title = "Bahasa",
            subtitle = "Indonesia",
            onClick = { }
        )
        HorizontalDivider()
        SettingsRow(
            icon = Icons.Default.Accessibility,
            title = "Aksesibilitas",
            subtitle = "Segera hadir",
            showChevron = false
        )
    }
}

@Composable
private fun SecurityContent(onChangePassword: () -> Unit) {
    Column {
        SettingsRow(
            icon = Icons.Default.Lock,
            title = "Ganti Password",
            subtitle = "Update kata sandi akun Anda",
            onClick = onChangePassword
        )
        HorizontalDivider()
        SettingsRow(
            icon = Icons.Default.Fingerprint,
            title = "Biometric Login",
            subtitle = "Segera hadir",
            showChevron = false
        )
        HorizontalDivider()
        SettingsRow(
            icon = Icons.Default.Devices,
            title = "Sesi Login",
            subtitle = "Segera hadir",
            showChevron = false
        )
    }
}

@Composable
private fun ApplicationContent(
    onAboutClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit,
    onLicensesClick: () -> Unit
) {
    Column {
        SettingsRow(
            icon = Icons.Default.Info,
            title = "Tentang Aplikasi",
            subtitle = "Versi & informasi aplikasi",
            onClick = onAboutClick
        )
        HorizontalDivider()
        SettingsRow(
            icon = Icons.Default.PrivacyTip,
            title = "Kebijakan Privasi",
            subtitle = "Pelindungan data Anda",
            onClick = onPrivacyClick
        )
        HorizontalDivider()
        SettingsRow(
            icon = Icons.Default.Gavel,
            title = "Syarat & Ketentuan",
            subtitle = "Aturan penggunaan aplikasi",
            onClick = onTermsClick
        )
        HorizontalDivider()
        SettingsRow(
            icon = Icons.Default.Code,
            title = "Lisensi Open Source",
            subtitle = "Library yang digunakan",
            onClick = onLicensesClick
        )
    }
}

@Composable
private fun SupportContent(
    onFaqClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onEmailClick: () -> Unit
) {
    Column {
        SettingsRow(
            icon = Icons.Default.QuestionAnswer,
            title = "FAQ",
            subtitle = "Pertanyaan yang sering diajukan",
            onClick = onFaqClick
        )
        HorizontalDivider()
        SettingsRow(
            icon = Icons.AutoMirrored.Filled.Chat,
            title = "Hubungi Admin (WhatsApp)",
            subtitle = "Chat langsung dengan admin",
            onClick = onWhatsAppClick
        )
        HorizontalDivider()
        SettingsRow(
            icon = Icons.Default.Email,
            title = "Hubungi Admin (Email)",
            subtitle = "support@anamiroh.com",
            onClick = onEmailClick
        )
        HorizontalDivider()
        SettingsRow(
            icon = Icons.Default.BugReport,
            title = "Laporkan Masalah",
            subtitle = "Laporkan bug atau kendala",
            onClick = onEmailClick
        )
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(120),
        label = ""
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .scale(scale)
            .bounceClick()
            .clickable(interactionSource = interactionSource, indication = null) { onLogout() }
            .animateEntrance(420),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.06f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "Logout Akun",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AccountSkeleton() {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ShimmerBox(
            modifier = Modifier.fillMaxWidth().height(280.dp),
            shape = RoundedCornerShape(24.dp)
        )
        repeat(5) {
            ShimmerBox(
                modifier = Modifier.fillMaxWidth().height(72.dp),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

// ============================================================
// HELPERS
// ============================================================

private fun formatTimestamp(timestamp: Timestamp?): String {
    if (timestamp == null) return "-"
    return try {
        val date = timestamp.toDate()
        SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")).format(date)
    } catch (e: Exception) { "-" }
}

@Composable
private fun HorizontalDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    )
}

// ============================================================
// DIALOGS (Premium)
// ============================================================

@Composable
private fun PremiumDialog(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                content = content
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDialog(
    user: com.agunganamiroh.data.model.User?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(user?.fullName ?: "") }
    var phone by remember { mutableStateOf(user?.phoneNumber ?: "") }
    var address by remember { mutableStateOf(user?.address ?: "") }

    PremiumDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Edit Profil",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Perbarui data pribadi Anda",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama Lengkap") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Nomor Telepon") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Alamat") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Batal") }
            Button(
                onClick = { onSave(name, phone, address) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text("Simpan", color = MaterialTheme.colorScheme.onPrimary) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    PremiumDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Ganti Password",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Minimal 6 karakter",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = oldPass,
            onValueChange = { oldPass = it },
            label = { Text("Password Lama") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = newPass,
            onValueChange = { newPass = it },
            label = { Text("Password Baru") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = confirmPass,
            onValueChange = { confirmPass = it },
            label = { Text("Konfirmasi Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Batal") }
            Button(
                onClick = {
                    when {
                        newPass != confirmPass -> error = "Password tidak cocok"
                        newPass.length < 6 -> error = "Minimal 6 karakter"
                        else -> onSave(oldPass, newPass)
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text("Update", color = MaterialTheme.colorScheme.onPrimary) }
        }
    }
}

@Composable
private fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onDismiss: () -> Unit,
    onSelect: (ThemeMode) -> Unit
) {
    PremiumDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Pilih Tema",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(20.dp))
        listOf(
            Triple("Terang", "Cerah & nyaman", ThemeMode.LIGHT),
            Triple("Gelap", "Elegan di malam hari", ThemeMode.DARK),
            Triple("Ikuti Sistem", "Menyesuaikan perangkat", ThemeMode.SYSTEM)
        ).forEach { (label, desc, mode) ->
            val selected = currentTheme == mode
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSelect(mode as ThemeMode) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    else
                        MaterialTheme.colorScheme.surface
                ),
                border = if (selected)
                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                else null,
                elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 1.dp else 0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selected,
                        onClick = { onSelect(mode as ThemeMode) },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) { Text("Tutup", color = MaterialTheme.colorScheme.primary) }
    }
}

@Composable
private fun LogoutConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    PremiumDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Konfirmasi Logout",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Apakah Anda yakin ingin keluar dari akun ini?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Batal") }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Logout", color = MaterialTheme.colorScheme.onError) }
        }
    }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    PremiumDialog(onDismissRequest = onDismiss) {
        InitialsAvatar(
            name = "AA",
            size = 64.dp,
            textSize = 24
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Anamiroh Agung",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Versi 1.0.0",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Aplikasi manajemen data jamaah umroh untuk agen perjalanan. Memudahkan pendataan, pembayaran, dan pelaporan.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "\u00A9 ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)} Agung Anamiroh",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) { Text("Tutup", color = MaterialTheme.colorScheme.primary) }
    }
}

@Composable
private fun SimpleInfoDialog(title: String, content: String, onDismiss: () -> Unit) {
    PremiumDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) { Text("Tutup", color = MaterialTheme.colorScheme.primary) }
    }
}

@Composable
private fun FaqDialog(onDismiss: () -> Unit) {
    PremiumDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.QuestionAnswer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "FAQ",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FaqItem("Bagaimana cara mendaftarkan jamaah?",
                "Buka menu 'Input Jamaah' dari dashboard, isi data jamaah, lalu simpan.")
            FaqItem("Bagaimana cara upload bukti bayar?",
                "Pilih jamaah dari menu 'Pembayaran', lalu upload bukti transfer.")
            FaqItem("Bagaimana melihat invoice?",
                "Buka menu 'Invoice' untuk melihat tagihan jamaah.")
            FaqItem("Bagaimana cara menghubungi admin?",
                "Gunakan menu 'Hubungi Admin' di bagian Bantuan.")
        }
        Spacer(modifier = Modifier.height(20.dp))
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) { Text("Tutup", color = MaterialTheme.colorScheme.primary) }
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = spring(dampingRatio = 0.7f)) + fadeIn(tween(150)),
            exit = shrinkVertically(animationSpec = tween(150)) + fadeOut(tween(150))
        ) {
            Text(
                text = answer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
