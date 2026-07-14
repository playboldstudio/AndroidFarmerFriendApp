package com.example.androidfarmerfriend.ui.screens.alerts

import androidx.lifecycle.ViewModel
import com.example.androidfarmerfriend.data.model.Alert
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AlertsViewModel(private val repository: FarmerRepository = FarmerRepository()) : ViewModel() {
    private val _alertsState = MutableStateFlow<List<Alert>>(emptyList())
    val alertsState: StateFlow<List<Alert>> = _alertsState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _alertsState.value = repository.getAlerts()
    }
}
