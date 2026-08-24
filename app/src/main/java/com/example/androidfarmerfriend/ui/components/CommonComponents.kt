package com.example.androidfarmerfriend.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.data.model.WeatherInfo
import com.example.androidfarmerfriend.ui.theme.FarmerTheme
import com.example.androidfarmerfriend.ui.theme.TrendRed
import kotlin.math.abs

/* ------------------------------------------------------------------ */
/* Weather icon mapping (kept from original implementation)            */
/* ------------------------------------------------------------------ */
fun weatherIconFor(code: Int): ImageVector = when (code) {
    0, 1 -> Icons.Default.WbSunny
    2 -> Icons.Default.WbCloudy
    3 -> Icons.Default.Cloud
    45, 48 -> Icons.Default.CloudQueue
    in 51..67 -> Icons.Default.Grain
    in 71..77 -> Icons.Default.Grain
    in 80..82 -> Icons.Default.Grain
    in 85..86 -> Icons.Default.Grain
    in 95..99 -> Icons.Default.Thunderstorm
    else -> Icons.Default.WbCloudy
}

/* ------------------------------------------------------------------ */
/* Cards                                                               */
/* ------------------------------------------------------------------ */

/** Rounded white card with a soft shadow — matches `.card` (24dp radius). */
@Composable
fun FarmerCard(
    modifier: Modifier = Modifier,
    containerColor: Color = FarmerTheme.colors.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(24.dp),
        content = content
    )
}

/* ------------------------------------------------------------------ */
/* Typography helpers                                                  */
/* ------------------------------------------------------------------ */

/** Big bold page title with optional green accent substring — matches `.hero-title`. */
@Composable
fun HeroTitle(
    text: String,
    accent: String? = null,
    modifier: Modifier = Modifier
) {
    val colors = FarmerTheme.colors
    val hasAccent = !accent.isNullOrBlank() && text.contains(accent)
    val annotated = if (hasAccent) {
        buildAnnotatedString {
            val idx = text.indexOf(accent!!)
            append(text.substring(0, idx))
            withStyle(SpanStyle(color = colors.primary)) { append(accent) }
            append(text.substring(idx + accent.length))
        }
    } else {
        AnnotatedString(text)
    }
    Text(
        text = annotated,
        color = colors.textPrimary,
        style = MaterialTheme.typography.headlineMedium,
        modifier = modifier
    )
}

/**
 * Sub-screen header: soft rounded back button + HeroTitle. Keeps the exact
 * title size/alignment of the tab screens (`.hero-title`) while giving pushed
 * destinations (Language / Privacy / Terms) a back affordance.
 */
@Composable
fun SubScreenHeader(
    title: String,
    onBack: () -> Unit,
    accent: String? = null,
    modifier: Modifier = Modifier
) {
    val colors = FarmerTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .clip(RoundedCornerShape(13.dp))
                .background(colors.surfaceMuted)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colors.textPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        HeroTitle(text = title, accent = accent, modifier = Modifier.weight(1f, fill = false))
    }
}

/** Location / context pill — matches `.loc-pill`. */
@Composable
fun LocPill(
    text: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = FarmerTheme.colors
    val clickableModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        modifier = modifier
            .then(clickableModifier)
            .background(colors.surface, RoundedCornerShape(999.dp))
            .border(BorderStroke(1.dp, colors.outline), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(colors.primaryBright, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = colors.textSecondary,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.width(3.dp))
        Icon(
            Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(14.dp)
        )
    }
}

/** Section heading with optional trailing action link — matches `.sec-title`. */
@Composable
fun SectionTitle(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = FarmerTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 22.dp, bottom = 12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = title,
            color = colors.textPrimary,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        if (actionText != null && onAction != null) {
            Text(
                text = actionText,
                color = colors.primary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
    }
}

/** Muted search field — matches `.search`. */
@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val colors = FarmerTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceMuted, RoundedCornerShape(15.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(9.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = colors.textTertiary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    color = colors.textPrimary,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                ),
                cursorBrush = SolidColor(colors.primary),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (value.isNotEmpty()) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                Icons.Default.Close,
                contentDescription = "Clear",
                tint = colors.textTertiary,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onValueChange("") }
            )
        }
    }
}

/* ------------------------------------------------------------------ */
/* Chips                                                               */
/* ------------------------------------------------------------------ */

/** Pill-shaped filter chip row — matches `.chips` / `.chip` / `.chip.active`. */
@Composable
fun PillChipGroup(
    filters: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            PillChip(
                text = filter,
                selected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) }
            )
        }
    }
}

/** Single pill chip. */
@Composable
fun PillChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = FarmerTheme.colors
    val bg = if (selected) colors.primary else colors.surface
    val fg = if (selected) colors.onPrimary else colors.textSecondary
    val border = if (selected) colors.primary else colors.outline
    Text(
        text = text,
        color = fg,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(BorderStroke(1.dp, border), RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 8.dp)
    )
}

/* ------------------------------------------------------------------ */
/* Icon tiles + list rows                                              */
/* ------------------------------------------------------------------ */

