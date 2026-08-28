package com.example.androidfarmerfriend.ui.components

import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.location.SelectedLocation
import com.example.androidfarmerfriend.data.model.WeatherInfo
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.data.util.loadSafely

/**
 * Shared weather fetch used by both the Home strip and the Weather screen.
 * Loads, maps success/null to [UiState.Success]/[UiState.Error] and maps any
 * exception to [UiState.Error], so the two ViewModels keep only their Loading
 * + state-copy plumbing.
 */
suspend fun FarmerRepository.loadWeatherState(
    location: SelectedLocation,
    strings: AppStrings
): UiState<WeatherInfo> {
    val weather = loadSafely { getWeather(location.lat, location.lon, location.name, strings) }
    return if (weather is UiState.Success && weather.data != null) {
        UiState.Success(weather.data)
    } else {
        val fallback = strings.weatherLoadError
        val message = if (weather is UiState.Error) weather.message.ifBlank { fallback } else fallback
        UiState.Error(message)
    }
}