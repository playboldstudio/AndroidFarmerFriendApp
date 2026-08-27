package com.example.androidfarmerfriend.ui.screens.cropnotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.data.localization.Language
import com.example.androidfarmerfriend.data.model.CropNote
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.data.util.loadSafely
import com.example.androidfarmerfriend.data.util.orEmpty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CropNotesViewModel(private val repository: FarmerRepository = FarmerRepository()) : ViewModel() {
    private val _state = MutableStateFlow(CropNotesState())
    val state: StateFlow<CropNotesState> = _state.asStateFlow()

    private var currentLanguage: Language = Language.ENGLISH

    fun loadData(language: Language = currentLanguage) {
        currentLanguage = language
        _state.value = rerender(_state.value.copy(notesState = UiState.Loading, searchQuery = ""))
        viewModelScope.launch {
            _state.value = rerender(
                _state.value.copy(notesState = loadSafely { repository.getCropNotes(language) })
            )
        }
    }

    fun onEvent(event: CropNoteEvent) {
        when (event) {
            is CropNoteEvent.SelectCrop -> {
                _state.value = rerender(
                    _state.value.copy(selectedCrop = event.crop, searchQuery = "")
                )
            }
            is CropNoteEvent.Search -> {
                _state.value = rerender(_state.value.copy(searchQuery = event.query))
            }
            is CropNoteEvent.Retry -> loadData()
        }
    }

    /** Recompute the derived crop list + filtered notes once per state change instead of per read. */
    private fun rerender(s: CropNotesState): CropNotesState = s.copy(
        crops = computeCrops(s),
        filteredNotes = computeFilteredNotes(s)
    )

    private fun computeCrops(s: CropNotesState): List<String> {
        val data = s.notesState.orEmpty()
        return listOf(ALL_CROPS) + data.map { it.cropName }.distinct()
    }

    private fun computeFilteredNotes(s: CropNotesState): List<CropNote> {
        val data = s.notesState.orEmpty()
        return data.filter { note ->
            val matchesCrop = s.selectedCrop == ALL_CROPS || note.cropName == s.selectedCrop
            val query = s.searchQuery.trim().lowercase()
            val matchesSearch = query.isEmpty() ||
                note.title.lowercase().contains(query) ||
                note.cropName.lowercase().contains(query) ||
                note.content.lowercase().contains(query)
            matchesCrop && matchesSearch
        }
    }
}
