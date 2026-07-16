package com.example.androidfarmerfriend

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.androidfarmerfriend.data.localization.LanguagePrefs
import com.example.androidfarmerfriend.ui.screens.MainScreen
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        applyLanguage()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidFarmerFriendTheme {
                MainScreen(
                    onRestart = { recreate() }
                )
            }
        }
    }

    private fun applyLanguage() {
        val languagePrefs = LanguagePrefs(this)
        val locale = languagePrefs.selectedLanguage.locale()
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }
}
