package com.example.androidfarmerfriend.util

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.example.androidfarmerfriend.R

/**
 * Opens links in a brand-styled Chrome Custom Tab — the modern, Telegram-like
 * "in-app browser": native speed, green toolbar, title, share action, and
 * slide transitions.
 */
object WebSearchUtil {

    /** Open a URL in a branded custom tab. */
    fun openUrl(context: Context, url: String) {
        val tabsIntent = buildTabsIntent(context)
        tabsIntent.launchUrl(context, Uri.parse(url))
    }

    /** Open a Google search for [query] in a branded custom tab. */
    fun search(context: Context, query: String) {
        val encodedQuery = Uri.encode(query)
        val url = "https://www.google.com/search?q=$encodedQuery"
        val tabsIntent = buildTabsIntent(context)
        tabsIntent.launchUrl(context, Uri.parse(url))
    }

    private fun buildTabsIntent(context: Context): CustomTabsIntent {
        val toolbarColor = ContextCompat.getColor(context, R.color.browser_toolbar)

        // Close (back) arrow as a Bitmap for the Custom Tabs toolbar.
        val closeIcon = ContextCompat.getDrawable(context, R.drawable.ic_arrow_back)?.let { d ->
            val bitmap = android.graphics.Bitmap.createBitmap(
                d.intrinsicWidth, d.intrinsicHeight, android.graphics.Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            d.setBounds(0, 0, canvas.width, canvas.height)
            d.draw(canvas)
            bitmap
        }

        return CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setUrlBarHidingEnabled(true)
            .setToolbarColor(toolbarColor)
            .setColorScheme(CustomTabsIntent.COLOR_SCHEME_SYSTEM)
            .setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left)
            .setExitAnimations(context, R.anim.slide_in_left, R.anim.slide_out_right)
            .addDefaultShareMenuItem()
            .apply { closeIcon?.let { setCloseButtonIcon(it) } }
            .build()
    }
}
