package com.example.androidfarmerfriend.ui.screens.weather

import com.example.androidfarmerfriend.data.location.SelectedLocation

sealed interface WeatherEvent {
    data object LoadInitial : WeatherEvent
    data class SelectLocation(val location: SelectedLocation) : WeatherEvent
    data object Retry : WeatherEvent
    /** Pull-to-refresh: reload the current location regardless of the once-guard. */
    data object Refresh : WeatherEvent
}
