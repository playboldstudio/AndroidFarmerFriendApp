# Farmer Friend — UI/UX Improvement Guide

> A screen-by-screen, data-grounded guide to improving the app's design and user experience.
> Every suggestion is verified against what the current APIs actually return.
> Suggestions that need new data sources are clearly flagged.

---

## Part 1 — What We Actually Have (API Reality Check)

Before suggesting improvements, here's what each data source actually provides. This determines what's feasible today vs what needs new infrastructure.

### Market Prices API (`vegetablemarketprice.com`)

| Endpoint | Returns | What we use | What we DON'T use |
|----------|---------|-------------|-------------------|
| `GET /api/dataapi/market/{location}/daywisedata?date=YYYY-MM-DD` | `VegetableMarketResponse<VegetableItem>` — list of items with `vegetablename`, `price` (Any), `retailprice`, `units`, `id`, `table.imageUrl` | Item name, price, units, image (veg only) | `startDateLimit`, `date`, `dataHeaders`, `retailprice` (mapped but rarely shown) |
| `GET /api/dataapi/fruits/{location}/daywisedata` | Same shape with `FruitItem` — `fruitname`, `price`, `retailprice`, `units` | Name, price, units | `retailprice` |
| `GET /api/dataapi/nonveg/{location}/daywisedata` | Same shape with `NonVegItem` — `nonvegname`, `price`, `units` | Name, price, units | — |
| `GET /api/dataapi/gold/{location}/daywisedata` | Same shape with `GoldItem` — `name`, `price`, `units` | Name, price, units | — |

**Critical limitation:** The API returns **only today's data** (or yesterday as fallback). There is **no historical price endpoint**. The `Crop.trend` field is hardcoded to `0.0` and `Crop.priceDiff` is never populated — because there's nothing to diff against.

### Egg Prices API (`ncee-ten.vercel.app`)

| Endpoint | Returns | What we use |
|----------|---------|-------------|
| `GET /api/egg-prices?month=MM&year=YYYY&type=Daily Rate Sheet` | `List<NcecEggPriceItem>` — each with `city`, `price`, `avg` | Matched city's price + avg (for priceDiffPercent) |

**Key:** This is the **only** category with trend data — `avg` (historical average) lets us compute `priceDiffPercent = ((price - avg) / avg) * 100`. No other market category has a baseline to compare against.

### Open-Meteo Weather API

| What we request | What we get | What we show |
|-----------------|-------------|--------------|
| `current`: temperature_2m, relative_humidity_2m, apparent_temperature, precipitation, weather_code, wind_speed_10m, wind_direction_10m | `OpenMeteoCurrent` — all fields nullable | Temperature, humidity, feels-like, wind speed, wind direction, weather code (→ condition text) |
| `daily`: temperature_2m_max, temperature_2m_min, precipitation_probability_max, weather_code | `OpenMeteoDaily` — 5-day arrays | High/low temps, rain chance, weather icons for forecast days |
| `forecast_days=5` | 5 daily entries | 5-day forecast pills |

**What we DON'T request (but Open-Meteo offers for free):**

| Available field | Could be useful for | Effort to add |
|-----------------|---------------------|---------------|
| `current.precipitation` (mm) | Actual rainfall amount, not just probability | Low — add to `current` param string + map to `WeatherInfo` |
| `current.is_day` (bool) | Day/night icon switching | Low — add to `current` param string |
| `daily.sunrise` / `daily.sunset` | Farm planning (spray timing, field work windows) | Low — add to `daily` param string |
| `daily.uv_index_max` | UV safety warnings | Low |
| `daily.wind_speed_10m_max` | Wind-based spray decisions | Low |
| Hourly forecast (`hourly` param) | Hourly temps for the next 24h | Medium — need new param + LazyRow |
| `daily.soil_temperature_6cm_max/min` | Crop-specific soil temp guidance | Low — free from Open-Meteo |
| `daily.et0_fao_evapotranspiration` | Irrigation scheduling | Low — free from Open-Meteo |

**Key insight:** Open-Meteo is free and returns far more data than we currently request. Adding `hourly`, `sunrise`, `sunset`, `uv_index_max`, `wind_speed_10m_max`, `soil_temperature_6cm_max/min`, and `et0_fao_evapotranspiration` requires **zero new API integrations** — just additional query params.

### Wikipedia Scraper

| Function | Search terms | Returns |
|----------|-------------|---------|
| `fetchCropNotes(language)` | Agriculture-related terms per language | `List<CropNote>` — `title`, `content` (cleaned excerpt), `sourceUrl` |
| `fetchDiseases(language)` | Plant disease terms per language | `List<Disease>` — `name`, `cropAffected` (from `description`), `sourceUrl` |
| `fetchSchemes(language)` | Agricultural scheme terms per language | `List<Scheme>` — `title`, `description` (cleaned excerpt), `sourceUrl` |

