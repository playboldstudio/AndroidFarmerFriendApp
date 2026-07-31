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
            is ProfileEvent.NavigateToTerms -> _navigation.value = "terms_of_use"

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
                        .replace("+91", "")
                        .replace("91", "")
                        .replace(" ", "")
                        .filter { it.isDigit() }
                )
            }
            is ProfileEvent.CancelEditing -> {
                _state.value = _state.value.copy(isEditing = false)
            }
            is ProfileEvent.SaveProfile -> {
                val rawPhone = _state.value.tempPhone.trim()
                    .removePrefix("+91").removePrefix("91")
                    .replace("\\s".toRegex(), "")
                val isValidPhone = rawPhone.length == 10 && rawPhone.all { it.isDigit() }

                if (_state.value.tempName.isBlank()) {
                    _state.value = _state.value.copy(
                        message = "Name cannot be empty"
                    )
                    return
                }
                if (rawPhone.isBlank()) {
                    _state.value = _state.value.copy(
                        phoneError = "Phone number is required"
                    )
                    return
                }
                if (!isValidPhone) {
                    _state.value = _state.value.copy(
                        phoneError = "Enter a valid 10-digit mobile number"
                    )
                    return
                }

                val cleanPhone = rawPhone
                userPrefs.userName = _state.value.tempName
                userPrefs.userPhone = cleanPhone
                _state.value = _state.value.copy(
                    isEditing = false,
                    userName = _state.value.tempName,
                    userPhone = cleanPhone,
                    phoneError = null
                )
                saveUserToFirestore(_state.value.tempName, cleanPhone)
            }
            is ProfileEvent.UpdateTempName -> {
                _state.value = _state.value.copy(tempName = event.name)
            }
            is ProfileEvent.UpdateTempPhone -> {
                // Auto-strip +91 or 91 prefix, spaces, and limit to 10 digits
                val cleaned = event.phone
                    .replace("+91", "")
                    .replace("91", "")
                    .replace(" ", "")
                    .filter { it.isDigit() }
                    .take(10)
                _state.value = _state.value.copy(
                    tempPhone = cleaned,
                    phoneError = null
                )
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
