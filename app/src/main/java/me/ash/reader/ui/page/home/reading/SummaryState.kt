package me.ash.reader.ui.page.home.reading

sealed interface SummaryState {
    object Idle : SummaryState
    object Loading : SummaryState
    data class Success(val summary: String) : SummaryState
    data class Error(val message: String) : SummaryState
}
