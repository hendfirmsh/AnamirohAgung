package com.agunganamiroh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agunganamiroh.data.model.Jamaah
import com.agunganamiroh.data.model.Pembayaran
import com.agunganamiroh.data.repository.PembayaranRepository
import com.agunganamiroh.data.repository.JamaahRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log

data class PaymentUiState(
    val loading: Boolean = false,
    val jamaahs: List<Jamaah> = emptyList(),
    val filteredJamaahs: List<Jamaah> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: String = "Semua",
    val selectedJamaah: Jamaah? = null,
    val paymentHistory: List<Pembayaran> = emptyList(),
    val error: String? = null,
    val success: Boolean = false
)

class PembayaranViewModel : ViewModel() {
    private val pembayaranRepository = PembayaranRepository()
    private val jamaahRepository = JamaahRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        loadJamaahs()
    }

    fun loadJamaahs() {
        val email = auth.currentUser?.email ?: return
        _uiState.update { it.copy(loading = true) }
        
        pembayaranRepository.getJamaahForAgent(email)
            .onEach { result ->
                result.fold(
                    onSuccess = { list ->
                        _uiState.update { 
                            it.copy(jamaahs = list, loading = false) 
                        }
                        filterJamaahs()
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(error = e.message, loading = false) }
                    }
                )
            }.launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterJamaahs()
    }

    fun onFilterChange(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
        filterJamaahs()
    }

    private fun filterJamaahs() {
        val query = _uiState.value.searchQuery.lowercase()
        val filter = _uiState.value.selectedFilter
        
        val filtered = _uiState.value.jamaahs.filter { j ->
            val matchesSearch = j.nama.lowercase().contains(query) ||
                    j.noHp.contains(query) ||
                    j.noPaspor.lowercase().contains(query)
            
            val matchesFilter = when(filter) {
                "Belum Lunas" -> !j.pelunasan
                "Lunas" -> j.pelunasan
                "Pending" -> j.status.lowercase() == "pending"
                "Approved" -> j.status.lowercase() == "approved"
                else -> true
            }
            
            matchesSearch && matchesFilter
        }
        
        _uiState.update { it.copy(filteredJamaahs = filtered) }
    }

    fun loadPaymentDetail(jamaahId: String) {
        _uiState.update { it.copy(loading = true, success = false, error = null) }
        
        // Load Jamaah
        jamaahRepository.getJamaahById(jamaahId)
            .onEach { result ->
                result.fold(
                    onSuccess = { jamaah ->
                        _uiState.update { it.copy(selectedJamaah = jamaah) }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(error = e.message) }
                    }
                )
            }.launchIn(viewModelScope)
            
        // Load Payment History
        pembayaranRepository.getPaymentsByJamaah(jamaahId)
            .onEach { result ->
                result.fold(
                    onSuccess = { history ->
                        _uiState.update { it.copy(paymentHistory = history, loading = false) }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(error = e.message, loading = false) }
                    }
                )
            }.launchIn(viewModelScope)
    }

    fun addPayment(
        nominal: Long,
        metode: String,
        catatan: String,
        tanggal: String
    ) {
        val jamaah = _uiState.value.selectedJamaah ?: return
        val agentEmail = auth.currentUser?.email ?: "unknown"

        val pembayaran = Pembayaran(
            jamaahId = jamaah.id,
            agentEmail = agentEmail,
            nominal = nominal,
            tanggal = tanggal,
            metode = metode,
            catatan = catatan,
            createdAt = Timestamp.now()
        )

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            pembayaranRepository.addPayment(pembayaran).fold(
                onSuccess = {
                    _uiState.update { it.copy(loading = false, success = true) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(loading = false, error = e.message) }
                }
            )
        }
    }
    
    fun clearStatus() {
        _uiState.update { it.copy(success = false, error = null) }
    }
}
