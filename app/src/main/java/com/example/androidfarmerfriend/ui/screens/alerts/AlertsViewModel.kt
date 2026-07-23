package com.example.androidfarmerfriend.ui.screens.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.alerts.AlertSeedData
import com.example.androidfarmerfriend.data.repository.FirestoreAlertRepository
import com.example.androidfarmerfriend.data.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlertsViewModel : ViewModel() {
    private val _state = MutableStateFlow(AlertsState())
    val state: StateFlow<AlertsState> = _state.asStateFlow()

    private val repository = FirestoreAlertRepository.getInstance()

    init {
        loadAlerts()
        startRealtimeListener()
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopListening()
    }

    fun onEvent(event: AlertEvent) {
        when (event) {
            is AlertEvent.SelectFilter -> {
                _state.value = _state.value.copy(selectedFilter = event.filter)
            }
            is AlertEvent.Refresh -> {
                loadAlerts()
            }
            is AlertEvent.MarkRead -> {
                viewModelScope.launch {
                    repository.markAsRead(event.alertId)
                    val current = (_state.value.alertsState as? UiState.Success)?.data ?: return@launch
                    _state.value = _state.value.copy(
                        alertsState = UiState.Success(
                            current.map { if (it.id == event.alertId) it.copy(isRead = true) else it }
                        )
                    )
                }
            }
        }
    }

    private fun startRealtimeListener() {
        repository.listenForAlerts(limit = 30) { alerts ->
            _state.value = _state.value.copy(
                alertsState = UiState.Success(alerts)
            )
        }
    }

    private fun loadAlerts() {
        _state.value = _state.value.copy(alertsState = UiState.Loading)
        viewModelScope.launch {
            try {
                val alerts = repository.getAlerts(limit = 30)
                if (alerts.isEmpty()) {
                    try {
                        AlertSeedData.seedAlerts()
                        val seeded = repository.getAlerts(limit = 30)
                        _state.value = _state.value.copy(
                            alertsState = UiState.Success(seeded)
                        )
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            alertsState = UiState.Success(emptyList())
                        )
                    }
                } else {
                    _state.value = _state.value.copy(
                        alertsState = UiState.Success(alerts)
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    alertsState = UiState.Error(e.message ?: "Failed to load alerts")
                )
            }
        }
    }
}
