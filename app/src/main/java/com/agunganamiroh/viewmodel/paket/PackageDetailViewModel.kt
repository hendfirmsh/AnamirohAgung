package com.agunganamiroh.viewmodel.paket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agunganamiroh.data.model.Paket
import com.agunganamiroh.data.repository.PaketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DetailUiState(
    val loading: Boolean = true,
    val paket: Paket? = null,
    val error: String? = null
)

class PackageDetailViewModel : ViewModel() {

    private val repository = PaketRepository()

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadPaket(paketId: String) {
        _uiState.value = DetailUiState(loading = true)
        viewModelScope.launch {
            repository.getPaketById(paketId).fold(
                onSuccess = { paket ->
                    _uiState.value = DetailUiState(
                        loading = false,
                        paket = paket,
                        error = if (paket == null) "Paket tidak ditemukan" else null
                    )
                },
                onFailure = { error ->
                    _uiState.value = DetailUiState(
                        loading = false,
                        error = error.message ?: "Gagal memuat detail paket"
                    )
                }
            )
        }
    }

    fun isAvailable(paket: Paket): Boolean {
        return paket.sisaSeat > 0
    }

    fun isExpired(paket: Paket): Boolean {
        val date = parseDateSafe(paket.tanggal) ?: return false
        return date.before(Date())
    }

    fun availabilityLabel(paket: Paket): String {
        return when {
            paket.sisaSeat <= 0 -> "Paket Penuh"
            isExpired(paket) -> "Keberangkatan telah lewat"
            else -> "Tersedia"
        }
    }

    fun canRegister(paket: Paket): Boolean {
        return paket.sisaSeat > 0 && !isExpired(paket)
    }

    companion object {
        private fun parseDateSafe(dateString: String): Date? {
            if (dateString.isBlank()) return null
            val formats = listOf(
                SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")),
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            )
            for (fmt in formats) {
                try {
                    return fmt.parse(dateString)
                } catch (_: Exception) {}
            }
            return null
        }
    }
}
