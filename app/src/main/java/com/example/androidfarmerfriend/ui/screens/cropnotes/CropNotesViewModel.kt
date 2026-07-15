package com.example.androidfarmerfriend.ui.screens.cropnotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.data.model.CropNote
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CropNoteEvent {
    data class SelectCrop(val crop: String) : CropNoteEvent
    data class Search(val query: String) : CropNoteEvent
    data object Retry : CropNoteEvent
}

data class CropNotesState(
    val notesState: UiState<List<CropNote>> = UiState.Loading,
    val selectedCrop: String = "அனைத்து",
    val searchQuery: String = ""
) {
    val crops: List<String>
        get() {
            val data = (notesState as? UiState.Success)?.data ?: return listOf("அனைத்து")
            return listOf("அனைத்து") + data.map { it.cropName }.distinct()
        }

    val filteredNotes: List<CropNote>
        get() {
            val data = (notesState as? UiState.Success)?.data ?: return emptyList()
            return data.filter { note ->
                val matchesCrop = selectedCrop == "அனைத்து" || note.cropName == selectedCrop
                val query = searchQuery.trim().lowercase()
                val matchesSearch = query.isEmpty() ||
                    note.title.lowercase().contains(query) ||
                    note.cropName.lowercase().contains(query) ||
                    note.content.lowercase().contains(query)
                matchesCrop && matchesSearch
            }
        }
}

class CropNotesViewModel(private val repository: FarmerRepository = FarmerRepository()) : ViewModel() {
    private val _state = MutableStateFlow(CropNotesState())
    val state: StateFlow<CropNotesState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun onEvent(event: CropNoteEvent) {
        when (event) {
            is CropNoteEvent.SelectCrop -> {
                _state.value = _state.value.copy(selectedCrop = event.crop, searchQuery = "")
            }
            is CropNoteEvent.Search -> {
                _state.value = _state.value.copy(searchQuery = event.query)
            }
            is CropNoteEvent.Retry -> loadData()
        }
    }

    private fun loadData() {
        _state.value = _state.value.copy(notesState = UiState.Loading)
        viewModelScope.launch {
            try {
                val notes = repository.getCropNotes()
                _state.value = _state.value.copy(notesState = UiState.Success(notes))
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    notesState = UiState.Error(e.message ?: "பயிர் குறிப்புகளை ஏற்ற முடியவில்லை")
                )
            }
        }
    }
}
