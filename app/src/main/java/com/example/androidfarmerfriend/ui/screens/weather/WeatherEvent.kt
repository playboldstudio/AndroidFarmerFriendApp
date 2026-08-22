package com.example.androidfarmerfriend.ui.screens.weather

import com.example.androidfarmerfriend.data.location.SelectedLocation

sealed interface WeatherEvent {
    data object LoadInitial : WeatherEvent
    data class SelectLocation(val location: SelectedLocation) : WeatherEvent
    data object Retry : WeatherEvent
}
