package com.example.androidfarmerfriend

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.data.localization.LanguagePrefs
import com.example.androidfarmerfriend.ui.components.UpdateBannerCard
import com.example.androidfarmerfriend.ui.screens.MainScreen
import com.example.androidfarmerfriend.ui.screens.SplashScreen
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme
import com.example.androidfarmerfriend.util.InAppUpdateHelper
import com.example.androidfarmerfriend.util.UpdateBanner

/** Renders the update banner only when one is active. */
@Composable
private fun UpdateBannerHost(
    banner: UpdateBanner?,
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    if (banner == null) return
    UpdateBannerCard(
        banner = banner,
        strings = LocalAppStrings.current,
        onAction = onAction,
        onDismiss = onDismiss,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

class MainActivity : ComponentActivity() {

    // Keep the platform splash on screen until the Compose splash has drawn its
    // first frame (prevents a blank/white flash), then hand off.
    private var systemSplashVisible by mutableStateOf(true)
    // Compose splash stays up until its animation finishes.
    private var composeSplashVisible by mutableStateOf(true)

    private lateinit var updateHelper: InAppUpdateHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        applyLanguage()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        updateHelper = InAppUpdateHelper(this)
        splashScreen.setKeepOnScreenCondition { systemSplashVisible }

        setContent {
            AndroidFarmerFriendTheme {
                if (composeSplashVisible) {
                    SplashScreen(
                        onReady = { systemSplashVisible = false },
                        onFinished = { composeSplashVisible = false }
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        UpdateBannerHost(
                            banner = updateHelper.banner,
                            onAction = {
                                when (updateHelper.banner) {
                                    is UpdateBanner.ReadyToInstall -> updateHelper.completeUpdate()
                                    else -> updateHelper.startDownload()
                                }
                            },
                            onDismiss = { updateHelper.dismiss() }
                        )
                        MainScreen(onRestart = { recreate() })
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateHelper.onResume()
    }

    private fun applyLanguage() {
        val languagePrefs = LanguagePrefs(this)
        val locale = languagePrefs.selectedLanguage.locale()
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }
}
