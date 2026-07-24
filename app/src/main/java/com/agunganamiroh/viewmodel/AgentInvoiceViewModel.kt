package com.agunganamiroh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agunganamiroh.data.model.Invoice
import com.agunganamiroh.data.model.Jamaah
import com.agunganamiroh.data.model.Pembayaran
import com.agunganamiroh.data.repository.InvoiceRepository
import com.agunganamiroh.data.repository.JamaahRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AgentInvoiceUiState(
    val loading: Boolean = false,
    val invoices: List<Invoice> = emptyList(),
    val filteredInvoices: List<Invoice> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: String = "Semua",
    val sortOrder: String = "terbaru",
    val selectedInvoice: Invoice? = null,
    val jamaah: Jamaah? = null,
    val paymentHistory: List<Pembayaran> = emptyList(),
    val totalPaid: Long = 0,
    val error: String? = null
)

class AgentInvoiceViewModel : ViewModel() {
    private val invoiceRepository = InvoiceRepository()
    private val jamaahRepository = JamaahRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(AgentInvoiceUiState())
    val uiState: StateFlow<AgentInvoiceUiState> = _uiState.asStateFlow()

    private val _allInvoices = MutableStateFlow<List<Invoice>>(emptyList())
    private var invoiceJob: Job? = null
    private var detailJob: Job? = null

    fun loadInvoices() {
        val email = auth.currentUser?.email ?: return
        invoiceJob?.cancel()
        _uiState.update { it.copy(loading = true) }

        invoiceJob = invoiceRepository.getInvoicesForAgent(email)
            .onEach { result ->
                result.fold(
                    onSuccess = { list ->
                        _allInvoices.value = list
                        filterAndSort()
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(loading = false, error = e.message) }
                    }
                )
            }.launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterAndSort()
    }

    fun onFilterChange(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
        filterAndSort()
    }

    fun onSortChange(order: String) {
        _uiState.update { it.copy(sortOrder = order) }
        filterAndSort()
    }

    private fun filterAndSort() {
        val query = _uiState.value.searchQuery.lowercase()
        val filter = _uiState.value.selectedFilter
        val sort = _uiState.value.sortOrder

        val filtered = _allInvoices.value.filter { invoice ->
            val matchesSearch = invoice.noInvoice.lowercase().contains(query) ||
                    invoice.jamaahName.lowercase().contains(query)

            val matchesFilter = when (filter) {
                "Paid" -> invoice.status == "paid"
                "Unpaid" -> invoice.status == "published"
                "Partial" -> invoice.status == "partial"
                else -> true
            }

            matchesSearch && matchesFilter
        }

        val sorted = when (sort) {
            "terlama" -> filtered.sortedBy { it.createdAt?.toDate()?.time ?: 0L }
            else -> filtered.sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
        }

        _uiState.update { it.copy(filteredInvoices = sorted, loading = false) }
    }

    fun loadInvoiceDetail(invoiceId: String) {
        detailJob?.cancel()
        _uiState.update { it.copy(loading = true, error = null, selectedInvoice = null) }

        invoiceRepository.getInvoiceById(invoiceId)
            .onEach { result ->
                result.fold(
                    onSuccess = { invoice ->
                        _uiState.update { it.copy(selectedInvoice = invoice, loading = false) }
                        loadJamaahDetail(invoice.jamaahId)
                        loadPaymentHistory(invoice.jamaahId)
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(error = e.message, loading = false) }
                    }
                )
            }.launchIn(viewModelScope)
    }

    private fun loadJamaahDetail(jamaahId: String) {
        jamaahRepository.getJamaahById(jamaahId)
            .onEach { result ->
                result.fold(
                    onSuccess = { jamaah ->
                        _uiState.update { it.copy(jamaah = jamaah) }
                    },
                    onFailure = { }
                )
            }.launchIn(viewModelScope)
    }

    private fun loadPaymentHistory(jamaahId: String) {
        jamaahRepository.getPaymentHistory(jamaahId)
            .onEach { result ->
                result.fold(
                    onSuccess = { payments ->
                        val totalPaid = payments.sumOf { it.nominal }
                        _uiState.update { it.copy(paymentHistory = payments, totalPaid = totalPaid) }
                    },
                    onFailure = { }
                )
            }.launchIn(viewModelScope)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        invoiceJob?.cancel()
        detailJob?.cancel()
    }
}
