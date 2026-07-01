package com.agunganamiroh.ui.screen.agent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.agunganamiroh.data.model.Jamaah

private val foundation: Any

// ============================================================
// THEME CONSTANTS
// ============================================================
private val GoldPrimary = Color(0xFFD4AF37)
private val GoldDark = Color(0xFFB8860B)
private val GoldLight = Color(0xFFF0E68C)
private val GoldPale = Color(0xFFFFF8E1)
private val GoldGradientStart = Color(0xFFD4AF37)
private val GoldGradientEnd = Color(0xFFB8860B)
private val BackgroundWhite = Color(0xFFFFFFFF)
private val SurfaceWhite = Color(0xFFFDFBF7)
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF4A4A4A)
private val TextMuted = Color(0xFF888888)
private val ErrorRed = Color(0xFFD32F2F)
private val SuccessGreen = Color(0xFF2E7D32)

// ============================================================
// DATA MODEL
// ============================================================
data class Jamaah(
    val id: String = "",
    val nama: String = "",
    val program: String = "",
    val keberangkatan: String = "",
    val noHp: String = "",
    val alamat: String = "",
    val gender: String = "",
    val binBinti: String = "",
    val tempatLahir: String = "",
    val tanggalLahir: String = "",
    val noPaspor: String = "",
    val dp: String = "",
    val agentId: String = "",
    val status: String = "pending"
)

// ============================================================
// MAIN SCREEN
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputJamaahScreen(
    navController: NavController,
    agentId: String = "agent_001"
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var isContentVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    // Form states
    var nama by remember { mutableStateOf("") }
    var noHp by remember { mutableStateOf("") }
    var alamat by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var binBinti by remember { mutableStateOf("") }
    var tempatLahir by remember { mutableStateOf("") }
    var tanggalLahir by remember { mutableStateOf("") }
    var program by remember { mutableStateOf("") }
    var keberangkatan by remember { mutableStateOf("") }
    var noPaspor by remember { mutableStateOf("") }
    var dp by remember { mutableStateOf("") }

    // Dropdown states
    var genderExpanded by remember { mutableStateOf(false) }
    var programExpanded by remember { mutableStateOf(false) }

    // DatePicker states
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val genderOptions = listOf("Laki-laki", "Perempuan")
    val programOptions = listOf("Umroh Reguler", "Umroh Plus Turki", "Umroh Ramadhan")

    LaunchedEffect(Unit) {
        delay(100)
        isContentVisible = true
    }

    fun validateForm(): Boolean {
        val emptyFields = mutableListOf<String>()
        if (nama.isBlank()) emptyFields.add("Nama Lengkap")
        if (noHp.isBlank()) emptyFields.add("Nomor HP")
        if (alamat.isBlank()) emptyFields.add("Alamat")
        if (gender.isBlank()) emptyFields.add("Gender")
        if (binBinti.isBlank()) emptyFields.add("Bin/Binti")
        if (tempatLahir.isBlank()) emptyFields.add("Tempat Lahir")
        if (tanggalLahir.isBlank()) emptyFields.add("Tanggal Lahir")
        if (program.isBlank()) emptyFields.add("Program Umroh")
        if (keberangkatan.isBlank()) emptyFields.add("Keberangkatan")
        if (noPaspor.isBlank()) emptyFields.add("Nomor Paspor")
        if (dp.isBlank()) emptyFields.add("DP")

        return if (emptyFields.isNotEmpty()) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Field berikut wajib diisi: ${emptyFields.joinToString(", ")}"
                )
            }
            false
        } else {
            true
        }
    }

    fun saveJamaah() {
        if (!validateForm()) return

        isLoading = true
        scope.launch {
            delay(1500)
            val jamaah = Jamaah(
                id = System.currentTimeMillis().toString(),
                nama = nama,
                program = program,
                keberangkatan = keberangkatan,
                noHp = noHp,
                alamat = alamat,
                gender = gender,
                binBinti = binBinti,
                tempatLahir = tempatLahir,
                tanggalLahir = tanggalLahir,
                noPaspor = noPaspor,
                dp = dp,
                agentId = agentId,
                status = "pending"
            )
            isLoading = false
            isSuccess = true
            delay(1000)
            navController.popBackStack()
        }
    }

    // DatePicker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                            tanggalLahir = sdf.format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Pilih", color = GoldPrimary, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal", color = TextMuted)
                }
            },
            colors = androidx.compose.material3.DatePickerDefaults.colors(
                containerColor = BackgroundWhite,
                titleContentColor = GoldPrimary,
                headlineContentColor = TextPrimary,
                selectedDayContainerColor = GoldPrimary,
                selectedDayContentColor = Color.White,
                todayContentColor = GoldPrimary,
                todayDateBorderColor = GoldPrimary
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = androidx.compose.material3.DatePickerDefaults.colors(
                    selectedDayContainerColor = GoldPrimary,
                    selectedDayContentColor = Color.White,
                    todayContentColor = GoldPrimary,
                    todayDateBorderColor = GoldPrimary,
                    dayContentColor = TextPrimary,
                    disabledDayContentColor = TextMuted
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Input Jamaah Baru",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GoldPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SurfaceWhite
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header Section
                item {
                    AnimatedVisibility(
                        visible = isContentVisible,
                        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 }
                    ) {
                        HeaderSection()
                    }
                }

                // Section 1: Data Pribadi
                item {
                    AnimatedVisibility(
                        visible = isContentVisible,
                        enter = fadeIn(tween(600, delayMillis = 100)) + slideInVertically(tween(600, delayMillis = 100)) { it / 3 }
                    ) {
                        DataPribadiCard(
                            nama = nama,
                            onNamaChange = { nama = it },
                            noHp = noHp,
                            onNoHpChange = { noHp = it },
                            alamat = alamat,
                            onAlamatChange = { alamat = it },
                            gender = gender,
                            onGenderChange = { gender = it },
                            genderExpanded = genderExpanded,
                            onGenderExpandedChange = { genderExpanded = it },
                            genderOptions = genderOptions,
                            binBinti = binBinti,
                            onBinBintiChange = { binBinti = it },
                            focusManager = focusManager
                        )
                    }
                }

                // Section 2: Data Kelahiran
                item {
                    AnimatedVisibility(
                        visible = isContentVisible,
                        enter = fadeIn(tween(700, delayMillis = 200)) + slideInVertically(tween(700, delayMillis = 200)) { it / 3 }
                    ) {
                        DataKelahiranCard(
                            tempatLahir = tempatLahir,
                            onTempatLahirChange = { tempatLahir = it },
                            tanggalLahir = tanggalLahir,
                            onTanggalLahirClick = { showDatePicker = true },
                            focusManager = focusManager
                        )
                    }
                }

                // Section 3: Data Umroh
                item {
                    AnimatedVisibility(
                        visible = isContentVisible,
                        enter = fadeIn(tween(800, delayMillis = 300)) + slideInVertically(tween(800, delayMillis = 300)) { it / 3 }
                    ) {
                        DataUmrohCard(
                            program = program,
                            onProgramChange = { program = it },
                            programExpanded = programExpanded,
                            onProgramExpandedChange = { programExpanded = it },
                            programOptions = programOptions,
                            keberangkatan = keberangkatan,
                            onKeberangkatanChange = { keberangkatan = it },
                            noPaspor = noPaspor,
                            onNoPasporChange = { noPaspor = it },
                            dp = dp,
                            onDpChange = { dp = it },
                            focusManager = focusManager
                        )
                    }
                }

                // Save Button
                item {
                    AnimatedVisibility(
                        visible = isContentVisible,
                        enter = fadeIn(tween(900, delayMillis = 400)) + slideInVertically(tween(900, delayMillis = 400)) { it / 3 }
                    ) {
                        SaveButton(
                            isLoading = isLoading,
                            isSuccess = isSuccess,
                            onClick = { saveJamaah() }
                        )
                    }
                }

                // Bottom spacer
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Loading Overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = BackgroundWhite
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = GoldPrimary,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Menyimpan data...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            // Success Overlay
            if (isSuccess) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = BackgroundWhite
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Berhasil!",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 20.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Data jamaah berhasil disimpan",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// HEADER SECTION
// ============================================================
@Composable
private fun HeaderSection() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GoldGradientStart, GoldGradientEnd)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = "Input Jamaah Baru",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Lengkapi data calon jamaah umroh",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextMuted,
                    fontSize = 14.sp
                )
            )
        }
    }
}

