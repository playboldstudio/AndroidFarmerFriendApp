package com.example.androidfarmerfriend.ui.screens.market

import com.example.androidfarmerfriend.data.model.MarketOption

sealed interface MarketEvent {
    data class SelectFilter(val filter: FilterType) : MarketEvent
    data class Search(val query: String) : MarketEvent
    data class ChangeMarket(val market: MarketOption) : MarketEvent
    data class Sort(val order: SortOrder) : MarketEvent
    data object Retry : MarketEvent
}
