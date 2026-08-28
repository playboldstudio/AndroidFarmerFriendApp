package com.example.androidfarmerfriend.ui.screens.disease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.data.localization.Language
import com.example.androidfarmerfriend.data.model.Disease
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.data.util.loadSafely
import com.example.androidfarmerfriend.data.util.orEmpty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiseaseViewModel(private val repository: FarmerRepository = FarmerRepository()) : ViewModel() {
    private val _state = MutableStateFlow(DiseaseState())
    val state: StateFlow<DiseaseState> = _state.asStateFlow()

    private var currentLanguage: Language = Language.ENGLISH

    fun loadData(language: Language = currentLanguage) {
        currentLanguage = language
        _state.value = rerender(_state.value.copy(diseasesState = UiState.Loading, searchQuery = ""))
        viewModelScope.launch {
            _state.value = rerender(
                _state.value.copy(diseasesState = loadSafely { repository.getDiseases(language) })
            )
        }
    }

    fun onEvent(event: DiseaseEvent) {
        when (event) {
            is DiseaseEvent.SelectFilter -> {
                _state.value = rerender(
                    _state.value.copy(selectedFilter = event.filter, searchQuery = "")
                )
            }
            is DiseaseEvent.Search -> {
                _state.value = rerender(_state.value.copy(searchQuery = event.query))
            }
            is DiseaseEvent.Retry -> loadData()
        }
    }

    /** Recompute the derived list once per state change instead of per read. */
    private fun rerender(s: DiseaseState): DiseaseState = s.copy(filteredDiseases = computeFiltered(s))

    private fun computeFiltered(s: DiseaseState): List<Disease> {
        val data = s.diseasesState.orEmpty()
        val query = s.searchQuery.trim().lowercase()
        return if (query.isEmpty()) data
        else data.filter { disease ->
            disease.name.lowercase().contains(query) ||
                disease.cropAffected.lowercase().contains(query)
        }
    }
}
