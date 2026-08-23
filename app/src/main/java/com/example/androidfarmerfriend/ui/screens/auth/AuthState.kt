package com.example.androidfarmerfriend.ui.screens.auth

enum class AuthMode { SIGN_IN, SIGN_UP }

data class AuthState(
    val mode: AuthMode = AuthMode.SIGN_IN,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
