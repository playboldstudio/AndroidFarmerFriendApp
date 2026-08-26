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
            selectedLanguage = language
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
                    tempEmail = _state.value.userEmail
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
                    tempEmail = _state.value.userEmail
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
            userEmail = current.userEmail,
            displayName = displayName(current.tempName.trim(), current.selectedLanguage),
            phoneError = null
        )
        saveUserToFirestore(current.tempName.trim(), rawPhone, current.userEmail)
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
            selectedLanguage = language
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
