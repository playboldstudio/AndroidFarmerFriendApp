package com.example.androidfarmerfriend.ui.screens.weather

import com.example.androidfarmerfriend.data.location.SelectedLocation
import com.example.androidfarmerfriend.data.model.WeatherInfo
import com.example.androidfarmerfriend.data.util.UiState

data class WeatherScreenState(
    val weatherState: UiState<WeatherInfo> = UiState.Loading,
    val selectedLocation: SelectedLocation? = null
)
