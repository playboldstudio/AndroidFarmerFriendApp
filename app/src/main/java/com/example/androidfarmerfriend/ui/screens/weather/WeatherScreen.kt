package com.example.androidfarmerfriend.ui.screens.weather

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.data.model.ForecastDay
import com.example.androidfarmerfriend.data.model.HourlyForecast
import com.example.androidfarmerfriend.data.model.WeatherInfo
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.ui.components.CompactWeatherCard
import com.example.androidfarmerfriend.ui.components.DayPill
import com.example.androidfarmerfriend.ui.components.ErrorState
import com.example.androidfarmerfriend.ui.components.FarmTipCard
import com.example.androidfarmerfriend.ui.components.WeatherFarmTips
import com.example.androidfarmerfriend.ui.components.HeroTitle
import com.example.androidfarmerfriend.ui.components.LocationPickerSheet
import com.example.androidfarmerfriend.ui.components.LocPill
import com.example.androidfarmerfriend.ui.components.SectionHeaderCompat
import com.example.androidfarmerfriend.ui.components.ShimmerBlockList
import com.example.androidfarmerfriend.ui.components.TintIconCircle
import com.example.androidfarmerfriend.ui.components.weatherIconFor
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme
import com.example.androidfarmerfriend.ui.theme.FarmerMotion
import com.example.androidfarmerfriend.ui.theme.FarmerSpacing
import com.example.androidfarmerfriend.ui.theme.FarmerTheme

