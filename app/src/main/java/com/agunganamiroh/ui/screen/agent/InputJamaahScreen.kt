package com.agunganamiroh.ui.screen.agent

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.agunganamiroh.data.model.Jamaah
import com.agunganamiroh.viewmodel.AuthViewModel
import com.agunganamiroh.viewmodel.JamaahViewModel
import com.agunganamiroh.viewmodel.PaketViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// ============================================================
// ENTERPRISE THEME COLORS
// ============================================================
private val BrandGold = Color(0xFFC89B3C)
private val BrandGoldLight = Color(0xFFF7E9B6)
private val AppBackground = Color(0xFFFAF8F5)
private val SurfaceWhite = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1F1F1F)
private val TextSecondary = Color(0xFF6B7280)
private val TextMuted = Color(0xFF9CA3AF)
private val SuccessGreen = Color(0xFF22C55E)
private val ErrorRed = Color(0xFFEF4444)

// ============================================================
// MAIN SCREEN
// ============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputJamaahScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    jamaahViewModel: JamaahViewModel = viewModel(),
    paketViewModel: PaketViewModel = viewModel()
) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val jamaahState by jamaahViewModel.uiState.collectAsStateWithLifecycle()
    val paketState by paketViewModel.uiState.collectAsStateWithLifecycle()
    
    val navBackStackEntry = navController.currentBackStackEntry
    val editId = navBackStackEntry?.arguments?.getString("id")
    val isEdit = editId != null

    val agentEmail = authState.user?.email ?: "unknown_agent"

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    var isVisible by remember { mutableStateOf(false) }
    
    // Dialog States
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showDraftDialog by remember { mutableStateOf(false) }

    // Form states
    var nama by remember { mutableStateOf("") }
    var noHp by remember { mutableStateOf("") }
    var alamat by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var binBinti by remember { mutableStateOf("") }
    var tempatLahir by remember { mutableStateOf("") }
    var tanggalLahir by remember { mutableStateOf("") }
    var noPaspor by remember { mutableStateOf("") }
    var dpInput by remember { mutableStateOf("") }
    var catatan by remember { mutableStateOf("") }
    
    // Checkbox states
    var ktpChecked by remember { mutableStateOf(false) }
    var kkChecked by remember { mutableStateOf(false) }
    var pasporChecked by remember { mutableStateOf(false) }
    var akteChecked by remember { mutableStateOf(false) }
    var meningitisChecked by remember { mutableStateOf(false) }

    // Prefill data if edit
    LaunchedEffect(editId) {
        if (isEdit) {
            jamaahViewModel.loadJamaahDetail(editId!!)
        }
    }

    LaunchedEffect(jamaahState.selectedJamaah) {
        if (isEdit && jamaahState.selectedJamaah != null) {
            val j = jamaahState.selectedJamaah!!
            nama = j.nama
            noHp = j.noHp
            alamat = j.alamat
            gender = j.gender
            binBinti = j.binBinti
            tempatLahir = j.tempatLahir
            tanggalLahir = j.tanggalLahir
            noPaspor = j.noPaspor
            dpInput = j.dp.toString()
            catatan = j.requestTambahan
            
            // Note: selectedPaket will be automatically loaded by ViewModel in loadJamaahDetail
            
            // Prefill checkboxes
            ktpChecked = j.ktp
            kkChecked = j.kk
            pasporChecked = j.paspor
            akteChecked = j.akte
            meningitisChecked = j.meningitis
        }
    }

    LaunchedEffect(jamaahState.updateSuccess) {
        if (jamaahState.updateSuccess) {
            android.util.Log.d("InputJamaahScreen", "Step 5: Success State Detected. Loading Hidden.")
            scope.launch {
                snackbarHostState.showSnackbar(if (isEdit) "✅ Data Jamaah berhasil diperbarui" else "✅ Data Jamaah berhasil disimpan")
            }
            delay(1000)
            android.util.Log.d("InputJamaahScreen", "Step 6: Navigation Triggered (popBackStack)")
            navController.popBackStack()
            jamaahViewModel.resetUpdateStatus()
        }
    }

    LaunchedEffect(jamaahState.error) {
        if (jamaahState.error != null) {
            snackbarHostState.showSnackbar("❌ Error: ${jamaahState.error}")
            jamaahViewModel.resetUpdateStatus()
        }
    }

    // Validation States
    var errors by remember { mutableStateOf(setOf<String>()) }

    // Dropdown states
    var genderExpanded by remember { mutableStateOf(false) }
    var programExpanded by remember { mutableStateOf(false) }

    // DatePicker states
    var showBirthDatePicker by remember { mutableStateOf(false) }
    val birthDatePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )

    val genderOptions = listOf("Laki-laki", "Perempuan")
    val programOptions = listOf("Umroh Reguler", "Umroh Plus Turki", "Umroh Ramadhan")

    // Handle Back Press
    BackHandler {
        if (nama.isNotEmpty() || noHp.isNotEmpty()) {
            showDraftDialog = true
        } else {
            navController.popBackStack()
        }
    }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    // Progress Calculation
    val requiredFields = listOf(nama, noHp, alamat, gender, tempatLahir, tanggalLahir, dpInput)
    val completedFields = requiredFields.count { it.isNotBlank() } + (if (jamaahState.selectedPaket != null) 1 else 0)
    val fillProgress = (completedFields.toFloat() / (requiredFields.size.toFloat() + 1))

    fun validate(): Boolean {
        val newErrors = mutableSetOf<String>()
        if (nama.isBlank()) newErrors.add("nama")
        if (noHp.isBlank() || noHp.length < 10) newErrors.add("noHp")
        if (alamat.isBlank()) newErrors.add("alamat")
        if (gender.isBlank()) newErrors.add("gender")
        if (tempatLahir.isBlank()) newErrors.add("tempatLahir")
        if (tanggalLahir.isBlank()) newErrors.add("tanggalLahir")
        if (jamaahState.selectedPaket == null) newErrors.add("program")
        if (dpInput.isBlank()) newErrors.add("dp")
        
        errors = newErrors
        return newErrors.isEmpty()
    }

    fun submitData() {
        android.util.Log.d("InputJamaahScreen", "Step 1: Save Button Clicked. isEdit=$isEdit")
        val selectedPaket = jamaahState.selectedPaket

        if (selectedPaket == null) {
            android.util.Log.d("InputJamaahScreen", "Step 1.1: Validation Failed - No Package Selected")
            scope.launch { snackbarHostState.showSnackbar("Please select an Umrah package.") }
            return
        }
        if (!validate()) {
            android.util.Log.d("InputJamaahScreen", "Step 1.2: Validation Failed: $errors")
            scope.launch {
                snackbarHostState.showSnackbar("Harap lengkapi semua field wajib")
            }
            return
        }

        android.util.Log.d("InputJamaahScreen", "Step 1.3: Validation Passed")
        if (isEdit) {
            android.util.Log.d("InputJamaahScreen", "Step 2: Firestore Save (Update) Started")
            val updates = mapOf(
                "nama" to nama,
                "program" to selectedPaket.title,
                "paketId" to selectedPaket.id,
                "keberangkatan" to selectedPaket.tanggal,
                "hargaPaket" to selectedPaket.harga,
                "noHp" to noHp,
                "alamat" to alamat,
                "gender" to gender,
                "binBinti" to binBinti,
                "tempatLahir" to tempatLahir,
                "tanggalLahir" to tanggalLahir,
                "noPaspor" to noPaspor,
                "dp" to (dpInput.toLongOrNull() ?: 0L),
                "requestTambahan" to catatan,
                "ktp" to ktpChecked,
                "kk" to kkChecked,
                "paspor" to pasporChecked,
                "akte" to akteChecked,
                "meningitis" to meningitisChecked
            )
            jamaahViewModel.updateJamaahDetail(editId!!, updates)
        } else {
            android.util.Log.d("InputJamaahScreen", "Step 2: Firestore Save (Register) Started")
            val jamaah = Jamaah(
                id = System.currentTimeMillis().toString(),
                nama = nama,
                program = selectedPaket.title,
                paketId = selectedPaket.id,
                keberangkatan = selectedPaket.tanggal,
                hargaPaket = selectedPaket.harga,
                noHp = noHp,
                alamat = alamat,
                gender = gender,
                binBinti = binBinti,
                tempatLahir = tempatLahir,
                tanggalLahir = tanggalLahir,
                noPaspor = noPaspor,
                dp = dpInput.toLongOrNull() ?: 0L,
                input_by = agentEmail,
                status = "pending",
                requestTambahan = catatan,
                ktp = ktpChecked,
                kk = kkChecked,
                paspor = pasporChecked,
                akte = akteChecked,
                meningitis = meningitisChecked
            )
            jamaahViewModel.registerJamaah(jamaah)
        }
    }

    if (showBirthDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showBirthDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    birthDatePickerState.selectedDateMillis?.let {
                        tanggalLahir = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                    }
                    showBirthDatePicker = false
                }) { Text("Pilih", color = BrandGold) }
            },
            dismissButton = { TextButton(onClick = { showBirthDatePicker = false }) { Text("Batal") } }
        ) {
            DatePicker(state = birthDatePickerState)
        }
    }

    // Confirmation Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Konfirmasi Penyimpanan", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menyimpan data jamaah ini?") },
            confirmButton = {
                Button(
                    onClick = { showConfirmDialog = false; submitData() },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGold)
                ) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Batal") }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Draft Dialog
    if (showDraftDialog) {
        AlertDialog(
            onDismissRequest = { showDraftDialog = false },
            title = { Text("Simpan sebagai Draft?", fontWeight = FontWeight.Bold) },
            text = { Text("Anda memiliki perubahan yang belum disimpan. Simpan sebagai draft atau keluar?") },
            confirmButton = {
                Button(onClick = { showDraftDialog = false /* TODO */ }, colors = ButtonDefaults.buttonColors(containerColor = BrandGold)) {
                    Text("Simpan Draft")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDraftDialog = false; navController.popBackStack() }) {
                    Text("Keluar", color = ErrorRed)
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFDF8), AppBackground)
                )
            )
    ) {
        IslamicPatternOverlay()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Pendaftaran", fontWeight = FontWeight.ExtraBold, color = BrandGold, fontSize = 20.sp) },
                    navigationIcon = {
                        IconButton(onClick = { 
                            if (nama.isNotEmpty() || noHp.isNotEmpty()) showDraftDialog = true else navController.popBackStack() 
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = BrandGold)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                StickySaveButton(
                    isLoading = jamaahState.loading,
                    isSuccess = jamaahState.updateSuccess,
                    isEdit = isEdit,
                    onClick = { if (validate()) showConfirmDialog = true else submitData() }
                )
            },
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 10 }
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item { RegistrationHeader() }
                    
                    item { ProgressCard(progress = fillProgress) }
                    
                    item { 
                        SummaryCard(
                            nama = nama, 
                            program = jamaahState.selectedPaket?.title ?: "-", 
                            keberangkatan = jamaahState.selectedPaket?.tanggal ?: "-",
                            status = if (isEdit) jamaahState.selectedJamaah?.status ?: "Draft" else "Draft"
                        ) 
                    }

                    item {
                        PersonalSection(
                            nama = nama, onNamaChange = { nama = it },
                            gender = gender, onGenderChange = { gender = it },
                            noHp = noHp, onNoHpChange = { input ->
                                val filtered = input.filter { it.isDigit() }
                                val formatted = if (filtered.isNotEmpty() && !filtered.startsWith("0")) "08$filtered" else filtered
                                if (formatted.length <= 15) noHp = formatted
                            },
                            alamat = alamat, onAlamatChange = { alamat = it },
                            binBinti = binBinti, onBinBintiChange = { binBinti = it },
                            genderExpanded = genderExpanded, onGenderExpandedChange = { genderExpanded = it },
                            genderOptions = genderOptions,
                            errors = errors,
                            focusManager = focusManager
                        )
                    }

                    item {
                        BirthSection(
                            tanggalLahir = tanggalLahir,
                            onDateSelected = { 
                                tanggalLahir = it
                                android.util.Log.d("DATE", "ViewModel Updated: $it")
                            },
                            tempatLahir = tempatLahir,
                            onTempatLahirChange = { tempatLahir = it },
                            isError = errors.contains("tanggalLahir") || errors.contains("tempatLahir"),
                            focusManager = focusManager
                        )
                    }

                    item {
                        UmrohSection(
                            selectedPaket = jamaahState.selectedPaket,
                            onProgramChange = { p ->
                                jamaahViewModel.onPaketSelected(p)
                            },
                            programExpanded = programExpanded,
                            onProgramExpandedChange = { programExpanded = it },
                            paketOptions = paketState.pakets,
                            errors = errors,
                            focusManager = focusManager
                        )
                    }

                    item {
                        FinanceSection(
                            noPaspor = noPaspor, onNoPasporChange = { noPaspor = it.uppercase() },
                            dpValue = dpInput, onDpChange = { dpInput = it },
                            errors = errors,
                            focusManager = focusManager
                        )
                    }

                    item {
                        DocumentSection(
                            ktp = ktpChecked, onKtpChange = { ktpChecked = it },
                            kk = kkChecked, onKkChange = { kkChecked = it },
                            paspor = pasporChecked, onPasporChange = { pasporChecked = it },
                            akte = akteChecked, onAkteChange = { akteChecked = it },
                            meningitis = meningitisChecked, onMeningitisChange = { meningitisChecked = it }
                        )
                    }

                    item {
                        NotesSection(catatan = catatan, onCatatanChange = { catatan = it })
                    }
                }
            }
        }

        // Loading Overlay
        if (jamaahState.loading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
                Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = SurfaceWhite)) {
                    Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = BrandGold, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Menyimpan data jamaah...", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                }
            }
        }
    }
}

