package com.example.androidfarmerfriend.ui.screens.schemes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.model.Scheme
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SchemeFilterType(val id: String, val displayKey: (AppStrings) -> String) {
    ALL("all", { it.filterAll }),
    CENTRAL("central", { it.filterCentral }),
    STATE("state", { it.filterState });

    companion object {
        fun fromId(id: String): SchemeFilterType = entries.find { it.id == id } ?: ALL
    }
}

sealed interface SchemeEvent {
    data class SelectFilter(val filter: SchemeFilterType) : SchemeEvent
    data class Search(val query: String) : SchemeEvent
    data object Retry : SchemeEvent
}

data class SchemesState(
    val schemesState: UiState<List<Scheme>> = UiState.Loading,
    val selectedFilter: SchemeFilterType = SchemeFilterType.ALL,
    val searchQuery: String = ""
) {
    val filteredSchemes: List<Scheme>
        get() {
            val data = (schemesState as? UiState.Success)?.data ?: return emptyList()
            val categoryFiltered = if (selectedFilter == SchemeFilterType.ALL) data
            else data.filter { it.category == selectedFilter.id }
            val query = searchQuery.trim().lowercase()
            return if (query.isEmpty()) categoryFiltered
            else categoryFiltered.filter {
                it.title.lowercase().contains(query) ||
                it.description.lowercase().contains(query)
            }
        }
}

class SchemesViewModel(private val repository: FarmerRepository = FarmerRepository()) : ViewModel() {
    private val _state = MutableStateFlow(SchemesState())
    val state: StateFlow<SchemesState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun onEvent(event: SchemeEvent) {
        when (event) {
            is SchemeEvent.SelectFilter -> {
                _state.value = _state.value.copy(selectedFilter = event.filter, searchQuery = "")
            }
            is SchemeEvent.Search -> {
                _state.value = _state.value.copy(searchQuery = event.query)
            }
            is SchemeEvent.Retry -> loadData()
        }
    }

    private fun loadData() {
        _state.value = _state.value.copy(schemesState = UiState.Loading)
        viewModelScope.launch {
            try {
                val schemes = repository.getSchemes()
                _state.value = _state.value.copy(schemesState = UiState.Success(schemes))
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    schemesState = UiState.Error(e.message ?: "Failed to load schemes")
                )
            }
        }
    }
}
