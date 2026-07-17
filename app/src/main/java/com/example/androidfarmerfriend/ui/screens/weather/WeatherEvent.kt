package com.example.androidfarmerfriend.ui.screens.weather

import com.example.androidfarmerfriend.data.location.SelectedLocation

sealed interface WeatherEvent {
    data class LoadLocation(val location: SelectedLocation) : WeatherEvent
    data object Retry : WeatherEvent
}
