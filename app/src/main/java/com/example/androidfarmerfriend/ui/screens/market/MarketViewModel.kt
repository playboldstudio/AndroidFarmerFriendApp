package com.example.androidfarmerfriend.ui.screens.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.model.Crop
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FilterType(val id: String, val displayKey: (AppStrings) -> String) {
    VEGETABLES("vegetables", { it.vegetables }),
    FRUITS("fruits", { it.fruits }),
    NONVEG("nonveg", { it.nonVeg }),
    GOLD("gold", { it.gold }),
    EGG("egg", { it.egg });

    companion object {
        fun fromId(id: String): FilterType = entries.find { it.id == id } ?: VEGETABLES
    }
}

sealed interface MarketEvent {
    data class SelectFilter(val filter: FilterType) : MarketEvent
    data class Search(val query: String) : MarketEvent
    data class ChangeLocation(val marketName: String) : MarketEvent
    data object Retry : MarketEvent
}

data class MarketState(
    val cropsState: UiState<List<Crop>> = UiState.Loading,
    val selectedFilter: FilterType = FilterType.VEGETABLES,
    val searchQuery: String = "",
    val locationName: String = "Namakkal",
    val fetchDate: String = ""
) {
    val filteredCrops: List<Crop>
        get() {
            val data = (cropsState as? UiState.Success)?.data ?: return emptyList()
            val query = searchQuery.trim().lowercase()
            return if (query.isEmpty()) data
            else data.filter {
                it.name.lowercase().contains(query) || it.nameEng.lowercase().contains(query)
            }
        }
}

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
