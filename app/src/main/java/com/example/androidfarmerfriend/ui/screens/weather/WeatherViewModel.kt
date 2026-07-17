package com.example.androidfarmerfriend.ui.screens.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.data.location.SelectedLocation
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(private val repository: FarmerRepository = FarmerRepository()) : ViewModel() {
    private val _state = MutableStateFlow(WeatherScreenState())
    val state: StateFlow<WeatherScreenState> = _state.asStateFlow()

    fun onEvent(event: WeatherEvent) {
        when (event) {
            is WeatherEvent.LoadLocation -> loadWeather(event.location)
            is WeatherEvent.Retry -> {
                val loc = _state.value.selectedLocation ?: return
                loadWeather(loc)
            }
        }
    }

    private fun loadWeather(location: SelectedLocation) {
        _state.value = _state.value.copy(selectedLocation = location, weatherState = UiState.Loading)
        viewModelScope.launch {
            try {
                val weather = repository.getWeather(location.lat, location.lon, location.name)
                _state.value = _state.value.copy(
                    weatherState = if (weather != null) UiState.Success(weather)
                    else UiState.Error("Weather data unavailable")
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    weatherState = UiState.Error(e.message ?: "Unable to load weather data")
                )
            }
        }
    }
}
