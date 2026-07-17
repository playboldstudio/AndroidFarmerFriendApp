package com.example.androidfarmerfriend.ui.screens.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MarketViewModel(private val repository: FarmerRepository = FarmerRepository()) : ViewModel() {
    private val _state = MutableStateFlow(MarketState())
    val state: StateFlow<MarketState> = _state.asStateFlow()

    fun onEvent(event: MarketEvent) {
        when (event) {
            is MarketEvent.SelectFilter -> loadData(event.filter)
            is MarketEvent.Search -> {
                _state.value = _state.value.copy(searchQuery = event.query)
            }
            is MarketEvent.ChangeLocation -> {
                _state.value = _state.value.copy(locationName = event.marketName)
                loadData(_state.value.selectedFilter)
            }
            is MarketEvent.Retry -> loadData(_state.value.selectedFilter)
        }
    }

    private fun loadData(filter: FilterType) {
        _state.value = _state.value.copy(selectedFilter = filter, cropsState = UiState.Loading, searchQuery = "")
        viewModelScope.launch {
            try {
                val location = _state.value.locationName.lowercase()
                val crops = when (filter) {
                    FilterType.VEGETABLES -> repository.getVegetablePrices(location)
                    FilterType.FRUITS -> repository.getFruitPrices(location)
                    FilterType.NONVEG -> repository.getNonVegPrices(location)
                    FilterType.GOLD -> repository.getGoldPrices(location)
                    FilterType.EGG -> repository.getEggPrices(location)
                }
                val date = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale("ta", "IN")).format(Date())
                _state.value = _state.value.copy(
                    cropsState = UiState.Success(crops),
                    fetchDate = date
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    cropsState = UiState.Error(e.message ?: "தரவுகளை ஏற்ற முடியவில்லை")
                )
            }
        }
    }
}