// ============================================================
// DATA PRIBADI CARD
// ============================================================
@Composable
private fun DataPribadiCard(
    nama: String,
    onNamaChange: (String) -> Unit,
    noHp: String,
    onNoHpChange: (String) -> Unit,
    alamat: String,
    onAlamatChange: (String) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit,
    genderExpanded: Boolean,
    onGenderExpandedChange: (Boolean) -> Unit,
    genderOptions: List<String>,
    binBinti: String,
    onBinBintiChange: (String) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    FormCard(title = "DATA PRIBADI", icon = Icons.Default.Person) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = nama,
                onValueChange = onNamaChange,
                label = { Text("Nama Lengkap") },
                placeholder = { Text("Masukkan nama lengkap") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = goldTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = noHp,
                onValueChange = { if (it.length <= 15) onNoHpChange(it) },
                label = { Text("Nomor HP") },
                placeholder = { Text("Contoh: 08123456789") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = goldTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = alamat,
                onValueChange = onAlamatChange,
                label = { Text("Alamat") },
                placeholder = { Text("Masukkan alamat lengkap") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = goldTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )

            // Gender Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = {},
                    label = { Text("Gender") },
                    placeholder = { Text("Pilih gender") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGenderExpandedChange(true) },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown",
                            tint = GoldPrimary
                        )
                    },
                    colors = goldTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )

                DropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { onGenderExpandedChange(false) },
                    modifier = Modifier.fillMaxWidth(0.9f),
                    containerColor = BackgroundWhite,
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
                ) {
                    genderOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    option,
                                    fontWeight = if (gender == option) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (gender == option) GoldPrimary else TextPrimary
                                )
                            },
                            onClick = {
                                onGenderChange(option)
                                onGenderExpandedChange(false)
                                focusManager.moveFocus(FocusDirection.Down)
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = binBinti,
                onValueChange = onBinBintiChange,
                label = { Text("Bin/Binti") },
                placeholder = { Text("Nama ayah/ibu") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = goldTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

// ============================================================
// DATA KELAHIRAN CARD
// ============================================================
@Composable
private fun DataKelahiranCard(
    tempatLahir: String,
    onTempatLahirChange: (String) -> Unit,
    tanggalLahir: String,
    onTanggalLahirClick: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    FormCard(title = "DATA KELAHIRAN", icon = Icons.Default.CalendarMonth) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = tempatLahir,
                onValueChange = onTempatLahirChange,
                label = { Text("Tempat Lahir") },
                placeholder = { Text("Masukkan tempat lahir") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = goldTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )

            // Tanggal Lahir with DatePicker
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = tanggalLahir,
                    onValueChange = {},
                    label = { Text("Tanggal Lahir") },
                    placeholder = { Text("Pilih tanggal lahir") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTanggalLahirClick() },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Pilih Tanggal",
                            tint = GoldPrimary
                        )
                    },
                    colors = goldTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

// ============================================================
// DATA UMROH CARD
// ============================================================
@Composable
private fun DataUmrohCard(
    program: String,
    onProgramChange: (String) -> Unit,
    programExpanded: Boolean,
    onProgramExpandedChange: (Boolean) -> Unit,
    programOptions: List<String>,
    keberangkatan: String,
    onKeberangkatanChange: (String) -> Unit,
    noPaspor: String,
    onNoPasporChange: (String) -> Unit,
    dp: String,
    onDpChange: (String) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    FormCard(title = "DATA UMROH", icon = Icons.Default.Person) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Program Umroh Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = program,
                    onValueChange = {},
                    label = { Text("Program Umroh") },
                    placeholder = { Text("Pilih program umroh") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProgramExpandedChange(true) },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown",
                            tint = GoldPrimary
                        )
                    },
                    colors = goldTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )

                DropdownMenu(
                    expanded = programExpanded,
                    onDismissRequest = { onProgramExpandedChange(false) },
                    modifier = Modifier.fillMaxWidth(0.9f),
                    containerColor = BackgroundWhite,
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
                ) {
                    programOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    option,
                                    fontWeight = if (program == option) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (program == option) GoldPrimary else TextPrimary
                                )
                            },
                            onClick = {
                                onProgramChange(option)
                                onProgramExpandedChange(false)
                                focusManager.moveFocus(FocusDirection.Down)
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = keberangkatan,
                onValueChange = onKeberangkatanChange,
                label = { Text("Keberangkatan") },
                placeholder = { Text("Contoh: 15 Januari 2027") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = goldTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = noPaspor,
                onValueChange = { if (it.length <= 20) onNoPasporChange(it.uppercase()) },
                label = { Text("Nomor Paspor") },
                placeholder = { Text("Masukkan nomor paspor") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = goldTextFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = dp,
                onValueChange = { newValue ->
                    val filtered = newValue.filter { it.isDigit() }
                    onDpChange(filtered)
                },
                label = { Text("DP (Down Payment)") },
                placeholder = { Text("Masukkan nominal DP") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                colors = goldTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                prefix = { Text("Rp ", color = GoldPrimary, fontWeight = FontWeight.SemiBold) }
            )
        }
    }
}

