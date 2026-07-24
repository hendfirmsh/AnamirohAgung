package com.agunganamiroh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agunganamiroh.data.model.Invoice
import com.agunganamiroh.data.model.Jamaah
import com.agunganamiroh.data.model.Paket
import com.agunganamiroh.data.repository.InvoiceRepository
import com.agunganamiroh.data.repository.JamaahRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class InvoiceFormData(
    val subtotal: Long = 0,
    val upgradeDouble: Long = 0,
    val upgradeTriple: Long = 0,
    val koper: Long = 0,
    val paspor: Long = 0,
    val vaksin: Long = 0,
    val diskon: Long = 0,
    val biayaLain: Long = 0,
    val keteranganBiayaLain: String = "",
    val tanggalJatuhTempo: String = ""
) {
    val totalTagihan: Long
        get() = (subtotal + upgradeDouble + upgradeTriple + koper + paspor + vaksin + biayaLain - diskon)
            .coerceAtLeast(0)
}

data class AdminInvoiceUiState(
    val loading: Boolean = false,
    val selectedTab: Int = 0,
    val allInvoices: List<Invoice> = emptyList(),
    val filteredInvoices: List<Invoice> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: String = "Semua",
    val approvedJamaahs: List<Jamaah> = emptyList(),
    val selectedJamaah: Jamaah? = null,
    val selectedPaket: Paket? = null,
    val invoiceForm: InvoiceFormData = InvoiceFormData(),
    val existingInvoices: List<Invoice> = emptyList(),
    val success: Boolean = false,
    val error: String? = null
)

class AdminInvoiceViewModel : ViewModel() {
    private val invoiceRepository = InvoiceRepository()
    private val jamaahRepository = JamaahRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(AdminInvoiceUiState())
    val uiState: StateFlow<AdminInvoiceUiState> = _uiState.asStateFlow()

    private var jamaahJob: Job? = null
    private var paketJob: Job? = null
    private var allInvoiceJob: Job? = null

    init {
        loadApprovedJamaahs()
        loadAllInvoices()
    }