/** Soft-tinted rounded tile containing a colored icon — matches `.orb` / `.avatar`. */
@Composable
fun TintIconCircle(
    icon: ImageVector,
    tint: Color,
    container: Color,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    cornerRadius: Dp = 15.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.42f)
        )
    }
}

/** List row — matches `.row`: avatar + title/subtitle + end slot. */
@Composable
fun RowCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = FarmerTheme.colors.primary,
    iconContainer: Color = FarmerTheme.colors.softGreen,
    leading: (@Composable () -> Unit)? = null,
    end: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    unread: Boolean = false
) {
    val colors = FarmerTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.surface,
        shadowElevation = 1.dp,
        border = if (unread) BorderStroke(1.5.dp, colors.alertRed) else null
    ) {
        Row(
            modifier = Modifier
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leading != null) {
                leading()
                Spacer(modifier = Modifier.width(13.dp))
            } else if (icon != null) {
                TintIconCircle(
                    icon = icon,
                    tint = iconTint,
                    container = iconContainer,
                    size = 46.dp,
                    cornerRadius = 15.dp
                )
                Spacer(modifier = Modifier.width(13.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = colors.textPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        maxLines = 2
                    )
                }
            }
            if (end != null) {
                Spacer(modifier = Modifier.width(10.dp))
                end()
            }
        }
    }
}

