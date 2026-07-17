package com.example.androidfarmerfriend.ui.screens.language

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.androidfarmerfriend.data.localization.LanguagePrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LanguageViewModel(application: Application) : AndroidViewModel(application) {
    private val languagePrefs = LanguagePrefs(application)
    
    private val _state = MutableStateFlow(LanguageState(selectedLanguage = languagePrefs.selectedLanguage))
    val state: StateFlow<LanguageState> = _state.asStateFlow()

    fun onEvent(event: LanguageEvent) {
        when (event) {
            is LanguageEvent.SelectLanguage -> {
                languagePrefs.selectedLanguage = event.language
                _state.value = _state.value.copy(selectedLanguage = event.language)
            }
        }
    }
}
