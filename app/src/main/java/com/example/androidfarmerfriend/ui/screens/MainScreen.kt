package com.example.androidfarmerfriend.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.data.localization.LanguagePrefs
import com.google.firebase.auth.FirebaseAuth
import com.example.androidfarmerfriend.ui.components.BottomNavItem
import com.example.androidfarmerfriend.ui.components.FloatingTabBar
import com.example.androidfarmerfriend.ui.navigation.Screen
import com.example.androidfarmerfriend.ui.screens.home.HomeScreen
import com.example.androidfarmerfriend.ui.auth.AuthBridge
import com.example.androidfarmerfriend.ui.screens.auth.AuthMode
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
import com.example.androidfarmerfriend.ui.theme.FarmerMotion

/** Routes where the floating tab bar is visible; sub-screens hide it. */
private val TOP_LEVEL_ROUTES = setOf(
    Screen.Home.route,
    Screen.Market.route,
    Screen.Weather.route,
    Screen.Alerts.route,
    Screen.Profile.route
)

/**
 * Screen transition spec. Push: new screen slides in from the start edge (a
 * quarter of its width) while the old fades out; pop reverses it. Durations and
 * easing come from the design system so every screen moves in the same voice.
 * Slide transitions animate an IntOffset, fades animate a Float — they need
 * matching spec types, so each has its own tween.
 */
private val navForwardSlide = tween<IntOffset>(FarmerMotion.durationSlow, easing = FarmerMotion.emphasizedDecelerate)
private val navForwardFade = tween<Float>(FarmerMotion.durationSlow, easing = FarmerMotion.emphasizedDecelerate)
private val navForwardExitSlide = tween<IntOffset>(FarmerMotion.durationNormal, easing = FarmerMotion.standardDecelerate)
private val navForwardExitFade = tween<Float>(FarmerMotion.durationNormal, easing = FarmerMotion.standardDecelerate)
private val navBackSlide = tween<IntOffset>(FarmerMotion.durationSlow, easing = FarmerMotion.emphasizedAccelerate)
private val navBackFade = tween<Float>(FarmerMotion.durationSlow, easing = FarmerMotion.emphasizedAccelerate)
private val navBackExitSlide = tween<IntOffset>(FarmerMotion.durationNormal, easing = FarmerMotion.standardAccelerate)
private val navBackExitFade = tween<Float>(FarmerMotion.durationNormal, easing = FarmerMotion.standardAccelerate)

private val screenNavEnter = slideInHorizontally(
    initialOffsetX = { it / 4 },
    animationSpec = navForwardSlide
) + fadeIn(navForwardFade)
private val screenNavExit = slideOutHorizontally(
    targetOffsetX = { -it / 8 },
    animationSpec = navForwardExitSlide
) + fadeOut(navForwardExitFade)
private val screenNavPopEnter = slideInHorizontally(
    initialOffsetX = { -it / 8 },
    animationSpec = navBackSlide
) + fadeIn(navBackFade)
private val screenNavPopExit = slideOutHorizontally(
    targetOffsetX = { it / 4 },
    animationSpec = navBackExitSlide
) + fadeOut(navBackExitFade)

@Composable
fun MainScreen(onRestart: () -> Unit = {}) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val languagePrefs = remember { LanguagePrefs(context) }
    var currentLang by remember { mutableStateOf(languagePrefs.selectedLanguage) }
    val strings = currentLang.strings()

    val auth = remember { FirebaseAuth.getInstance() }
    var signedIn by remember { mutableStateOf(auth.currentUser != null) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { signedIn = it.currentUser != null }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    // Sign-in navigates to a full-screen auth page instead of overlay.
    val requestSignIn: (AuthMode, () -> Unit) -> Unit =
        { mode, _ ->
            val modeArg = if (mode == AuthMode.SIGN_UP) "up" else "in"
            navController.navigate("auth/$modeArg")
        }

    // After successful auth, pop back to the previous screen.
    LaunchedEffect(signedIn) {
        if (signedIn) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute?.startsWith("auth/") == true) {
                navController.popBackStack()
            }
        }
    }

    CompositionLocalProvider(
        LocalAppStrings provides strings,
        AuthBridge.LocalIsSignedIn provides signedIn,
        AuthBridge.LocalRequestSignIn provides requestSignIn
    ) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Market,
        BottomNavItem.Weather,
        BottomNavItem.Alerts,
        BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (currentRoute in TOP_LEVEL_ROUTES) {
                FloatingTabBar(
                    currentRoute = currentRoute,
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
        }
        ) { innerPadding ->
        SharedTransitionLayout {
        // The enclosing SharedTransitionScope coordinates shared-element keys
        // across destinations; thread it to Home/Weather so their weather card
        // can be marked as a shared element.
        val sharedScope = this
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { screenNavEnter },
            exitTransition = { screenNavExit },
            popEnterTransition = { screenNavPopEnter },
            popExitTransition = { screenNavPopExit }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    sharedTransitionScope = sharedScope,
                    animatedVisibilityScope = this,
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
            composable(Screen.Weather.route) {
                WeatherScreen()
            }
            composable(Screen.Alerts.route) {
                AlertsScreen(
                    onNavigateToAlert = { route -> navController.navigate(route) }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigate = { route ->
                        when (route) {
                            "language" -> navController.navigate(Screen.Language.route)
                            "privacy_policy" -> navController.navigate(Screen.PrivacyPolicy.route)
                            "terms_of_use" -> navController.navigate(Screen.TermsOfUse.route)
                            else -> {}
                        }
                    },
                    onRestart = onRestart
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
            // Full-screen auth page
            composable(
                route = "auth/{mode}",
                arguments = listOf(navArgument("mode") { type = NavType.StringType })
            ) { backStackEntry ->
                val modeArg = backStackEntry.arguments?.getString("mode") ?: "in"
                val initialMode = if (modeArg == "up") AuthMode.SIGN_UP else AuthMode.SIGN_IN
                AuthScreen(
                    initialMode = initialMode,
                    onDismissed = { navController.popBackStack() }
                )
            }
        }
        }
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