**What Wikipedia gives us:** Article title, a ~200-char cleaned excerpt, and a link to the full article.

**What it does NOT give us:** Structured disease data (symptoms, treatment, prevention), scheme eligibility criteria, seasonal relevance, crop-specific images.

### Geocoding API (Open-Meteo)

| Endpoint | Returns | What we use |
|----------|---------|-------------|
| `GET /v1/search?name=X&count=10` | `List<GeocodingResult>` — `name`, `latitude`, `longitude`, `admin1`, `country` | Location search + lat/lon for weather |

### Firestore (Alerts)

| What we store | Source |
|--------------|--------|
| Price alerts (from `PriceAlertWorker`) | Koyambedu prices only — **hardcoded location** |
| Weather alerts (from `WeatherAlertWorker`) | User's saved location — correct |
| Crop alerts (from FCM / `AlertSeedData`) | Push notifications from server |

**Bug:** `PriceAlertWorker` hardcodes `"koyambedu"` as the market — alerts are always for Koyambedu regardless of user location.

---

## Part 2 — Feasible Improvements (No New APIs Needed)

These suggestions work with data the app already receives or can request from existing free APIs.

---

### Global Improvements

#### 1. Dynamic Color (Android 12+) — ✅ Feasible

Let users personalize with wallpaper colors. Keep farmer green as the default.

```kotlin
val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= 31 ->
        if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
    darkTheme -> darkColorScheme(...)
    else -> lightColorScheme(...)
}
```

**Toggle location:** Profile → Appearance → "Use wallpaper colors"
**Fallback:** Static farmer green palette for Android < 12.

*Source: android-design-guidelines §1.1, R1.11*

---

#### 2. Navigation Transitions — ✅ Feasible

All 12 screen switches are instant. Adding motion gives spatial context.

| Transition | Duration | Easing | When |
|------------|----------|--------|------|
| Tab switch (Home↔Market↔Weather↔Alerts↔Profile) | 200ms | Crossfade | Same-depth navigation |
| Sub-screen push (Home→Disease, etc.) | 300ms enter, 200ms exit | Slide from right + fade | Deeper navigation |
| Bottom sheet open/close | 250ms | M3 emphasized decelerate/accelerate | Modal overlays |

```kotlin
composable(
    Screen.Disease.route,
    enterTransition = { slideInHorizontally(tween(300)) + fadeIn(tween(200)) },
    exitTransition = { slideOutHorizontally(tween(200)) + fadeOut(tween(150)) }
)
```

*Source: motion-system.md §Duration Bands*

---

#### 3. Predictive Back Gesture (Android 13+) — ✅ Feasible

Show a live preview of the previous screen on edge swipe.

```xml
<application android:enableOnBackInvokedCallback="true">
```

For sub-screens (Disease, Schemes, CropNotes, Language), the exiting screen scales down toward the swipe edge.

*Source: android-design-guidelines §2.4, R2.10–R2.13*

---

#### 4. Haptic Feedback — ✅ Feasible

| Trigger | Haptic Type |
|---------|-------------|
| Pull-to-refresh trigger | `HapticFeedbackType.LongPress` |
| Filter chip tap | `HapticFeedbackType.TextHandleMove` |
| FAB tap (share) | `HapticFeedbackType.LongPress` |

```kotlin
val haptic = LocalHapticFeedback.current
Modifier.clickable {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    onClick()
}
```

*Source: mobile-app-ui-design §Polish & Details*

---

