package com.example.androidfarmerfriend.ui.screens.market

sealed interface MarketEvent {
    data class SelectFilter(val filter: FilterType) : MarketEvent
    data class Search(val query: String) : MarketEvent
    data class ChangeLocation(val marketName: String) : MarketEvent
    data object Retry : MarketEvent
}
