package com.example.androidfarmerfriend.ui.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.androidfarmerfriend.data.localization.LanguagePrefs
import com.example.androidfarmerfriend.data.util.UserPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userPrefs = UserPrefs(application)
    private val languagePrefs = LanguagePrefs(application)

    private val _state = MutableStateFlow(
        ProfileState(
            userName = userPrefs.userName,
            userPhone = userPrefs.userPhone,
            selectedLanguage = languagePrefs.selectedLanguage
        )
    )
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _navigation = MutableStateFlow<String?>(null)
    val navigation: StateFlow<String?> = _navigation.asStateFlow()

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.NavigateToLanguage -> _navigation.value = "language"
            
            is ProfileEvent.NavigateToDetails,
            is ProfileEvent.NavigateToLands,
            is ProfileEvent.NavigateToNotifications,
            is ProfileEvent.NavigateToPrivacy,
            is ProfileEvent.NavigateToSettings -> {
                val isTamil = _state.value.selectedLanguage == com.example.androidfarmerfriend.data.localization.Language.TAMIL
                _state.value = _state.value.copy(
                    message = if (isTamil) "இந்த அம்சம் விரைவில் வரும்" else "This feature is coming soon"
                )
            }
            
            is ProfileEvent.StartEditing -> {
                _state.value = _state.value.copy(
                    isEditing = true,
                    tempName = _state.value.userName,
                    tempPhone = _state.value.userPhone
                )
            }
            is ProfileEvent.CancelEditing -> {
                _state.value = _state.value.copy(isEditing = false)
            }
            is ProfileEvent.SaveProfile -> {
                if (_state.value.tempName.isNotBlank() && _state.value.tempPhone.isNotBlank()) {
                    userPrefs.userName = _state.value.tempName
                    userPrefs.userPhone = _state.value.tempPhone
                    _state.value = _state.value.copy(
                        isEditing = false,
                        userName = _state.value.tempName,
                        userPhone = _state.value.tempPhone
                    )
                }
            }
            is ProfileEvent.UpdateTempName -> {
                _state.value = _state.value.copy(tempName = event.name)
            }
            is ProfileEvent.UpdateTempPhone -> {
                _state.value = _state.value.copy(tempPhone = event.phone)
            }
            is ProfileEvent.DismissMessage -> {
                _state.value = _state.value.copy(message = null)
            }
        }
    }

    fun onNavigated() {
        _navigation.value = null
    }
}
