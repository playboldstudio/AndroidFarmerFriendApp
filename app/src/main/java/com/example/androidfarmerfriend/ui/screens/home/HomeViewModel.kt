package com.example.androidfarmerfriend.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.location.LocationPrefs
import com.example.androidfarmerfriend.data.location.SelectedLocation
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.data.util.UserPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FarmerRepository()
    private val locationPrefs = LocationPrefs(application)
    private val userPrefs = UserPrefs(application)

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private var strings: AppStrings = AppStrings.English

    fun setStrings(strings: AppStrings) {
        this.strings = strings
        refreshGreeting()
    }

    /** Location search used by the location picker sheet. */
    suspend fun searchLocations(query: String): List<SelectedLocation> =
        repository.searchLocations(query)

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.LoadInitial -> loadInitial()
            is HomeEvent.SelectLocation -> selectLocation(event.location)
            is HomeEvent.Retry -> {
                val loc = _state.value.selectedLocation ?: return
                loadWeather(loc)
            }
        }
    }

    private fun loadInitial() {
        val saved = locationPrefs.selectedLocation
        refreshGreeting()
        loadWeather(saved)
    }

    private fun selectLocation(location: SelectedLocation) {
        locationPrefs.selectedLocation = location
        loadWeather(location)
    }

    private fun refreshGreeting() {
        val name = personalizedName(userPrefs.userName)
        _state.value = _state.value.copy(
            greetingName = name,
            greeting = if (name != null) "${strings.welcomeBack}, $name 👋" else "${strings.welcomeBack} 👋"
        )
    }

    private fun loadWeather(location: SelectedLocation) {
        _state.value = _state.value.copy(selectedLocation = location, weatherState = UiState.Loading)
        viewModelScope.launch {
            try {
                val weather = repository.getWeather(location.lat, location.lon, location.name, strings)
                _state.value = _state.value.copy(
                    weatherState = if (weather != null) UiState.Success(weather)
                    else UiState.Error(strings.weatherLoadError)
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    weatherState = UiState.Error(e.message ?: strings.weatherLoadError)
                )
            }
        }
    }

    companion object {
        /** Placeholder names render no name in the greeting. */
        fun personalizedName(rawName: String): String? =
            rawName.trim().takeIf { !UserPrefs.isPlaceholderName(it) }
    }
}
