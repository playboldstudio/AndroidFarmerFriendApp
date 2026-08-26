package com.example.androidfarmerfriend.ui.screens.weather

import com.example.androidfarmerfriend.data.location.SelectedLocation
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.data.model.WeatherInfo

data class WeatherScreenState(
    val selectedLocation: SelectedLocation? = null,
    val weatherState: UiState<WeatherInfo> = UiState.Loading
)
