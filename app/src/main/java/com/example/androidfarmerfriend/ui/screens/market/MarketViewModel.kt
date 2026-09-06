package com.example.androidfarmerfriend.ui.screens.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.data.api.NetworkErrors
import com.example.androidfarmerfriend.data.model.Crop
import com.example.androidfarmerfriend.data.model.MarketData
import com.example.androidfarmerfriend.data.model.MarketOption
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.data.util.orEmpty
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
                _state.value = rerender(
                    _state.value.copy(
                        selectedFilter = event.filter,
                        selectedMarket = newMarket
                    )
                )
                loadData(event.filter, newMarket)
            }
            is MarketEvent.Search -> {
                _state.value = rerender(_state.value.copy(searchQuery = event.query))
            }
            is MarketEvent.ChangeMarket -> {
                _state.value = rerender(_state.value.copy(selectedMarket = event.market))
                loadData(_state.value.selectedFilter, event.market)
            }
            is MarketEvent.Sort -> {
                _state.value = rerender(_state.value.copy(sortBy = event.order))
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
        _state.value = rerender(_state.value.copy(cropsState = UiState.Loading, searchQuery = ""))
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
                    // Both today and yesterday returned nothing (no exception thrown) —
                    // record a diagnostic so an empty market isn't invisible in Crashlytics.
                    if (crops.isEmpty()) {
                        NetworkErrors.record(
                            "MarketViewModel.emptyData",
                            IllegalStateException("No data for ${filter.id} at $slug (today+${effectiveDate})")
                        )
                    }
                }

                _state.value = rerender(
                    _state.value.copy(
                        cropsState = UiState.Success(crops),
                        fetchDate = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(
                            SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(effectiveDate)!!
                        )
                    )
                )
            } catch (e: Exception) {
                // Record the real exception+stack, then surface a friendly message.
                NetworkErrors.record("MarketViewModel.loadData", e)
                _state.value = rerender(
                    _state.value.copy(
                        cropsState = UiState.Error(NetworkErrors.friendlyMessage(e))
                    )
                )
            }
        }
    }

    /** Recompute the search/filter derived list once per state change instead of per read. */
    private fun rerender(s: MarketState): MarketState = s.copy(filteredCrops = computeFilteredCrops(s))

    private fun computeFilteredCrops(s: MarketState): List<Crop> {
        val data = s.cropsState.orEmpty()
        val query = s.searchQuery.trim().lowercase()
        val filtered = if (query.isEmpty()) data
        else data.filter {
            it.name.lowercase().contains(query) || it.nameEng.lowercase().contains(query)
        }
        return when (s.sortBy) {
            SortOrder.NAME_ASC -> filtered.sortedBy { it.nameEng.ifBlank { it.name } }
            SortOrder.NAME_DESC -> filtered.sortedByDescending { it.nameEng.ifBlank { it.name } }
            SortOrder.PRICE_LOW -> filtered.sortedBy { it.priceValue }
            SortOrder.PRICE_HIGH -> filtered.sortedByDescending { it.priceValue }
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
