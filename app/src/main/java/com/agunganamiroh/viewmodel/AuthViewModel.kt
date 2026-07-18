package com.agunganamiroh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agunganamiroh.data.model.User
import com.agunganamiroh.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val loginSuccess: Boolean = false,
    val role: String = "",
    val user: User? = null,
    val error: String? = null
)

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        val currentUser = repository.getCurrentUser()
        if (currentUser != null) {
            viewModelScope.launch {
                val userData = repository.getUserData(currentUser.uid)
                _uiState.value = AuthUiState(
                    loginSuccess = true,
                    role = userData?.role ?: "agent",
                    user = userData
                )
            }
        }
    }

    fun login(
        email: String,
        password: String
    ) {

        viewModelScope.launch {

            _uiState.value = AuthUiState(
                loading = true
            )

            try {

                val result =
                    repository.login(
                        email,
                        password
                    )

                result.fold(

                    onSuccess = { user ->

                        val userData =
                            repository.getUserData(
                                user.uid
                            )

                        _uiState.value =
                            AuthUiState(
                                loading = false,
                                loginSuccess = true,
                                role = userData?.role ?: "agent",
                                user = userData
                            )
                    },

                    onFailure = { exception ->

                        _uiState.value =
                            AuthUiState(
                                loading = false,
                                error = exception.message
                                    ?: "Login gagal"
                            )
                    }
                )

            } catch (e: Exception) {

                _uiState.value =
                    AuthUiState(
                        loading = false,
                        error = e.message
                            ?: "Login gagal"
                    )
            }
        }
    }

    fun clearError() {

        _uiState.value =
            _uiState.value.copy(
                error = null
            )
    }

    fun logout() {
        repository.logout()
        _uiState.value = AuthUiState()
    }

    fun isLoggedIn(): Boolean {
        return repository.isLoggedIn()
    }
}
