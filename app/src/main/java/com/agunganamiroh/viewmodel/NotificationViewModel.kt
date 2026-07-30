package com.agunganamiroh.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.agunganamiroh.data.model.NotificationCategory
import com.agunganamiroh.data.model.NotificationItem
import com.agunganamiroh.data.preferences.NotificationReadManager
import com.agunganamiroh.data.repository.ActivityRepository
import com.agunganamiroh.data.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class NotificationFilter(val displayName: String) {
    ALL("Semua"),
    UNREAD("Belum Dibaca"),
    PAYMENT("Pembayaran"),
    INVOICE("Invoice"),
    JAMAAH("Jamaah"),
    SYSTEM("Sistem"),
    SECURITY("Keamanan")
}

data class NotificationUiState(
    val isLoading: Boolean = true,
    val notifications: List<NotificationItem> = emptyList(),
    val filteredNotifications: List<NotificationItem> = emptyList(),
    val unreadCount: Int = 0,
    val filter: NotificationFilter = NotificationFilter.ALL,
    val searchQuery: String = "",
    val error: String? = null
)

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ActivityRepository()
    private val notificationRepo = NotificationRepository.getInstance()
    private val readManager = NotificationReadManager(application)
    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow(NotificationUiState())
    val state: StateFlow<NotificationUiState> = _state.asStateFlow()

    private val _filter = MutableStateFlow(NotificationFilter.ALL)
    val filter: StateFlow<NotificationFilter> = _filter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var activityJob: kotlinx.coroutines.Job? = null

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        activityJob?.cancel()
        activityJob = viewModelScope.launch {
            combine(
                flow { emit(repository.getActivities()) },
                readManager.readIds,
                _filter,
                _searchQuery
            ) { activityResult, readIds, currentFilter, query ->
                val activities = activityResult.getOrNull().orEmpty()
                val notifications = activities.map { activity ->
                    notificationRepo.mapToNotification(activity, activity.id in readIds)
                }

                val filtered = applyFilter(notifications, currentFilter, query)
                val unread = notifications.count { !it.isRead }

                _state.update {
                    NotificationUiState(
                        isLoading = false,
                        notifications = notifications,
                        filteredNotifications = filtered,
                        unreadCount = unread,
                        filter = currentFilter,
                        searchQuery = query,
                        error = activityResult.exceptionOrNull()?.message
                    )
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun applyFilter(
        notifications: List<NotificationItem>,
        filter: NotificationFilter,
        query: String
    ): List<NotificationItem> {
        var result = notifications

        result = when (filter) {
            NotificationFilter.ALL -> result
            NotificationFilter.UNREAD -> result.filter { !it.isRead }
            NotificationFilter.PAYMENT -> result.filter { it.category == NotificationCategory.PAYMENT }
            NotificationFilter.INVOICE -> result.filter { it.category == NotificationCategory.INVOICE }
            NotificationFilter.JAMAAH -> result.filter { it.category == NotificationCategory.JAMAAH }
            NotificationFilter.SYSTEM -> result.filter { it.category == NotificationCategory.SYSTEM || it.category == NotificationCategory.ANNOUNCEMENT }
            NotificationFilter.SECURITY -> result.filter { it.category == NotificationCategory.SECURITY }
        }

        if (query.isNotBlank()) {
            val q = query.lowercase()
            result = result.filter {
                it.title.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.category.displayName.lowercase().contains(q)
            }
        }

        return result
    }

    fun setFilter(filter: NotificationFilter) {
        _filter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun markAsRead(activityId: String) {
        viewModelScope.launch {
            readManager.markAsRead(activityId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            val ids = _state.value.notifications.map { it.id }
            readManager.markAllAsRead(ids)
        }
    }

    fun refresh() {
        activityJob?.cancel()
        _state.update { it.copy(isLoading = true) }
        observeNotifications()
    }

    override fun onCleared() {
        super.onCleared()
        activityJob?.cancel()
    }
}
