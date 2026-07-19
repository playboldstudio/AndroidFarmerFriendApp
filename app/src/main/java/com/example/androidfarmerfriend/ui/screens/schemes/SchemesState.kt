package com.example.androidfarmerfriend.ui.screens.schemes

import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.model.Scheme
import com.example.androidfarmerfriend.data.util.UiState

enum class SchemeFilterType(val id: String, val displayKey: (AppStrings) -> String) {
    ALL("all", { it.filterAll });

    companion object {
        fun fromId(id: String): SchemeFilterType = entries.find { it.id == id } ?: ALL
    }
}

data class SchemesState(
    val schemesState: UiState<List<Scheme>> = UiState.Loading,
    val selectedFilter: SchemeFilterType = SchemeFilterType.ALL,
    val searchQuery: String = ""
) {
    val filteredSchemes: List<Scheme>
        get() {
            val data = (schemesState as? UiState.Success)?.data ?: return emptyList()
            val query = searchQuery.trim().lowercase()
            return if (query.isEmpty()) data
            else data.filter {
                it.title.lowercase().contains(query) ||
                it.description.lowercase().contains(query)
            }
        }
}
