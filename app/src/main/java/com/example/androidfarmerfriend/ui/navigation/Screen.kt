package com.example.androidfarmerfriend.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Location : Screen("location")
    object Language : Screen("language")
    object Home : Screen("home")
    object Market : Screen("market")
    object Weather : Screen("weather")
    object Disease : Screen("disease")
    object Schemes : Screen("schemes")
    object Alerts : Screen("alerts")
    object Profile : Screen("profile")
    object CropNotes : Screen("crop_notes")
    object PrivacyPolicy : Screen("privacy_policy")
}
