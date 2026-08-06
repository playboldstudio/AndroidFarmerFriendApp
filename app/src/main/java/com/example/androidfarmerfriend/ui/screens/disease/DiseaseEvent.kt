package com.example.androidfarmerfriend.ui.screens.disease

sealed interface DiseaseEvent {
    data class SelectFilter(val filter: DiseaseFilterType) : DiseaseEvent
    data class Search(val query: String) : DiseaseEvent
    data object Retry : DiseaseEvent
}
