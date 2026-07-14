package com.example.androidfarmerfriend.ui.screens.disease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.data.model.Disease
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiseaseViewModel(private val repository: FarmerRepository = FarmerRepository()) : ViewModel() {
    private val _diseasesState = MutableStateFlow<List<Disease>>(emptyList())
    val diseasesState: StateFlow<List<Disease>> = _diseasesState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _diseasesState.value = repository.getDiseases()
            } catch (e: Exception) {
                _error.value = e.message ?: "நோய் தரவுகளை ஏற்ற முடியவில்லை"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun retry() {
        loadData()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
