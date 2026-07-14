package com.example.androidfarmerfriend.ui.screens.home

import androidx.lifecycle.ViewModel
import com.example.androidfarmerfriend.data.model.WeatherInfo
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(private val repository: FarmerRepository = FarmerRepository()) : ViewModel() {
    private val _weatherState = MutableStateFlow<WeatherInfo?>(null)
    val weatherState: StateFlow<WeatherInfo?> = _weatherState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _weatherState.value = repository.getWeather()
    }
}
