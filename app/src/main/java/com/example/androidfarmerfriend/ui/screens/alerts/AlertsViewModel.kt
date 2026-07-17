package com.example.androidfarmerfriend.ui.screens.alerts

import androidx.lifecycle.ViewModel
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AlertsViewModel(private val repository: FarmerRepository = FarmerRepository()) : ViewModel() {
    private val _state = MutableStateFlow(AlertsState())
    val state: StateFlow<AlertsState> = _state.asStateFlow()

    init {
        val alerts = repository.getAlerts()
        _state.value = AlertsState(alertsState = UiState.Success(alerts))
    }

    fun onEvent(event: AlertEvent) {
        when (event) {
            is AlertEvent.SelectFilter -> {
                _state.value = _state.value.copy(selectedFilter = event.filter)
            }
        }
    }
}
