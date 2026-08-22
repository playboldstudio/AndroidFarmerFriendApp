package com.example.androidfarmerfriend.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.androidfarmerfriend.data.model.WeatherInfo
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.ui.components.EmptyState
import com.example.androidfarmerfriend.ui.components.HeroTitle
import com.example.androidfarmerfriend.ui.components.LocationPickerSheet
import com.example.androidfarmerfriend.ui.components.LocPill
import com.example.androidfarmerfriend.ui.components.NotificationPermissionBanner
import com.example.androidfarmerfriend.ui.components.SectionTitle
import com.example.androidfarmerfriend.ui.components.ShimmerList
import com.example.androidfarmerfriend.ui.components.TintIconCircle
import com.example.androidfarmerfriend.ui.components.WeatherHeroCard
import com.example.androidfarmerfriend.ui.navigation.Screen
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme
import com.example.androidfarmerfriend.ui.theme.FarmerSpacing
import com.example.androidfarmerfriend.ui.theme.FarmerTheme

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    val strings = LocalAppStrings.current
    var showLocationPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.setStrings(strings)
        viewModel.onEvent(HomeEvent.LoadInitial)
    }

    LaunchedEffect(strings) {
        viewModel.setStrings(strings)
    }

    val currentLocation = state.selectedLocation
    if (showLocationPicker && currentLocation != null) {
        LocationPickerSheet(
            currentLocation = currentLocation,
            onLocationSelected = { loc ->
                showLocationPicker = false
                viewModel.onEvent(HomeEvent.SelectLocation(loc))
            },
            onSearch = { query -> viewModel.searchLocations(query) },
            onDismiss = { showLocationPicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FarmerTheme.colors.background)
            .padding(horizontal = FarmerSpacing.lg)
            .verticalScroll(rememberScrollState())
    ) {
        HomeHeader(
            strings = strings,
            onProfileClick = { onNavigate(Screen.Profile.route) }
        )

        NotificationPermissionBanner(modifier = Modifier.padding(bottom = FarmerSpacing.md))

        HeroTitle(text = state.greeting, accent = state.greetingName)

        Spacer(Modifier.height(FarmerSpacing.s))

        LocPill(
            text = state.selectedLocation?.name.orEmpty(),
            onClick = { showLocationPicker = true }
        )

        Spacer(Modifier.height(FarmerSpacing.s))

        when (val weatherState = state.weatherState) {
            is UiState.Loading -> ShimmerList(rowCount = 2, rowHeight = 190.dp)
            is UiState.Error -> EmptyState(
                icon = Icons.Default.CloudOff,
                title = strings.weatherNoData
            )
            is UiState.Success -> WeatherHeroCard(
                weather = weatherState.data,
                strings = strings,
                modifier = Modifier.padding(top = FarmerSpacing.lg),
                onClick = { onNavigate(Screen.Weather.route) }
            )
        }

        SectionTitle(title = strings.quickAccess)

        QuickAccessGrid(onNavigate = onNavigate, strings = strings)

        Spacer(Modifier.height(FarmerSpacing.xxl))
    }
}

@Composable
private fun HomeHeader(strings: AppStrings, onProfileClick: () -> Unit) {
    val colors = FarmerTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = FarmerSpacing.lg, bottom = FarmerSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(colors.softGreen, RoundedCornerShape(13.dp))
        ) {
            Image(
                painter = painterResource(R.mipmap.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(42.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = strings.appName,
                style = MaterialTheme.typography.titleLarge,
                color = colors.primary
            )
            Text(
                text = strings.farmerFriendTamil,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = colors.textSecondary
            )
        }
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(colors.softGreen)
                .border(1.dp, colors.outline, CircleShape)
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = strings.navProfile,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

data class QuickActionItem(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val container: Color,
    val route: String
)

@Composable
fun QuickAccessGrid(onNavigate: (String) -> Unit = {}, strings: AppStrings = AppStrings.English) {
    val colors = FarmerTheme.colors
    val items = listOf(
        QuickActionItem(strings.navMarket, Icons.Default.BarChart, colors.primary, colors.softMint, Screen.Market.route),
        QuickActionItem(strings.weatherTitle, Icons.Default.WbCloudy, colors.weatherBlue, colors.softBlue, Screen.Weather.route),
        QuickActionItem(strings.schemesTitle, Icons.Default.LibraryBooks, colors.alertGreen, colors.softLavender, Screen.Schemes.route),
        QuickActionItem(strings.diseaseTitle, Icons.Default.BugReport, colors.diseaseOrange, colors.softOrange, Screen.Disease.route),
        QuickActionItem(strings.alertsTitle, Icons.Default.Notifications, colors.alertPurple, colors.softPurple, Screen.Alerts.route),
        QuickActionItem(strings.cropNotesTitle, Icons.Default.MenuBook, colors.cropBrown, colors.softBrown, Screen.CropNotes.route)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        items.chunked(3).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { item ->
                    QuickAccessTile(item, onNavigate, Modifier.weight(1f))
                }
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
            .border(1.dp, colors.outline, RoundedCornerShape(22.dp))
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
        Spacer(Modifier.height(7.dp))
        Text(
            text = item.title,
            color = colors.textPrimary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
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
