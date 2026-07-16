package com.example.androidfarmerfriend.ui.screens.disease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.model.Disease
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class DiseaseFilterType(val id: String, val displayKey: (AppStrings) -> String) {
    ALL("all", { it.filterAll }),
    LEAF("leaf", { it.filterLeafDiseases }),
    FRUIT("fruit", { it.filterFruitDiseases });
}

sealed interface DiseaseEvent {
    data class SelectFilter(val filter: DiseaseFilterType) : DiseaseEvent
    data class Search(val query: String) : DiseaseEvent
    data object Retry : DiseaseEvent
}

data class DiseaseState(
    val diseasesState: UiState<List<Disease>> = UiState.Loading,
    val selectedFilter: DiseaseFilterType = DiseaseFilterType.ALL,
    val searchQuery: String = ""
) {
    val filteredDiseases: List<Disease>
        get() {
            val data = (diseasesState as? UiState.Success)?.data ?: return emptyList()
            val query = searchQuery.trim().lowercase()
            return data.filter { disease ->
                val matchesFilter = selectedFilter == DiseaseFilterType.ALL ||
                    disease.name.contains(selectedFilter.id, ignoreCase = true) ||
                    disease.cropAffected.contains(selectedFilter.id, ignoreCase = true)
                val matchesSearch = query.isEmpty() ||
                    disease.name.lowercase().contains(query) ||
                    disease.cropAffected.lowercase().contains(query)
                matchesFilter && matchesSearch
            }
        }
}

class DiseaseViewModel(private val repository: FarmerRepository = FarmerRepository()) : ViewModel() {
    private val _state = MutableStateFlow(DiseaseState())
    val state: StateFlow<DiseaseState> = _state.asStateFlow()

    init {
        loadData()
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

    private fun loadData() {
        _state.value = _state.value.copy(diseasesState = UiState.Loading)
        viewModelScope.launch {
            try {
                val diseases = repository.getDiseases()
                _state.value = _state.value.copy(diseasesState = UiState.Success(diseases))
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    diseasesState = UiState.Error(e.message ?: "Failed to load diseases")
                )
            }
        }
    }
}