// ============================================================
// COMPONENTS
// ============================================================

@Composable
private fun RegistrationHeader() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().background(
                brush = Brush.horizontalGradient(listOf(BrandGold.copy(alpha = 0.05f), SurfaceWhite))
            ).padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(BrandGold.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Assignment, null, tint = BrandGold, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Formulir Pendaftaran", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Lengkapi data calon jamaah dengan benar", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun ProgressCard(progress: Float) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Progress Pengisian", style = MaterialTheme.typography.labelMedium, color = TextSecondary, fontWeight = FontWeight.Medium)
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelLarge, color = BrandGold, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = BrandGold,
                trackColor = BrandGold.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
private fun SummaryCard(nama: String, program: String, keberangkatan: String, status: String = "Draft") {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BrandGold.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                SummaryItem("Nama", nama.ifBlank { "-" })
                SummaryItem("Program", program.ifBlank { "-" })
            }
            Box(modifier = Modifier.width(1.dp).height(40.dp).background(BrandGold.copy(alpha = 0.2f)))
            Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                SummaryItem("Berangkat", keberangkatan.ifBlank { "-" })
                SummaryItem("Status", status, isStatus = true)
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, isStatus: Boolean = false) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isStatus) SuccessGreen else TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalSection(
    nama: String, onNamaChange: (String) -> Unit,
    gender: String, onGenderChange: (String) -> Unit,
    noHp: String, onNoHpChange: (String) -> Unit,
    alamat: String, onAlamatChange: (String) -> Unit,
    binBinti: String, onBinBintiChange: (String) -> Unit,
    genderExpanded: Boolean, onGenderExpandedChange: (Boolean) -> Unit,
    genderOptions: List<String>,
    errors: Set<String>,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    SectionCard(title = "DATA PRIBADI", description = "Informasi identitas calon jamaah.", icon = Icons.Default.Person) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            EnterpriseTextField(
                value = nama, onValueChange = onNamaChange,
                label = "Nama Lengkap", placeholder = "Sesuai KTP",
                leadingIcon = Icons.Default.Badge,
                isError = errors.contains("nama"),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            // Gender Dropdown
            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = onGenderExpandedChange,
                modifier = Modifier.fillMaxWidth()
            ) {
                EnterpriseTextField(
                    value = gender, onValueChange = {},
                    label = "Jenis Kelamin", placeholder = "Pilih",
                    leadingIcon = Icons.Default.Transgender,
                    readOnly = true,
                    isError = errors.contains("gender"),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                )
                ExposedDropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { onGenderExpandedChange(false) },
                    modifier = Modifier.background(SurfaceWhite)
                ) {
                    genderOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = { onGenderChange(option); onGenderExpandedChange(false) }
                        )
                    }
                }
            }

            EnterpriseTextField(
                value = noHp, onValueChange = onNoHpChange,
                label = "Nomor HP", placeholder = "08...",
                leadingIcon = Icons.Default.Phone,
                isError = errors.contains("noHp"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            EnterpriseTextField(
                value = alamat, onValueChange = onAlamatChange,
                label = "Alamat Domisili", placeholder = "Lengkap",
                leadingIcon = Icons.Default.LocationOn,
                isError = errors.contains("alamat"),
                minLines = 2,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            EnterpriseTextField(
                value = binBinti, onValueChange = onBinBintiChange,
                label = "Bin / Binti", placeholder = "Nama orang tua",
                leadingIcon = Icons.Default.FamilyRestroom,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthSection(
    tanggalLahir: String,
    onDateSelected: (String) -> Unit,
    tempatLahir: String,
    onTempatLahirChange: (String) -> Unit,
    isError: Boolean,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    SectionCard(title = "DATA KELAHIRAN", description = "Lengkapi tempat & tanggal lahir.", icon = Icons.Default.Cake) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            EnterpriseTextField(
                value = tempatLahir, onValueChange = onTempatLahirChange,
                label = "Tempat Lahir", placeholder = "Kota/Kabupaten",
                leadingIcon = Icons.Default.Map,
                isError = isError && tempatLahir.isBlank(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            DatePickerField(
                label = "Tanggal Lahir",
                value = tanggalLahir,
                onDateSelected = onDateSelected,
                isError = isError && tanggalLahir.isBlank()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit,
    isError: Boolean
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )

    // Pre-fill DatePicker if editing
    LaunchedEffect(showDatePicker) {
        if (showDatePicker && value.isNotEmpty()) {
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = sdf.parse(value)
                date?.let {
                    datePickerState.selectedDateMillis = it.time
                }
            } catch (e: Exception) {
                android.util.Log.e("DATE", "Error parsing pre-fill date: ${e.message}")
            }
        }
    }

    if (showDatePicker) {
        android.util.Log.d("DATE", "Dialog Opened")
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))
                        android.util.Log.d("DATE", "Date Selected = $formattedDate")
                        onDateSelected(formattedDate)
                    }
                    showDatePicker = false
                }) { Text("Pilih", color = BrandGold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                android.util.Log.d("DATE", "Field Clicked")
                showDatePicker = true
            }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            placeholder = { Text("Pilih Tanggal") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = if (isError) ErrorRed else BrandGold, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                if (value.isNotBlank() && !isError) Icon(Icons.Default.Check, null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
            },
            isError = isError,
            readOnly = true,
            enabled = false, // Disable to prevent keyboard and internal focus, Box handles click
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = TextPrimary,
                disabledBorderColor = if (isError) ErrorRed else Color(0xFFE5E7EB),
                disabledLabelColor = if (isError) ErrorRed else TextSecondary,
                disabledLeadingIconColor = if (isError) ErrorRed else BrandGold,
                disabledPlaceholderColor = TextMuted
            ),
            supportingText = if (isError) { { Text("Wajib diisi", color = ErrorRed) } } else null
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UmrohSection(
    selectedPaket: com.agunganamiroh.data.model.Paket?,
    onProgramChange: (com.agunganamiroh.data.model.Paket) -> Unit,
    programExpanded: Boolean,
    onProgramExpandedChange: (Boolean) -> Unit,
    paketOptions: List<com.agunganamiroh.data.model.Paket>,
    errors: Set<String>,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    SectionCard(title = "PROGRAM UMROH", description = "Pilihan paket Umroh & Haji.", icon = Icons.Default.FlightTakeoff) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ExposedDropdownMenuBox(
                expanded = programExpanded,
                onExpandedChange = onProgramExpandedChange,
                modifier = Modifier.fillMaxWidth()
            ) {
                EnterpriseTextField(
                    value = selectedPaket?.title ?: "", onValueChange = {},
                    label = "Pilih Paket", placeholder = "Klik untuk memilih paket",
                    leadingIcon = Icons.Default.TravelExplore,
                    readOnly = true,
                    isError = errors.contains("program") || (errors.isNotEmpty() && selectedPaket == null),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = programExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                )
                ExposedDropdownMenu(
                    expanded = programExpanded,
                    onDismissRequest = { onProgramExpandedChange(false) },
                    modifier = Modifier.background(SurfaceWhite)
                ) {
                    paketOptions.forEach { paket ->
                        val isSoldOut = paket.sisaSeat <= 0
                        DropdownMenuItem(
                            text = { PackageDropdownItem(paket) },
                            onClick = { 
                                onProgramChange(paket)
                                onProgramExpandedChange(false) 
                            },
                            enabled = !isSoldOut
                        )
                    }
                }
            }

            if (selectedPaket != null) {
                PackageSummaryCard(selectedPaket)
            }
        }
    }
}

@Composable
private fun PackageDropdownItem(paket: com.agunganamiroh.data.model.Paket) {
    val isSoldOut = paket.sisaSeat <= 0
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(paket.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (isSoldOut) TextMuted else TextPrimary)
            Text("${paket.tanggal} • ${paket.durasi}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(format.format(paket.harga).replace(",00", ""), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold, color = if (isSoldOut) TextMuted else BrandGold)
            if (isSoldOut) {
                Text("Sold Out", style = MaterialTheme.typography.labelSmall, color = ErrorRed, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PackageSummaryCard(paket: com.agunganamiroh.data.model.Paket) {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandGold.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = BrandGold, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Detail Paket Terpilih", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = BrandGold)
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryRow("Keberangkatan", paket.tanggal)
                SummaryRow("Durasi", paket.durasi)
                SummaryRow("Harga", format.format(paket.harga).replace(",00", ""))
                SummaryRow("Maskapai", paket.maskapai)
                SummaryRow("Sisa Seat", "${paket.sisaSeat} Kursi")
                SummaryRow("Hotel Makkah", paket.hotelMakkah)
                SummaryRow("Hotel Madinah", paket.hotelMadinah)
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
private fun FinanceSection(
    noPaspor: String, onNoPasporChange: (String) -> Unit,
    dpValue: String, onDpChange: (String) -> Unit,
    errors: Set<String>,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    SectionCard(title = "FINANSIAL & PASPOR", description = "Nomor paspor & pembayaran awal.", icon = Icons.Default.Payments) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            EnterpriseTextField(
                value = noPaspor, onValueChange = onNoPasporChange,
                label = "Nomor Paspor", placeholder = "Opsional",
                leadingIcon = Icons.Default.ContactPage,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            val formattedDp = remember(dpValue) {
                if (dpValue.isEmpty()) "Rp 0" 
                else NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(dpValue.toLongOrNull() ?: 0L).replace(",00", "")
            }

            EnterpriseTextField(
                value = formattedDp, onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }
                    onDpChange(digits)
                },
                label = "Down Payment (DP)", placeholder = "Rp 0",
                leadingIcon = Icons.Default.Payments,
                isError = errors.contains("dp"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )
        }
    }
}

@Composable
private fun DocumentSection(
    ktp: Boolean, onKtpChange: (Boolean) -> Unit,
    kk: Boolean, onKkChange: (Boolean) -> Unit,
    paspor: Boolean, onPasporChange: (Boolean) -> Unit,
    akte: Boolean, onAkteChange: (Boolean) -> Unit,
    meningitis: Boolean, onMeningitisChange: (Boolean) -> Unit
) {
    SectionCard(title = "DOKUMEN PERSYARATAN", description = "Ceklis dokumen yang sudah diterima.", icon = Icons.Default.FactCheck) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DocumentCheckboxItem("Fotokopi KTP", ktp, onKtpChange)
            DocumentCheckboxItem("Fotokopi KK", kk, onKkChange)
            DocumentCheckboxItem("Paspor Asli", paspor, onPasporChange)
            DocumentCheckboxItem("Akte Lahir / Ijazah / Buku Nikah", akte, onAkteChange)
            DocumentCheckboxItem("Buku Kuning (Meningitis)", meningitis, onMeningitisChange)
        }
    }
}

@Composable
private fun DocumentCheckboxItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = BrandGold)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = if (checked) TextPrimary else TextSecondary)
    }
}

