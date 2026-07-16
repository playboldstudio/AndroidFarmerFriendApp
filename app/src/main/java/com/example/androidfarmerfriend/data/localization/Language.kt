package com.example.androidfarmerfriend.data.localization

import android.content.Context
import android.content.SharedPreferences
import java.util.Locale

enum class Language(val code: String, val displayTamil: String, val displayEnglish: String) {
    TAMIL("ta", "தமிழ்", "தமிழ்"),
    ENGLISH("en", "ஆங்கிலம்", "English");

    fun locale(): Locale = Locale(code)
}

class LanguagePrefs(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)

    var selectedLanguage: Language
        get() {
            val code = prefs.getString(KEY_LANG, null) ?: return Language.TAMIL
            return Language.entries.find { it.code == code } ?: Language.TAMIL
        }
        set(value) {
            prefs.edit().putString(KEY_LANG, value.code).apply()
        }

    companion object {
        private const val KEY_LANG = "selected_language"
    }
}
