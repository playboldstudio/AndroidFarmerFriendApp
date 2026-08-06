package com.example.androidfarmerfriend.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.ui.navigation.Screen
import com.example.androidfarmerfriend.ui.theme.FarmerTheme

/** Bottom nav item model (shared by MainScreen + FloatingTabBar). */
sealed class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val labelKey: (AppStrings) -> String
) {
    object Home : BottomNavItem(Screen.Home, Icons.Default.Home, { it.navHome })
    object Market : BottomNavItem(Screen.Market, Icons.Default.BarChart, { it.navMarket })
    object Weather : BottomNavItem(Screen.Weather, Icons.Default.WbCloudy, { it.navWeather })
    object Alerts : BottomNavItem(Screen.Alerts, Icons.Default.Notifications, { it.navAlerts })
    object Profile : BottomNavItem(Screen.Profile, Icons.Default.Person, { it.navProfile })
}

/**
 * Floating rounded pill tab bar — matches `.tabbar`.
 * Icons only; the active tab sits in a soft-green rounded pill.
 */
@Composable
fun FloatingTabBar(
    currentRoute: String?,
    items: List<BottomNavItem>,
    onTabSelected: (BottomNavItem) -> Unit
) {
    val colors = FarmerTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = colors.surface,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, colors.outline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val selected = item.screen.route == currentRoute
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .then(if (selected) Modifier.background(colors.softGreen) else Modifier)
                            .clickable { onTabSelected(item) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (selected) colors.primary else colors.textTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
