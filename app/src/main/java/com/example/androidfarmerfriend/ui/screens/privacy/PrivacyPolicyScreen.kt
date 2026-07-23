package com.example.androidfarmerfriend.ui.screens.privacy

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.androidfarmerfriend.R
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.ui.theme.FarmerGreenPrimary
import com.example.androidfarmerfriend.ui.theme.GrayText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit = {}) {
    val strings = LocalAppStrings.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.privacyPolicy,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = false
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.defaultTextEncodingName = "UTF-8"

                    loadDataWithBaseURL(
                        null,
                        PRIVACY_POLICY_HTML,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            }
        )
    }
}

private const val PRIVACY_POLICY_HTML = """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            padding: 20px;
            color: #333;
            line-height: 1.6;
            background: #F9F9F9;
        }
        h1 { color: #2E7D32; font-size: 22px; margin-bottom: 8px; }
        h2 { color: #2E7D32; font-size: 18px; margin-top: 24px; margin-bottom: 8px; }
        p { margin-bottom: 12px; font-size: 15px; }
        .date { color: #757575; font-size: 13px; margin-bottom: 20px; }
        ul { margin-left: 20px; margin-bottom: 12px; }
        li { margin-bottom: 6px; font-size: 15px; }
        a { color: #2E7D32; text-decoration: none; }
    </style>
</head>
<body>
    <h1>Privacy Policy</h1>
    <p class="date">Last updated: July 2025</p>

    <p>Farmer Friend ("we", "our", or "us") is committed to protecting your privacy. This Privacy Policy explains how we collect, use, and safeguard your information when you use our mobile application.</p>

    <h2>Information We Collect</h2>
    <p>We may collect the following types of information:</p>
    <ul>
        <li><strong>Personal Information:</strong> Name and phone number, which you voluntarily provide in the app profile section.</li>
        <li><strong>Location Data:</strong> Approximate location (city/region) that you manually select for weather and market price information.</li>
        <li><strong>Device Information:</strong> Firebase Cloud Messaging (FCM) token for sending push notifications about price alerts, weather warnings, and crop disease alerts.</li>
        <li><strong>Usage Data:</strong> App interaction data collected through Firebase Analytics to improve app performance and user experience.</li>
    </ul>

    <h2>How We Use Your Information</h2>
    <p>We use the collected information to:</p>
    <ul>
        <li>Provide personalized weather forecasts, market prices, and agricultural information.</li>
        <li>Send relevant push notifications about price changes, weather alerts, and crop disease warnings.</li>
        <li>Improve app functionality and user experience.</li>
        <li>Ensure app security and prevent abuse.</li>
    </ul>

    <h2>Data Storage and Security</h2>
    <p>Your data is stored securely using Google Firebase services. We implement appropriate technical and organizational measures to protect your personal information against unauthorized access, alteration, disclosure, or destruction.</p>

    <h2>Data Sharing</h2>
    <p>We do not sell, trade, or otherwise transfer your personal information to outside parties. Your data may be shared only with:</p>
    <ul>
        <li>Google Firebase services (for app functionality, analytics, and crash reporting).</li>
        <li>Third-party weather and market price APIs (only location data, no personal information).</li>
    </ul>

    <h2>Third-Party Services</h2>
    <p>Our app uses the following third-party services:</p>
    <ul>
        <li><strong>Firebase Analytics:</strong> For app usage analytics.</li>
        <li><strong>Firebase Crashlytics:</strong> For crash reporting and app stability.</li>
        <li><strong>Firebase Cloud Messaging:</strong> For push notifications.</li>
        <li><strong>Open-Meteo API:</strong> For weather data.</li>
        <li><strong>Vegetable Market Price API:</strong> For market price data.</li>
    </ul>

    <h2>Your Rights</h2>
    <p>You have the right to:</p>
    <ul>
        <li>Access the personal information we hold about you.</li>
        <li>Request correction of inaccurate personal information.</li>
        <li>Request deletion of your personal information.</li>
        <li>Opt-out of push notifications through your device settings.</li>
    </ul>

    <h2>Children's Privacy</h2>
    <p>Our app is not intended for use by children under the age of 13. We do not knowingly collect personal information from children under 13.</p>

    <h2>Changes to This Policy</h2>
    <p>We may update this Privacy Policy from time to time. We will notify you of any changes by posting the new Privacy Policy within the app.</p>

    <h2>Contact Us</h2>
    <p>If you have any questions about this Privacy Policy, please contact us at:</p>
    <p><a href="mailto:playboldstudio@gmail.com">playboldstudio@gmail.com</a></p>
</body>
</html>
"""
