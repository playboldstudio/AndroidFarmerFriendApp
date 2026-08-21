package com.example.androidfarmerfriend.ui.screens.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.data.model.MarketData
import com.example.androidfarmerfriend.data.model.MarketOption
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
            is MarketEvent.SelectFilter -> {
                // Auto-switch to a supported market if current one doesn't support the new filter
                val currentMarket = _state.value.selectedMarket
                val newMarket = if (!currentMarket.supportsCategory(event.filter)) {
                    MarketData.defaultMarketForCategory(event.filter)
                } else {
                    currentMarket
                }
                _state.value = _state.value.copy(
                    selectedFilter = event.filter,
                    selectedMarket = newMarket
                )
                loadData(event.filter, newMarket)
            }
            is MarketEvent.Search -> {
                _state.value = _state.value.copy(searchQuery = event.query)
            }
            is MarketEvent.ChangeMarket -> {
                _state.value = _state.value.copy(selectedMarket = event.market)
                loadData(_state.value.selectedFilter, event.market)
            }
            is MarketEvent.Retry -> loadData(_state.value.selectedFilter, _state.value.selectedMarket)
        }
    }

    fun loadInitialData() {
        val filter = _state.value.selectedFilter
        val market = _state.value.selectedMarket
        loadData(filter, market)
    }

    private fun loadData(filter: FilterType, market: MarketOption) {
        _state.value = _state.value.copy(cropsState = UiState.Loading, searchQuery = "")
        viewModelScope.launch {
            try {
                val crops = when (filter) {
                    FilterType.VEGETABLES -> repository.getVegetablePrices(market.apiSlug)
                    FilterType.FRUITS -> repository.getFruitPrices(market.apiSlug)
                    FilterType.NONVEG -> repository.getNonVegPrices(market.apiSlug)
                    FilterType.GOLD -> repository.getGoldPrices(market.apiSlug)
                    FilterType.EGG -> repository.getEggPrices(market.apiSlug)
                }
                val date = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date())
                _state.value = _state.value.copy(
                    cropsState = UiState.Success(crops),
                    fetchDate = date
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    cropsState = UiState.Error(e.message ?: "Failed to load data")
                )
            }
        }
    }
}
