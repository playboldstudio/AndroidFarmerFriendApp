package com.example.androidfarmerfriend.ui.screens.schemes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.data.localization.Language
import com.example.androidfarmerfriend.data.model.Scheme
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.data.util.loadSafely
import com.example.androidfarmerfriend.data.util.orEmpty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SchemesViewModel(private val repository: FarmerRepository = FarmerRepository()) : ViewModel() {
    private val _state = MutableStateFlow(SchemesState())
    val state: StateFlow<SchemesState> = _state.asStateFlow()

    private var currentLanguage: Language = Language.ENGLISH

    fun loadData(language: Language = currentLanguage) {
        currentLanguage = language
        _state.value = rerender(_state.value.copy(schemesState = UiState.Loading, searchQuery = ""))
        viewModelScope.launch {
            _state.value = rerender(
                _state.value.copy(schemesState = loadSafely { repository.getSchemes(language) })
            )
        }
    }

    fun onEvent(event: SchemeEvent) {
        when (event) {
            is SchemeEvent.SelectFilter -> {
                _state.value = rerender(
                    _state.value.copy(selectedFilter = event.filter, searchQuery = "")
                )
            }
            is SchemeEvent.Search -> {
                _state.value = rerender(_state.value.copy(searchQuery = event.query))
            }
            is SchemeEvent.Retry -> loadData()
        }
    }

    /** Recompute the derived list once per state change instead of per read. */
    private fun rerender(s: SchemesState): SchemesState = s.copy(filteredSchemes = computeFiltered(s))

    private fun computeFiltered(s: SchemesState): List<Scheme> {
        val data = s.schemesState.orEmpty()
        val query = s.searchQuery.trim().lowercase()
        return if (query.isEmpty()) data
        else data.filter {
            it.title.lowercase().contains(query) ||
                it.description.lowercase().contains(query)
        }
    }
}