    private fun loadApprovedJamaahs() {
        _uiState.update { it.copy(loading = true) }
        invoiceRepository.getApprovedJamaahs()
            .onEach { result ->
                result.fold(
                    onSuccess = { list ->
                        _uiState.update { it.copy(approvedJamaahs = list, loading = false) }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(error = e.message, loading = false) }
                    }
                )
            }.launchIn(viewModelScope)
    }

    private fun loadAllInvoices() {
        allInvoiceJob?.cancel()
        invoiceRepository.getAllInvoices()
            .onEach { result ->
                result.fold(
                    onSuccess = { list ->
                        _uiState.update { it.copy(allInvoices = list) }
                        applyFilters()
                    },
                    onFailure = { }
                )
            }.launchIn(viewModelScope)
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun onFilterChange(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
        applyFilters()
    }

    private fun applyFilters() {
        val query = _uiState.value.searchQuery.lowercase()
        val filter = _uiState.value.selectedFilter
        val filtered = _uiState.value.allInvoices.filter { invoice ->
            val matchesSearch = invoice.noInvoice.lowercase().contains(query) ||
                    invoice.jamaahName.lowercase().contains(query)
            val matchesFilter = when (filter) {
                "Paid" -> invoice.status == "paid"
                "Unpaid" -> invoice.status == "published"
                "Partial" -> invoice.status == "partial"
                "Cancelled" -> invoice.status == "cancelled"
                else -> true
            }
            matchesSearch && matchesFilter
        }
        _uiState.update { it.copy(filteredInvoices = filtered) }
    }

    fun selectJamaahById(jamaahId: String) {
        val found = _uiState.value.approvedJamaahs.find { it.id == jamaahId }
        if (found != null) {
            selectJamaah(found)
        }
    }

    fun selectJamaah(jamaah: Jamaah) {
        _uiState.update { it.copy(
            selectedJamaah = jamaah,
            invoiceForm = InvoiceFormData(subtotal = jamaah.hargaPaket),
            selectedPaket = null
        ) }
        if (jamaah.paketId.isNotEmpty()) {
            observePaket(jamaah.paketId)
        }
        observeExistingInvoices(jamaah.id)
    }

    private fun observePaket(paketId: String) {
        paketJob?.cancel()
        paketJob = jamaahRepository.getPaketById(paketId)
            .onEach { result ->
                result.fold(
                    onSuccess = { paket ->
                        _uiState.update { it.copy(selectedPaket = paket) }
                    },
                    onFailure = { }
                )
            }.launchIn(viewModelScope)
    }

    private fun observeExistingInvoices(jamaahId: String) {
        invoiceRepository.getInvoiceByJamaahId(jamaahId)
            .onEach { result ->
                result.fold(
                    onSuccess = { invoices ->
                        _uiState.update { it.copy(existingInvoices = invoices) }
                    },
                    onFailure = { }
                )
            }.launchIn(viewModelScope)
    }

    fun updateFormField(field: String, value: Any) {
        val form = _uiState.value.invoiceForm
        _uiState.update {
            it.copy(invoiceForm = when (field) {
                "subtotal" -> form.copy(subtotal = (value as Long))
                "upgradeDouble" -> form.copy(upgradeDouble = (value as Long))
                "upgradeTriple" -> form.copy(upgradeTriple = (value as Long))
                "koper" -> form.copy(koper = (value as Long))
                "paspor" -> form.copy(paspor = (value as Long))
                "vaksin" -> form.copy(vaksin = (value as Long))
                "diskon" -> form.copy(diskon = (value as Long))
                "biayaLain" -> form.copy(biayaLain = (value as Long))
                "keteranganBiayaLain" -> form.copy(keteranganBiayaLain = (value as String))
                "tanggalJatuhTempo" -> form.copy(tanggalJatuhTempo = (value as String))
                else -> form
            })
        }
    }

    fun createInvoice() {
        val jamaah = _uiState.value.selectedJamaah ?: run {
            _uiState.update { it.copy(error = "Pilih jamaah terlebih dahulu") }
            return
        }
        val form = _uiState.value.invoiceForm
        val user = auth.currentUser ?: run {
            _uiState.update { it.copy(error = "Silakan login ulang") }
            return
        }

        val invoice = Invoice(
            jamaahId = jamaah.id,
            jamaahName = jamaah.nama,
            agentEmail = jamaah.input_by,
            agentName = jamaah.input_by,
            createdBy = user.uid,
            createdByEmail = user.email ?: "",
            totalTagihan = form.totalTagihan,
            subtotal = form.subtotal,
            upgradeDouble = form.upgradeDouble,
            upgradeTriple = form.upgradeTriple,
            koper = form.koper,
            paspor = form.paspor,
            vaksin = form.vaksin,
            diskon = form.diskon,
            biayaLain = form.biayaLain,
            keteranganBiayaLain = form.keteranganBiayaLain,
            status = "published",
            tanggalInvoice = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
            tanggalJatuhTempo = form.tanggalJatuhTempo
        )

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            invoiceRepository.createInvoice(invoice).fold(
                onSuccess = {
                    _uiState.update { it.copy(loading = false, success = true) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(loading = false, error = e.message) }
                }
            )
        }
    }

    fun cancelInvoice(invoiceId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            invoiceRepository.cancelInvoice(invoiceId).fold(
                onSuccess = {
                    _uiState.update { it.copy(loading = false) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(loading = false, error = e.message) }
                }
            )
        }
    }

    fun refresh() {
        loadApprovedJamaahs()
        loadAllInvoices()
    }

    fun clearStatus() {
        _uiState.update { it.copy(success = false, error = null) }
    }

    fun resetSelection() {
        _uiState.update { it.copy(
            selectedJamaah = null,
            selectedPaket = null,
            invoiceForm = InvoiceFormData(),
            existingInvoices = emptyList(),
            success = false,
            error = null
        ) }
    }
}
