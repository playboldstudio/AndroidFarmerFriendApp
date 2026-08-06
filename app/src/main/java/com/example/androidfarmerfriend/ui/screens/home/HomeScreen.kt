package com.example.androidfarmerfriend.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.R
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.data.location.LocationPrefs
import com.example.androidfarmerfriend.data.model.WeatherInfo
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.data.util.UserPrefs
import com.example.androidfarmerfriend.ui.components.EmptyState
import com.example.androidfarmerfriend.ui.components.FullScreenLoading
import com.example.androidfarmerfriend.ui.components.HeroTitle
import com.example.androidfarmerfriend.ui.components.LocationPickerSheet
import com.example.androidfarmerfriend.ui.components.LocPill
import com.example.androidfarmerfriend.ui.components.NotificationPermissionBanner
import com.example.androidfarmerfriend.ui.components.SectionTitle
import com.example.androidfarmerfriend.ui.components.TintIconCircle
import com.example.androidfarmerfriend.ui.components.WeatherHeroCard
import com.example.androidfarmerfriend.ui.navigation.Screen
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme
import com.example.androidfarmerfriend.ui.theme.FarmerTheme

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val locationPrefs = remember { LocationPrefs(context) }
    val userPrefs = remember { UserPrefs(context) }
    var selectedLocation by remember { mutableStateOf(locationPrefs.selectedLocation) }
    var showLocationPicker by remember { mutableStateOf(false) }

    val greeting = remember(userPrefs.userName, strings) {
        val name = userPrefs.userName
        val defaultFarmerNames = setOf(
            "விவசாயி", "Farmer", "किसान", "రైతు", "കർഷകൻ",
            "ರೈತ", "शेतकरी", "কৃষক", "ਕਿਸਾਨ", "ખેડૂત", "ଚାଷୀ"
        )
        if (name.isNotBlank() && name !in defaultFarmerNames) {
            val greetingWithoutEmoji = strings.homeGreeting.replace(" 👋", "")
            val parts = greetingWithoutEmoji.split(",").map { it.trim() }
            if (parts.size >= 2) {
                "${parts[0]}, $name! 👋"
            } else {
                strings.homeGreeting
            }
        } else {
            strings.homeGreeting
        }
    }

    LaunchedEffect(Unit) {
        viewModel.setStrings(strings)
        viewModel.onEvent(HomeEvent.LoadLocation(selectedLocation))
    }

    LaunchedEffect(strings) {
        viewModel.setStrings(strings)
    }

    val repository = remember { FarmerRepository() }

    if (showLocationPicker) {
        LocationPickerSheet(
            currentLocation = selectedLocation,
            onLocationSelected = { loc ->
                selectedLocation = loc
                locationPrefs.selectedLocation = loc
                showLocationPicker = false
                viewModel.onEvent(HomeEvent.LoadLocation(loc))
            },
            onSearch = { query -> repository.searchLocations(query) },
            onDismiss = { showLocationPicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FarmerTheme.colors.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        FarmerFriendLogo(strings = strings)

        NotificationPermissionBanner(modifier = Modifier.padding(bottom = 12.dp))

        HeroTitle(text = greeting, accent = extractedName(greeting))

        Spacer(modifier = Modifier.height(8.dp))

        LocPill(
            text = selectedLocation.name,
            onClick = { showLocationPicker = true }
        )

        Spacer(modifier = Modifier.height(8.dp))

        when (val weatherState = state.weatherState) {
            is UiState.Loading -> FullScreenLoading()
            is UiState.Error -> EmptyState(
                icon = Icons.Default.CloudOff,
                title = strings.weatherNoData
            )
            is UiState.Success -> WeatherHeroCard(
                weather = weatherState.data,
                strings = strings,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        SectionTitle(title = strings.quickAccess)

        QuickAccessGrid(onNavigate = onNavigate, strings = strings)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/** Returns the personalized name from the greeting, if any. */
private fun extractedName(greeting: String): String? {
    val defaultFarmerNames = setOf(
        "விவசாயி", "Farmer", "किसान", "రైతు", "കർഷകൻ",
        "ರೈತ", "शेतकरी", "কৃষক", "ਕਿਸਾਨ", "ખેડૂત", "ଚାଷୀ"
    )
    val match = Regex(",\\s*([^,!]+)!?\\s*👋").find(greeting)
    val candidate = match?.groupValues?.get(1)?.trim()
    return if (!candidate.isNullOrBlank() && candidate !in defaultFarmerNames) candidate else null
}

@Composable
fun FarmerFriendLogo(strings: AppStrings = AppStrings.Tamil) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(FarmerTheme.colors.softGreen, RoundedCornerShape(13.dp))
        ) {
            Image(
                painter = painterResource(R.mipmap.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(42.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = strings.appName,
                color = FarmerTheme.colors.primary,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.3).sp
            )
            Text(
                text = strings.farmerFriendTamil,
                color = FarmerTheme.colors.textSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun WeatherSummaryCard(weather: WeatherInfo, strings: AppStrings = AppStrings.Tamil) {
    WeatherHeroCard(weather = weather, strings = strings)
}

@Composable
fun WeatherStatItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = FarmerTheme.colors.textSecondary, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = FarmerTheme.colors.textPrimary)
    }
}

data class QuickActionItem(val title: String, val icon: ImageVector, val color: Color, val container: Color, val route: String)

@Composable
fun QuickAccessGrid(onNavigate: (String) -> Unit = {}, strings: AppStrings = AppStrings.Tamil) {
    val colors = FarmerTheme.colors
    val items = listOf(
        QuickActionItem(strings.navMarket, Icons.Default.BarChart, colors.primary, colors.softMint, Screen.Market.route),
        QuickActionItem(strings.weatherTitle, Icons.Default.WbCloudy, colors.weatherBlue, colors.softBlue, Screen.Weather.route),
        QuickActionItem(strings.schemesTitle, Icons.Default.LibraryBooks, colors.alertGreen, colors.softLavender, Screen.Schemes.route),
        QuickActionItem(strings.diseaseTitle, Icons.Default.BugReport, colors.diseaseOrange, colors.softOrange, Screen.Disease.route),
        QuickActionItem(strings.alertsTitle, Icons.Default.Notifications, colors.alertPurple, colors.softPurple, Screen.Alerts.route),
        QuickActionItem(strings.cropNotesTitle, Icons.Default.MenuBook, colors.cropBrown, colors.softBrown, Screen.CropNotes.route)
    )

    // Fixed 6-item grid: plain Column-of-Rows (not lazy) so it works inside a
    // scrollable parent without infinite-height measurement issues.
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        items.chunked(3).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { item ->
                    QuickAccessTile(item, onNavigate, Modifier.weight(1f))
                }
                // Keep the row evenly distributed if the last row is short.
                if (rowItems.size < 3) {
                    repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun QuickAccessTile(
    item: QuickActionItem,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FarmerTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(colors.surface, RoundedCornerShape(22.dp))
            .clickable { onNavigate(item.route) }
            .padding(vertical = 14.dp, horizontal = 4.dp)
    ) {
        TintIconCircle(
            icon = item.icon,
            tint = item.color,
            container = item.container,
            size = 52.dp,
            cornerRadius = 26.dp
        )
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = item.title,
            color = colors.textPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp,
            maxLines = 2
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AndroidFarmerFriendTheme {
        HomeScreen()
    }
}
