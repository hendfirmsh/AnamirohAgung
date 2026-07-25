package com.agunganamiroh.viewmodel.paket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agunganamiroh.data.model.Paket
import com.agunganamiroh.data.repository.PaketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PackageSortOption(val label: String) {
    TANGGAL_ASC("Keberangkatan Terdekat"),
    HARGA_ASC("Harga Terendah"),
    HARGA_DESC("Harga Tertinggi"),
    SEAT_DESC("Seat Terbanyak")
}

data class CatalogUiState(
    val loading: Boolean = true,
    val pakets: List<Paket> = emptyList(),
    val filteredPakets: List<Paket> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: String = "Semua",
    val selectedSort: PackageSortOption = PackageSortOption.TANGGAL_ASC,
    val error: String? = null
)

class PackageCatalogViewModel : ViewModel() {

    private val repository = PaketRepository()

    private val _pakets = MutableStateFlow<List<Paket>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("Semua")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _selectedSort = MutableStateFlow(PackageSortOption.TANGGAL_ASC)
    val selectedSort: StateFlow<PackageSortOption> = _selectedSort.asStateFlow()

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        observePakets()
        observeFilters()
    }

    private fun observePakets() {
        viewModelScope.launch {
            repository.getPaketRealtime().collect { result ->
                result.fold(
                    onSuccess = { pakets ->
                        _pakets.value = pakets
                        _uiState.update {
                            it.copy(loading = false, pakets = pakets, error = null)
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(loading = false, error = error.message ?: "Gagal memuat paket")
                        }
                    }
                )
            }
        }
    }

    private fun observeFilters() {
        viewModelScope.launch {
            combine(_pakets, _searchQuery, _selectedFilter, _selectedSort) { pakets, query, filter, sort ->
                applyFilters(pakets, query, filter, sort)
            }.collect { filtered ->
                _uiState.update { it.copy(filteredPakets = filtered) }
            }
        }
    }

    private fun applyFilters(
        pakets: List<Paket>,
        query: String,
        filter: String,
        sort: PackageSortOption
    ): List<Paket> {
        val searched = if (query.isBlank()) pakets
        else pakets.filter { p ->
            p.title.contains(query, ignoreCase = true) ||
            p.maskapai.contains(query, ignoreCase = true) ||
            p.durasi.contains(query, ignoreCase = true)
        }

        val filtered = when (filter) {
            "Tersedia" -> searched.filter { it.sisaSeat > 0 }
            "Segera Berangkat" -> searched.filter { isDepartingSoon(it.tanggal) }
            else -> searched
        }

        return when (sort) {
            PackageSortOption.TANGGAL_ASC -> filtered.sortedBy { parseDateSafe(it.tanggal)?.time ?: Long.MAX_VALUE }
            PackageSortOption.HARGA_ASC -> filtered.sortedBy { it.harga }
            PackageSortOption.HARGA_DESC -> filtered.sortedByDescending { it.harga }
            PackageSortOption.SEAT_DESC -> filtered.sortedByDescending { it.sisaSeat }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun setSort(sort: PackageSortOption) {
        _selectedSort.value = sort
    }

    fun refresh() {
        _uiState.update { it.copy(loading = true, error = null) }
        observePakets()
    }

    companion object {
        private fun isDepartingSoon(tanggal: String): Boolean {
            val date = parseDateSafe(tanggal) ?: return false
            val now = Date()
            val diff = (date.time - now.time) / (1000L * 60 * 60 * 24)
            return diff in 0..30
        }

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
