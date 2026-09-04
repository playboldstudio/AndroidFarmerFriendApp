package com.example.androidfarmerfriend.ui.screens.schemes

sealed interface SchemeEvent {
    data class SelectFilter(val filter: SchemeFilterType) : SchemeEvent
    data class Search(val query: String) : SchemeEvent
    data class ToggleSave(val url: String) : SchemeEvent
    data object Retry : SchemeEvent
}
