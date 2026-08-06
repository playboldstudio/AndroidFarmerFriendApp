package com.example.androidfarmerfriend.ui.screens.privacy

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.ui.components.SubScreenHeader
import com.example.androidfarmerfriend.ui.theme.FarmerTheme

/** Hosted privacy policy (FlyCricket doc hosting). */
private const val PRIVACY_POLICY_URL =
    "https://doc-hosting.flycricket.io/farmer-friend-privacy-policy/b8e9b2d9-76fb-4e2c-a736-7647e5589687/privacy"

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit = {}) {
    val strings = LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FarmerTheme.colors.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        SubScreenHeader(title = strings.privacyPolicy, onBack = onBack)

        Spacer(modifier = Modifier.height(8.dp))

        AndroidView(
            modifier = Modifier
                .fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.defaultTextEncodingName = "UTF-8"
                    loadUrl(PRIVACY_POLICY_URL)
                }
            }
        )
    }
}
