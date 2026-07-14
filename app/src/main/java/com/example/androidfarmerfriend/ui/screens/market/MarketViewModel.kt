package com.example.androidfarmerfriend.ui.screens.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.data.model.Crop
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MarketViewModel(private val repository: FarmerRepository = FarmerRepository()) : ViewModel() {
    private val _allCrops = MutableStateFlow<List<Crop>>(emptyList())
    private val _cropsState = MutableStateFlow<List<Crop>>(emptyList())
    val cropsState: StateFlow<List<Crop>> = _cropsState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedFilter = MutableStateFlow("காய்கறிகள்")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        onFilterSelected("காய்கறிகள்")
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        applyLocalFilter()
    }

    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
        _searchQuery.value = ""
        viewModelScope.launch {
            _isLoading.value = true
            _cropsState.value = emptyList()
            try {
                val crops = when (filter) {
                    "காய்கறிகள்" -> repository.getVegetablePrices()
                    "பழங்கள்" -> repository.getFruitPrices()
                    "தங்கம்" -> repository.getGoldPrices()
                    "முட்டை" -> repository.getEggPrices()
                    else -> repository.getMarketPrices()
                }
                _allCrops.value = crops
                _cropsState.value = crops
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun applyLocalFilter() {
        val query = _searchQuery.value.trim().lowercase()
        _cropsState.value = if (query.isEmpty()) {
            _allCrops.value
        } else {
            _allCrops.value.filter {
                it.name.lowercase().contains(query) || it.nameEng.lowercase().contains(query)
            }
        }
    }
}
