package com.example.androidfarmerfriend.ui.screens.market

import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.model.Crop
import com.example.androidfarmerfriend.data.model.MarketData
import com.example.androidfarmerfriend.data.model.MarketOption
import com.example.androidfarmerfriend.data.util.UiState

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

enum class SortOrder(val label: (AppStrings) -> String) {
    NAME_ASC({ it.sortNameAZ }),
    NAME_DESC({ it.sortNameZA }),
    PRICE_LOW({ it.sortPriceLow }),
    PRICE_HIGH({ it.sortPriceHigh });
}

data class MarketState(
    val cropsState: UiState<List<Crop>> = UiState.Loading,
    val selectedFilter: FilterType = FilterType.VEGETABLES,
    val selectedMarket: MarketOption = MarketData.defaultMarketForCategory(FilterType.VEGETABLES),
    val searchQuery: String = "",
    val fetchDate: String = "",
    val sortBy: SortOrder = SortOrder.NAME_ASC,
    /** Derived in the ViewModel whenever search/filter/data change — not recomputed per read. */
    val filteredCrops: List<Crop> = emptyList()
)
