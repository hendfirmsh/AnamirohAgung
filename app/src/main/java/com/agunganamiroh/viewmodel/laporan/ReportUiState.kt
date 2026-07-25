package com.agunganamiroh.viewmodel.laporan

sealed interface ReportUiState {
    data object Loading : ReportUiState
    data class Success(
        val analytics: AnalyticsResult,
        val agentName: String = ""
    ) : ReportUiState
    data class Error(val message: String) : ReportUiState
}
