package com.example.androidfarmerfriend.ui.screens.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.util.UserPrefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val userPrefs = UserPrefs(application)

    private var strings: AppStrings = AppStrings.English

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    /** Called from the composable so error copy follows the app language. */
    fun setStrings(value: AppStrings) {
        strings = value
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.SetMode -> _state.value = _state.value.copy(mode = event.mode, error = null)
            is AuthEvent.UpdateName -> _state.value = _state.value.copy(name = event.value)
            is AuthEvent.UpdatePhone -> _state.value = _state.value.copy(phone = event.value)
            is AuthEvent.UpdateEmail -> _state.value = _state.value.copy(email = event.value.trim())
            is AuthEvent.UpdatePassword -> _state.value = _state.value.copy(password = event.value)
            is AuthEvent.SetError -> _state.value = _state.value.copy(error = event.message)
            is AuthEvent.GoogleSignedIn -> signInWithGoogle(event.idToken)
            is AuthEvent.Submit -> submit()
            is AuthEvent.ForgotPassword -> sendPasswordReset()
        }
    }

    private fun signInWithGoogle(idToken: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val user = result.user
                if (user != null) {
                    val name = user.displayName ?: ""
                    val email = user.email ?: ""

                    // Persist locally so ProfileViewModel reads correct values immediately.
                    userPrefs.userName = name
                    userPrefs.userEmail = email

                    // Ensure a profile doc exists for first-time Google users.
                    firestore.collection("users").document(user.uid)
                        .set(
                            hashMapOf(
                                "name" to (user.displayName ?: ""),
                                "email" to (user.email ?: ""),
                                "createdAt" to System.currentTimeMillis()
                            ),
                            com.google.firebase.firestore.SetOptions.merge()
                        )
                        .await()
                }
                _state.value = _state.value.copy(isLoading = false, error = null)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: strings.googleSignInFailed)
            }
        }
    }

    /** Sends a Firebase password-reset email for the entered address. */
    private fun sendPasswordReset() {
        val current = _state.value
        if (current.isLoading) return
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(current.email).matches()) {
            _state.value = current.copy(error = strings.invalidEmailError)
            return
        }
        _state.value = current.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(current.email).await()
                // Show where to look next; the user stays on the sign-in form.
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = null,
                    info = strings.passwordResetSent
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = mapAuthError(e))
            }
        }
    }

    private fun submit() {
        val current = _state.value
        if (current.isLoading) return

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(current.email).matches()) {
            _state.value = current.copy(error = strings.invalidEmailError)
            return
        }
        if (current.password.length < 6) {
            _state.value = current.copy(error = strings.shortPasswordError)
            return
        }
        if (current.mode == AuthMode.SIGN_UP && current.name.isBlank()) {
            _state.value = current.copy(error = strings.emptyNameError)
            return
        }

        _state.value = current.copy(isLoading = true, error = null, info = null)
        viewModelScope.launch {
            try {
                when (current.mode) {
                    AuthMode.SIGN_IN -> auth.signInWithEmailAndPassword(current.email, current.password).await()
                    AuthMode.SIGN_UP -> {
                        val user = auth.createUserWithEmailAndPassword(current.email, current.password).await().user
                        saveProfile(user?.uid, current)
                    }
                }
                // Success flips MainScreen gating via the auth-state listener.
                _state.value = _state.value.copy(isLoading = false, error = null)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = mapAuthError(e)
                )
            }
        }
    }

    /** Maps raw Firebase exceptions to short, human-friendly messages. */
    private fun mapAuthError(e: Exception): String = when (e) {
        is FirebaseAuthInvalidUserException -> strings.noAccountError
        is FirebaseAuthInvalidCredentialsException -> strings.wrongPasswordError
        is FirebaseAuthUserCollisionException -> strings.emailInUseError
        is FirebaseAuthWeakPasswordException -> e.reason ?: strings.shortPasswordError
        else -> e.message?.takeIf { it.isNotBlank() } ?: strings.genericAuthError
    }

    private suspend fun saveProfile(uid: String?, current: AuthState) {
        if (uid == null) return
        val phone = current.phone.filter { it.isDigit() }.take(10)
        val name = current.name.trim()

        // Persist locally so ProfileViewModel reads the correct values immediately.
        userPrefs.userName = name
        userPrefs.userPhone = phone
        userPrefs.userEmail = current.email

        // Also persist to Firestore.
        firestore.collection("users")
            .document(uid)
            .set(
                hashMapOf(
                    "name" to name,
                    "phone" to phone,
                    "email" to current.email,
                    "createdAt" to System.currentTimeMillis()
                )
            )
            .await()
    }
}
