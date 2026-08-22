package com.example.androidfarmerfriend.ui.screens.weather

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.location.LocationPrefs
import com.example.androidfarmerfriend.data.location.SelectedLocation
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FarmerRepository()
    private val locationPrefs = LocationPrefs(application)

    private val _state = MutableStateFlow(WeatherScreenState())
    val state: StateFlow<WeatherScreenState> = _state.asStateFlow()

    private var currentStrings: AppStrings = AppStrings.English

    fun setStrings(strings: AppStrings) {
        currentStrings = strings
    }

    /** Location search used by the location picker sheet. */
    suspend fun searchLocations(query: String): List<SelectedLocation> =
        repository.searchLocations(query)

    fun onEvent(event: WeatherEvent) {
        when (event) {
            is WeatherEvent.LoadInitial -> loadWeather(locationPrefs.selectedLocation)
            is WeatherEvent.SelectLocation -> {
                locationPrefs.selectedLocation = event.location
                loadWeather(event.location)
            }
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
                val weather = repository.getWeather(location.lat, location.lon, location.name, currentStrings)
                _state.value = _state.value.copy(
                    weatherState = if (weather != null) UiState.Success(weather)
                    else UiState.Error(currentStrings.weatherLoadError)
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    weatherState = UiState.Error(e.message ?: currentStrings.weatherLoadError)
                )
            }
        }
    }
}
