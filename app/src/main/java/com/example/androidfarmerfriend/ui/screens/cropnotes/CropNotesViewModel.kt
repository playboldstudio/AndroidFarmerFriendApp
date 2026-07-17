package com.example.androidfarmerfriend.ui.screens.cropnotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
                    notesState = UiState.Error(e.message ?: "Failed to load notes")
                )
            }
        }
    }
}
