package com.agunganamiroh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agunganamiroh.data.model.Activity
import com.agunganamiroh.data.repository.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActivityUiState(
    val loading: Boolean = false,
    val activities: List<Activity> = emptyList(),
    val error: String? = null
)

class ActivityViewModel : ViewModel() {

    private val repository = ActivityRepository()

    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    init {
        loadActivities()
    }

    fun loadActivities() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true)

            try {
                val result = repository.getActivities()
                result.fold(
                    onSuccess = { activities ->
                        _uiState.value = ActivityUiState(
                            loading = false,
                            activities = activities
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = ActivityUiState(
                            loading = false,
                            error = exception.message ?: "Gagal memuat aktivitas"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = ActivityUiState(
                    loading = false,
                    error = e.message ?: "Gagal memuat aktivitas"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}