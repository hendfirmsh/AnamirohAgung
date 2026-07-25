package com.agunganamiroh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agunganamiroh.data.model.Jamaah
import com.agunganamiroh.data.model.Paket
import com.agunganamiroh.data.repository.JamaahRepository
import com.agunganamiroh.data.repository.PaketRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class CountdownState {
    data object Unknown : CountdownState()
    data object Today : CountdownState()
    data class Upcoming(val days: Int) : CountdownState()
    data class Departed(val daysAgo: Int) : CountdownState()
}

enum class ReadinessStatus {
    READY, NEEDS_ATTENTION, CRITICAL, DEPARTED
}

enum class UrgencyLevel {
    CRITICAL, HIGH, ATTENTION, READY, DEPARTED
}

enum class PackageRelationState {
    LINKED, LEGACY_DATE_ONLY, MISSING_PACKAGE, MISSING_DATE
}

enum class PaymentStatus {
    LUNAS, BELUM_LUNAS, BELUM_MEMBAYAR, DATA_TIDAK_TERSEDIA
}

data class JamaahReadiness(
    val jamaah: Jamaah,
    val packageRelation: PackageRelationState = PackageRelationState.MISSING_PACKAGE,
    val resolvedPackageName: String? = null,
    val resolvedDepartureDate: String? = null,
    val paymentComplete: Boolean = false,
    val remainingPayment: Long = 0,
    val paymentStatus: PaymentStatus = PaymentStatus.DATA_TIDAK_TERSEDIA,
    val documentsComplete: Boolean = false,
    val missingDocuments: List<String> = emptyList(),
    val approvalComplete: Boolean = false,
    val overallStatus: ReadinessStatus = ReadinessStatus.NEEDS_ATTENTION,
    val urgency: UrgencyLevel = UrgencyLevel.ATTENTION,
    val countdown: CountdownState = CountdownState.Unknown
)

data class DeparturePackage(
    val paket: Paket?,
    val jamaahs: List<JamaahReadiness>,
    val totalJamaah: Int = 0,
    val readyCount: Int = 0,
    val attentionCount: Int = 0,
    val readinessPercent: Float = 0f,
    val nearestDepartureDate: String? = null,
    val countdown: CountdownState = CountdownState.Unknown
)

data class DepartureUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val packages: List<DeparturePackage> = emptyList(),
    val urgentItems: List<JamaahReadiness> = emptyList(),
    val dataAttentionItems: List<JamaahReadiness> = emptyList(),
    val totalUpcomingJamaah: Int = 0,
    val readyCount: Int = 0,
    val attentionCount: Int = 0,
    val criticalCount: Int = 0,
    val nearestDeparture: DeparturePackage? = null,
    val filter: String = "Semua"
)

class DepartureViewModel : ViewModel() {
    private val jamaahRepository = JamaahRepository()
    private val paketRepository = PaketRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(DepartureUiState())
    val uiState: StateFlow<DepartureUiState> = _uiState.asStateFlow()

    private var jamaahJob: Job? = null
    private var paketJob: Job? = null

    private val _allJamaahs = MutableStateFlow<List<Jamaah>>(emptyList())
    private val _allPakets = MutableStateFlow<List<Paket>>(emptyList())

    private var jamaahLoaded = false
    private var paketLoaded = false

    init {
        observeData()
    }

    private fun observeData() {
        val user = auth.currentUser
        user?.email?.let { email ->
            jamaahJob = jamaahRepository.getJamaahRealtime(email)
                .onEach { result ->
                    result.fold(
                        onSuccess = { list ->
                            _allJamaahs.value = list
                            jamaahLoaded = true
                            tryCalculateReadiness()
                        },
                        onFailure = { e ->
                            _uiState.update { it.copy(loading = false, error = e.message) }
                        }
                    )
                }
                .launchIn(viewModelScope)

            paketJob = paketRepository.getPaketRealtime()
                .onEach { result ->
                    result.fold(
                        onSuccess = { list ->
                            _allPakets.value = list
                            paketLoaded = true
                            tryCalculateReadiness()
                        },
                        onFailure = { e ->
                            _uiState.update { it.copy(loading = false, error = e.message) }
                        }
                    )
                }
                .launchIn(viewModelScope)
        }
    }

