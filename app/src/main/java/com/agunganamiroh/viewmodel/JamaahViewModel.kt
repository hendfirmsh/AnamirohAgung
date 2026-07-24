package com.agunganamiroh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agunganamiroh.data.model.Jamaah
import com.agunganamiroh.data.repository.JamaahRepository
import com.agunganamiroh.data.repository.PembayaranRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

data class JamaahUiState(
    val loading: Boolean = false,
    val jamaahs: List<Jamaah> = emptyList(),
    val error: String? = null,
    val searchQuery: String = "",
    val selectedFilter: String = "Semua",
    val selectedJamaah: Jamaah? = null,
    val selectedPaket: com.agunganamiroh.data.model.Paket? = null,
    val payments: List<com.agunganamiroh.data.model.Pembayaran> = emptyList(),
    val updateSuccess: Boolean = false
)

class JamaahViewModel : ViewModel() {
    val repository = JamaahRepository()
    private val pembayaranRepository = PembayaranRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(JamaahUiState())
    val uiState: StateFlow<JamaahUiState> = _uiState.asStateFlow()

    private val _allJamaahs = MutableStateFlow<List<Jamaah>>(emptyList())
    private var jamaahJob: Job? = null
    private var detailJob: Job? = null
    private var paketJob: Job? = null
    private var paymentsJob: Job? = null

    init {
        observeJamaah()
    }

    private fun observeJamaah() {
        val user = auth.currentUser
        Log.d("JamaahViewModel", "Auth Check: UID=${user?.uid}, Email=${user?.email}, Name=${user?.displayName}")

        user?.email?.let { email ->
            loadJamaahByAgent(email)
        } ?: run {
            Log.e("JamaahViewModel", "Current user email is null")
        }
    }

    fun loadJamaahByAgent(agentEmail: String) {
        Log.d("JamaahViewModel", "Loading jamaah for agent: $agentEmail")
        jamaahJob?.cancel()
        _uiState.update { it.copy(loading = true) }

        jamaahJob = repository.getJamaahRealtime(agentEmail)
            .onEach { result ->
                result.fold(
                    onSuccess = { list ->
                        Log.d("JamaahViewModel", "Successfully loaded ${list.size} jamaahs")
                        _allJamaahs.value = list
                        filterData()
                    },
                    onFailure = { e ->
                        Log.e("JamaahViewModel", "Error loading jamaahs: ${e.message}")
                        _uiState.update { it.copy(loading = false, error = e.message) }
                    }
                )
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterData()
    }

    fun onFilterChange(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
        filterData()
    }

    fun onPaketSelected(paket: com.agunganamiroh.data.model.Paket) {
        Log.d("JamaahViewModel", "Package selected: ID=${paket.id}, Title=${paket.title}, Price=${paket.harga}")
        _uiState.update { it.copy(selectedPaket = paket) }
    }

    private fun filterData() {
        val query = _uiState.value.searchQuery.lowercase()
        val filter = _uiState.value.selectedFilter

        val filteredList = _allJamaahs.value.filter { jamaah ->
            val matchesSearch = jamaah.nama.lowercase().contains(query) ||
                    jamaah.noHp.contains(query) ||
                    jamaah.noPaspor.lowercase().contains(query)

            val matchesFilter = when (filter) {
                "Pending" -> jamaah.status.lowercase() == "pending"
                "Approved" -> jamaah.status.lowercase() == "approved"
                "Rejected" -> jamaah.status.lowercase() == "rejected"
                "Pelunasan" -> jamaah.pelunasan
                "Belum Lunas" -> !jamaah.pelunasan
                else -> true
            }

            matchesSearch && matchesFilter
        }

        _uiState.update { it.copy(jamaahs = filteredList, loading = false) }
    }

    fun deleteJamaah(id: String) {
        viewModelScope.launch {
            repository.deleteJamaah(id)
        }
    }

    fun loadJamaahDetail(id: String) {
        detailJob?.cancel()
        paymentsJob?.cancel()
        paketJob?.cancel()
        _uiState.update { it.copy(loading = true, error = null, selectedJamaah = null, selectedPaket = null) }

        detailJob = repository.getJamaahById(id)
            .onEach { result ->
                result.fold(
                    onSuccess = { jamaah ->
                        _uiState.update { it.copy(selectedJamaah = jamaah, loading = false) }
                        if (jamaah.paketId.isNotEmpty()) {
                            observePaket(jamaah.paketId)
                        }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(error = e.message, loading = false) }
                    }
                )
            }.launchIn(viewModelScope)

        paymentsJob = repository.getPaymentHistory(id)
            .onEach { result ->
                result.fold(
                    onSuccess = { payments ->
                        _uiState.update { it.copy(payments = payments) }
                    },
                    onFailure = { e ->
                        Log.e("JamaahViewModel", "Error loading payments: ${e.message}")
                    }
                )
            }.launchIn(viewModelScope)
    }

    private fun observePaket(paketId: String) {
        paketJob?.cancel()
        paketJob = repository.getPaketById(paketId)
            .onEach { result ->
                result.fold(
                    onSuccess = { paket ->
                        _uiState.update { it.copy(selectedPaket = paket) }
                    },
                    onFailure = { e ->
                        Log.e("JamaahViewModel", "Error loading paket: ${e.message}")
                    }
                )
            }.launchIn(viewModelScope)
    }

    fun addPayment(
        amount: Long,
        method: String,
        notes: String
    ) {
        val jamaah = _uiState.value.selectedJamaah ?: return

        val pembayaran = com.agunganamiroh.data.model.Pembayaran(
            jamaahId = jamaah.id,
            nominal = amount,
            tanggal = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
            catatan = notes,
            metode = method,
            agentEmail = auth.currentUser?.email ?: "system",
            createdAt = Timestamp.now()
        )

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            pembayaranRepository.addPayment(pembayaran).fold(
                onSuccess = {
                    _uiState.update { it.copy(loading = false, updateSuccess = true) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(loading = false, error = e.message) }
                }
            )
        }
    }

    fun resetUpdateStatus() {
        _uiState.update { it.copy(updateSuccess = false, error = null, loading = false) }
    }

    fun registerJamaah(jamaah: Jamaah) {
        viewModelScope.launch {
            Log.d("JamaahViewModel", "Step 2: Repository Add Started")
            _uiState.update { it.copy(loading = true, error = null) }
            repository.addJamaah(jamaah).fold(
                onSuccess = {
                    Log.d("JamaahViewModel", "Step 3: Firestore Add Success")
                    _uiState.update { it.copy(loading = false, updateSuccess = true) }
                },
                onFailure = { e ->
                    Log.e("JamaahViewModel", "Step 4: Firestore Add Failed: ${e.message}")
                    _uiState.update { it.copy(loading = false, error = e.message) }
                }
            )
        }
    }

    fun updateJamaahDetail(id: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            Log.d("JamaahViewModel", "Step 2: Repository Update Started for ID: $id")
            _uiState.update { it.copy(loading = true, error = null) }
            repository.updateJamaah(id, updates).fold(
                onSuccess = {
                    Log.d("JamaahViewModel", "Step 3: Firestore Update Success")
                    _uiState.update { it.copy(loading = false, updateSuccess = true) }
                },
                onFailure = { e ->
                    Log.e("JamaahViewModel", "Step 4: Firestore Update Failed: ${e.message}")
                    _uiState.update { it.copy(loading = false, error = e.message) }
                }
            )
        }
    }
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
