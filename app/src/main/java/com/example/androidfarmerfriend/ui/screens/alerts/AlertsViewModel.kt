package com.example.androidfarmerfriend.ui.screens.alerts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.alerts.AlertSeedData
import com.example.androidfarmerfriend.data.location.LocationPrefs
import com.example.androidfarmerfriend.data.repository.FirestoreAlertRepository
import com.example.androidfarmerfriend.data.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlertsViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(
        AlertsState(locationName = LocationPrefs(application).selectedLocation.name)
    )
    val state: StateFlow<AlertsState> = _state.asStateFlow()

    private val repository = FirestoreAlertRepository.getInstance()

    init {
        // Use only the realtime listener — it fires immediately with current data
        // and on every subsequent update, avoiding the duplicate load race condition
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
                // Restart the realtime listener to get fresh data
                repository.stopListening()
                startRealtimeListener()
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
            is AlertEvent.MarkAllRead -> {
                val current = (_state.value.alertsState as? UiState.Success)?.data ?: return
                viewModelScope.launch {
                    try {
                        repository.markAllAsRead(current.filter { !it.isRead }.map { it.id })
                    } catch (_: Exception) {
                        // Realtime listener will reconcile; nothing user-facing to do.
                    }
                    // Optimistic local update so the badge drops immediately.
                    _state.value = _state.value.copy(
                        alertsState = UiState.Success(current.map { it.copy(isRead = true) })
                    )
                }
            }
        }
    }

    private var hasSeeded = false

    private fun startRealtimeListener() {
        repository.listenForAlerts(limit = 30) { alerts ->
            if (alerts.isEmpty() && !hasSeeded) {
                hasSeeded = true
                viewModelScope.launch {
                    try {
                        AlertSeedData.seedAlerts()
                    } catch (_: Exception) {}
                }
            }
            _state.value = _state.value.copy(
                alertsState = UiState.Success(alerts)
            )
        }
    }
}
