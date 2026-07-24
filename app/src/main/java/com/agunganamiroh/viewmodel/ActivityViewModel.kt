package com.agunganamiroh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agunganamiroh.data.model.Activity
import com.agunganamiroh.data.repository.ActivityRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ActivityUiState(
    val loading: Boolean = false,
    val activities: List<Activity> = emptyList(),
    val error: String? = null
)

class ActivityViewModel : ViewModel() {
    private val repository = ActivityRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    init {
        observeActivities()
    }

    fun observeActivities() {
        val email = auth.currentUser?.email ?: return
        _uiState.update { it.copy(loading = true) }

        repository.getRecentActivities(email)
            .onEach { result ->
                result.fold(
                    onSuccess = { list ->
                        _uiState.update { it.copy(activities = list, loading = false) }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(error = e.message, loading = false) }
                    }
                )
            }.launchIn(viewModelScope)
    }
}
