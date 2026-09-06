package com.example.androidfarmerfriend.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.example.androidfarmerfriend.ui.auth.AuthBridge
import com.example.androidfarmerfriend.data.model.Crop
import com.example.androidfarmerfriend.data.model.ForecastDay
import com.example.androidfarmerfriend.data.model.WeatherInfo
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.data.repository.FirestoreAlertRepository
import com.example.androidfarmerfriend.ui.components.CrossfadeUiState
import com.example.androidfarmerfriend.ui.components.EmptyState
import com.example.androidfarmerfriend.ui.components.FarmTipCard
import com.example.androidfarmerfriend.ui.components.WeatherFarmTips
import com.example.androidfarmerfriend.ui.components.HeroTitle
import com.example.androidfarmerfriend.ui.components.LocationPickerSheet
import com.example.androidfarmerfriend.ui.components.CompactWeatherCard
import com.example.androidfarmerfriend.ui.components.DayPill
import com.example.androidfarmerfriend.ui.components.LocPill
import com.example.androidfarmerfriend.ui.components.NotificationPermissionBanner
import com.example.androidfarmerfriend.ui.components.ShimmerBlockList
import com.example.androidfarmerfriend.ui.components.ShimmerList
import com.example.androidfarmerfriend.ui.components.weatherIconFor
import com.example.androidfarmerfriend.ui.navigation.Screen
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme
import com.example.androidfarmerfriend.ui.theme.FarmerSpacing
import com.example.androidfarmerfriend.ui.theme.FarmerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope? = null,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope? = null,
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

    // Re-key on signed-in state too: logging out (or in) must recompute the
    // greeting, otherwise the previously cached user name lingers on the tab.
    val isSignedIn = AuthBridge.LocalIsSignedIn.current
    LaunchedEffect(strings, isSignedIn) {
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

    // Honest pull-to-refresh: spinner tracks the real weather UiState.
    PullToRefreshBox(
        isRefreshing = state.weatherState is UiState.Loading,
        onRefresh = { viewModel.onEvent(HomeEvent.Refresh) },
        modifier = Modifier.background(FarmerTheme.colors.background)
    ) {
    com.example.androidfarmerfriend.ui.components.CenteredMaxWidth(
        modifier = Modifier.fillMaxWidth()
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

        CrossfadeUiState(state = state.weatherState) { weatherState ->
            when (weatherState) {
                is UiState.Loading -> ShimmerBlockList(blockHeight = 150.dp)
                is UiState.Error -> EmptyState(
                    icon = Icons.Default.CloudOff,
                    title = strings.weatherNoData
                )
                is UiState.Success -> ExpandableWeatherCard(
                    weather = weatherState.data,
                    strings = strings,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onExpand = { onNavigate(Screen.Weather.route) }
                )
            }
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

        QuickAccessGrid(
            onNavigate = onNavigate,
            strings = strings,
            alertBadge = unreadCount,
            marketBadge = state.marketPreview.size
        )

        Spacer(Modifier.height(FarmerSpacing.lg))

        val farmTipBody = when (val ws = state.weatherState) {
            is UiState.Success -> WeatherFarmTips.tipFor(ws.data) ?: strings.farmTipGeneric
            else -> strings.farmTipGeneric
        }
        FarmTipCard(title = strings.farmTipTitle, body = farmTipBody)

        Spacer(Modifier.height(FarmerSpacing.xxl))
        }
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
                text = "Farmer Friend",
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
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(crops.size) { index ->
            val crop = crops[index]
            val unit = crop.price.substringAfter("/ ", "").ifBlank { crop.units }
            // Category-aware icon + tint + soft background for the
            // thumbnail tile — mirrors the MarketScreen pattern.
            val (fallbackIcon, iconTint, bg) = when (crop.category) {
                "vegetable" -> Triple(Icons.Default.Eco, colors.categoryVegetable, colors.softGreen)
                "fruit"     -> Triple(Icons.Default.ShoppingBasket, colors.categoryFruit, colors.softOrange)
                "nonveg"    -> Triple(Icons.Default.Restaurant, colors.categoryNonVeg, colors.softRed)
                "gold"      -> Triple(Icons.Default.Diamond, colors.categoryGold, colors.softLavender)
                "egg"       -> Triple(Icons.Default.Egg, colors.categoryEgg, colors.softBrown)
                else        -> Triple(Icons.Default.BarChart, colors.primary, colors.softMint)
            }
            Column(
                modifier = Modifier
                    .width(110.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.outline, RoundedCornerShape(18.dp))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(bg),
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
                                Icon(fallbackIcon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                            },
                            error = {
                                Icon(fallbackIcon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                            }
                        )
                    } else {
                        Icon(fallbackIcon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
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
fun QuickAccessGrid(
    onNavigate: (String) -> Unit = {},
    strings: AppStrings = AppStrings.English,
    alertBadge: Int = 0,
    marketBadge: Int = 0
) {
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
                    val badge = when (item.route) {
                        Screen.Alerts.route -> alertBadge
                        Screen.Market.route -> marketBadge
                        else -> 0
                    }
                    QuickAccessTile(item, onNavigate, Modifier.weight(1f), badge = badge)
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
    modifier: Modifier = Modifier,
    badge: Int = 0
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
            // Numeric badge — shows unread alerts or market item count.
            if (badge > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(18.dp)
                        .background(colors.alertRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (badge > 9) "9+" else "$badge",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = item.title,
            color = colors.textPrimary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Home weather card that expands in-place to show a 3-day mini-forecast.
 * Tapping the card itself navigates to the full Weather screen.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ExpandableWeatherCard(
    weather: WeatherInfo,
    strings: AppStrings,
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope?,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope?,
    onExpand: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val previewDays = weather.forecast.take(3)

    // No shared-element transition: it renders on its own overlay layer and,
    // paired with the weather screen's crossfaded scrolling column, caused the
    // card to overlap the sections below it. The expand/collapse animation
    // (animateContentSize) and the mini-forecast reveal are kept.
    val cardModifier = Modifier
        .animateContentSize()
        .padding(top = FarmerSpacing.s)

    Column(modifier = cardModifier) {
        Box {
            CompactWeatherCard(
                weather = weather,
                strings = strings,
                onClick = onExpand
            )
            // Chevron hint in the bottom-right corner.
            if (previewDays.isNotEmpty()) {
                Icon(
                    imageVector = if (expanded)
                        Icons.Default.ExpandLess
                    else
                        Icons.Default.ExpandMore,
                    contentDescription = if (expanded) strings.viewAllLabel else strings.viewAllLabel,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 14.dp, bottom = 10.dp)
                        .size(22.dp)
                        .clickable { expanded = !expanded }
                )
            }
        }

        AnimatedVisibility(visible = expanded && previewDays.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(FarmerSpacing.s),
                modifier = Modifier.padding(top = FarmerSpacing.s)
            ) {
                previewDays.forEach { day ->
                    MiniForecastTile(day = day, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/** Compact tile for the home expandable-weather mini forecast. */
@Composable
private fun MiniForecastTile(day: ForecastDay, modifier: Modifier = Modifier) {
    val colors = FarmerTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(colors.surface, RoundedCornerShape(16.dp))
            .padding(vertical = FarmerSpacing.s, horizontal = FarmerSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(FarmerSpacing.xs)
    ) {
        Text(
            text = day.day,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            weatherIconFor(day.weatherCode),
            contentDescription = null,
            tint = colors.textPrimary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "${day.maxTemp} / ${day.minTemp}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            maxLines = 1
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
