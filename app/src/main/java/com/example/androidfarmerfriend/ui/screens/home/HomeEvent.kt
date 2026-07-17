package com.example.androidfarmerfriend.ui.screens.home

import com.example.androidfarmerfriend.data.location.SelectedLocation

sealed interface HomeEvent {
    data class LoadLocation(val location: SelectedLocation) : HomeEvent
    data object Retry : HomeEvent
}
