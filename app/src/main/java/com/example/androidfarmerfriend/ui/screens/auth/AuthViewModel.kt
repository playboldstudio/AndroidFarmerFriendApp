package com.example.androidfarmerfriend.ui.screens.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

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
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Google sign-in failed")
            }
        }
    }

    private fun submit() {
        val current = _state.value
        if (current.isLoading) return

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(current.email).matches()) {
            _state.value = current.copy(error = "Enter a valid email address")
            return
        }
        if (current.password.length < 6) {
            _state.value = current.copy(error = "Password must be at least 6 characters")
            return
        }
        if (current.mode == AuthMode.SIGN_UP && current.name.isBlank()) {
            _state.value = current.copy(error = "Name cannot be empty")
            return
        }

        _state.value = current.copy(isLoading = true, error = null)
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
                    error = e.message ?: "Authentication failed"
                )
            }
        }
    }

    private suspend fun saveProfile(uid: String?, current: AuthState) {
        if (uid == null) return
        val phone = current.phone.filter { it.isDigit() }.take(10)
        firestore.collection("users")
            .document(uid)
            .set(
                hashMapOf(
                    "name" to current.name.trim(),
                    "phone" to phone,
                    "email" to current.email,
                    "createdAt" to System.currentTimeMillis()
                )
            )
            .await()
    }
}
