package com.example.androidfarmerfriend.ui.screens.cropnotes

import com.example.androidfarmerfriend.data.model.CropNote
import com.example.androidfarmerfriend.data.util.UiState

const val ALL_CROPS = "__all__"

data class CropNotesState(
    val notesState: UiState<List<CropNote>> = UiState.Loading,
    val selectedCrop: String = ALL_CROPS,
    val searchQuery: String = "",
    /** Derived in the ViewModel whenever notes/selection/search change — not recomputed per read. */
    val crops: List<String> = listOf(ALL_CROPS),
    /** Derived in the ViewModel alongside [crops]. */
    val filteredNotes: List<CropNote> = emptyList()
)