    private fun tryCalculateReadiness() {
        if (!jamaahLoaded || !paketLoaded) return
        calculateReadiness()
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(filter = filter) }
        if (jamaahLoaded && paketLoaded) calculateReadiness()
    }

    private fun parseDate(dateString: String): Date? {
        if (dateString.isBlank()) return null
        val formats = listOf(
            SimpleDateFormat("dd MMMM yyyy", Locale.Builder().setLanguage("id").setRegion("ID").build()),
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            SimpleDateFormat("dd MMM yyyy", Locale.Builder().setLanguage("id").setRegion("ID").build())
        )
        for (fmt in formats) {
            try {
                return fmt.parse(dateString)
            } catch (_: Exception) {}
        }
        return null
    }

    private fun computeCountdown(dateString: String?): CountdownState {
        if (dateString.isNullOrBlank()) return CountdownState.Unknown
        val date = parseDate(dateString) ?: return CountdownState.Unknown
        val now = CalendarUtils.removeTime(Date())
        val target = CalendarUtils.removeTime(date)
        val diff = (target.time - now.time) / (1000L * 60 * 60 * 24)
        return when {
            diff < 0 -> CountdownState.Departed(daysAgo = (-diff).toInt())
            diff == 0L -> CountdownState.Today
            else -> CountdownState.Upcoming(days = diff.toInt())
        }
    }

    private fun calculateJamaahReadiness(
        jamaah: Jamaah,
        paketList: List<Paket>
    ): JamaahReadiness {
        val paket = paketList.find { it.id == jamaah.paketId }

        val packageRelation: PackageRelationState
        val departureDate: String?
        val resolvedPackageName: String?

        if (paket != null) {
            resolvedPackageName = paket.title
            departureDate = paket.tanggal.takeIf { it.isNotBlank() }
            packageRelation = if (departureDate != null) PackageRelationState.LINKED
                             else PackageRelationState.MISSING_DATE
        } else if (jamaah.keberangkatan?.isNotBlank() == true) {
            resolvedPackageName = null
            departureDate = jamaah.keberangkatan
            packageRelation = PackageRelationState.LEGACY_DATE_ONLY
        } else {
            resolvedPackageName = null
            departureDate = null
            packageRelation = PackageRelationState.MISSING_PACKAGE
        }

        val countdown = if (packageRelation == PackageRelationState.LINKED ||
                            packageRelation == PackageRelationState.LEGACY_DATE_ONLY) {
            computeCountdown(departureDate)
        } else {
            CountdownState.Unknown
        }

        val hasPriceData = jamaah.hargaPaket > 0
        val paymentComplete = jamaah.pelunasan ||
                (hasPriceData && jamaah.dp >= jamaah.hargaPaket)
        val remaining = if (hasPriceData) {
            (jamaah.hargaPaket - jamaah.dp).coerceAtLeast(0)
        } else 0L

        val paymentStatus = when {
            paymentComplete -> PaymentStatus.LUNAS
            hasPriceData && jamaah.dp > 0 -> PaymentStatus.BELUM_LUNAS
            hasPriceData -> PaymentStatus.BELUM_MEMBAYAR
            else -> PaymentStatus.DATA_TIDAK_TERSEDIA
        }

        val docMap = mapOf(
            "Akte" to jamaah.akte,
            "KTP" to jamaah.ktp,
            "KK" to jamaah.kk,
            "Paspor" to jamaah.paspor,
            "Vaksin Meningitis" to jamaah.meningitis
        )
        val documentsComplete = docMap.values.all { it }
        val missingDocs = docMap.filter { !it.value }.keys.toList()

        val approvalComplete = jamaah.status.lowercase() == "approved"

        val overallStatus = when {
            countdown is CountdownState.Departed -> ReadinessStatus.DEPARTED
            countdown is CountdownState.Upcoming && countdown.days <= 7 && !(paymentComplete && documentsComplete && approvalComplete) -> ReadinessStatus.CRITICAL
            paymentComplete && documentsComplete && approvalComplete -> ReadinessStatus.READY
            else -> ReadinessStatus.NEEDS_ATTENTION
        }

        val urgency = when {
            countdown is CountdownState.Departed -> UrgencyLevel.DEPARTED
            countdown is CountdownState.Upcoming && countdown.days <= 7 && overallStatus != ReadinessStatus.READY -> UrgencyLevel.CRITICAL
            countdown is CountdownState.Upcoming && countdown.days <= 14 && overallStatus != ReadinessStatus.READY -> UrgencyLevel.HIGH
            countdown !is CountdownState.Unknown && overallStatus != ReadinessStatus.READY -> UrgencyLevel.ATTENTION
            else -> UrgencyLevel.READY
        }

        return JamaahReadiness(
            jamaah = jamaah,
            packageRelation = packageRelation,
            resolvedPackageName = resolvedPackageName,
            resolvedDepartureDate = departureDate,
            paymentComplete = paymentComplete,
            remainingPayment = remaining,
            paymentStatus = paymentStatus,
            documentsComplete = documentsComplete,
            missingDocuments = missingDocs,
            approvalComplete = approvalComplete,
            overallStatus = overallStatus,
            urgency = urgency,
            countdown = countdown
        )
    }

    private fun calculateReadiness() {
        val jamaahs = _allJamaahs.value
        val pakets = _allPakets.value
        val currentFilter = _uiState.value.filter

        val validPaketIds = pakets.map { it.id }.toSet()

        val jamaahReadinessList = jamaahs.map { jamaah ->
            calculateJamaahReadiness(jamaah, pakets)
        }

        val (orphaned, connected) = jamaahReadinessList.partition { readiness ->
            val pid = readiness.jamaah.paketId
            pid.isBlank() || pid !in validPaketIds
        }

        val groupedByPaketId = connected.groupBy { it.jamaah.paketId }

        val packages = groupedByPaketId.map { (paketId, readinessList) ->
            val paket = pakets.find { it.id == paketId }
            val activeReadiness = readinessList.filter { it.overallStatus != ReadinessStatus.DEPARTED }

            val totalJamaah = activeReadiness.size
            val readyCount = activeReadiness.count { it.overallStatus == ReadinessStatus.READY }
            val attentionCount = activeReadiness.count { it.overallStatus != ReadinessStatus.READY }
            val readinessPercent = if (totalJamaah > 0) readyCount.toFloat() / totalJamaah else 0f

            val nearestDateString = paket?.tanggal?.takeIf { it.isNotBlank() }
            val countdown = computeCountdown(nearestDateString)

            DeparturePackage(
                paket = paket,
                jamaahs = readinessList.sortedBy { sortKey(it.countdown) },
                totalJamaah = totalJamaah,
                readyCount = readyCount,
                attentionCount = attentionCount,
                readinessPercent = readinessPercent,
                nearestDepartureDate = nearestDateString,
                countdown = countdown
            )
        }

        val sortedPackages = packages
            .filter { pkg -> pkg.jamaahs.any { it.overallStatus != ReadinessStatus.DEPARTED } }
            .sortedBy { sortKey(it.countdown) }

        val filteredPackages = when (currentFilter) {
            "Mendatang" -> sortedPackages.filter {
                it.countdown is CountdownState.Upcoming || it.countdown is CountdownState.Today
            }
            "Bulan Ini" -> {
                val now = CalendarUtils.removeTime(Date())
                sortedPackages.filter { pkg ->
                    val date = pkg.nearestDepartureDate?.let { parseDate(it) } ?: return@filter false
                    val cal = java.util.Calendar.getInstance().apply { time = now }
                    val targetCal = java.util.Calendar.getInstance().apply { time = date }
                    targetCal.get(java.util.Calendar.MONTH) == cal.get(java.util.Calendar.MONTH) &&
                            targetCal.get(java.util.Calendar.YEAR) == cal.get(java.util.Calendar.YEAR)
                }
            }
            "Perlu Perhatian" -> sortedPackages.filter { pkg -> pkg.attentionCount > 0 }
            "Sudah Berangkat" -> packages.filter { pkg ->
                pkg.jamaahs.isNotEmpty() && pkg.jamaahs.all { it.overallStatus == ReadinessStatus.DEPARTED }
            }
            else -> sortedPackages
        }

        val urgentItems = jamaahReadinessList
            .filter { it.overallStatus != ReadinessStatus.READY && it.overallStatus != ReadinessStatus.DEPARTED }
            .sortedBy { sortKey(it.countdown) }

        val dataAttentionItems = orphaned.filter {
            it.overallStatus != ReadinessStatus.DEPARTED
        }

        val activeReadinessAll = jamaahReadinessList.filter { it.overallStatus != ReadinessStatus.DEPARTED }
        val totalUpcoming = activeReadinessAll.size
        val readyCountAll = activeReadinessAll.count { it.overallStatus == ReadinessStatus.READY }
        val attentionCountAll = activeReadinessAll.count {
            it.overallStatus == ReadinessStatus.NEEDS_ATTENTION || it.overallStatus == ReadinessStatus.CRITICAL
        }
        val criticalCountAll = activeReadinessAll.count { it.overallStatus == ReadinessStatus.CRITICAL }
        val nearestPkg = sortedPackages.firstOrNull()

        _uiState.update {
            DepartureUiState(
                loading = false,
                packages = filteredPackages,
                urgentItems = urgentItems,
                dataAttentionItems = dataAttentionItems,
                totalUpcomingJamaah = totalUpcoming,
                readyCount = readyCountAll,
                attentionCount = attentionCountAll,
                criticalCount = criticalCountAll,
                nearestDeparture = nearestPkg,
                filter = currentFilter
            )
        }
    }

    private fun sortKey(countdown: CountdownState): Int = when (countdown) {
        is CountdownState.Today -> 0
        is CountdownState.Upcoming -> countdown.days
        is CountdownState.Unknown -> Int.MAX_VALUE
        is CountdownState.Departed -> Int.MAX_VALUE - 1
    }
}

object CalendarUtils {
    fun removeTime(date: Date): Date {
        val cal = java.util.Calendar.getInstance()
        cal.time = date
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.time
    }
}