#### 5. Edge-to-Edge Display — ✅ Feasible

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
    }
}
```

The Scaffold already handles `innerPadding`, so content stays clear of bars. The green background extends behind the status bar.

*Source: android-design-guidelines §3.3, R3.6–R3.7*

---

#### 6. Snackbar for Quick Feedback — ✅ Feasible

| Action | Snackbar Message |
|--------|-----------------|
| Market share FAB tap | "Copied to clipboard ✓" |
| Alert marked as read | "Alert marked as read" |
| Location changed | "Showing prices for {location}" |
| Language changed | "App language updated" |

*Source: android-design-guidelines §5.5, R5.14–R5.16*

---

#### 7. Keyboard Padding on Search Screens — ✅ Feasible

On Market, Disease, Schemes, and CropNotes screens, add `imePadding()` so the keyboard doesn't cover the search field.

```kotlin
Column(modifier = Modifier.imePadding()) {
    SearchField(...)
    LazyColumn { ... }
}
```

---

#### 8. Fix PriceAlertWorker Location — ✅ Feasible (Bug Fix)

`PriceAlertWorker` hardcodes `"koyambedu"`. It should read the user's saved location from `LocationPrefs` (same as `WeatherAlertWorker` does). Without this, every user gets Koyambedu-specific price alerts regardless of their actual market.

---

### Home Screen

**Current state:** Header, greeting, weather card, market preview strip (3 crops), quick-access grid, farm tip card.

**Data available:** Weather (`WeatherInfo` with temp, condition, humidity, wind, forecast), top vegetable prices from Koyambedu (hardcoded fallback), unread alert count.

#### Idea 1: Time-of-Day Contextual Greeting — ✅ Feasible

Go beyond "Good morning, {name}":

| Time | Style |
|------|-------|
| 5–7 AM | "Early riser 🌅 {name}" + "Good time to check today's prices" |
| 8–11 AM | "Good morning, {name}" + weather-based tip |
| 12–2 PM | "Good afternoon, {name}" + "Markets are active now" |
| 5–7 PM | "Good evening, {name}" + tomorrow's forecast preview |
| After 8 PM | "Wind down, {name}" + "Tomorrow's forecast" prominent |

**Data needed:** System clock — already available. No API change.

---

#### Idea 2: Expand Weather Card to 3-Day Mini-Forecast — ✅ Feasible

When the user taps the compact weather card, expand it in-place to reveal 3 day-pills instead of navigating away.

```kotlin
Column(modifier = Modifier.animateContentSize(tween(300))) {
    CompactWeatherCard(...)
    if (expanded) {
        MiniForecastRow(forecast.take(3))  // 3 day-pills
    }
}
```

**Data available:** `WeatherInfo.forecast` already contains 5 `ForecastDay` items.

---

#### Idea 3: Swipeable Market Preview — ✅ Feasible

Replace the static 3-crop strip with a horizontal `LazyRow` the user can swipe through.

```kotlin
LazyRow(horizontalArrangement = ArracedBy(10.dp)) {
    items(crops) { crop -> MarketPreviewCard(crop) }
    item { SeeAllCard(onClick = { onNavigate(Screen.Market.route) }) }
}
```

**Data available:** `marketPreview` list (currently `.take(3)`, can show more). The API returns many items; we just truncate to 4.

**Note:** Currently the app fetches 4 crops for preview but the UI only renders 3. The 4th is silently discarded.

---

#### Idea 4: Weather-Based Farm Tip — ✅ Feasible

Replace the static `farmTipGeneric` with a dynamic tip based on weather data:

| Condition | Tip |
|-----------|-----|
| Rain > 60% tomorrow | "Good day to skip irrigation — rain expected" |
| Wind > 30 km/h | "Avoid pesticide spraying today — wind too strong" |
| Humidity > 85% | "Watch for fungal disease risk in humid conditions" |
| Temp > 40°C | "Hydrate crops during peak heat — mulching helps" |
| Temp < 10°C | "Cover sensitive crops — cold stress risk tonight" |

**Data available:** `WeatherInfo` has humidity, wind speed, rain chance, temperature, and 5-day forecast.

---

#### Idea 5: Quick-Access Grid Badge Counts — ✅ Feasible

Add badge counts to the quick-access tiles:

```
🪙 Market (3 new)     🌤 Weather
📋 Schemes            🐛 Disease
🔔 Alerts (5)         📝 Crop Notes
```

**Data available:** Market count from `marketPreview.size`, alert count already available via `FirestoreAlertRepository.listenForUnreadCount()`. Schemes/disease counts need to be passed from their ViewModels (or counted from the full list).

---

### Market Screen

**Current state:** Hero title, search, chip filters (Veg/Fruit/NonVeg/Gold/Egg), LazyColumn with crop cards, pull-to-refresh, share FAB.

**Data available per item:** Name (local + English), formatted price string, numeric price, units, image URL (vegetables only), retail price (veg + fruit only).

#### Idea 1: Animated Filter Switch — ✅ Feasible

Crossfade list content when switching filters instead of snapping:

```kotlin
AnimatedContent(targetState = state.selectedFilter) { filter ->
    LazyColumn { items(state.filteredCrops) { ... } }
}
```

Duration: 200ms crossfade.

---

#### Idea 2: Sort Options — ✅ Feasible (Client-Side)

Add a sort icon in the top bar:

```
↕ Sort by:
  • Price: Low to High
  • Price: High to Low
  • Name: A to Z
