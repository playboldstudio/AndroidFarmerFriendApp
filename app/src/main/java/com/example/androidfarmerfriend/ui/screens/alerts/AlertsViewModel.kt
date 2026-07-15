package com.example.androidfarmerfriend.ui.screens.alerts

import androidx.lifecycle.ViewModel
import com.example.androidfarmerfriend.data.model.Alert
import com.example.androidfarmerfriend.data.model.AlertType
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface AlertEvent {
    data class SelectFilter(val filter: String) : AlertEvent
}

data class AlertsState(
    val alertsState: UiState<List<Alert>> = UiState.Loading,
    val selectedFilter: String = "அனைத்து"
) {
    val filteredAlerts: List<Alert>
        get() {
            val data = (alertsState as? UiState.Success)?.data ?: return emptyList()
            if (selectedFilter == "அனைத்து") return data
            val type = when (selectedFilter) {
                "விலை அலர்ட்" -> AlertType.PRICE
                "வானிலை" -> AlertType.WEATHER
                "பயிர்" -> AlertType.CROP
                else -> return data
            }
            return data.filter { it.type == type }
        }
}

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
