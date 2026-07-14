package com.example.androidfarmerfriend.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.androidfarmerfriend.ui.navigation.Screen
import com.example.androidfarmerfriend.ui.screens.home.HomeScreen
import com.example.androidfarmerfriend.ui.screens.market.MarketScreen
import com.example.androidfarmerfriend.ui.screens.alerts.AlertsScreen
import com.example.androidfarmerfriend.ui.screens.weather.WeatherScreen
import com.example.androidfarmerfriend.ui.screens.profile.ProfileScreen
import com.example.androidfarmerfriend.ui.screens.disease.DiseaseScreen
import com.example.androidfarmerfriend.ui.screens.schemes.SchemesScreen
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme
import com.example.androidfarmerfriend.ui.theme.GrayText

sealed class BottomNavItem(val screen: Screen, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem(Screen.Home, Icons.Default.Home, "முகப்பு")
    object Market : BottomNavItem(Screen.Market, Icons.Default.BarChart, "மார்க்கெட்")
    object Weather : BottomNavItem(Screen.Weather, Icons.Default.WbCloudy, "வானிலை")
    object Alerts : BottomNavItem(Screen.Alerts, Icons.Default.Notifications, "அலர்ட்கள்")
    object Profile : BottomNavItem(Screen.Profile, Icons.Default.Person, "புரோஃபைல்")
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Market,
        BottomNavItem.Weather,
        BottomNavItem.Alerts,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.size(height = 70.dp, width = 400.dp)
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                item.icon, 
                                contentDescription = item.label,
                                modifier = Modifier.size(24.dp)
                            ) 
                        },
                        label = { 
                            Text(
                                item.label, 
                                fontSize = 10.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else GrayText
                            ) 
                        },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = GrayText,
                            unselectedTextColor = GrayText,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Market.route) { MarketScreen() }
            composable(Screen.Weather.route) { WeatherScreen() }
            composable(Screen.Alerts.route) { AlertsScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
            composable(Screen.Disease.route) { DiseaseScreen() }
            composable(Screen.Schemes.route) { SchemesScreen() }
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