```

**Data available:** `Crop.priceValue` (numeric) and `Crop.nameEng` — both are already in the model.

---

#### Idea 3: Retail Price Comparison — ✅ Feasible

For vegetables and fruits, show retail price alongside wholesale price:

```
┌─────────────────────────────────────┐
│ 🍅 Tomato              ₹32/kg      │
│    Retail: ₹40/kg                  │
└─────────────────────────────────────┘
```

**Data available:** `Crop.retailPrice` is already mapped from the API for vegetables and fruits. It's just not prominently displayed.

---

#### Idea 4: Pull-to-Refresh with Contextual Status — ✅ Feasible

During refresh, show timing-aware messages:

| Phase | Message |
|-------|---------|
| 0–2s | "Fetching latest prices…" |
| 2–5s | "Market data can take a moment…" |
| 5s+ | "Still connecting to market servers…" |

The app already has `SlowNetworkState` — apply the same honest-loading philosophy.

---

#### Idea 5: Egg Price Trend Context — ✅ Feasible

Egg prices are the **only** category with trend data (`priceDiffPercent` from `avg`). Make this prominent:

```
┌─────────────────────────────────────┐
│ 🥚 Egg (Chennai)                    │
│    ₹5.50 / piece                    │
│    ▲ +8.2% above monthly average    │
└─────────────────────────────────────┘
```

**Data available:** `Crop.priceDiffPercent` is already computed in `NcecEggPriceItem.toCrop()`. Just need better UI treatment.

---

#### Idea 6: Image Thumbnails for All Categories — ⚠️ Partially Feasible

Currently only vegetables have images (`ItemImageTable.imageUrl`). Fruits, nonveg, gold, and eggs have no image data from the API.

**Option A (free):** Use category-appropriate Material icons as placeholders (already done — `BarChart` icon). Improve by using distinct icons per category: 🍅 for tomato, 🍎 for apple, 🥚 for egg, etc.

**Option B (needs new API):** Find a food image API. This would require a new integration — not worth the complexity for the current scope.

---

#### ❌ NOT Feasible: Price History Sparkline

The market API returns **only today's daily data**. There is no historical endpoint. Without 7+ days of data, sparklines cannot be generated.

**To make this work:** Would need a backend that stores daily price snapshots over time, or find an API with historical data. This is a significant new infrastructure requirement.

---

#### ❌ NOT Feasible: Price Comparison Mode (Multi-Day)

Same reason — no historical data to compare across days.

---

### Weather Screen

**Current state:** Hero title, location pill, 5-day forecast pills, stat tiles (temp, humidity, wind, rain), farm tip card.

**Data available:** Current conditions (temp, humidity, feels-like, wind speed/direction, weather code), 5-day daily forecast (high/low, rain probability, weather code).

#### Idea 1: Enable Hourly Forecast — ✅ Feasible (No New API)

Open-Meteo supports hourly data — we just need to request it:

```kotlin
@GET("v1/forecast")
suspend fun getForecast(
    @Query("latitude") latitude: Double,
    @Query("longitude") longitude: Double,
    @Query("current") current: String = "temperature_2m,...",
    @Query("daily") daily: String = "temperature_2m_max,...",
    @Query("hourly") hourly: String = "temperature_2m,weather_code",  // NEW
    @Query("forecast_days") forecastDays: Int = 5,
    @Query("timezone") timezone: String = "auto"
): OpenMeteoResponse
```

Add a horizontally scrollable hourly row below the daily forecast:

```
Now   1PM   2PM   3PM   4PM   5PM   6PM   7PM
☀️    🌤    🌤    ☁️    ☁️    🌧    🌧    🌤
32°   33°   33°   31°   30°   28°   27°   26°
```

**Data available:** Free from Open-Meteo. Just add `hourly` to the query params and a new DTO field.

---

#### Idea 2: Sunrise/Sunset Times — ✅ Feasible (No New API)

```kotlin
@Query("daily") daily: String = "temperature_2m_max,...,sunrise,sunset"
```

Show a visual arc for planning field work:

```
🌅 6:12 AM        ☀️ 12:30 PM        🌇 6:45 PM
   ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓░░░░░░░░░░░░░░░░░░░░░░░░
                   ↑ current time
