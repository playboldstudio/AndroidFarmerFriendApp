package com.example.androidfarmerfriend.ui.screens.profile

sealed interface ProfileEvent {
    data object NavigateToDetails : ProfileEvent
    data object NavigateToLands : ProfileEvent
    data object NavigateToLanguage : ProfileEvent
    data object NavigateToNotifications : ProfileEvent
    data object NavigateToPrivacy : ProfileEvent
    data object NavigateToTerms : ProfileEvent
    data object NavigateToSettings : ProfileEvent
    data object ConfirmLogout : ProfileEvent
    data object DismissLogout : ProfileEvent
    data object SignOut : ProfileEvent
    
    // Edit Profile Events
    data object StartEditing : ProfileEvent
    data object CancelEditing : ProfileEvent
    data object SaveProfile : ProfileEvent
    data class UpdateTempName(val name: String) : ProfileEvent
    data class UpdateTempPhone(val phone: String) : ProfileEvent
    
    data object DismissMessage : ProfileEvent
    data class SelectThemeMode(val mode: String) : ProfileEvent
}
