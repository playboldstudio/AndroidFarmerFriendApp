package com.example.androidfarmerfriend.ui.screens.cropnotes

import com.example.androidfarmerfriend.data.model.CropNote
import com.example.androidfarmerfriend.data.util.UiState

private const val ALL_CROPS = "__all__"

data class CropNotesState(
    val notesState: UiState<List<CropNote>> = UiState.Loading,
    val selectedCrop: String = ALL_CROPS,
    val searchQuery: String = ""
) {
    val crops: List<String>
        get() {
            val data = (notesState as? UiState.Success)?.data ?: return listOf(ALL_CROPS)
            return listOf(ALL_CROPS) + data.map { it.cropName }.distinct()
        }

    val filteredNotes: List<CropNote>
        get() {
            val data = (notesState as? UiState.Success)?.data ?: return emptyList()
            return data.filter { note ->
                val matchesCrop = selectedCrop == ALL_CROPS || note.cropName == selectedCrop
                val query = searchQuery.trim().lowercase()
                val matchesSearch = query.isEmpty() ||
                    note.title.lowercase().contains(query) ||
                    note.cropName.lowercase().contains(query) ||
                    note.content.lowercase().contains(query)
                matchesCrop && matchesSearch
            }
        }
}