```

**Data available:** Free from Open-Meteo `daily.sunrise` and `daily.sunset`.

---

#### Idea 3: UV Index Warning — ✅ Feasible (No New API)

```kotlin
@Query("daily") daily: String = "...,uv_index_max"
```

| UV Level | Alert |
|----------|-------|
| 8+ | "Very High UV — avoid midday field exposure" |
| 6–7 | "High UV — wear protective clothing" |
| 3–5 | "Moderate UV — sunscreen recommended" |

**Data available:** Free from Open-Meteo `daily.uv_index_max`.

---

#### Idea 4: Wind-Based Spray Advisory — ✅ Feasible (No New API)

```kotlin
@Query("daily") daily: String = "...,wind_speed_10m_max"
```

| Wind | Advisory |
|------|----------|
| > 30 km/h | "Avoid pesticide spraying — wind will drift chemicals" |
| > 20 km/h | "Use drift-reducing nozzles — moderate wind expected" |
| < 10 km/h | "Ideal conditions for spraying" |

**Data available:** `current.windSpeed` is already fetched. Adding `daily.wind_speed_10m_max` gives the forecast max.

---

#### Idea 5: Agricultural Weather Insights — ✅ Feasible

Turn raw weather data into farming actions using data we already have:

| Data We Have | Insight |
|-------------|---------|
| `daily.precipitationProbabilityMax[0]` > 60% | "Skip irrigation tomorrow — rain expected" |
| `current.windSpeed` > 30 km/h | "Don't spray pesticides today — wind too strong" |
| `current.humidity` > 85% | "High humidity — watch for fungal disease risk" |
| `daily.tempMin[0]` < 10°C | "Cover sensitive crops tonight — cold stress risk" |
| `daily.tempMax[0]` > 40°C | "Irrigate early morning — extreme heat expected" |

**Data available:** All fields already in `WeatherInfo` and `ForecastDay`.

---

#### Idea 6: Soil Temperature & Evapotranspiration — ✅ Feasible (No New API)

```kotlin
@Query("daily") daily: String = "...,soil_temperature_6cm_max,soil_temperature_6cm_min,et0_fao_evapotranspiration"
```

| Metric | Farm Use |
|--------|----------|
| Soil temp 6cm | Seed germination timing, root activity |
| ET0 (evapotranspiration) | Irrigation scheduling — how much water crops need |

**Data available:** Free from Open-Meteo. Requires new DTO fields + UI tiles.

---

#### ❌ NOT Feasible: 7-Day Forecast

Open-Meteo returns up to 16 days, but the current `forecast_days=5` is intentional for mobile UX density. Extending to 7 is feasible but may be too much for a mobile card. Consider 5 as the sweet spot.

---

### Alerts Screen

**Current state:** Hero title with unread badge, chip filters (All/Price/Weather/Crop), alert list with RowCard.

**Data available:** Alert title, message, timestamp (Long), type (PRICE/WEATHER/CROP), isRead, actionRoute, location.

#### Idea 1: Swipe-to-Dismiss with Undo — ✅ Feasible

Allow swiping an alert to mark as read, with snackbar undo:

```kotlin
SwipeToDismissBox(
    state = dismissState,
    backgroundContent = {
        Box(Modifier.fillMaxSize().background(colors.alertGreen)) {
            Icon(Icons.Default.Check, ..., tint = Color.White)
        }
    }
) {
    AlertItem(alert)
}
```

*Source: android-design-guidelines §7.2, R7.3*

---

#### Idea 2: Time-Based Grouping — ✅ Feasible

Group alerts by time using `Alert.timestamp`:

```
Today
  ⛈️ Thunderstorm Alert — 2 hours ago
  📊 Price Alert — 5 hours ago
Yesterday
  🌧 Heavy Rain Warning — yesterday
Earlier
  🌡 Heat Wave Alert — 3 days ago
