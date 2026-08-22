package com.example.androidfarmerfriend.ui.screens.privacy

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.androidfarmerfriend.ui.components.SubScreenHeader
import com.example.androidfarmerfriend.ui.theme.FarmerSpacing
import com.example.androidfarmerfriend.ui.theme.FarmerTheme

/** Shared host for the legal WebView pages with a thin loading indicator. */
@Composable
internal fun LegalWebViewScreen(title: String, url: String, onBack: () -> Unit) {
    val colors = FarmerTheme.colors
    var isLoading by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = FarmerSpacing.lg)
    ) {
        Spacer(Modifier.height(FarmerSpacing.lg))

        SubScreenHeader(title = title, onBack = onBack)

        Spacer(Modifier.height(FarmerSpacing.s))

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = colors.primary,
                trackColor = colors.surfaceMuted
            )
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, pageUrl: String?) {
                            isLoading = false
                        }
                    }
                    settings.javaScriptEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.defaultTextEncodingName = "UTF-8"
                    loadUrl(url)
                }
            }
        )
    }
}