@Composable
fun WeatherScreen(
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope? = null,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope? = null,
    viewModel: WeatherViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val strings = LocalAppStrings.current
    var showLocationPicker by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(strings) {
        viewModel.setStrings(strings)
        viewModel.onEvent(WeatherEvent.LoadInitial)
    }

    val currentLocation = state.selectedLocation
    if (showLocationPicker && currentLocation != null) {
        LocationPickerSheet(
            currentLocation = currentLocation,
            onLocationSelected = { loc ->
                showLocationPicker = false
                viewModel.onEvent(WeatherEvent.SelectLocation(loc))
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
            Spacer(Modifier.height(FarmerSpacing.lg))

            HeroTitle(text = strings.weatherTitle)

            Spacer(Modifier.height(FarmerSpacing.s))

            LocPill(
                text = state.selectedLocation?.name.orEmpty(),
                onClick = { showLocationPicker = true }
            )

        // Plain when() — no CrossfadeUiState here. AnimatedContent inside
        // a verticalScroll Column causes vertical overlap of outgoing/incoming
        // content, stacking the weather card behind stat rows and farm tip.
        when (val weatherState = state.weatherState) {
            is UiState.Loading -> ShimmerBlockList(
                blockHeight = 140.dp,
                modifier = Modifier.padding(top = FarmerSpacing.md)
            )
            is UiState.Error -> ErrorState(
                message = weatherState.message.ifBlank { strings.weatherLoadError },
                onRetry = { viewModel.onEvent(WeatherEvent.Retry) },
                modifier = Modifier.padding(top = 32.dp)
            )
            is UiState.Success -> WeatherDetailedView(weather = weatherState.data, strings = strings)
        }

        Spacer(Modifier.height(FarmerSpacing.xxl))
        }
    }
}

@Composable
fun WeatherDetailedView(weather: WeatherInfo, strings: AppStrings = AppStrings.English) {
    var selectedDay by androidx.compose.runtime.saveable.rememberSaveable { mutableIntStateOf(0) }

    CompactWeatherCard(
        weather = weather,
        strings = strings,
        modifier = Modifier.padding(top = FarmerSpacing.md)
    )

    // Hourly strip — next few hours so farmers plan spraying / irrigation.
    if (weather.hourly.isNotEmpty()) {
        Spacer(Modifier.height(FarmerSpacing.lg))
        SectionHeaderCompat(title = strings.hourlyForecastLabel)
        HourlyForecastStrip(hourly = weather.hourly)
    }

    // Sunrise / sunset — golden-hour windows for spraying.
    if (weather.sunrise.isNotBlank() || weather.sunset.isNotBlank()) {
        Spacer(Modifier.height(FarmerSpacing.lg))
        Row(horizontalArrangement = Arrangement.spacedBy(FarmerSpacing.s)) {
            if (weather.sunrise.isNotBlank()) {
                SunTimeTile(
                    icon = Icons.Default.WbSunny,
                    label = strings.sunriseLabel,
                    time = weather.sunrise,
                    iconTint = FarmerTheme.colors.diseaseOrange,
                    iconContainer = FarmerTheme.colors.softOrange,
                    modifier = Modifier.weight(1f)
                )
            }
            if (weather.sunset.isNotBlank()) {
                SunTimeTile(
                    icon = Icons.Default.WbTwilight,
                    label = strings.sunsetLabel,
                    time = weather.sunset,
                    iconTint = FarmerTheme.colors.alertPurple,
                    iconContainer = FarmerTheme.colors.softLavender,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (weather.forecast.isNotEmpty()) {
        SectionHeaderCompat(title = strings.nextDaysLabel)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            weather.forecast.forEachIndexed { index, day ->
                DayPill(
                    day = day.day,
                    weatherCode = day.weatherCode,
                    temp = day.maxTemp,
                    selected = index == selectedDay,
                    onClick = { selectedDay = index }
                )
            }
        }

        // The selected pill drives a detail card so the selection has an effect;
        // a crossfade makes the swap feel settled instead of instant.
        val detail = weather.forecast[selectedDay.coerceIn(weather.forecast.indices)]
        AnimatedContent(
            targetState = detail,
            transitionSpec = {
                (fadeIn(
                    animationSpec = tween(
                        FarmerMotion.durationNormal,
                        easing = FarmerMotion.standardDecelerate
                    )
                ) togetherWith
                    fadeOut(
                        animationSpec = tween(
                            FarmerMotion.durationNormal,
                            easing = FarmerMotion.standardDecelerate
                        )
                    ))
            },
            label = "dayDetailCrossfade"
        ) { day ->
            SelectedDayDetailCard(day = day, strings = strings)
        }

        SectionHeaderCompat(title = strings.rangeTitle)
        ForecastRangeList(forecast = weather.forecast)
    }

    SectionHeaderCompat(title = strings.detailsTitle)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FarmerTheme.colors.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WeatherStatTile(
                    icon = Icons.Default.WaterDrop,
                    iconContainer = FarmerTheme.colors.softBlue,
                    iconTint = FarmerTheme.colors.weatherBlue,
                    label = strings.rainChance,
                    value = weather.rainChance,
                    modifier = Modifier.weight(1f)
                )
                WeatherStatTile(
                    icon = Icons.Default.Opacity,
                    iconContainer = FarmerTheme.colors.softMint,
                    iconTint = FarmerTheme.colors.primary,
                    label = strings.humidity,
                    value = weather.humidity,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WeatherStatTile(
                    icon = Icons.Default.Air,
                    iconContainer = FarmerTheme.colors.softLavender,
                    iconTint = FarmerTheme.colors.alertPurple,
                    label = strings.windSpeed,
                    value = weather.windSpeed,
                    modifier = Modifier.weight(1f)
                )
                WeatherStatTile(
                    icon = Icons.Default.Explore,
                    iconContainer = FarmerTheme.colors.softOrange,
                    iconTint = FarmerTheme.colors.diseaseOrange,
                    label = strings.windDirection,
                    value = weather.windDirection,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    Spacer(Modifier.height(14.dp))

    val farmTipBody = WeatherFarmTips.tipFor(weather) ?: strings.farmTipGeneric
    FarmTipCard(title = strings.farmTipTitle, body = farmTipBody)
}

/** Detail card for whichever forecast day-pill is selected. */
@Composable
private fun SelectedDayDetailCard(day: ForecastDay, strings: AppStrings) {
    val colors = FarmerTheme.colors
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.softBlue)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    weatherIconFor(day.weatherCode),
                    contentDescription = null,
                    tint = colors.weatherBlue,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = day.day,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${day.maxTemp} / ${day.minTemp}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.primaryDeep
                )
            }
        }
    }
}

/** Rows with a gradient bar showing each day's min–max span within the week's range. */
@Composable
private fun ForecastRangeList(forecast: List<ForecastDay>) {
    fun tempOf(value: String) = value.dropLast(1).toIntOrNull() ?: 0

    val globalMin = forecast.minOfOrNull { tempOf(it.minTemp) } ?: 0
    val globalMax = forecast.maxOfOrNull { tempOf(it.maxTemp) } ?: 1
    val span = (globalMax - globalMin).coerceAtLeast(1)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        forecast.forEach { day ->
            val min = tempOf(day.minTemp)
            val max = tempOf(day.maxTemp)
            val startFraction = ((min - globalMin).toFloat() / span).coerceIn(0f, 1f)
            val endFraction = ((max - globalMin).toFloat() / span).coerceIn(0f, 1f)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(FarmerTheme.colors.surface)
                    .padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                Text(
                    text = day.day,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = FarmerTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(min = 48.dp, max = 72.dp)
                )
                Icon(
                    weatherIconFor(day.weatherCode),
                    contentDescription = null,
                    tint = FarmerTheme.colors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "${min}°",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = FarmerTheme.colors.weatherBlue,
                    textAlign = TextAlignEndCompat,
                    modifier = Modifier.width(30.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .padding(horizontal = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(3.dp))
                            .background(FarmerTheme.colors.surfaceMuted)
                    )
                    Row(modifier = Modifier.fillMaxSize()) {
                        val barFraction = (endFraction - startFraction).coerceIn(0.06f, 1f)
                        if (startFraction > 0.01f) {
                            Spacer(Modifier.weight(startFraction))
                        }
                        Box(
                            modifier = Modifier
                                .weight(barFraction)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(FarmerTheme.colors.weatherBlue, FarmerTheme.colors.diseaseOrange)
                                    )
                                )
                        )
                        if (endFraction < 0.99f) {
                            Spacer(Modifier.weight(1f - endFraction))
                        }
                    }
                }
                Text(
                    text = "${max}°",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = FarmerTheme.colors.diseaseOrange,
                    modifier = Modifier.width(30.dp)
                )
            }
        }
    }
}