// ============================================================
// FORM CARD COMPONENT
// ============================================================
@Composable
private fun FormCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = BackgroundWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = GoldDark,
                        letterSpacing = 1.sp
                    )
                )
            }
            HorizontalDivider(
                color = GoldPrimary.copy(alpha = 0.15f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

// ============================================================
// GOLD TEXT FIELD COLORS
// ============================================================
@Composable
private fun goldTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = GoldPrimary,
    unfocusedBorderColor = Color(0xFFE0E0E0),
    focusedLabelColor = GoldPrimary,
    unfocusedLabelColor = TextMuted,
    focusedLeadingIconColor = GoldPrimary,
    focusedTrailingIconColor = GoldPrimary,
    cursorColor = GoldPrimary,
    focusedContainerColor = BackgroundWhite,
    unfocusedContainerColor = BackgroundWhite,
    errorBorderColor = ErrorRed,
    errorLabelColor = ErrorRed
)

// ============================================================
// SAVE BUTTON
// ============================================================
@Composable
private fun SaveButton(
    isLoading: Boolean,
    isSuccess: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "save_scale"
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GoldPrimary,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        ),
        enabled = !isLoading && !isSuccess,
        interactionSource = interactionSource
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Menyimpan...",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        } else {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "SIMPAN JAMAAH",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

// ============================================================
// PREVIEW
// ============================================================
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun InputJamaahScreenPreview() {
    MaterialTheme {
        InputJamaahScreen(
            navController = rememberNavController()
        )
    }
}