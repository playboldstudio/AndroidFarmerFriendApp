package com.example.androidfarmerfriend.data.util

import android.content.Context
import android.content.SharedPreferences

class UserPrefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    var userName: String
        get() = prefs.getString(KEY_NAME, DEFAULT_NAME) ?: DEFAULT_NAME
        set(value) { prefs.edit().putString(KEY_NAME, value).commit() }

    var userPhone: String
        get() = prefs.getString(KEY_PHONE, DEFAULT_PHONE) ?: DEFAULT_PHONE
        set(value) { prefs.edit().putString(KEY_PHONE, value).commit() }

    var userEmail: String
        get() = prefs.getString(KEY_EMAIL, DEFAULT_EMAIL) ?: DEFAULT_EMAIL
        set(value) { prefs.edit().putString(KEY_EMAIL, value).commit() }

    /** Wallpaper-derived dynamic color (Android 12+). Off by default. */
    var dynamicColorEnabled: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_COLOR, false)
        set(value) { prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, value).commit() }

    companion object {
        private const val KEY_NAME = "user_name"
        private const val KEY_PHONE = "user_phone"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"

        const val DEFAULT_NAME = "Farmer"
        const val DEFAULT_PHONE = "9876543210"
        const val DEFAULT_EMAIL = ""

        val PLACEHOLDER_NAMES = setOf(
            DEFAULT_NAME, "விவசாயி", "किसान", "రైతు", "കർഷകൻ",
            "ರೈತ", "शेतकरी", "কৃষক", "ਕਿਸਾਨ", "ખેડૂત", "ଚାଷୀ"
        )

        fun isPlaceholderName(name: String): Boolean =
            name.isBlank() || name.trim() in PLACEHOLDER_NAMES
    }
}
