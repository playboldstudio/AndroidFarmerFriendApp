package com.example.androidfarmerfriend.ui.screens.privacy

import androidx.compose.runtime.Composable

private const val PRIVACY_POLICY_URL =
    "https://doc-hosting.flycricket.io/farmer-friend-privacy-policy/b8e9b2d9-76fb-4e2c-a736-7647e5589687/privacy"

private val PRIVACY_SECTIONS = listOf(
    LegalSection(
        heading = "Overview",
        body = "Farmer Friend (\"we\", \"our\") helps farmers check market prices, weather, " +
            "alerts and farming information. This policy explains what data we collect and how we use it."
    ),
    LegalSection(
        heading = "Data We Collect",
        bullets = listOf(
            "Profile details you enter: name and mobile number.",
            "Selected location used for weather and market lookups.",
            "Firebase Cloud Messaging token, so alerts can be delivered to your device.",
            "Anonymous usage analytics and crash reports (Firebase Analytics & Crashlytics)."
        )
    ),
    LegalSection(
        heading = "How We Use Data",
        bullets = listOf(
            "Show prices and weather for your chosen markets and locations.",
            "Send price, weather and crop alerts through notifications.",
            "Improve app stability and fix crashes."
        )
    ),
    LegalSection(
        heading = "Permissions",
        body = "The app asks for the notification permission on Android 13+ so it can deliver " +
            "alerts. You can decline or revoke it at any time; the rest of the app keeps working."
    ),
    LegalSection(
        heading = "Data Sharing",
        body = "We do not sell your personal data. Data is stored with Google Firebase services " +
            "and shared only as needed to run those services."
    ),
    LegalSection(
        heading = "Data Retention & Deletion",
        body = "Profile data is kept while your profile exists. Uninstalling the app or contacting " +
            "us removes your profile record from our systems."
    )
)

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit = {}) {
    LegalContentScreen(
        title = "Privacy Policy",
        lastUpdated = "August 2026",
        sections = PRIVACY_SECTIONS,
        fullDocumentUrl = PRIVACY_POLICY_URL,
        onBack = onBack
    )
}
