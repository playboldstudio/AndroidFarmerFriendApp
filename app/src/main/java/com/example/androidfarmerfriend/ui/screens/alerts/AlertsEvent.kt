package com.example.androidfarmerfriend.ui.screens.alerts

sealed interface AlertEvent {
    data class SelectFilter(val filter: AlertFilterType) : AlertEvent
    data object Refresh : AlertEvent
    data class MarkRead(val alertId: String) : AlertEvent
}