/** Rising / falling percent tag — matches `.trend.up` / `.trend.down`. */
@Composable
fun TrendTag(percent: Double?) {
    val colors = FarmerTheme.colors
    val value = percent ?: 0.0
    if (value == 0.0) return
    val up = value > 0
    val arrow = if (up) "▲" else "▼"
    val text = "$arrow ${"%.0f".format(abs(value))}%"
    Text(
        text = text,
        color = if (up) colors.primary else TrendRed,
        style = MaterialTheme.typography.labelMedium.copy(
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (up) colors.softGreen else colors.softRed)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

/** Colored label chip on matching soft background — matches `.alert-chip`. */
@Composable
fun AlertChip(
    text: String,
    color: Color,
    container: Color
) {
    Text(
        text = text.uppercase(),
        color = color,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(container)
            .padding(horizontal = 9.dp, vertical = 4.dp)
    )
}

/* ------------------------------------------------------------------ */
/* Menu row                                                            */
/* ------------------------------------------------------------------ */

/** Settings/details row — matches `.menu-row`. */
@Composable
fun MenuRow(
    icon: ImageVector,
    iconTint: Color = FarmerTheme.colors.primary,
    iconContainer: Color = FarmerTheme.colors.softGreen,
    title: String,
    trailing: String? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = FarmerTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TintIconCircle(
            icon = icon,
            tint = iconTint,
            container = iconContainer,
            size = 36.dp,
            cornerRadius = 12.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = colors.textPrimary,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            Text(
                text = trailing,
                color = colors.textSecondary,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(16.dp)
        )
    }
}

/* ------------------------------------------------------------------ */
/* Weather hero                                                        */
/* ------------------------------------------------------------------ */

/** Green-gradient weather hero — matches `.weather-hero`. Shared by Home + Weather. */
@Composable
fun WeatherHeroCard(
    weather: WeatherInfo,
    strings: AppStrings,
    modifier: Modifier = Modifier,
    centered: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val colors = FarmerTheme.colors
    val gradient = Brush.linearGradient(
        colorStops = arrayOf(
            0f to colors.primaryBright,
            0.55f to colors.primary,
            1f to colors.primaryDeep
        ),
        start = Offset.Zero,
        end = Offset.Infinite
    )
    val (tempValue, tempUnit) = splitTemperature(weather.temperature)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        // Decorative glows
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-50).dp)
                .size(170.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.14f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-30).dp, y = 70.dp)
                .size(150.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(colors.primaryBright.copy(alpha = 0.25f), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        Column(modifier = Modifier.padding(22.dp)) {
            if (centered) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = weather.condition,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Icon(
                        weatherIconFor(weather.weatherCode),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    HeroTemperature(tempValue, tempUnit, large = true)
                    if (weather.todayHigh.isNotEmpty() && weather.todayLow.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "H:${weather.todayHigh}  L:${weather.todayLow}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = weather.location,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = weather.condition,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Icon(
                        weatherIconFor(weather.weatherCode),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                HeroTemperature(tempValue, tempUnit, large = false)
                if (weather.todayHigh.isNotEmpty() && weather.todayLow.isNotEmpty()) {
                    Text(
                        text = "H:${weather.todayHigh}  L:${weather.todayLow}",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (weather.feelsLike.isNotBlank()) {
                    Text(
                        text = "${strings.feelsLike}: ${weather.feelsLike}",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            WeatherHeroStats(
                weather = weather,
                strings = strings,
                modifier = Modifier.padding(top = 18.dp)
            )
        }
    }
}

/** Big temperature with unit superscript. */
@Composable
private fun HeroTemperature(value: String, unit: String, large: Boolean) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = value,
            color = Color.White,
            style = if (large) MaterialTheme.typography.displayLarge
                    else MaterialTheme.typography.displayMedium
        )
        if (unit.isNotEmpty()) {
            Text(
                text = unit,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private fun splitTemperature(temp: String): Pair<String, String> {
    val match = Regex("^(\\d+)\\s*(.*)$").find(temp.trim())
    return if (match != null) match.groupValues[1] to match.groupValues[2]
    else temp to ""
}

/** White stat row inside the weather hero — matches `.weather-stats`. */
@Composable
fun WeatherHeroStats(
    weather: WeatherInfo,
    strings: AppStrings,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.18f))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            HeroStat(icon = Icons.Default.WaterDrop, value = weather.rainChance, label = strings.rain)
            HeroStat(icon = Icons.Default.Opacity, value = weather.humidity, label = strings.humidity)
            HeroStat(icon = Icons.Default.Air, value = weather.windSpeed, label = strings.wind)
        }
    }
}

@Composable
private fun HeroStat(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = value,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall
            )
        }
        Text(
            text = label.uppercase(),
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/**
 * Compact left-aligned weather summary used on Home and Weather tabs.
 */
@Composable
fun CompactWeatherCard(
    weather: WeatherInfo,
    strings: AppStrings,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = FarmerTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(listOf(colors.primaryBright, colors.primary, colors.primaryDeep))
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = weather.condition,
                    color = Color.White.copy(alpha = 0.95f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = weather.temperature,
                        color = Color.White,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 44.sp,
                        letterSpacing = (-2).sp
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.padding(bottom = 4.dp)) {
                        if (weather.feelsLike.isNotBlank()) {
                            Text(
                                text = strings.feelsLike + " " + weather.feelsLike,
                                color = Color.White.copy(alpha = 0.92f),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        if (weather.todayHigh.isNotEmpty() && weather.todayLow.isNotEmpty()) {
                            Text(
                                text = "H:" + weather.todayHigh + " · L:" + weather.todayLow,
                                color = Color.White.copy(alpha = 0.92f),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
            Icon(
                weatherIconFor(weather.weatherCode),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            WeatherChipText("💧 " + strings.rain + " " + weather.rainChance)
            WeatherChipText("💦 " + strings.humidity + " " + weather.humidity)
            WeatherChipText("🍃 " + strings.wind + " " + weather.windSpeed)
        }
    }
}

/** Human-readable host of a source URL, e.g. "ta.wikipedia.org". */
fun urlHostLabel(url: String): String = try {
    android.net.Uri.parse(url).host.orEmpty().removePrefix("www.").ifBlank { "" }
} catch (_: Exception) {
    ""
}

/** Small section title used across tab screens. */
@Composable
fun SectionHeaderCompat(title: String, modifier: Modifier = Modifier) {    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = FarmerTheme.colors.textPrimary,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 22.dp, bottom = 12.dp)
    )
}

@Composable
fun WeatherChipText(text: String) {
    Text(
        text = text,
        color = Color.White,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

/* ------------------------------------------------------------------ */
/* Forecast day pill                                                   */
/* ------------------------------------------------------------------ */

/** Single forecast day pill — matches `.day-pill`. */
@Composable
fun DayPill(
    day: String,
    weatherCode: Int,
    temp: String,
    selected: Boolean,
    onClick: (() -> Unit)? = null
) {
    val colors = FarmerTheme.colors
    val bg = if (selected) colors.primary else colors.surface
    val fg = if (selected) colors.onPrimary else colors.textPrimary
    val fgSub = if (selected) colors.onPrimary.copy(alpha = 0.85f) else colors.textSecondary
    Column(
        modifier = Modifier
            .width(62.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(
                if (selected) BorderStroke(0.dp, Color.Transparent)
                else BorderStroke(1.dp, colors.outline),
                RoundedCornerShape(20.dp)
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = day,
            color = fgSub,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Icon(
            weatherIconFor(weatherCode),
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = temp,
            color = fg,
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize = 12.5.sp,
                fontWeight = FontWeight.ExtraBold
            )
        )
    }
}

/* ------------------------------------------------------------------ */
/* Farm tip card                                                       */
/* ------------------------------------------------------------------ */

/** Soft-green tip card — matches the farm-tip `.card` on the weather screen. */
@Composable
fun FarmTipCard(title: String, body: String, modifier: Modifier = Modifier) {
    val colors = FarmerTheme.colors
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.softGreen)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "🌱  $title",
                color = colors.primary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = body,
                color = colors.textSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/* ------------------------------------------------------------------ */
/* Empty / error / loading                                             */
/* ------------------------------------------------------------------ */

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String? = null
) {
    val colors = FarmerTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = colors.textTertiary, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    subtitle,
                    color = colors.textTertiary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val colors = FarmerTheme.colors
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Refresh, contentDescription = null, tint = TrendRed, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(10.dp))
        Text(message, color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = LocalAppStrings.current.retry,
            color = colors.onPrimary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(colors.primary)
                .clickable(onClick = onRetry)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        )
    }
}

@Composable
fun FullScreenLoading(modifier: Modifier = Modifier) {
    val colors = FarmerTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = colors.primary,
            modifier = Modifier.size(40.dp),
            strokeWidth = 4.dp
        )
    }
}
