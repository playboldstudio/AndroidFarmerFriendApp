package com.example.androidfarmerfriend.ui.screens.disease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.data.localization.Language
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiseaseViewModel(private val repository: FarmerRepository = FarmerRepository()) : ViewModel() {
    private val _state = MutableStateFlow(DiseaseState())
    val state: StateFlow<DiseaseState> = _state.asStateFlow()

    private var currentLanguage: Language = Language.TAMIL

    fun loadData(language: Language = currentLanguage) {
        currentLanguage = language
        _state.value = _state.value.copy(diseasesState = UiState.Loading, searchQuery = "")
        viewModelScope.launch {
            try {
                val diseases = repository.getDiseases(language)
                _state.value = _state.value.copy(diseasesState = UiState.Success(diseases))
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    diseasesState = UiState.Error(e.message ?: "Failed to load diseases")
                )
            }
        }
    }

    fun onEvent(event: DiseaseEvent) {
        when (event) {
            is DiseaseEvent.SelectFilter -> {
                _state.value = _state.value.copy(selectedFilter = event.filter, searchQuery = "")
            }
            is DiseaseEvent.Search -> {
                _state.value = _state.value.copy(searchQuery = event.query)
            }
            is DiseaseEvent.Retry -> loadData()
        }
    }
}
