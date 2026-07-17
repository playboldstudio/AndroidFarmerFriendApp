package com.example.androidfarmerfriend.ui.screens.schemes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
