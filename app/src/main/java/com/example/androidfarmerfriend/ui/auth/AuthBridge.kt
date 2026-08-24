package com.example.androidfarmerfriend.ui.auth

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Lightweight bridge exposing auth state and the contextual sign-in prompt
 * to any screen without threading callbacks through navigation.
 */
object AuthBridge {
    val LocalIsSignedIn = staticCompositionLocalOf { false }

    /** Requests sign-in; runs [onSuccess] once the user is authenticated. */
    val LocalRequestSignIn = staticCompositionLocalOf<(onSuccess: () -> Unit) -> Unit> { {} }
}
