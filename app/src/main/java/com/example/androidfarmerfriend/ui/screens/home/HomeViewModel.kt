package com.example.androidfarmerfriend.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.data.api.NetworkErrors
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.location.LocationPrefs
import com.example.androidfarmerfriend.data.location.SelectedLocation
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.data.util.UserPrefs
import com.example.androidfarmerfriend.ui.components.loadWeatherState
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
    private var loadedOnce = false

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
        // Load data only the first time the screen is shown; returning to the tab
        // must not re-trigger a full refetch. Error retry is handled separately.
        if (loadedOnce) return
        loadedOnce = true
        val saved = locationPrefs.selectedLocation
        refreshGreeting()
        loadWeather(saved)
        loadMarketPreview(saved)
    }

    private fun selectLocation(location: SelectedLocation) {
        locationPrefs.selectedLocation = location
        loadWeather(location)
        loadMarketPreview(location)
    }

    /** Top vegetable rates near the selected location for the home strip. */
    private fun loadMarketPreview(location: SelectedLocation) {
        viewModelScope.launch {
            try {
                val slug = location.marketName.lowercase().replace(" ", "")
                val crops = repository.getVegetablePrices(slug)
                    .ifEmpty { repository.getVegetablePrices("koyambedu") }
                _state.value = _state.value.copy(marketPreview = crops.take(4))
            } catch (e: Exception) {
                // Preview is optional; home works without it — but still report the failure.
                NetworkErrors.record("HomeViewModel.loadMarketPreview", e)
            }
        }
    }

    private fun refreshGreeting() {
        val name = personalizedName(userPrefs.userName)
        _state.value = _state.value.copy(
            greetingName = name,
            greeting = if (name != null) "${strings.welcomeBack}, $name" else strings.welcomeBack
        )
    }

    private fun loadWeather(location: SelectedLocation) {
        _state.value = _state.value.copy(selectedLocation = location, weatherState = UiState.Loading)
        viewModelScope.launch {
            _state.value = _state.value.copy(weatherState = repository.loadWeatherState(location, strings))
        }
    }

    companion object {
        /** Placeholder names render no name in the greeting. */
        fun personalizedName(rawName: String): String? =
            rawName.trim().takeIf { !UserPrefs.isPlaceholderName(it) }
    }
}
