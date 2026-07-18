package com.agunganamiroh.ui.screen.agent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.agunganamiroh.data.model.User
import com.agunganamiroh.viewmodel.ProfileViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

// ============================================================
// PREMIUM THEME COLORS
// ============================================================
private val BrandGold = Color(0xFFC89B3C)
private val BrandGoldLight = Color(0xFFF7E9B6)
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
fun AgentProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatus()
        }
    }

    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            snackbarHostState.showSnackbar("Profil berhasil diperbarui")
            viewModel.clearStatus()
            showEditDialog = false
        }
    }

    LaunchedEffect(uiState.passwordChangeSuccess) {
        if (uiState.passwordChangeSuccess) {
            snackbarHostState.showSnackbar("Password berhasil diubah")
            viewModel.clearStatus()
            showPasswordDialog = false
        }
    }

    LaunchedEffect(uiState.logoutSuccess) {
        if (uiState.logoutSuccess) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Profil Agent", 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandGold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = BrandGold)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Notification */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notification", tint = BrandGold)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = SurfaceWhite
                )
            )
        },
        containerColor = AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                ProfileHeader(
                    user = uiState.user,
                    onEditPhoto = { /* Placeholder */ }
                )
            }

            item {
                ProfileStatistics(user = uiState.user)
            }

            item {
                InformationSection(user = uiState.user)
            }

            item {
                SettingsSection(
                    onEditProfile = { showEditDialog = true },
                    onChangePassword = { showPasswordDialog = true }
                )
            }

            item {
                DangerZone(onLogout = { showLogoutDialog = true })
            }
        }

        // Dialogs (unchanged business logic)
        if (showEditDialog) {
            EditProfileDialog(
                user = uiState.user,
                onDismiss = { showEditDialog = false },
                onSave = { name, phone, branch ->
                    viewModel.updateProfile(name, phone, branch)
                }
            )
        }

        if (showPasswordDialog) {
            ChangePasswordDialog(
                onDismiss = { showPasswordDialog = false },
                onSave = { old, new ->
                    viewModel.changePassword(old, new)
                }
            )
        }

        if (showLogoutDialog) {
            LogoutConfirmDialog(
                onDismiss = { showLogoutDialog = false },
                onConfirm = { viewModel.logout() }
            )
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandGold)
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    user: User?,
    onEditPhoto: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(BrandGold.copy(alpha = 0.05f), SurfaceWhite)
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(BrandGold.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = BrandGold
                        )
                    }
                    Surface(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable { onEditPhoto() },
                        color = BrandGold,
                        tonalElevation = 4.dp
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Edit Photo",
                            tint = Color.White,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = user?.fullName ?: user?.companyName ?: "-",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrandGold.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = (user?.role ?: "AGENT").uppercase(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = BrandGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "${user?.companyName ?: "Agung Anamiroh"} • ${user?.branch ?: "-"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ProfileStatistics(user: User?) {
    // Placeholder statistics as they are not currently in the User model
    // But designed to match the Dashboard requirements
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatMiniCard(
            label = "Jamaah",
            value = "0", // Derived count would go here
            icon = Icons.Default.Group,
            modifier = Modifier.weight(1f)
        )
        StatMiniCard(
            label = "Invoice",
            value = "0",
            icon = Icons.Default.ReceiptLong,
            modifier = Modifier.weight(1f)
        )
        StatMiniCard(
            label = "Join Date",
            value = user?.createdAt?.let { 
                SimpleDateFormat("yyyy", Locale.forLanguageTag("id-ID")).format(it.toDate()) 
            } ?: "-",
            icon = Icons.Default.Event,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatMiniCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = BrandGold, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
    }
}

@Composable
private fun InformationSection(user: User?) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Informasi Akun",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = BrandGold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                InformationItem(icon = Icons.Default.Email, title = "Email", value = user?.email ?: "-")
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = AppBackground)
                InformationItem(icon = Icons.Default.Phone, title = "Nomor HP", value = user?.phoneNumber ?: "-")
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = AppBackground)
                InformationItem(icon = Icons.Default.LocationOn, title = "Cabang", value = user?.branch ?: "-")
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = AppBackground)
                InformationItem(
                    icon = Icons.Default.CalendarMonth, 
                    title = "Bergabung", 
                    value = formatTimestamp(user?.createdAt)
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = AppBackground)
                InformationItem(
                    icon = Icons.Default.History, 
                    title = "Terakhir Login", 
                    value = formatTimestamp(user?.lastLogin)
                )
            }
        }
    }
}

