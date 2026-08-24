package com.example.androidfarmerfriend.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.localization.Language
import com.example.androidfarmerfriend.data.localization.LanguagePrefs
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.google.firebase.auth.FirebaseAuth
import com.example.androidfarmerfriend.ui.components.BottomNavItem
import com.example.androidfarmerfriend.ui.components.FloatingTabBar
import com.example.androidfarmerfriend.ui.navigation.Screen
import com.example.androidfarmerfriend.ui.screens.home.HomeScreen
import com.example.androidfarmerfriend.ui.auth.AuthBridge
import com.example.androidfarmerfriend.ui.screens.auth.AuthScreen
import com.example.androidfarmerfriend.ui.screens.market.MarketScreen
import com.example.androidfarmerfriend.ui.screens.alerts.AlertsScreen
import com.example.androidfarmerfriend.ui.screens.weather.WeatherScreen
import com.example.androidfarmerfriend.ui.screens.profile.ProfileScreen
import com.example.androidfarmerfriend.ui.screens.disease.DiseaseScreen
import com.example.androidfarmerfriend.ui.screens.schemes.SchemesScreen
import com.example.androidfarmerfriend.ui.screens.cropnotes.CropNotesScreen
import com.example.androidfarmerfriend.ui.screens.language.LanguageScreen
import com.example.androidfarmerfriend.ui.screens.privacy.PrivacyPolicyScreen
import com.example.androidfarmerfriend.ui.screens.privacy.TermsOfUseScreen
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme

@Composable
fun MainScreen(onRestart: () -> Unit = {}) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val languagePrefs = remember { LanguagePrefs(context) }
    var currentLang by remember { mutableStateOf(languagePrefs.selectedLanguage) }
    val strings = currentLang.strings()

    val auth = remember { FirebaseAuth.getInstance() }
    var signedIn by remember { mutableStateOf(auth.currentUser != null) }
    var showAuthOverlay by remember { mutableStateOf(false) }
    var pendingAfterAuth by remember { mutableStateOf<(() -> Unit)?>(null) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { signedIn = it.currentUser != null }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    // Sign-in is optional: the app runs as guest; gated features call
    // requestSignIn and their action runs after a successful sign-in.
    fun requestSignIn(onSuccess: () -> Unit) {
        pendingAfterAuth = onSuccess
        showAuthOverlay = true
    }

    LaunchedEffect(signedIn) {
        if (signedIn && showAuthOverlay) {
            showAuthOverlay = false
            pendingAfterAuth?.invoke()
            pendingAfterAuth = null
        }
    }

    CompositionLocalProvider(
        LocalAppStrings provides strings,
        AuthBridge.LocalIsSignedIn provides signedIn,
        AuthBridge.LocalRequestSignIn provides ::requestSignIn
    ) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Market,
        BottomNavItem.Weather,
        BottomNavItem.Alerts,
        BottomNavItem.Profile
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            FloatingTabBar(
                currentRoute = currentDestination?.route,
                items = items,
                strings = strings,
                onTabSelected = { item ->
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.Market.route) { MarketScreen() }
            composable(Screen.Weather.route) { WeatherScreen() }
            composable(Screen.Alerts.route) { AlertsScreen() }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigate = { route ->
                        when (route) {
                            "language" -> navController.navigate(Screen.Language.route)
                            "privacy_policy" -> navController.navigate(Screen.PrivacyPolicy.route)
                            "terms_of_use" -> navController.navigate(Screen.TermsOfUse.route)
                            else -> {}
                        }
                    }
                )
            }
            composable(Screen.Disease.route) {
                DiseaseScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Schemes.route) {
                SchemesScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.CropNotes.route) {
                CropNotesScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Language.route) {
                LanguageScreen(
                    onLanguageChanged = { lang ->
                        currentLang = lang
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.PrivacyPolicy.route) {
                PrivacyPolicyScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.TermsOfUse.route) {
                TermsOfUseScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

        if (showAuthOverlay) {
            AuthOverlayHost(onDismiss = {
                showAuthOverlay = false
                pendingAfterAuth = null
            })
        }
    }
}

@Composable
private fun AuthOverlayHost(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AuthScreen(onDismissed = onDismiss)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    AndroidFarmerFriendTheme {
        MainScreen()
    }
}