@Composable
private fun NotesSection(catatan: String, onCatatanChange: (String) -> Unit) {
    SectionCard(title = "CATATAN", description = "Informasi tambahan (opsional).", icon = Icons.AutoMirrored.Filled.Notes) {
        EnterpriseTextField(
            value = catatan, onValueChange = onCatatanChange,
            label = "Catatan Khusus", placeholder = "Contoh: Alergi makanan, butuh kursi roda",
            leadingIcon = Icons.Default.EditNote,
            minLines = 3,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
    }
}

@Composable
private fun StickySaveButton(isLoading: Boolean, isSuccess: Boolean, isEdit: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceWhite,
        shadowElevation = 8.dp
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, tween(120), label = "")

        Box(modifier = Modifier.padding(20.dp)) {
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(58.dp).scale(scale),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                enabled = !isLoading && !isSuccess,
                interactionSource = interactionSource
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(if (isEdit) "Memperbarui..." else "Menyimpan...", fontWeight = FontWeight.Bold)
                } else if (isSuccess) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isEdit) "Berhasil Diperbarui" else "Berhasil Disimpan", fontWeight = FontWeight.Bold)
                } else {
                    Icon(if (isEdit) Icons.Default.Update else Icons.Default.Save, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(if (isEdit) "UPDATE DATA JAMAAH" else "SIMPAN DATA JAMAAH", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@Composable
private fun EnterpriseTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    isError: Boolean = false,
    readOnly: Boolean = false,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(leadingIcon, null, tint = if (isError) ErrorRed else BrandGold, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                if (trailingIcon != null) trailingIcon()
                else if (value.isNotBlank() && !isError) Icon(Icons.Default.Check, null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
            },
            isError = isError,
            readOnly = readOnly,
            minLines = minLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandGold,
                focusedLabelColor = BrandGold,
                cursorColor = BrandGold,
                unfocusedBorderColor = Color(0xFFE5E7EB),
                errorBorderColor = ErrorRed
            ),
            supportingText = if (isError) { { Text("Wajib diisi dengan benar", color = ErrorRed) } } else null
        )
        
        // Transparent overlay to capture clicks if readOnly
        if (readOnly) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { /* The modifier on EnterpriseTextField will handle this */ }
                    )
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    description: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(BrandGold.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = BrandGold, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = BrandGold, letterSpacing = 1.sp)
                Text(description, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, Color(0xFFF3F4F6))
        ) {
            Box(modifier = Modifier.padding(20.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun IslamicPatternOverlay() {
    Canvas(modifier = Modifier.fillMaxSize().alpha(0.04f)) {
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
                    color = BrandGold,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InputJamaahScreenPreview() {
    InputJamaahScreen(navController = rememberNavController())
}
