package com.agunganamiroh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agunganamiroh.data.model.Activity
import com.agunganamiroh.data.repository.ActivityRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ActivityFilter(val displayName: String) {
    ALL("Semua"),
    JAMAAH("Jamaah"),
    PAYMENT("Pembayaran"),
    INVOICE("Invoice")
}

enum class PeriodFilter(val displayName: String) {
    TODAY("Hari Ini"),
    WEEK("7 Hari"),
    MONTH_30("30 Hari"),
    THIS_MONTH("Bulan Ini"),
    ALL("Semua")
}

data class RiwayatUiState(
    val loading: Boolean = true,
    val activities: List<Activity> = emptyList(),
    val groupedActivities: List<Pair<String, List<Activity>>> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: ActivityFilter = ActivityFilter.ALL,
    val selectedPeriod: PeriodFilter = PeriodFilter.ALL,
    val error: String? = null
)

class RiwayatViewModel : ViewModel() {

    private val repository = ActivityRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow(RiwayatUiState())
    val state: StateFlow<RiwayatUiState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(ActivityFilter.ALL)
    val selectedFilter: StateFlow<ActivityFilter> = _selectedFilter.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(PeriodFilter.ALL)
    val selectedPeriod: StateFlow<PeriodFilter> = _selectedPeriod.asStateFlow()

    private var activityJob: kotlinx.coroutines.Job? = null

    init {
        observeActivities()
    }

    private fun observeActivities() {
        val email = auth.currentUser?.email ?: return

        activityJob?.cancel()
        activityJob = viewModelScope.launch {
            combine(
                repository.getRecentActivities(email, limit = 0),
                _searchQuery,
                _selectedFilter,
                _selectedPeriod
            ) { activityResult, query, filter, period ->
                val activities = activityResult.getOrNull().orEmpty()
                val filtered = applyFilters(activities, filter, period, query)
                val grouped = groupByDate(filtered)

                _state.update {
                    RiwayatUiState(
                        loading = false,
                        activities = activities,
                        groupedActivities = grouped,
                        searchQuery = query,
                        selectedFilter = filter,
                        selectedPeriod = period,
                        error = activityResult.exceptionOrNull()?.message
                    )
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun applyFilters(
        activities: List<Activity>,
        filter: ActivityFilter,
        period: PeriodFilter,
        query: String
    ): List<Activity> {
        var result = activities

        result = when (filter) {
            ActivityFilter.ALL -> result
            ActivityFilter.JAMAAH -> result.filter { it.type.startsWith("JAMAAH_") }
            ActivityFilter.PAYMENT -> result.filter { it.type.startsWith("PAYMENT_") }
            ActivityFilter.INVOICE -> result.filter { it.type.startsWith("INVOICE_") }
        }

        result = when (period) {
            PeriodFilter.ALL -> result
            else -> {
                val now = Calendar.getInstance()
                val cutoff = Calendar.getInstance().apply {
                    when (period) {
                        PeriodFilter.TODAY -> time = now.time
                        PeriodFilter.WEEK -> add(Calendar.DAY_OF_YEAR, -7)
                        PeriodFilter.MONTH_30 -> add(Calendar.DAY_OF_YEAR, -30)
                        PeriodFilter.THIS_MONTH -> {
                            set(Calendar.DAY_OF_MONTH, 1)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        else -> {}
                    }
                }
                result.filter { activity ->
                    activity.createdAt?.toDate()?.let { date ->
                        date.after(cutoff.time) || date == cutoff.time
                    } ?: false
                }
            }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter { activity ->
                activity.title.lowercase().contains(q) ||
                activity.description.lowercase().contains(q) ||
                activity.jamaahName.lowercase().contains(q)
            }
        }

        return result
    }

    private fun groupByDate(activities: List<Activity>): List<Pair<String, List<Activity>>> {
        if (activities.isEmpty()) return emptyList()

        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(today.time)

        val grouped = activities.groupBy { activity ->
            val cal = Calendar.getInstance()
            activity.createdAt?.toDate()?.let { cal.time = it }

            val activityDateStr = dateFormat.format(cal.time)
            when {
                activityDateStr == todayStr -> "HARI INI"
                cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "KEMARIN"
                cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) -> {
                    val dayNames = arrayOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
                    val currentWeek = today.get(Calendar.WEEK_OF_YEAR)
                    val activityWeek = cal.get(Calendar.WEEK_OF_YEAR)
                    if (currentWeek == activityWeek) {
                        dayNames[cal.get(Calendar.DAY_OF_WEEK) - 1]
                    } else {
                        val monthNames = arrayOf(
                            "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
                            "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
                        )
                        "${cal.get(Calendar.DAY_OF_MONTH)} ${monthNames[cal.get(Calendar.MONTH)]}"
                    }
                }
                else -> {
                    val monthNames = arrayOf(
                        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
                    )
                    "${monthNames[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}"
                }
            }
        }

        val sectionOrder = listOf("HARI INI", "KEMARIN") +
            listOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu") +
            (1..31).flatMap { day ->
                listOf(
                    "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
                    "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
                ).map { "$day $it" }
            } +
            listOf(
                "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"
            ).flatMap { month ->
                (2020..2030).map { "$month $it" }
            }

        return grouped.entries.sortedBy { entry ->
            val index = sectionOrder.indexOf(entry.key)
            if (index >= 0) index else Int.MAX_VALUE
        }.map { it.key to it.value }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: ActivityFilter) {
        _selectedFilter.value = filter
    }

    fun setPeriod(period: PeriodFilter) {
        _selectedPeriod.value = period
    }

    fun refresh() {
        activityJob?.cancel()
        _state.update { it.copy(loading = true) }
        observeActivities()
    }

    override fun onCleared() {
        super.onCleared()
        activityJob?.cancel()
    }
}
