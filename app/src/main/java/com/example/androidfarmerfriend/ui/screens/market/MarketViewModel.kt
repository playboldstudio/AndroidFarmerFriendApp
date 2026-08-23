package com.example.androidfarmerfriend.ui.screens.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.data.model.Crop
import com.example.androidfarmerfriend.data.model.MarketData
import com.example.androidfarmerfriend.data.model.MarketOption
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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
                val slug = market.apiSlug
                val today = dateFormat().format(java.util.Date())
                var crops = fetchCrops(filter, slug, today)
                var effectiveDate = today

                // Markets publish late; fall back to yesterday's sheet when today is empty.
                if (crops.isEmpty()) {
                    val yesterdayCal = java.util.Calendar.getInstance()
                    yesterdayCal.add(java.util.Calendar.DATE, -1)
                    effectiveDate = dateFormat().format(yesterdayCal.time)
                    crops = fetchCrops(filter, slug, effectiveDate)
                }

                _state.value = _state.value.copy(
                    cropsState = UiState.Success(crops),
                    fetchDate = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(
                        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(effectiveDate)!!
                    )
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    cropsState = UiState.Error(e.message ?: "Failed to load data")
                )
            }
        }
    }

    private suspend fun fetchCrops(filter: FilterType, slug: String, date: String): List<Crop> =
        when (filter) {
            FilterType.VEGETABLES -> repository.getVegetablePrices(slug, date)
            FilterType.FRUITS -> repository.getFruitPrices(slug, date)
            FilterType.NONVEG -> repository.getNonVegPrices(slug, date)
            FilterType.GOLD -> repository.getGoldPrices(slug, date)
            FilterType.EGG -> repository.getEggPrices(slug)
        }

    private fun dateFormat() = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
}
