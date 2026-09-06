package com.example.androidfarmerfriend.ui.screens.schemes

import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.model.Scheme
import com.example.androidfarmerfriend.data.util.UiState

enum class SchemeFilterType(val id: String, val displayKey: (AppStrings) -> String) {
    ALL("all", { it.filterAll }),
    SAVED("saved", { it.filterSaved });

    companion object {
        fun fromId(id: String): SchemeFilterType = entries.find { it.id == id } ?: ALL
    }
}

data class SchemesState(
    val schemesState: UiState<List<Scheme>> = UiState.Loading,
    val selectedFilter: SchemeFilterType = SchemeFilterType.ALL,
    val searchQuery: String = "",
    /** Source URLs the user has saved; empty means nothing bookmarked. */
    val savedUrls: Set<String> = emptySet(),
    /** Derived in the ViewModel whenever schemes/search/filter change — not recomputed per read. */
    val filteredSchemes: List<Scheme> = emptyList()
)