```

**Data available:** `Alert.timestamp` (Long) is already in the model. The UI already uses `relativeTimeLabel(alert.timestamp)` — just needs grouping logic.

---

#### Idea 3: Alert Priority Visual Treatment — ✅ Feasible

Differentiate severity with border accents:

| Priority | Visual |
|----------|--------|
| Critical (thunderstorm, extreme heat) | Red left border + alert icon |
| Warning (heavy rain, frost) | Orange left border |
| Info (price change, general) | Default gray border |

**Data available:** `Alert.type` (PRICE/WEATHER/CROP) + content analysis of `Alert.message` for severity.

---

#### Idea 4: "Mark All as Read" Button — ✅ Feasible

Add a text button in the header when unread count > 0:

```
Alerts (5 unread)         [Mark all read]
```

**Data available:** `AlertsState.unreadCount` already computed. `AlertsViewModel` already has `MarkAllRead` event.

---

#### Idea 5: Fix Location Staleness — ✅ Feasible (Bug Fix)

`AlertsState.locationName` is set once at ViewModel init and never updates. When the user changes location on Home/Weather, the alerts screen still shows the old name. Should re-read from `LocationPrefs` on each composition or location change event.

---

### Disease Detection Screen

**Current state:** SubScreenHeader, search field, RowCard list with source chips. Tapping opens Wikipedia in browser.

**Data available per item:** Disease name, crop affected (from `description` field), Wikipedia URL.

#### Idea 1: Disease Category Color Coding — ✅ Feasible

Group diseases by crop type with color-coded chips:

```
🍅 Tomato diseases (3)
🥔 Potato diseases (2)
🌾 Rice diseases (4)
```

**Data available:** `Disease.cropAffected` is already in the model. Just needs grouping logic + colored chips.

---

#### Idea 2: Inline Excerpt Preview — ✅ Feasible

Show the Wikipedia excerpt (which we already fetch but don't display) as a preview before opening the browser:

```
┌─────────────────────────────────────┐
│ 🐛 Late Blight                     │
│ Affects: Tomato, Potato            │
│                                    │
│ "Late blight is a disease of potato│
│  and tomato caused by..."          │
│                                    │
│ 📖 Wikipedia →                     │
└─────────────────────────────────────┘
```

**Data available:** The scraper fetches `page.excerpt` (cleaned HTML) but the `Disease` model only stores `name` and `cropAffected`. The excerpt is used as `cropAffected` fallback. To show a proper preview, we'd need to add an `excerpt` field to the `Disease` model and populate it from the scraper.

**Effort:** Low — add one field to `Disease`, pass `description ?: cleanedExcerpt` for `cropAffected`, and a separate `excerpt` for the preview.

---

#### ❌ NOT Feasible: Disease Detail Bottom Sheet with Symptoms/Treatment

Wikipedia search only returns title + excerpt + description. It does NOT return structured data like symptoms, treatment steps, or prevention measures. The scraper cannot provide this without a fundamentally different approach (parsing full Wikipedia article pages).

**To make this work:** Would need either (a) a dedicated plant disease API, or (b) on-device ML for image-based disease detection, or (c) a curated local database of common crop diseases with structured data.

---

#### ❌ NOT Feasible: Camera-Based Disease Detection (in current scope)

Would require ML Kit image labeling or a custom TFLite model — a significant new feature, not a design improvement. Out of scope for this guide.

---

### Government Schemes Screen

**Current state:** SubScreenHeader, search, RowCard list with source chips. Tapping opens Wikipedia in browser.

**Data available per item:** Scheme title, description (excerpt), Wikipedia URL.

#### Idea 1: Bookmark/Save Schemes — ✅ Feasible

Allow users to save schemes locally with a heart/bookmark icon:

```kotlin
IconButton(onClick = { viewModel.toggleBookmark(scheme) }) {
    Icon(
        if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
        contentDescription = "Save scheme"
    )
}
```

Add a "Saved" tab to filter. Store bookmarks in `DataStore` or Room.

**Data available:** Scheme title + sourceUrl for identification.

---

#### Idea 2: Inline Excerpt Preview — ✅ Feasible

Same as Disease — show the Wikipedia excerpt as a preview:

```
┌─────────────────────────────────────┐
│ 📋 PM-KISAN                        │
│ "PM-KISAN is a Central Sector..."  │
│                                    │
│ 📖 Wikipedia →                     │
└─────────────────────────────────────┘
```

**Data available:** `Scheme.description` already contains the cleaned excerpt — just needs to be displayed.

---

#### ❌ NOT Feasible: Scheme Eligibility Quick-Check

Schemes are Wikipedia article excerpts — they have **no eligibility fields, no application deadlines, no status indicators**. The scraper returns `title`, `excerpt`, and `sourceUrl` only.

**To make this work:** Would need a dedicated government schemes API (e.g., MyScheme.gov.in API) with structured eligibility criteria, deadlines, and application status.

---

#### ❌ NOT Feasible: Scheme Status Indicators (Active/Deadline/Expired)

Same reason — Wikipedia excerpts don't have status or deadline data.

---

### Crop Notes Screen

**Current state:** SubScreenHeader, LocPill, search, horizontal crop filter chips, LazyColumn of CropNoteItems.

**Data available per item:** Crop name, title, content (excerpt), sourceUrl.

#### Idea 1: Expandable Note Cards — ✅ Feasible

Instead of showing full content inline, use expandable cards:

```kotlin
Column(modifier = Modifier.animateContentSize(tween(300))) {
    Text(content.take(100), maxLines = 2)
    if (expanded) {
        Text(fullContent)
    }
}
```

**Data available:** `CropNote.content` already contains the cleaned excerpt.

---

#### Idea 2: Emoji-Enhanced Crop Filter Chips — ✅ Feasible

Replace text-only chips with emoji-enhanced chips:

```
🌾 All  |  🍚 Rice  |  🥬 Vegetables  |  🌽 Pulses  |  🥜 Groundnut
```

**Data available:** `CropNote.cropName` is already extracted. Just needs emoji mapping.

---

#### Idea 3: "Seasonal Tips" Section — ⚠️ Partially Feasible

Add a time-aware section at the top:

```
🌱 Right Now (September)
  • Kharif season — good time for rice, maize, soybean
  • Watch for pest attacks in humid weather