@Composable
private fun InformationItem(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(BrandGold.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = BrandGold, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = TextMuted.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun SettingsSection(
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Pengaturan",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = BrandGold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                SettingsItem(icon = Icons.Default.Edit, title = "Edit Profil", onClick = onEditProfile)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = AppBackground)
                SettingsItem(icon = Icons.Default.Lock, title = "Ganti Password", onClick = onChangePassword)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = AppBackground)
                SettingsItem(icon = Icons.Default.Notifications, title = "Notifikasi", onClick = { /* TODO */ })
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = AppBackground)
                SettingsItem(icon = Icons.Default.Language, title = "Bahasa", onClick = { /* TODO */ })
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = AppBackground)
                SettingsItem(icon = Icons.Default.Info, title = "Tentang Aplikasi", onClick = { /* TODO */ })
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = AppBackground)
                SettingsItem(icon = Icons.Default.PrivacyTip, title = "Kebijakan Privasi", onClick = { /* TODO */ })
            }
        }
    }
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f, tween(120), label = "")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = BrandGold, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun DangerZone(onLogout: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.05f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLogout() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, tint = ErrorRed, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "Logout Akun", style = MaterialTheme.typography.bodyMedium, color = ErrorRed, fontWeight = FontWeight.Bold)
        }
    }
}

// ============================================================
// HELPERS & DIALOGS
// ============================================================

private fun formatTimestamp(timestamp: Timestamp?): String {
    if (timestamp == null) return "-"
    return try {
        val date = timestamp.toDate()
        SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")).format(date)
    } catch (e: Exception) {
        "-"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDialog(
    user: User?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(user?.fullName ?: user?.companyName ?: "") }
    var phone by remember { mutableStateOf(user?.phoneNumber ?: "") }
    var branch by remember { mutableStateOf(user?.branch ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profil", color = BrandGold, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Lengkap") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandGold, focusedLabelColor = BrandGold)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Nomor Telepon") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandGold, focusedLabelColor = BrandGold)
                )
                OutlinedTextField(
                    value = branch,
                    onValueChange = { branch = it },
                    label = { Text("Cabang") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandGold, focusedLabelColor = BrandGold)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, phone, branch) },
                colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan Perubahan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextSecondary)
            }
        },
        containerColor = SurfaceWhite
    )
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ganti Password", color = BrandGold, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = oldPass,
                    onValueChange = { oldPass = it },
                    label = { Text("Password Lama") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandGold, focusedLabelColor = BrandGold)
                )
                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = { Text("Password Baru") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandGold, focusedLabelColor = BrandGold)
                )
                OutlinedTextField(
                    value = confirmPass,
                    onValueChange = { confirmPass = it },
                    label = { Text("Konfirmasi Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandGold, focusedLabelColor = BrandGold)
                )
                if (error != null) {
                    Text(error!!, color = ErrorRed, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPass != confirmPass) {
                        error = "Password tidak cocok"
                    } else if (newPass.length < 6) {
                        error = "Minimal 6 karakter"
                    } else {
                        onSave(oldPass, newPass)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Update Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextSecondary)
            }
        },
        containerColor = SurfaceWhite
    )
}

@Composable
private fun LogoutConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Konfirmasi Logout") },
        text = { Text("Apakah Anda yakin ingin keluar dari akun ini?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Logout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        containerColor = SurfaceWhite
    )
}
