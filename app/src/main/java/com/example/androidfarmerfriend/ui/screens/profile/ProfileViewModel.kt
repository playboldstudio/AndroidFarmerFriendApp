package com.example.androidfarmerfriend.ui.screens.profile

import android.app.Application
import android.content.SharedPreferences
import android.os.Build
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
import kotlinx.coroutines.tasks.await

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userPrefs = UserPrefs(application)
    private val languagePrefs = LanguagePrefs(application)
    private val locationPrefs = LocationPrefs(application)
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val fcmPrefs: SharedPreferences =
        application.getSharedPreferences("fcm_prefs", Application.MODE_PRIVATE)

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _navigation = MutableStateFlow<String?>(null)
    val navigation: StateFlow<String?> = _navigation.asStateFlow()

    init {
        // When the user is signed in, fetch their profile from Firestore.
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            fetchUserProfile(firebaseUser.uid)
            // Also cache email from Firebase Auth if local is empty.
            val authEmail = firebaseUser.email ?: ""
            if (_state.value.userEmail.isBlank() && authEmail.isNotBlank()) {
                _state.value = _state.value.copy(userEmail = authEmail)
                userPrefs.userEmail = authEmail
            }
        }
    }

    private fun initialState(): ProfileState {
        val language = languagePrefs.selectedLanguage
        return ProfileState(
            userName = userPrefs.userName,
            userPhone = userPrefs.userPhone,
            userEmail = userPrefs.userEmail,
            displayName = displayName(userPrefs.userName, language),
            locationName = locationPrefs.selectedLocation.name,
            languageLabel = language.displayEnglish,
            selectedLanguage = language,
            themeMode = userPrefs.themeMode
        )
    }

    /** Fetch user profile from Firestore and update local state. */
    private fun fetchUserProfile(uid: String) {
        viewModelScope.launch {
            try {
                val doc = firestore.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    val name = doc.getString("name") ?: ""
                    val phone = doc.getString("phone") ?: ""
                    val email = doc.getString("email") ?: ""
                    val language = _state.value.selectedLanguage

                    // Update local prefs if Firestore has data
                    if (name.isNotBlank() && !UserPrefs.isPlaceholderName(name)) {
                        userPrefs.userName = name
                    }
                    if (phone.isNotBlank()) {
                        userPrefs.userPhone = phone
                    }
                    if (email.isNotBlank()) {
                        userPrefs.userEmail = email
                    }

                    _state.value = _state.value.copy(
                        userName = userPrefs.userName,
                        userPhone = userPrefs.userPhone,
                        userEmail = userPrefs.userEmail,
                        displayName = displayName(userPrefs.userName, language)
                    )
                }
            } catch (_: Exception) {
                // Firestore fetch failed — fall back to local prefs.
            }
        }
    }

    /** Placeholder names render as the localized "Guest" label. */
    private fun displayName(rawName: String, language: com.example.androidfarmerfriend.data.localization.Language): String {
        val strings = language.strings()
        return if (UserPrefs.isPlaceholderName(rawName)) strings.guestLabel else rawName.trim()
    }

    /** Deep-link to the app's system notification settings page. */
    private fun openSystemNotificationSettings() {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getApplication<Application>().packageName)
            } else {
                @Suppress("DEPRECATION")
                android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(android.net.Uri.fromParts("package", getApplication<Application>().packageName, null))
            }
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            getApplication<Application>().startActivity(intent)
        } catch (_: Exception) {
            // No settings activity available on this device — stay silent.
        }
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.NavigateToLanguage -> _navigation.value = "language"
            is ProfileEvent.NavigateToPrivacy -> _navigation.value = "privacy_policy"
            is ProfileEvent.NavigateToTerms -> _navigation.value = "terms_of_use"

            // My Details opens the existing inline edit form; Notifications
            // jumps to the system notification settings (matching its "On"
            // trailing label). Lands has no backend yet — honest snackbar.
            is ProfileEvent.NavigateToDetails -> {
                _state.value = _state.value.copy(
                    isEditing = true,
                    tempName = placeholderAwareSavedName(),
                    tempPhone = digitsOnly(_state.value.userPhone),
                    tempEmail = _state.value.userEmail,
                    nameError = null,
                    phoneError = null
                )
            }
            is ProfileEvent.NavigateToNotifications -> {
                openSystemNotificationSettings()
            }
            is ProfileEvent.NavigateToLands,
            is ProfileEvent.NavigateToSettings -> {
                _state.value = _state.value.copy(
                    message = _state.value.selectedLanguage.strings().comingSoon
                )
            }

            is ProfileEvent.StartEditing -> {
                _state.value = _state.value.copy(
                    isEditing = true,
                    tempName = placeholderAwareSavedName(),
                    tempPhone = digitsOnly(_state.value.userPhone),
                    tempEmail = _state.value.userEmail,
                    nameError = null,
                    phoneError = null
                )
            }
            is ProfileEvent.CancelEditing -> {
                _state.value = _state.value.copy(
                    isEditing = false,
                    nameError = null,
                    phoneError = null
                )
            }
            is ProfileEvent.SaveProfile -> {
                saveProfile()
            }
            is ProfileEvent.UpdateTempName -> {
                _state.value = _state.value.copy(
                    tempName = event.name.take(NAME_MAX_LENGTH),
                    nameError = null
                )
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
                // Clear the locally cached profile so the Home greeting and Profile
                // revert to the guest/placeholder state instead of the signed-out
                // user's real name. Firebase auth is separate from UserPrefs; without
                // this the name survives logout.
                userPrefs.userName = UserPrefs.DEFAULT_NAME
                userPrefs.userPhone = UserPrefs.DEFAULT_PHONE
                userPrefs.userEmail = UserPrefs.DEFAULT_EMAIL
            }
            is ProfileEvent.SelectThemeMode -> {
                userPrefs.themeMode = event.mode
                _state.value = _state.value.copy(themeMode = event.mode)
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
        val strings = current.selectedLanguage.strings()

        val trimmedName = current.tempName.trim()
        if (trimmedName.isBlank()) {
            _state.value = current.copy(nameError = strings.nameError)
            return
        }
        if (rawPhone.isBlank()) {
            _state.value = current.copy(phoneError = strings.phoneRequiredError)
            return
        }
        if (!isValidIndianMobile(rawPhone)) {
            _state.value = current.copy(phoneError = strings.mobileInvalidError)
            return
        }

        userPrefs.userName = trimmedName
        userPrefs.userPhone = rawPhone

        _state.value = current.copy(
            isEditing = false,
            userName = trimmedName,
            userPhone = rawPhone,
            userEmail = current.userEmail,
            displayName = displayName(trimmedName, current.selectedLanguage),
            nameError = null,
            phoneError = null
        )
        saveUserToFirestore(trimmedName, rawPhone, current.userEmail)
    }

    fun onNavigated() {
        _navigation.value = null
    }

    /** Re-read local prefs + Firestore so the UI reflects any changes made outside this VM. */
    fun refresh() {
        val language = languagePrefs.selectedLanguage
        val freshState = initialState()
        _state.value = freshState.copy(
            displayName = displayName(freshState.userName, language),
            languageLabel = language.displayEnglish,
            selectedLanguage = language,
            themeMode = userPrefs.themeMode
        )
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            fetchUserProfile(firebaseUser.uid)
        }
    }

    private fun saveUserToFirestore(name: String, phone: String, email: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val userData = hashMapOf(
                "name" to name,
                "phone" to phone,
                "email" to email,
                "lastUpdated" to System.currentTimeMillis()
            )
            firestore.collection("users")
                .document(uid)
                .set(userData, com.google.firebase.firestore.SetOptions.merge())
                .await()
        }
    }

    companion object {
        /** Hard cap for the display name — enforced on input and before save. */
        const val NAME_MAX_LENGTH = 30

        private fun digitsOnly(value: String): String =
            value.filter { it.isDigit() }.take(10)

        /** Keep only digits; drop a leading +91 / 91 country code exactly once.
         *  A stray "91" inside the number (e.g. 9141234567) is preserved — the old
         *  global replace corrupted valid numbers that start with 91. */
        private fun normalizePhone(input: String): String {
            var digits = input.filter { it.isDigit() }.take(12)
            if (digits.length == 12 && digits.startsWith("91")) digits = digits.drop(2)
            else if (digits.length == 11 && digits.startsWith("91")) digits = digits.drop(2)
            return digits.take(10)
        }

        private fun isValidIndianMobile(phone: String): Boolean =
            phone.length == 10 && phone.all { it.isDigit() }
    }
}