```

**Problem:** `CropNote.season` is always empty (never populated by the scraper). We'd need either:
- A hardcoded seasonal calendar (simple, maintainable)
- Or enrich the scraper to extract season info from Wikipedia content

**Recommendation:** Use a hardcoded seasonal calendar keyed to `java.util.Calendar.MONTH`. Low effort, high value.

---

### Profile Screen

**Current state:** Identity card, settings sections, theme toggle, logout.

#### Idea 1: Settings with Icons and Descriptions — ✅ Feasible

Upgrade settings from plain text to icon + title + subtitle:

```
🌐  Language
    Tamil (தமிழ்)

🔔  Notifications
    Price alerts, Weather alerts, Daily digest

🌙  Appearance
    System default
```

**Data available:** Current settings are hardcoded — just needs visual upgrade.

---

#### Idea 2: Fix Profile Location Staleness — ✅ Feasible (Bug Fix)

`ProfileState.locationName` is set once at init and never updates. Should re-read from `LocationPrefs` when the composable resumes.

---

#### Idea 3: Fix TempEmail Dead Field — ✅ Feasible (Bug Fix)

`ProfileState.tempEmail` exists but the edit form has no email input. Either remove the field or add an email edit capability.

---

### Splash Screen

**Current state:** Logo scale animation (0.7→1.0, 450ms), text fade (400ms), 900ms hold, then navigate.

#### Idea 1: Reduce Dead Time — ✅ Feasible

The 900ms post-animation hold is dead time. Add a tagline fade-in during the hold:

```
Logo scales in (450ms)
App name fades in (400ms)
↓
"Made for Indian Farmers" fades in (300ms delay)
↓
Navigate after 600ms total hold
```

---

#### Idea 2: Shared Element Transition to Home — ⚠️ Partially Feasible

After splash, the logo could animate into the Home screen header. Requires Navigation Compose 2.8+ shared element support. Check current dependency version.

---

### Auth Screen

**Current state:** Full-screen sign in/up with email/password.

#### Idea 1: Social Login Options — ✅ Feasible

Add Google Sign-In as a one-tap option:

```
┌─────────────────────────────┐
│  🌾 Welcome to Farmer       │
│     Friend                  │
│                             │
│  [Sign in with Google]      │
│                             │
│  ── or continue with ──     │
│                             │
│  [Email field]              │
│  [Password field]           │
│  [Sign In]                  │
└─────────────────────────────┘
```

Reduces friction for Android users with Google accounts.

---

## Part 3 — Architecture Cleanup Suggestions

Based on the full data-flow analysis, here are structural improvements worth considering.

### 1. Unify Pull-to-Refresh Pattern

**Problem:** Three different PTR implementations:
- Market: spinner tied to real `UiState.Loading` ✅
- Alerts: custom `awaitingRefresh` flag synced to state transitions ✅
- Disease/Schemes/CropNotes: local `isRefreshing` boolean with fixed 600ms delay ❌

**Fix:** Extract a shared `rememberPullToRefreshState()` composable that ties to `UiState.Loading` consistently.

---

### 2. Remove Dead Filter Scaffolding

**Problem:** `DiseaseFilterType` and `SchemeFilterType` both only contain `ALL` with no UI rendering. The filter infrastructure exists but is unused.

**Fix:** Either implement real filters (by crop type, by category) or remove the dead code. Since Wikipedia data doesn't provide structured categories, removing is simpler.

---

### 3. Populate or Remove Crop.trend

**Problem:** `Crop.trend` is hardcoded to `0.0` in 4 of 5 categories. `Crop.priceDiff` is never populated. These fields exist in the model but carry no data.

**Options:**
- **Remove:** Clean up the model. Only keep `priceDiffPercent` (used by egg).
- **Compute from API data:** The API response has `dataHeaders` and `startDateLimit` — investigate whether these contain any historical context.
- **Future-proof:** If a historical API becomes available, the fields are ready. But for now, they're dead weight.

---

### 4. Unify ViewModel Base Classes

**Problem:** Home, Weather, Alerts, Profile use `AndroidViewModel` (need Application). Market, Disease, Schemes, CropNotes use plain `ViewModel`.

**Fix:** For screens that need `LocationPrefs` or `UserPrefs`, pass them as constructor parameters or use Hilt DI. This avoids the `AndroidViewModel` dependency and makes testing easier.

---

### 5. Fix searchLocations Event Bypass

**Problem:** Home and Weather expose `searchLocations()` as a direct suspend function callable from Composables, bypassing the event sealed interface.

**Fix:** Route through events:
```kotlin
sealed interface WeatherEvent {
    data class SearchLocation(val query: String) : WeatherEvent
}
```

---

### 6. Fix PriceAlertWorker Hardcoded Location

**Problem:** `PriceAlertWorker` hardcodes `"koyambedu"`. All price alerts are Koyambedu-specific regardless of user location.

**Fix:** Read from `LocationPrefs` in the worker's `doWork()`. The `WeatherAlertWorker` already does this correctly.

---

### 7. Add Missing Fields to Disease Model

**Problem:** The scraper fetches `page.excerpt` but the `Disease` model only stores `name` and `cropAffected`. The excerpt is partially used as `cropAffected` fallback.

**Fix:**
```kotlin
data class Disease(
    val id: Int,
    val name: String,
    val cropAffected: String,
    val excerpt: String,     // NEW — cleaned Wikipedia excerpt
    val sourceUrl: String,
    val imageUrl: String
)
```

This enables inline preview without opening the browser.

---

## Part 4 — Empty States Guide

Every screen's empty state should be actionable:

| Screen | Current | Improved |
|--------|---------|----------|
| Market | Generic icon + title | "No prices available for {market} yet. Markets may publish by 9 AM." + [Switch market →] |
| Disease | Icon + "No diseases found" | "Search for a crop disease above, or browse by category." + category chips |
| Schemes | Icon + "No schemes found" | "Try different keywords, or check eligibility criteria." + [Saved schemes →] |
| Crop Notes | Icon + "No notes found" | "Select a crop above to see farming notes." + crop chips highlighted |
| Weather | Icon + "No data" | "Weather data unavailable. Check your location settings." + [Change location →] |

---

## Part 5 — Motion & Animation Guidelines

### Named Transition System

| Animation | Duration | Curve | Usage |
|-----------|----------|-------|-------|
| Tap feedback | 100ms | M3 standard | Button press, chip select |
| Micro-animation | 150ms | M3 standard decelerate | Icon appear, badge pop |
| State change | 200ms | M3 standard | Filter switch, toggle |
| Screen transition | 300ms | M3 standard decelerate | Nav push/pop |
| Modal entry | 250ms | M3 emphasized decelerate | Bottom sheet, dialog |
| Expand/collapse | 250ms | spring(damping=0.75, stiffness=1500) | Card expand, content reveal |

### Reduced Motion

Respect the system's "Reduce motion" setting:

```kotlin
val reduceMotion = LocalContext.current.resources.configuration
    .animDurationScale == 0f
