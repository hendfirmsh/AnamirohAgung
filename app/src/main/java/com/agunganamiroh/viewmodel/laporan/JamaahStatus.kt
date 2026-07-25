package com.agunganamiroh.viewmodel.laporan

enum class JamaahStatus {
    APPROVED, PENDING, REJECTED, UNKNOWN
}

fun normalizeJamaahStatus(status: String): JamaahStatus = when {
    status.equals("approved", true) || status.equals("verified", true) -> JamaahStatus.APPROVED
    status.equals("pending", true) -> JamaahStatus.PENDING
    status.equals("rejected", true) || status.equals("ditolak", true) -> JamaahStatus.REJECTED
    else -> JamaahStatus.UNKNOWN
}

data class MonthlyTrend(
    val month: String,
    val count: Int = 0,
    val amount: Long = 0
)

data class PackagePerformance(
    val packageName: String,
    val jamaahCount: Int,
    val totalNilai: Long
)

data class AnalyticsResult(
    val totalJamaah: Int = 0,
    val statusCounts: Map<JamaahStatus, Int> = JamaahStatus.entries.associateWith { 0 },
    val totalNilaiPenjualan: Long = 0,
    val totalPembayaranDiterima: Long = 0,
    val sisaTagihan: Long = 0,
    val invoiceAktif: Int = 0,
    val invoiceLunas: Int = 0,
    val registrationTrend: List<MonthlyTrend> = emptyList(),
    val paymentTrend: List<MonthlyTrend> = emptyList(),
    val packagePerformance: List<PackagePerformance> = emptyList()
)
