package com.example.androidfarmerfriend.ui.screens.disease

import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.model.Disease
import com.example.androidfarmerfriend.data.util.UiState

enum class DiseaseFilterType(val id: String, val displayKey: (AppStrings) -> String) {
    ALL("all", { it.filterAll });
}

data class DiseaseState(
    val diseasesState: UiState<List<Disease>> = UiState.Loading,
    val selectedFilter: DiseaseFilterType = DiseaseFilterType.ALL,
    val searchQuery: String = "",
    /** Derived in the ViewModel whenever diseases/search/filter change — not recomputed per read. */
    val filteredDiseases: List<Disease> = emptyList()
)
