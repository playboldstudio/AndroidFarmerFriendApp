package com.example.androidfarmerfriend.ui.screens.privacy

import androidx.compose.runtime.Composable

private const val TERMS_OF_USE_URL =
    "https://doc-hosting.flycricket.io/farmer-friend-terms-of-use/63df81a9-6229-4119-ae1d-149e7b5de893/terms"

private val TERMS_SECTIONS = listOf(
    LegalSection(
        heading = "Acceptance",
        body = "By using Farmer Friend you agree to these terms. If you do not agree, please do not use the app."
    ),
    LegalSection(
        heading = "Information Only, Not Advice",
        body = "Prices, weather and farming content are provided for general information. Market rates " +
            "and forecasts can be incomplete or delayed. Always confirm with local authorities or buyers " +
            "before making financial decisions."
    ),
    LegalSection(
        heading = "Acceptable Use",
        bullets = listOf(
            "Use the app only for lawful purposes.",
            "Do not attempt to disrupt, reverse-engineer or overload the service."
        )
    ),
    LegalSection(
        heading = "Intellectual Property",
        body = "The app, its design and its content are owned by us or our licensors and may not be " +
            "copied without permission."
    ),
    LegalSection(
        heading = "Availability & Changes",
        body = "Features depend on third-party data sources and internet connectivity. We may change " +
            "or discontinue parts of the service at any time."
    ),
    LegalSection(
        heading = "Limitation of Liability",
        body = "To the maximum extent permitted by law, we are not liable for losses arising from the " +
            "use of, or reliance on, information in this app."
    )
)

@Composable
fun TermsOfUseScreen(onBack: () -> Unit = {}) {
    LegalContentScreen(
        title = "Terms of Use",
        lastUpdated = "August 2026",
        sections = TERMS_SECTIONS,
        fullDocumentUrl = TERMS_OF_USE_URL,
        onBack = onBack
    )
}
