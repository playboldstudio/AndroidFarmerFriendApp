package com.example.androidfarmerfriend.ui.screens.language

import com.example.androidfarmerfriend.data.localization.Language

sealed interface LanguageEvent {
    data class SelectLanguage(val language: Language) : LanguageEvent
}
