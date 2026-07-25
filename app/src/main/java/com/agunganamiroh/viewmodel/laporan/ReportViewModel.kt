package com.agunganamiroh.viewmodel.laporan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agunganamiroh.data.model.Invoice
import com.agunganamiroh.data.model.Jamaah
import com.agunganamiroh.data.model.Paket
import com.agunganamiroh.data.model.Pembayaran
import com.agunganamiroh.data.repository.InvoiceRepository
import com.agunganamiroh.data.repository.JamaahRepository
import com.agunganamiroh.data.repository.PaketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReportViewModel : ViewModel() {

    private val jamaahRepository = JamaahRepository()
    private val invoiceRepository = InvoiceRepository()
    private val paketRepository = PaketRepository()

    private val _state = MutableStateFlow<ReportUiState>(ReportUiState.Loading)
    val state: StateFlow<ReportUiState> = _state.asStateFlow()

    private var agentEmail: String = ""
    private var agentName: String = ""

    private var latestJamaahs: List<Jamaah> = emptyList()
    private var latestPayments: List<Pembayaran> = emptyList()
    private var latestInvoices: List<Invoice> = emptyList()
    private var latestPakets: List<Paket> = emptyList()

    fun loadData(email: String, name: String = "") {
        agentEmail = email
        agentName = name
        _state.value = ReportUiState.Loading

        viewModelScope.launch {
            jamaahRepository.getJamaahRealtime(email).collect { result ->
                result.fold(
                    onSuccess = { jamaahs ->
                        latestJamaahs = jamaahs
                        recompute()
                    },
                    onFailure = { e ->
                        _state.value = ReportUiState.Error(e.message ?: "Gagal memuat data jamaah")
                    }
                )
            }
        }

        viewModelScope.launch {
            jamaahRepository.getAllPaymentsByAgent(email).collect { result ->
                result.fold(
                    onSuccess = { payments ->
                        latestPayments = payments
                        recompute()
                    },
                    onFailure = { }
                )
            }
        }

        viewModelScope.launch {
            invoiceRepository.getInvoicesForAgent(email).collect { result ->
                result.fold(
                    onSuccess = { invoices ->
                        latestInvoices = invoices
                        recompute()
                    },
                    onFailure = { }
                )
            }
        }

        viewModelScope.launch {
            paketRepository.getPaketRealtime().collect { result ->
                result.fold(
                    onSuccess = { pakets ->
                        latestPakets = pakets
                        recompute()
                    },
                    onFailure = { }
                )
            }
        }
    }

    private fun recompute() {
        val analytics = ReportAnalyticsEngine.compute(
            jamaahs = latestJamaahs,
            payments = latestPayments,
            invoices = latestInvoices,
            pakets = latestPakets
        )
        _state.value = ReportUiState.Success(
            analytics = analytics,
            agentName = agentName
        )
    }
}
