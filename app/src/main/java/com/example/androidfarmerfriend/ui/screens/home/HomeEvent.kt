package com.example.androidfarmerfriend.ui.screens.home

import com.example.androidfarmerfriend.data.location.SelectedLocation

sealed interface HomeEvent {
    data object LoadInitial : HomeEvent
    data class SelectLocation(val location: SelectedLocation) : HomeEvent
    data object Retry : HomeEvent
}
