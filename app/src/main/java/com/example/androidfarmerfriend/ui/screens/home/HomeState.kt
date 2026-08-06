package com.example.androidfarmerfriend.ui.screens.home

import com.example.androidfarmerfriend.data.location.SelectedLocation
import com.example.androidfarmerfriend.data.model.WeatherInfo
import com.example.androidfarmerfriend.data.util.UiState

data class HomeState(
    val weatherState: UiState<WeatherInfo> = UiState.Loading,
    val selectedLocation: SelectedLocation? = null
)
