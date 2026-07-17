package com.example.androidfarmerfriend.ui.screens.disease

import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.model.Disease
import com.example.androidfarmerfriend.data.util.UiState

enum class DiseaseFilterType(val id: String, val displayKey: (AppStrings) -> String) {
    ALL("all", { it.filterAll }),
    LEAF("leaf", { it.filterLeafDiseases }),
    FRUIT("fruit", { it.filterFruitDiseases });
}

data class DiseaseState(
    val diseasesState: UiState<List<Disease>> = UiState.Loading,
    val selectedFilter: DiseaseFilterType = DiseaseFilterType.ALL,
    val searchQuery: String = ""
) {
    val filteredDiseases: List<Disease>
        get() {
            val data = (diseasesState as? UiState.Success)?.data ?: return emptyList()
            val query = searchQuery.trim().lowercase()
            return data.filter { disease ->
                val matchesFilter = selectedFilter == DiseaseFilterType.ALL ||
                    disease.name.contains(selectedFilter.id, ignoreCase = true) ||
                    disease.cropAffected.contains(selectedFilter.id, ignoreCase = true)
                val matchesSearch = query.isEmpty() ||
                    disease.name.lowercase().contains(query) ||
                    disease.cropAffected.lowercase().contains(query)
                matchesFilter && matchesSearch
            }
        }
}
