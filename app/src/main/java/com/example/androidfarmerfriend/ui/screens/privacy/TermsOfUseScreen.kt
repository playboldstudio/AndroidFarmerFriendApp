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

/** Hosted terms of use (FlyCricket doc hosting). */
private const val TERMS_URL =
    "https://doc-hosting.flycricket.io/farmer-friend-terms-of-use/63df81a9-6229-4119-ae1d-149e7b5de893/terms"

@Composable
fun TermsOfUseScreen(onBack: () -> Unit = {}) {
    val strings = LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FarmerTheme.colors.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        SubScreenHeader(title = strings.termsOfUse, onBack = onBack)

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
                    loadUrl(TERMS_URL)
                }
            }
        )
    }
}
