package com.example.androidfarmerfriend.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProfileEvent {
    data object NavigateToDetails : ProfileEvent
    data object NavigateToLands : ProfileEvent
    data object NavigateToLanguage : ProfileEvent
    data object NavigateToNotifications : ProfileEvent
    data object NavigateToPrivacy : ProfileEvent
    data object NavigateToSettings : ProfileEvent
    data object ShowLogoutDialog : ProfileEvent
    data object DismissLogoutDialog : ProfileEvent
    data object ConfirmLogout : ProfileEvent
}

data class ProfileState(
    val showLogoutDialog: Boolean = false,
    val userName: String = "விவசாயி",
    val userPhone: String = "+91 98765 43210",
    val languageDisplay: String = "தமிழ்"
)

class ProfileViewModel : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _navigation = MutableStateFlow<String?>(null)
    val navigation: StateFlow<String?> = _navigation.asStateFlow()

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.NavigateToDetails -> _navigation.value = "details"
            is ProfileEvent.NavigateToLands -> _navigation.value = "lands"
            is ProfileEvent.NavigateToLanguage -> _navigation.value = "language"
            is ProfileEvent.NavigateToNotifications -> _navigation.value = "notifications"
            is ProfileEvent.NavigateToPrivacy -> _navigation.value = "privacy"
            is ProfileEvent.NavigateToSettings -> _navigation.value = "settings"
            is ProfileEvent.ShowLogoutDialog -> _state.value = _state.value.copy(showLogoutDialog = true)
            is ProfileEvent.DismissLogoutDialog -> _state.value = _state.value.copy(showLogoutDialog = false)
            is ProfileEvent.ConfirmLogout -> {
                _state.value = _state.value.copy(showLogoutDialog = false)
            }
        }
    }

    fun onNavigated() {
        _navigation.value = null
    }
}
