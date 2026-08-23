package com.example.androidfarmerfriend.ui.screens.profile

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.data.localization.LanguagePrefs
import com.example.androidfarmerfriend.data.location.LocationPrefs
import com.example.androidfarmerfriend.data.util.UserPrefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userPrefs = UserPrefs(application)
    private val languagePrefs = LanguagePrefs(application)
    private val locationPrefs = LocationPrefs(application)
    private val firestore = FirebaseFirestore.getInstance()

    private val fcmPrefs: SharedPreferences =
        application.getSharedPreferences("fcm_prefs", Application.MODE_PRIVATE)

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _navigation = MutableStateFlow<String?>(null)
    val navigation: StateFlow<String?> = _navigation.asStateFlow()

    private fun initialState(): ProfileState {
        val language = languagePrefs.selectedLanguage
        return ProfileState(
            userName = userPrefs.userName,
            userPhone = userPrefs.userPhone,
            displayName = displayName(userPrefs.userName, language),
            locationName = locationPrefs.selectedLocation.name,
            languageLabel = language.displayEnglish,
            selectedLanguage = language
        )
    }

    /** Placeholder names render as the localized "Guest" label. */
    private fun displayName(rawName: String, language: com.example.androidfarmerfriend.data.localization.Language): String {
        val strings = language.strings()
        return if (UserPrefs.isPlaceholderName(rawName)) strings.guestLabel else rawName.trim()
    }

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
                    tempName = placeholderAwareSavedName(),
                    tempPhone = digitsOnly(_state.value.userPhone)
                )
            }
            is ProfileEvent.CancelEditing -> {
                _state.value = _state.value.copy(isEditing = false)
            }
            is ProfileEvent.SaveProfile -> {
                saveProfile()
            }
            is ProfileEvent.UpdateTempName -> {
                _state.value = _state.value.copy(tempName = event.name)
            }
            is ProfileEvent.UpdateTempPhone -> {
                _state.value = _state.value.copy(
                    tempPhone = normalizePhone(event.phone),
                    phoneError = null
                )
            }
            is ProfileEvent.DismissMessage -> {
                _state.value = _state.value.copy(message = null)
            }
            is ProfileEvent.ConfirmLogout -> {
                _state.value = _state.value.copy(showLogoutDialog = true)
            }
            is ProfileEvent.DismissLogout -> {
                _state.value = _state.value.copy(showLogoutDialog = false)
            }
            is ProfileEvent.SignOut -> {
                _state.value = _state.value.copy(showLogoutDialog = false)
                FirebaseAuth.getInstance().signOut()
            }
        }
    }

    private fun placeholderAwareSavedName(): String {
        val saved = _state.value.userName
        return if (UserPrefs.isPlaceholderName(saved)) "" else saved
    }

    private fun saveProfile() {
        val current = _state.value
        val rawPhone = normalizePhone(current.tempPhone)

        if (current.tempName.isBlank()) {
            _state.value = current.copy(message = "Name cannot be empty")
            return
        }
        if (rawPhone.isBlank()) {
            _state.value = current.copy(phoneError = "Phone number is required")
            return
        }
        if (!isValidIndianMobile(rawPhone)) {
            _state.value = current.copy(phoneError = "Enter a valid 10-digit mobile number")
            return
        }

        userPrefs.userName = current.tempName.trim()
        userPrefs.userPhone = rawPhone

        _state.value = current.copy(
            isEditing = false,
            userName = current.tempName.trim(),
            userPhone = rawPhone,
            displayName = displayName(current.tempName.trim(), current.selectedLanguage),
            phoneError = null
        )
        saveUserToFirestore(current.tempName.trim(), rawPhone)
    }

    fun onNavigated() {
        _navigation.value = null
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

    companion object {
        private fun digitsOnly(value: String): String =
            value.filter { it.isDigit() }.take(10)

        private fun normalizePhone(input: String): String =
            input.replace("+91", "").replace("91", "", true)
                .replace("\\s".toRegex(), "")
                .filter { it.isDigit() }
                .take(10)

        private fun isValidIndianMobile(phone: String): Boolean =
            phone.length == 10 && phone.all { it.isDigit() }
    }
}
