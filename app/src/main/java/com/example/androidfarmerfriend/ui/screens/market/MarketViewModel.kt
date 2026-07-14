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

    private val _selectedFilter = MutableStateFlow("காய்கறிகள்")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val crops = repository.getMarketPrices()
            _allCrops.value = crops
            applyFilter(_selectedFilter.value)
        }
    }

    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
        applyFilter(filter)
    }

    private fun applyFilter(filter: String) {
        viewModelScope.launch {
            val all = _allCrops.value
            val category = when (filter) {
                "காய்கறிகள்" -> "vegetable"
                "பழங்கள்" -> "fruit"
                "தானியங்கள்" -> "grain"
                "முட்டை" -> "egg"
                else -> null
            }
            if (category == "egg") {
                val eggs = repository.getEggPrices()
                _cropsState.value = eggs
            } else if (category != null) {
                _cropsState.value = all.filter { it.category == category }
            } else {
                _cropsState.value = all
            }
        }
    }
}
