package com.agunganamiroh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agunganamiroh.data.model.Paket
import com.agunganamiroh.data.repository.PaketRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PaketUiState(
    val loading: Boolean = false,
    val pakets: List<Paket> = emptyList(),
    val error: String? = null
)

class PaketViewModel : ViewModel() {
    private val repository = PaketRepository()

    private val _uiState = MutableStateFlow(PaketUiState())
    val uiState: StateFlow<PaketUiState> = _uiState.asStateFlow()

    init {
        observePakets()
    }

    fun observePakets() {
        _uiState.update { it.copy(loading = true) }
        repository.getPaketRealtime()
            .onEach { result ->
                result.fold(
                    onSuccess = { list ->
                        _uiState.update { it.copy(pakets = list, loading = false) }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(error = e.message ?: "Gagal memuat paket", loading = false) }
                    }
                )
            }.launchIn(viewModelScope)
    }
}