val duration = if (reduceMotion) 0 else 300
```

### Stagger Rules

When animating lists (market items, alerts, crop notes):
- Delay between items: 20–40ms
- Total stagger budget: max 200ms
- Only first 5–7 items stagger; rest arrive together

---

## Part 6 — Accessibility Quick Wins

| # | Change | Impact | Effort |
|---|--------|--------|--------|
| 1 | Add `contentDescription` to PillChip, DayPill, QuickAccessTile | TalkBack navigation | Low |
| 2 | Increase touch targets to 48dp (HeaderIconButton, SearchField clear) | Accessibility floor | Low |
| 3 | Fix `textTertiary` contrast (darken in both palettes) | WCAG AA compliance | Low |
| 4 | Add `mergeDescendants` to RowCard items | TalkBack groups icon+title+subtitle | Low |
| 5 | Test at 200% font scale | No clipping/overlap | Medium |

---

## Part 7 — Dark Mode Polish Checklist

| Area | Check |
|------|-------|
| CompactWeatherCard gradient | Green gradient on green-tinted dark surface — verify text readability |
| WeatherChipStat glass pill | `Color.White.copy(alpha=0.18f)` on gradient — verify in dark mode |
| Bottom sheet background | Should use `surface` token, not hardcoded white |
| Splash screen gradient | `softMint → background` — verify both look intentional in dark |
| SearchField clear button | 32dp touch target in dark mode — verify visibility |
| SourceChip in DiseaseScreen | `surfaceMuted` in dark mode — verify enough contrast with `textTertiary` |

---

## Quick Reference: Design Principles

| Principle | Rule | Source |
|-----------|------|--------|
| **60/30/10 Color** | 60% neutral base, 30% text/dark, 10% accent (green) | mobile-app-ui-design |
| **8-Point Grid** | All spacing divisible by 4 or 8 | mobile-app-ui-design |
| **48dp Touch Targets** | Android minimum floor | android-design-guidelines R6.5 |
| **1.4–1.6x Line Height** | For body text readability | quality-bars.md |
| **One FAB Per Screen** | Market screen only (share) | android-design-guidelines R5.1 |
| **Peak-End Rule** | Design the best moment + the last impression | mobile-app-ui-design |
| **Recognition > Recall** | Labels + icons beat icon-only | heuristics.md |
| **Reduce Friction** | Primary actions in thumb zone (bottom 1/3) | mobile-app-ui-design |
| **Honest Loading** | Show real timing, not fake instant | quality-bars.md §States |
| **Emotional Feedback** | Pair functional with delight (haptics, animations) | mobile-app-ui-design |