private val TextAlignEndCompat = androidx.compose.ui.text.style.TextAlign.End

@Composable
private fun WeatherStatTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconContainer: Color,
    iconTint: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = FarmerTheme.colors
    Row(
        modifier = modifier
            .background(colors.surfaceMuted, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TintIconCircle(icon = icon, tint = iconTint, container = iconContainer, size = 34.dp, cornerRadius = 11.dp)
        Spacer(Modifier.width(10.dp))
        // weight(1f) lets both tiles in the row share width evenly; every text is
        // constrained + ellipsized so a long localized value/label never overflows
        // the tile or pushes its neighbour.
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

/** Horizontal strip of hourly forecast tiles (next ~8 hours). */
@Composable
private fun HourlyForecastStrip(hourly: List<HourlyForecast>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(FarmerSpacing.s),
        contentPadding = PaddingValues(horizontal = FarmerSpacing.xs)
    ) {
        items(hourly, key = { it.hourLabel }) { hour ->
            HourlyTile(hour)
        }
    }
}

@Composable
private fun HourlyTile(hour: HourlyForecast) {
    val colors = FarmerTheme.colors
    Column(
        modifier = Modifier
            .width(62.dp)
            .background(colors.surface, RoundedCornerShape(16.dp))
            .padding(vertical = FarmerSpacing.s, horizontal = FarmerSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FarmerSpacing.xs)
    ) {
        Text(
            text = hour.hourLabel,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Icon(
            weatherIconFor(hour.weatherCode),
            contentDescription = null,
            tint = colors.textPrimary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = hour.temp,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            maxLines = 1
        )
        if (hour.rainChance != null && hour.rainChance > 0) {
            Text(
                text = "${hour.rainChance}%",
                style = MaterialTheme.typography.labelSmall,
                color = colors.weatherBlue,
                maxLines = 1
            )
        }
    }
}

/** Sunrise or sunset tile — sits in a pair inside a Row. */
@Composable
private fun SunTimeTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    time: String,
    iconTint: Color,
    iconContainer: Color,
    modifier: Modifier = Modifier
) {
    val colors = FarmerTheme.colors
    Row(
        modifier = modifier
            .background(colors.surface, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TintIconCircle(
            icon = icon,
            tint = iconTint,
            container = iconContainer,
            size = 36.dp,
            cornerRadius = 12.dp
        )
        Spacer(Modifier.width(FarmerSpacing.s))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = time,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    AndroidFarmerFriendTheme {
        WeatherScreen()
    }
}
