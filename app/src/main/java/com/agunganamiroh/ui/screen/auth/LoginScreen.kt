package com.agunganamiroh.ui.screen.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agunganamiroh.R
import kotlinx.coroutines.flow.StateFlow
import com.agunganamiroh.viewmodel.AuthViewModel
import com.agunganamiroh.viewmodel.AuthUiState
import androidx.lifecycle.viewmodel.compose.viewModel

// ============================================================
// WARNA APLIKASI AGUNG ANAMIROH
// ============================================================
private val ColorBackground = Color(0xFFFFFFFF)
private val ColorGold = Color(0xFFD4AF37)
private val ColorGoldDark = Color(0xFFB8960C)
private val ColorTextPrimary = Color(0xFF111111)
private val ColorTextSecondary = Color(0xFF666666)
private val ColorBorder = Color(0xFFE5E5E5)
private val ColorButtonText = Color(0xFF000000)
private val ColorError = Color(0xFFD32F2F)


// ============================================================
// LOGIN SCREEN
// ============================================================
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Handle login success
    LaunchedEffect(uiState.loginSuccess) {

        if (uiState.loginSuccess) {

            if (uiState.loginSuccess) {

                onLoginSuccess(
                    uiState.role
                )
            }
        }
    }

    // Handle error
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMsg ->
            snackbarHostState.showSnackbar(message = errorMsg)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ColorBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Image(
                painter = painterResource(
                    id = R.drawable.background_login
                ),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // ============================================================
                // LOGO PERUSAHAAN
                // ============================================================
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo Agung Anamiroh",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ============================================================
                // JUDUL APLIKASI
                // ============================================================


                Spacer(modifier = Modifier.height(6.dp))

                // ============================================================
                // SUBTITLE
                // ============================================================
                Text(
                    text = "Sistem Manajemen Umroh",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorTextSecondary,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ============================================================
                // CARD LOGIN
                // ============================================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ColorBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = ColorGold.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // --------------------------------------------------------
                        // LABEL FORM
                        // --------------------------------------------------------
                        Text(
                            text = "Masuk ke Akun Anda",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ColorTextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Silakan masukkan email dan password Anda",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = ColorTextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // --------------------------------------------------------
                        // TEXTFIELD EMAIL
                        // --------------------------------------------------------
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                emailError = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text(
                                    text = "Email",
                                    color = ColorTextSecondary
                                )
                            },
                            placeholder = {
                                Text(
                                    text = "contoh@email.com",
                                    color = ColorTextSecondary.copy(alpha = 0.5f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email Icon",
                                    tint = ColorGold
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            isError = emailError != null || uiState.error != null,
                            supportingText = {
                                if (emailError != null) {
                                    Text(
                                        text = emailError!!,
                                        color = ColorError,
                                        fontSize = 12.sp
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ColorGold,
                                unfocusedBorderColor = ColorBorder,
                                focusedLabelColor = ColorGold,
                                unfocusedLabelColor = ColorTextSecondary,
                                cursorColor = ColorGold,
                                focusedTextColor = ColorTextPrimary,
                                unfocusedTextColor = ColorTextPrimary,
                                errorBorderColor = ColorError,
                                errorCursorColor = ColorError
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // --------------------------------------------------------
                        // TEXTFIELD PASSWORD
                        // --------------------------------------------------------
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                passwordError = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text(
                                    text = "Password",
                                    color = ColorTextSecondary
                                )
                            },
                            placeholder = {
                                Text(
                                    text = "Masukkan password",
                                    color = ColorTextSecondary.copy(alpha = 0.5f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Lock Icon",
                                    tint = ColorGold
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { passwordVisible = !passwordVisible }
                                ) {
                                    Icon(
                                        imageVector = if (passwordVisible)
                                            Icons.Default.VisibilityOff
                                        else
                                            Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible)
                                            "Sembunyikan Password"
                                        else
                                            "Tampilkan Password",
                                        tint = ColorTextSecondary
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            isError = passwordError != null || uiState.error != null,
                            supportingText = {
                                if (passwordError != null) {
                                    Text(
                                        text = passwordError!!,
                                        color = ColorError,
                                        fontSize = 12.sp
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ColorGold,
                                unfocusedBorderColor = ColorBorder,
                                focusedLabelColor = ColorGold,
                                unfocusedLabelColor = ColorTextSecondary,
                                cursorColor = ColorGold,
                                focusedTextColor = ColorTextPrimary,
                                unfocusedTextColor = ColorTextPrimary,
                                errorBorderColor = ColorError,
                                errorCursorColor = ColorError
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // --------------------------------------------------------
                        // ERROR MESSAGE
                        // --------------------------------------------------------
                        if (uiState.error != null) {
                            Text(
                                text = uiState.error!!,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = ColorError,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // --------------------------------------------------------
                        // TOMBOL LOGIN
                        // --------------------------------------------------------
                        Button(
                            onClick = {
                                // Validasi input
                                var isValid = true
                                if (email.isBlank()) {
                                    emailError = "Email tidak boleh kosong"
                                    isValid = false
                                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                    emailError = "Format email tidak valid"
                                    isValid = false
                                }
                                if (password.isBlank()) {
                                    passwordError = "Password tidak boleh kosong"
                                    isValid = false
                                } else if (password.length < 6) {
                                    passwordError = "Password minimal 6 karakter"
                                    isValid = false
                                }

                                if (isValid) {
                                    viewModel.login(email, password)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ColorGold,
                                contentColor = ColorButtonText,
                                disabledContainerColor = ColorGold.copy(alpha = 0.5f),
                                disabledContentColor = ColorButtonText.copy(alpha = 0.5f)
                            ),
                            enabled = !uiState.loading
                        ) {
                            if (uiState.loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = ColorButtonText,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "MASUK",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorButtonText,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ============================================================
                // FOOTER
                // ============================================================
                Text(
                    text = "\u00A9 Agung Anamiroh Tour & Travel",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = ColorTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {

    MaterialTheme {

        Text(
            text = "Login Screen Preview"
        )

    }
}