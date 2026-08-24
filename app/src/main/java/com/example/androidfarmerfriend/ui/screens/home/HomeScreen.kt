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
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.R
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.data.model.Crop
import com.example.androidfarmerfriend.data.model.WeatherInfo
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.ui.components.EmptyState
import com.example.androidfarmerfriend.ui.components.FarmTipCard
import com.example.androidfarmerfriend.ui.components.HeroTitle
import com.example.androidfarmerfriend.ui.components.LocationPickerSheet
import com.example.androidfarmerfriend.ui.components.CompactWeatherCard
import com.example.androidfarmerfriend.ui.components.LocPill
import com.example.androidfarmerfriend.ui.components.NotificationPermissionBanner
import com.example.androidfarmerfriend.ui.components.ShimmerList
import com.example.androidfarmerfriend.ui.components.weatherIconFor
import com.example.androidfarmerfriend.ui.navigation.Screen
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme
import com.example.androidfarmerfriend.ui.theme.FarmerSpacing
import com.example.androidfarmerfriend.ui.theme.FarmerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            onProfileClick = { onNavigate(Screen.Profile.route) },
            onAlertsClick = { onNavigate(Screen.Alerts.route) }
        )

        NotificationPermissionBanner(modifier = Modifier.padding(bottom = FarmerSpacing.md))

        HeroTitle(text = state.greeting, accent = state.greetingName)

        Spacer(Modifier.height(FarmerSpacing.xs))

        DateLine()

        Spacer(Modifier.height(FarmerSpacing.s))

        LocPill(
            text = state.selectedLocation?.name.orEmpty(),
            onClick = { showLocationPicker = true }
        )

        Spacer(Modifier.height(FarmerSpacing.s))

        when (val weatherState = state.weatherState) {
            is UiState.Loading -> ShimmerList(rowCount = 1, rowHeight = 150.dp)
            is UiState.Error -> EmptyState(
                icon = Icons.Default.CloudOff,
                title = strings.weatherNoData
            )
            is UiState.Success -> CompactWeatherCard(
                weather = weatherState.data,
                strings = strings,
                modifier = Modifier.padding(top = FarmerSpacing.s),
                onClick = { onNavigate(Screen.Weather.route) }
            )
        }

        if (state.marketPreview.isNotEmpty()) {
            SectionHeader(
                title = strings.todaysMarketTitle,
                action = strings.viewAllLabel,
                onAction = { onNavigate(Screen.Market.route) }
            )
            MarketPreviewStrip(crops = state.marketPreview)
        }

        SectionHeader(title = strings.quickAccess)

        QuickAccessGrid(onNavigate = onNavigate, strings = strings)

        Spacer(Modifier.height(FarmerSpacing.lg))

        FarmTipCard(title = strings.farmTipTitle, body = strings.farmTipGeneric)

        Spacer(Modifier.height(FarmerSpacing.xxl))
    }
}

@Composable
private fun DateLine() {
    val label = remember {
        SimpleDateFormat("EEEE · dd MMMM yyyy", Locale.ENGLISH).format(Date()).uppercase()
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        letterSpacing = 0.6.sp,
        color = FarmerTheme.colors.textTertiary
    )
}

@Composable
private fun HomeHeader(strings: AppStrings, onProfileClick: () -> Unit, onAlertsClick: () -> Unit) {
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
        HeaderIconButton(
            icon = Icons.Default.Notifications,
            contentDescription = strings.navAlerts,
            showDot = true,
            onClick = onAlertsClick
        )
        Spacer(Modifier.width(FarmerSpacing.s))
        HeaderIconButton(
            icon = Icons.Default.Person,
            contentDescription = strings.navProfile,
            onClick = onProfileClick
        )
    }
}

@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    showDot: Boolean = false
) {
    val colors = FarmerTheme.colors
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(colors.surface)
            .border(1.dp, colors.outline, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = colors.textSecondary,
            modifier = Modifier.size(19.dp)
        )
        if (showDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 7.dp, end = 8.dp)
                    .size(8.dp)
                    .background(colors.alertRed, CircleShape)
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    val colors = FarmerTheme.colors
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = FarmerSpacing.lg, bottom = FarmerSpacing.s)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        if (action != null && onAction != null) {
            Text(
                text = "$action ›",
                color = colors.primary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
    }
}

@Composable
private fun MarketPreviewStrip(crops: List<Crop>) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        crops.take(3).forEach { crop ->
            val unit = crop.price.substringAfter("/ ", "").ifBlank { crop.units }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(FarmerTheme.colors.surface)
                    .border(1.dp, FarmerTheme.colors.outline, RoundedCornerShape(18.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = crop.nameEng.ifBlank { crop.name },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = FarmerTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "₹${"%.0f".format(crop.priceValue)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = FarmerTheme.colors.primaryDeep
                )
                Text(
                    text = "per $unit",
                    style = MaterialTheme.typography.labelSmall,
                    color = FarmerTheme.colors.textTertiary
                )
            }
        }
    }
}

data class QuickActionItem(
    val title: String,
    val icon: ImageVector,
    val tint: Color,
    val tileBackground: Color,
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
            .background(item.tileBackground, RoundedCornerShape(20.dp))
            .clickable { onNavigate(item.route) }
            .padding(vertical = 15.dp, horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(colors.surface.copy(alpha = 0.85f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(item.icon, contentDescription = item.title, tint = item.tint, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.height(8.dp))
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

// Retained reference so the arrow import stays honest even if LocPill changes.
private val ChevronDown = Icons.Default.KeyboardArrowDown

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AndroidFarmerFriendTheme {
        HomeScreen()
    }
}
