package com.agunganamiroh.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.agunganamiroh.data.model.User
import com.agunganamiroh.data.preferences.NotificationPreferenceManager
import com.agunganamiroh.data.preferences.NotificationPrefs
import com.agunganamiroh.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountCenterState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val jamaahCount: Int = 0,
    val jamaahApproved: Int = 0,
    val jamaahPending: Int = 0,
    val invoiceCount: Int = 0,
    val invoicePaid: Int = 0,
    val totalRevenue: Long = 0,
    val notificationPrefs: NotificationPrefs = NotificationPrefs(),
    val error: String? = null,
    val updateSuccess: Boolean = false,
    val passwordChangeSuccess: Boolean = false,
    val logoutSuccess: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProfileRepository()
    private val notificationManager = NotificationPreferenceManager(application)

    private val _state = MutableStateFlow(AccountCenterState())
    val state: StateFlow<AccountCenterState> = _state.asStateFlow()
    val uiState: StateFlow<AccountCenterState> = state

    val notificationPrefs = notificationManager.notificationPrefs

    private var userJob: kotlinx.coroutines.Job? = null
    private var jamaahJob: kotlinx.coroutines.Job? = null
    private var invoiceJob: kotlinx.coroutines.Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        val uid = repository.getCurrentUserUid() ?: return
        val email = repository.getCurrentUserEmail() ?: return

        userJob?.cancel()
        userJob = viewModelScope.launch {
            repository.observeUser(uid).collect { result ->
                result.fold(
                    onSuccess = { user ->
                        _state.update { it.copy(isLoading = false, user = user) }
                    },
                    onFailure = { e ->
                        _state.update { it.copy(isLoading = false, error = e.message) }
                    }
                )
            }
        }

        jamaahJob?.cancel()
        jamaahJob = viewModelScope.launch {
            repository.observeJamaahCount(email).collect { result ->
                result.fold(
                    onSuccess = { list ->
                        val approved = list.count { it.status.equals("approved", true) || it.status.equals("verified", true) }
                        val pending = list.count { it.status.equals("pending", true) }
                        _state.update {
                            it.copy(
                                jamaahCount = list.size,
                                jamaahApproved = approved,
                                jamaahPending = pending
                            )
                        }
                    },
                    onFailure = { }
                )
            }
        }

        invoiceJob?.cancel()
        invoiceJob = viewModelScope.launch {
            repository.observeInvoiceCount(email).collect { result ->
                result.fold(
                    onSuccess = { list ->
                        val paid = list.count { it.status == "paid" }
                        val totalRevenue = list.filter { it.status == "paid" }.sumOf { it.totalTagihan }
                        _state.update {
                            it.copy(
                                invoiceCount = list.size,
                                invoicePaid = paid,
                                totalRevenue = totalRevenue
                            )
                        }
                    },
                    onFailure = { }
                )
            }
        }
    }

    fun updateProfile(fullName: String, phoneNumber: String, address: String) {
        val uid = repository.getCurrentUserUid() ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, updateSuccess = false) }
            val updates = mutableMapOf<String, Any>()
            if (fullName.isNotBlank()) updates["fullName"] = fullName
            if (phoneNumber.isNotBlank()) updates["phoneNumber"] = phoneNumber
            updates["address"] = address
            repository.updateProfile(uid, updates).fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, updateSuccess = true) }
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun changePassword(oldPass: String, newPass: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, passwordChangeSuccess = false) }
            repository.changePassword(oldPass, newPass).fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, passwordChangeSuccess = true) }
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun setPushNotifications(enabled: Boolean) {
        viewModelScope.launch { notificationManager.setPushEnabled(enabled) }
    }

    fun setPaymentNotifications(enabled: Boolean) {
        viewModelScope.launch { notificationManager.setPaymentEnabled(enabled) }
    }

    fun setInvoiceNotifications(enabled: Boolean) {
        viewModelScope.launch { notificationManager.setInvoiceEnabled(enabled) }
    }

    fun setApprovalNotifications(enabled: Boolean) {
        viewModelScope.launch { notificationManager.setApprovalEnabled(enabled) }
    }

    fun logout() {
        repository.logout()
        _state.update { it.copy(logoutSuccess = true) }
    }

    fun clearStatus() {
        _state.update {
            it.copy(error = null, updateSuccess = false, passwordChangeSuccess = false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        userJob?.cancel()
        jamaahJob?.cancel()
        invoiceJob?.cancel()
    }
}
