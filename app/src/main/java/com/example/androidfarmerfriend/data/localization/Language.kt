package com.example.androidfarmerfriend.data.localization

import android.content.Context
import android.content.SharedPreferences
import java.util.Locale

enum class Language(val code: String, val displayTamil: String, val displayEnglish: String) {
    TAMIL("ta", "தமிழ்", "Tamil"),
    ENGLISH("en", "ஆங்கிலம்", "English"),
    HINDI("hi", "இந்தி", "Hindi"),
    TELUGU("te", "தெலுங்கு", "Telugu"),
    MALAYALAM("ml", "மலையாளம்", "Malayalam"),
    KANNADA("kn", "கன்னடம்", "Kannada"),
    MARATHI("mr", "மராத்தி", "Marathi"),
    BENGALI("bn", "வங்காளம்", "Bengali"),
    PUNJABI("pa", "பஞ்சாபி", "Punjabi"),
    GUJARATI("gu", "குஜராத்தி", "Gujarati"),
    ODIA("or", "ஒடியா", "Odia");

    fun locale(): Locale = Locale(code)

    fun strings(): AppStrings = when (this) {
        TAMIL -> AppStrings.Tamil
        ENGLISH -> AppStrings.English
        HINDI -> AppStrings.Hindi
        TELUGU -> AppStrings.Telugu
        MALAYALAM -> AppStrings.Malayalam
        KANNADA -> AppStrings.Kannada
        MARATHI -> AppStrings.Marathi
        BENGALI -> AppStrings.Bengali
        PUNJABI -> AppStrings.Punjabi
        GUJARATI -> AppStrings.Gujarati
        ODIA -> AppStrings.Odia
    }
}

class LanguagePrefs(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)

    var selectedLanguage: Language
        get() {
            val code = prefs.getString(KEY_LANG, null) ?: return Language.ENGLISH
            return Language.entries.find { it.code == code } ?: Language.ENGLISH
        }
        set(value) {
            prefs.edit().putString(KEY_LANG, value.code).apply()
        }

    companion object {
        private const val KEY_LANG = "selected_language"
    }
}
