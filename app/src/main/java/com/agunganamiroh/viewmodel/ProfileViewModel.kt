package com.agunganamiroh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agunganamiroh.data.model.User
import com.agunganamiroh.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val updateSuccess: Boolean = false,
    val passwordChangeSuccess: Boolean = false,
    val logoutSuccess: Boolean = false
)

class ProfileViewModel : ViewModel() {
    private val repository = ProfileRepository()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        val uid = repository.getCurrentUserUid()
        if (uid == null) {
            _uiState.value = _uiState.value.copy(error = "Not authenticated")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.loadProfile(uid).fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(isLoading = false, user = user)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun updateProfile(fullName: String, phoneNumber: String, branch: String) {
        val uid = repository.getCurrentUserUid() ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, updateSuccess = false)
            val updates = mapOf(
                "fullName" to fullName,
                "phoneNumber" to phoneNumber,
                "branch" to branch
            )
            repository.updateProfile(uid, updates).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, updateSuccess = true)
                    loadProfile()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun changePassword(oldPass: String, newPass: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, passwordChangeSuccess = false)
            repository.changePassword(oldPass, newPass).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, passwordChangeSuccess = true)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun logout() {
        repository.logout()
        _uiState.value = _uiState.value.copy(logoutSuccess = true)
    }

    fun clearStatus() {
        _uiState.value = _uiState.value.copy(
            error = null,
            updateSuccess = false,
            passwordChangeSuccess = false
        )
    }
}
