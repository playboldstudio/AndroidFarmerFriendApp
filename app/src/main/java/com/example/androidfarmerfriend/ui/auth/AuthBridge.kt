package com.example.androidfarmerfriend.ui.auth

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.androidfarmerfriend.ui.screens.auth.AuthMode

/**
 * Lightweight bridge exposing auth state and the contextual sign-in prompt
 * to any screen without threading callbacks through navigation.
 */
object AuthBridge {
    val LocalIsSignedIn = staticCompositionLocalOf { false }

    /**
     * Requests authentication; [onSuccess] runs once the user is signed in.
     * [mode] pre-selects Sign In or Sign Up on the prompt.
     */
    val LocalRequestSignIn =
        staticCompositionLocalOf<(mode: com.example.androidfarmerfriend.ui.screens.auth.AuthMode, onSuccess: () -> Unit) -> Unit> { { _, _ -> } }
}
