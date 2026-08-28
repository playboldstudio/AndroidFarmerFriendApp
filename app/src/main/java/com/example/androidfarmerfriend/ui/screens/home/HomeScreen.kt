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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.example.androidfarmerfriend.R
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.localization.LanguagePrefs
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.data.model.Crop
import com.example.androidfarmerfriend.data.model.WeatherInfo
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.data.repository.FirestoreAlertRepository
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
    val state by viewModel.state.collectAsStateWithLifecycle()

    val strings = LocalAppStrings.current
    var showLocationPicker by remember { mutableStateOf(false) }

    // Live unread-alert count for the header bell. Uses its own repository
    // listener slot so it never fights the alerts screen's list listener.
    var unreadCount by remember { mutableIntStateOf(0) }
    DisposableEffect(Unit) {
        val repo = FirestoreAlertRepository.getInstance()
        repo.listenForUnreadCount(limit = 30) { unreadCount = it }
        onDispose { repo.stopUnreadListening() }
    }

    LaunchedEffect(strings) {
        viewModel.setStrings(strings)
        viewModel.onEvent(HomeEvent.LoadInitial)
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

    com.example.androidfarmerfriend.ui.components.CenteredMaxWidth(
        modifier = Modifier.background(FarmerTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = FarmerSpacing.lg)
                .verticalScroll(rememberScrollState())
        ) {
        HomeHeader(
            strings = strings,
            unreadCount = unreadCount,
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

        Spacer(Modifier.height(FarmerSpacing.md))

        if (state.marketPreview.isNotEmpty()) {
            val colors = FarmerTheme.colors
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = FarmerSpacing.lg, bottom = FarmerSpacing.s)
            ) {
                Text(
                    text = strings.navMarket,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${strings.viewAllLabel} ›",
                    color = colors.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigate(Screen.Market.route) }
                )
            }
            MarketPreviewStrip(crops = state.marketPreview)
        }

        SectionHeader(title = strings.quickAccess)

        QuickAccessGrid(onNavigate = onNavigate, strings = strings)

        Spacer(Modifier.height(FarmerSpacing.lg))

        FarmTipCard(title = strings.farmTipTitle, body = strings.farmTipGeneric)

        Spacer(Modifier.height(FarmerSpacing.xxl))
        }
    }
}

@Composable
private fun DateLine() {
    val strings = LocalAppStrings.current
    // Localized day/month names; the app language drives the locale.
    val context = LocalContext.current
    val label = remember(strings) {
        val locale = LanguagePrefs(context).selectedLanguage.locale()
        SimpleDateFormat("EEEE · dd MMMM yyyy", locale).format(Date())
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        letterSpacing = 0.6.sp,
        color = FarmerTheme.colors.textTertiary
    )
}

@Composable
private fun HomeHeader(
    strings: AppStrings,
    unreadCount: Int,
    onProfileClick: () -> Unit,
    onAlertsClick: () -> Unit
) {
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
            showDot = unreadCount > 0,
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
            // 44dp: comfortably above the minimum touch-target floor while
            // keeping the 38dp visual footprint.
            .size(44.dp)
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
            modifier = Modifier.size(20.dp)
        )
        if (showDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 9.dp, end = 10.dp)
                    .size(9.dp)
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
    val colors = FarmerTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        crops.take(3).forEach { crop ->
            val unit = crop.price.substringAfter("/ ", "").ifBlank { crop.units }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.outline, RoundedCornerShape(18.dp))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Veg image
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.softMint),
                    contentAlignment = Alignment.Center
                ) {
                    if (crop.imageUrl.isNotBlank()) {
                        SubcomposeAsyncImage(
                            model = crop.imageUrl,
                            contentDescription = crop.nameEng.ifBlank { crop.name },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop,
                            loading = {
                                Icon(
                                    Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            error = {
                                Icon(
                                    Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        )
                    } else {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = crop.nameEng.ifBlank { crop.name },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "₹${"%.0f".format(crop.priceValue)}/$unit",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.primaryDeep,
                    textAlign = TextAlign.Center
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
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AndroidFarmerFriendTheme {
        HomeScreen()
    }
}
