package com.agunganamiroh.viewmodel.laporan

import com.agunganamiroh.data.model.Invoice
import com.agunganamiroh.data.model.Jamaah
import com.agunganamiroh.data.model.Paket
import com.agunganamiroh.data.model.Pembayaran
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportAnalyticsEngine {

    private val monthFormat = SimpleDateFormat("MMM", Locale("id", "ID"))
    private val yearMonthFormat = SimpleDateFormat("yyyy-MM", Locale("id", "ID"))

    fun compute(
        jamaahs: List<Jamaah>,
        payments: List<Pembayaran>,
        invoices: List<Invoice>,
        pakets: List<Paket>
    ): AnalyticsResult {
        val totalJamaah = jamaahs.size

        val statusCounts = JamaahStatus.entries.associateWith { status ->
            jamaahs.count { normalizeJamaahStatus(it.status) == status }
        }

        val totalNilaiPenjualan = jamaahs.sumOf { it.hargaPaket }

        val totalPembayaranDiterima = payments.sumOf { it.nominal }

        val sisaTagihan = totalNilaiPenjualan - totalPembayaranDiterima

        val invoiceAktif = invoices.count {
            it.status != "paid" && it.status != "cancelled"
        }
        val invoiceLunas = invoices.count { it.status == "paid" }

        val registrationTrend = buildMonthlyTrend(
            items = jamaahs,
            extractTimestamp = { it.createdAt?.toDate()?.time },
            countMapper = { list -> list.size.toLong() }
        )

        val paymentTrend = buildMonthlyTrend(
            items = payments,
            extractTimestamp = { it.createdAt?.toDate()?.time },
            countMapper = { list -> list.sumOf { it.nominal } }
        )

        val packagePerformance = computePackagePerformance(jamaahs, pakets)

        return AnalyticsResult(
            totalJamaah = totalJamaah,
            statusCounts = statusCounts,
            totalNilaiPenjualan = totalNilaiPenjualan,
            totalPembayaranDiterima = totalPembayaranDiterima,
            sisaTagihan = sisaTagihan,
            invoiceAktif = invoiceAktif,
            invoiceLunas = invoiceLunas,
            registrationTrend = registrationTrend,
            paymentTrend = paymentTrend,
            packagePerformance = packagePerformance
        )
    }

    private fun <T> buildMonthlyTrend(
        items: List<T>,
        extractTimestamp: (T) -> Long?,
        countMapper: (List<T>) -> Long
    ): List<MonthlyTrend> {
        val grouped = items
            .filter { extractTimestamp(it) != null }
            .groupBy { item ->
                val millis = extractTimestamp(item)!!
                yearMonthFormat.format(Date(millis))
            }
            .toSortedMap()

        if (grouped.isEmpty()) return emptyList()

        return grouped.map { (yearMonth, group) ->
            val parts = yearMonth.split("-")
            val monthNum = parts[1].toIntOrNull() ?: 1
            val monthNames = arrayOf(
                "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
                "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
            )
            val monthLabel = monthNames.getOrElse(monthNum - 1) { "?" }
            val countVal = countMapper(group).toInt()
            val amountVal = countMapper(group)
            MonthlyTrend(
                month = monthLabel,
                count = countVal,
                amount = amountVal
            )
        }
    }

    private fun computePackagePerformance(
        jamaahs: List<Jamaah>,
        pakets: List<Paket>
    ): List<PackagePerformance> {
        val paketMap = pakets.associateBy { it.id }
        return jamaahs.groupBy { it.paketId }
            .map { (paketId, group) ->
                val paketName = paketMap[paketId]?.title ?: "Paket $paketId"
                PackagePerformance(
                    packageName = paketName,
                    jamaahCount = group.size,
                    totalNilai = group.sumOf { it.hargaPaket }
                )
            }
            .sortedByDescending { it.jamaahCount }
    }
}
