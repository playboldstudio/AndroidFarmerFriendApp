package com.example.androidfarmerfriend.ui.screens.profile

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.data.localization.LanguagePrefs
import com.example.androidfarmerfriend.data.util.UserPrefs
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userPrefs = UserPrefs(application)
    private val languagePrefs = LanguagePrefs(application)
    private val firestore = FirebaseFirestore.getInstance()

    private val fcmPrefs: SharedPreferences =
        application.getSharedPreferences("fcm_prefs", Application.MODE_PRIVATE)

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
            is ProfileEvent.NavigateToPrivacy -> _navigation.value = "privacy_policy"

            is ProfileEvent.NavigateToDetails,
            is ProfileEvent.NavigateToLands,
            is ProfileEvent.NavigateToNotifications,
            is ProfileEvent.NavigateToSettings -> {
                _state.value = _state.value.copy(
                    message = _state.value.selectedLanguage.strings().comingSoon
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
                    saveUserToFirestore(_state.value.tempName, _state.value.tempPhone)
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

    private fun saveUserToFirestore(name: String, phone: String) {
        val token = fcmPrefs.getString("fcm_token", null) ?: return
        viewModelScope.launch {
            val userData = hashMapOf(
                "name" to name,
                "phone" to phone,
                "fcmToken" to token,
                "lastUpdated" to System.currentTimeMillis()
            )
            firestore.collection("users")
                .document(token)
                .set(userData)
        }
    }

    fun onNavigated() {
        _navigation.value = null
    }
}
