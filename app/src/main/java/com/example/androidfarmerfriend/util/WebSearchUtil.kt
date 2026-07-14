package com.example.androidfarmerfriend.util

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

object WebSearchUtil {
    fun search(context: Context, query: String) {
        val encodedQuery = Uri.encode(query)
        val url = "https://www.google.com/search?q=$encodedQuery"
        val tabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setUrlBarHidingEnabled(true)
            .build()
        tabsIntent.launchUrl(context, Uri.parse(url))
    }

    fun openUrl(context: Context, url: String) {
        val tabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setUrlBarHidingEnabled(true)
            .build()
        tabsIntent.launchUrl(context, Uri.parse(url))
    }
}
