package com.agunganamiroh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agunganamiroh.data.model.Jamaah
import com.agunganamiroh.data.repository.JamaahRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Activity(
    val id: String,
    val title: String,
    val subtitle: String,
    val time: Long,
    val type: String, // "registration", "status", "payment"
    val status: String = ""
)

data class JamaahUiState(
    val loading: Boolean = false,
    val jamaahs: List<Jamaah> = emptyList(),
    val activities: List<Activity> = emptyList(),
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
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(JamaahUiState())
    val uiState: StateFlow<JamaahUiState> = _uiState.asStateFlow()

    private val _allJamaahs = MutableStateFlow<List<Jamaah>>(emptyList())
    private val _recentPayments = MutableStateFlow<List<com.agunganamiroh.data.model.Pembayaran>>(emptyList())
    private var jamaahJob: Job? = null
    private var globalPaymentsJob: Job? = null
    private var detailJob: Job? = null
    private var paketJob: Job? = null
    private var paymentsJob: Job? = null

    init {
    }

    fun loadJamaahByAgent(agentEmail: String) {
        Log.d("JamaahViewModel", "Loading jamaah for agent: $agentEmail")
        jamaahJob?.cancel()
        globalPaymentsJob?.cancel()
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

        globalPaymentsJob = repository.getGlobalPaymentsByAgent(agentEmail)
            .onEach { result ->
                result.fold(
                    onSuccess = { list ->
                        _recentPayments.value = list
                        filterData()
                    },
                    onFailure = { e ->
                        Log.e("JamaahViewModel", "Error loading global payments: ${e.message}")
                    }
                )
            }.launchIn(viewModelScope)
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

        // Generate activities from data
        val activities = mutableListOf<Activity>()
        
        // 1. From Jamaah Documents (Registration & Status)
        _allJamaahs.value.forEach { jamaah ->
            // Registration
            activities.add(
                Activity(
                    id = "reg_${jamaah.id}",
                    title = "Pendaftaran Baru",
                    subtitle = jamaah.nama,
                    time = jamaah.id.toLongOrNull() ?: 0L,
                    type = "registration"
                )
            )
            
            // Status update (simulated based on status)
            if (jamaah.status.lowercase() != "pending") {
                activities.add(
                    Activity(
                        id = "stat_${jamaah.id}",
                        title = "Jamaah ${jamaah.status.uppercase()}",
                        subtitle = jamaah.nama,
                        time = (jamaah.id.toLongOrNull() ?: 0L) + 5000,
                        type = "status",
                        status = jamaah.status
                    )
                )
            }
        }

        // 2. From Global Payments
        _recentPayments.value.forEach { payment ->
            val jamaahName = _allJamaahs.value.find { it.id == payment.jamaahId }?.nama ?: "Jamaah"
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            val amountFormatted = format.format(payment.jumlah).replace(",00", "")
            
            activities.add(
                Activity(
                    id = "pay_${payment.id}",
                    title = "Pembayaran Diterima",
                    subtitle = "$jamaahName • +$amountFormatted",
                    time = try { 
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(payment.tanggal)?.time ?: 0L 
                    } catch (e: Exception) { 0L },
                    type = "payment"
                )
            )
        }

        val sortedActivities = activities.sortedByDescending { it.time }.take(10)

        _uiState.update { it.copy(jamaahs = filteredList, activities = sortedActivities, loading = false) }
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
                        // Start observing paket when jamaah is loaded
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
        val limit = if (jamaah.hargaPaket > 0) jamaah.hargaPaket else 0L
        
        val newTotalDp = jamaah.dp + amount
        val isLunas = newTotalDp >= limit && limit > 0

        val pembayaran = com.agunganamiroh.data.model.Pembayaran(
            jamaahId = jamaah.id,
            jumlah = amount,
            tanggal = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            keterangan = notes,
            status = "success",
            dibuatOleh = auth.currentUser?.email ?: "system"
        )

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            repository.addPayment(jamaah.id, pembayaran, newTotalDp, isLunas).fold(
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
}
