package com.example.androidfarmerfriend

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.androidfarmerfriend.data.localization.LanguagePrefs
import com.example.androidfarmerfriend.ui.screens.MainScreen
import com.example.androidfarmerfriend.ui.screens.SplashScreen
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme

class MainActivity : ComponentActivity() {

    // Keep the platform splash on screen until the Compose splash has drawn its
    // first frame (prevents a blank/white flash), then hand off.
    private var systemSplashVisible by mutableStateOf(true)
    // Compose splash stays up until its animation finishes.
    private var composeSplashVisible by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        applyLanguage()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition { systemSplashVisible }

        setContent {
            AndroidFarmerFriendTheme {
                if (composeSplashVisible) {
                    SplashScreen(
                        onReady = { systemSplashVisible = false },
                        onFinished = { composeSplashVisible = false }
                    )
                } else {
                    MainScreen(onRestart = { recreate() })
                }
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
