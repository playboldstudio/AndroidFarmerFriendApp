package com.example.androidfarmerfriend.data.util

import android.content.Context
import android.content.SharedPreferences

class UserPrefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    var userName: String
        get() = prefs.getString(KEY_NAME, "விவசாயி") ?: "விவசாயி"
        set(value) = prefs.edit().putString(KEY_NAME, value).apply()

    var userPhone: String
        get() = prefs.getString(KEY_PHONE, "9876543210") ?: "9876543210"
        set(value) = prefs.edit().putString(KEY_PHONE, value).apply()

    companion object {
        private const val KEY_NAME = "user_name"
        private const val KEY_PHONE = "user_phone"
    }
}
