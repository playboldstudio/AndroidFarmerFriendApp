package com.example.androidfarmerfriend.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.androidfarmerfriend.ui.components.FarmerCard
import com.example.androidfarmerfriend.ui.components.FullScreenLoading
import com.example.androidfarmerfriend.ui.components.LocationPickerSheet
import com.example.androidfarmerfriend.ui.components.ScreenHeader
import com.example.androidfarmerfriend.ui.components.weatherIconFor
import com.example.androidfarmerfriend.ui.navigation.Screen
import com.example.androidfarmerfriend.ui.theme.*

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val locationPrefs = remember { LocationPrefs(context) }
    var selectedLocation by remember { mutableStateOf(locationPrefs.selectedLocation) }
    var showLocationPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onEvent(HomeEvent.LoadLocation(selectedLocation))
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
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        FarmerFriendLogo(strings = strings)

        Spacer(modifier = Modifier.height(12.dp))

        ScreenHeader(
            title = strings.homeGreeting,
            subtitle = selectedLocation.name,
            isHome = true,
            showSearch = false,
            onLocationClick = { showLocationPicker = true }
        )

        Spacer(modifier = Modifier.height(8.dp))

        when (val weatherState = state.weatherState) {
            is UiState.Loading -> FullScreenLoading()
            is UiState.Error -> Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = GrayText, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(strings.weatherNoData, color = GrayText, style = MaterialTheme.typography.bodySmall)
                }
            }
            is UiState.Success -> WeatherSummaryCard(weather = weatherState.data, strings = strings)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = strings.quickAccess,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        QuickAccessGrid(onNavigate = onNavigate, strings = strings)
    }
}

@Composable
fun FarmerFriendLogo(strings: AppStrings = AppStrings.Tamil) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.mipmap.ic_launcher),
            contentDescription = "Farmer Friend Logo",
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = strings.appName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = FarmerGreenPrimary
            )
            Text(
                text = strings.farmerFriendTamil,
                style = MaterialTheme.typography.bodySmall,
                color = GrayText
            )
        }
    }
}

@Composable
fun WeatherSummaryCard(weather: WeatherInfo, strings: AppStrings = AppStrings.Tamil) {
    FarmerCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    weatherIconFor(weather.weatherCode),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = WeatherYellow
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = weather.temperature,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = weather.condition,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (weather.todayLow.isNotEmpty() && weather.todayHigh.isNotEmpty()) {
                        Text(
                            text = java.lang.String.format(strings.lowHigh, weather.todayLow, weather.todayHigh),
                            style = MaterialTheme.typography.bodySmall,
                            color = GrayText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                WeatherStatItem(label = strings.rain, value = weather.rainChance, icon = Icons.Default.WaterDrop)
                WeatherStatItem(label = strings.humidity, value = weather.humidity, icon = Icons.Default.Opacity)
                WeatherStatItem(label = strings.wind, value = weather.windSpeed, icon = Icons.Default.Air)
            }
        }
    }
}

@Composable
fun WeatherStatItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = GrayText, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

data class QuickActionItem(val title: String, val icon: ImageVector, val color: Color, val route: String)

@Composable
fun QuickAccessGrid(onNavigate: (String) -> Unit = {}, strings: AppStrings = AppStrings.Tamil) {
    val items = listOf(
        QuickActionItem(strings.navMarket, Icons.Default.BarChart, FarmerGreenSecondary, Screen.Market.route),
        QuickActionItem(strings.weatherTitle, Icons.Default.WbCloudy, WeatherBlue, Screen.Weather.route),
        QuickActionItem(strings.schemesTitle, Icons.Default.LibraryBooks, SchemeLightGreen, Screen.Schemes.route),
        QuickActionItem(strings.diseaseTitle, Icons.Default.BugReport, DiseaseOrange, Screen.Disease.route),
        QuickActionItem(strings.alertsTitle, Icons.Default.Notifications, AlertPurple, Screen.Alerts.route),
        QuickActionItem(strings.cropNotesTitle, Icons.Default.MenuBook, CropNotesBrown, Screen.CropNotes.route)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(items) { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onNavigate(item.route) }
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    shadowElevation = 1.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(item.icon, contentDescription = item.title, tint = item.color, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AndroidFarmerFriendTheme {
        HomeScreen()
    }
}
