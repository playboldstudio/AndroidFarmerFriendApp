package com.example.androidfarmerfriend.ui.screens.auth

sealed interface AuthEvent {
    data class SetMode(val mode: AuthMode) : AuthEvent
    data class UpdateName(val value: String) : AuthEvent
    data class UpdatePhone(val value: String) : AuthEvent
    data class UpdateEmail(val value: String) : AuthEvent
    data class UpdatePassword(val value: String) : AuthEvent
    data object Submit : AuthEvent
}
