package com.example.androidfarmerfriend.ui.screens.cropnotes

import androidx.lifecycle.ViewModel
import com.example.androidfarmerfriend.data.model.CropNote
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CropNotesViewModel(private val repository: FarmerRepository = FarmerRepository()) : ViewModel() {
    private val _cropNotesState = MutableStateFlow<List<CropNote>>(emptyList())
    val cropNotesState: StateFlow<List<CropNote>> = _cropNotesState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _cropNotesState.value = repository.getCropNotes()
    }
}
