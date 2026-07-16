package com.example.androidfarmerfriend.ui.screens.alerts

import androidx.lifecycle.ViewModel
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.model.Alert
import com.example.androidfarmerfriend.data.model.AlertType
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AlertFilterType(val id: String, val displayKey: (AppStrings) -> String, val alertType: AlertType?) {
    ALL("all", { it.filterAll }, null),
    PRICE("price", { it.filterPriceAlert }, AlertType.PRICE),
    WEATHER("weather", { it.filterWeather }, AlertType.WEATHER),
    CROP("crop", { it.filterCrop }, AlertType.CROP);
}

sealed interface AlertEvent {
    data class SelectFilter(val filter: AlertFilterType) : AlertEvent
}

data class AlertsState(
    val alertsState: UiState<List<Alert>> = UiState.Loading,
    val selectedFilter: AlertFilterType = AlertFilterType.ALL
) {
    val filteredAlerts: List<Alert>
        get() {
            val data = (alertsState as? UiState.Success)?.data ?: return emptyList()
            if (selectedFilter == AlertFilterType.ALL) return data
            return data.filter { it.type == selectedFilter.alertType }
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
