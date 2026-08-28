package com.example.androidfarmerfriend.ui.screens.alerts

import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.model.Alert
import com.example.androidfarmerfriend.data.model.AlertType
import com.example.androidfarmerfriend.data.util.UiState

enum class AlertFilterType(val id: String, val displayKey: (AppStrings) -> String, val alertType: AlertType?) {
    ALL("all", { it.filterAll }, null),
    PRICE("price", { it.filterPriceAlert }, AlertType.PRICE),
    WEATHER("weather", { it.filterWeather }, AlertType.WEATHER),
    CROP("crop", { it.filterCrop }, AlertType.CROP);
}

data class AlertsState(
    val alertsState: UiState<List<Alert>> = UiState.Loading,
    val selectedFilter: AlertFilterType = AlertFilterType.ALL,
    val locationName: String = "",
    /** Derived in the ViewModel whenever alerts/filter change — not recomputed per read. */
    val filteredAlerts: List<Alert> = emptyList(),
    /** Derived in the ViewModel alongside [filteredAlerts]. */
    val unreadCount: Int = 0
)
