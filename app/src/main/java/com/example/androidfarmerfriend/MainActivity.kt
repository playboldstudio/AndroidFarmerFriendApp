package com.example.androidfarmerfriend

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.localization.LanguagePrefs
import com.example.androidfarmerfriend.data.util.UserPrefs
import com.example.androidfarmerfriend.ui.components.UpdateBannerCard
import com.example.androidfarmerfriend.ui.screens.MainScreen
import com.example.androidfarmerfriend.ui.screens.SplashScreen
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme
import com.example.androidfarmerfriend.ui.theme.ThemeMode
import com.example.androidfarmerfriend.util.InAppUpdateHelper
import com.example.androidfarmerfriend.util.UpdateBanner

/**
 * Wraps the update card in a centered Dialog (instead of the old inline banner),
 * shown in English regardless of the in-app language, so a pending update stays
 * clearly visible and reads the same for every user.
 */
@Composable
private fun UpdateBannerCardDialogHost(
    banner: UpdateBanner?,
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    if (banner == null) return
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = true,
            dismissOnBackPress = banner != UpdateBanner.ReadyToInstall,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            UpdateBannerCard(
                banner = banner,
                strings = AppStrings.English,
                onAction = onAction,
                onDismiss = onDismiss
            )
        }
    }
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
            val userPrefs = UserPrefs(this)
            val themeMode = ThemeMode.fromKey(userPrefs.themeMode)
            AndroidFarmerFriendTheme(themeMode = themeMode) {
                if (composeSplashVisible) {
                    SplashScreen(
                        onReady = { systemSplashVisible = false },
                        onFinished = { composeSplashVisible = false }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        MainScreen(onRestart = { recreate() })
                        // Overlay window — centered English update dialog.
                        UpdateBannerCardDialogHost(
                            banner = updateHelper.banner,
                            onAction = {
                                when (updateHelper.banner) {
                                    is UpdateBanner.ReadyToInstall -> updateHelper.completeUpdate()
                                    else -> updateHelper.startDownload()
                                }
                            },
                            onDismiss = { updateHelper.dismiss() }
                        )
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
