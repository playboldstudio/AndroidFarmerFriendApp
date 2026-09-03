# Farmer Friend — Final UI Transformation Guide

> A one-time comprehensive guide that fixes every structural UI issue and establishes the design system foundation.
> After this, future work touches **specific screens only** — never the whole app.

---

## Table of Contents

1. [Design System Foundation (Token Cleanup + New Tokens)](#1-design-system-foundation)
2. [Multilingual Text Resilience](#2-multilingual-text-resilience)
3. [Market Screen: Images, Categories & Coverage](#3-market-screen-images-categories--coverage)
4. [Shared Component Standards](#4-shared-component-standards)
5. [Screen-by-Screen Quick Reference](#5-screen-by-screen-quick-reference)

---

## 1. Design System Foundation

### 1.1 Dead Code Cleanup

**28 legacy standalone color vals are completely unused** outside `Color.kt`. Remove all of these:

```
FarmerGreenPrimary, FarmerGreenSecondary, FarmerGreenTertiary
FarmerGreenDarkPrimary, FarmerGreenDarkSecondary, FarmerGreenDarkTertiary
BackgroundLight, SurfaceLight, OnBackgroundLight, OnSurfaceLight, OutlineLight, SurfaceVariantLight
BackgroundDark, SurfaceDark, OnBackgroundDark, OnSurfaceDark, OutlineDark, SurfaceVariantDark
TrendGreen, WeatherYellow, WeatherBlue, AlertBlue, AlertGreen, AlertRed
GrayText, DiseaseOrange, SchemeLightGreen, AlertPurple, CropNotesBrown
```

Migrate the 3 usages of `TrendRed` in `CommonComponents.kt` to `FarmerTheme.colors.alertRed` (same hex `0xFFE53935`), then delete `TrendRed`.

**After cleanup:** `Color.kt` should contain only `FarmerColors` data class + `LightFarmerColors` + `DarkFarmerColors` + the theme composition locals. Zero top-level vals.

---

### 1.2 New Semantic Color Tokens

Add to `FarmerColors`:

```kotlin
data class FarmerColors(
    // ... existing 30 tokens ...

    // NEW: Semantic roles
    val destructive: Color,         // Irreversible actions (delete, logout)
    val onDestructive: Color,
    val destructiveContainer: Color,
    val link: Color,                // Tappable text
    val disabled: Color,            // Disabled foreground
    val disabledContainer: Color,   // Disabled background
    val scrim: Color,               // Modal overlay backdrop
    val inverseSurface: Color,      // Snackbar backgrounds
    val inverseOnSurface: Color,

    // NEW: Surface hierarchy (Material 3 surface containers)
    val surfaceDim: Color,
    val surfaceBright: Color,
    val surfaceContainer: Color,
    val surfaceContainerLow: Color,
    val surfaceContainerHigh: Color,

    // NEW: Category-specific colors (for market item thumbnails)
    val categoryVegetable: Color,
    val categoryFruit: Color,
    val categoryNonVeg: Color,
    val categoryGold: Color,
    val categoryEgg: Color,
)
```

Define values for both light and dark palettes. Example for light:

```kotlin
val LightFarmerColors = FarmerColors(
    // existing tokens...

    destructive = Color(0xFFD32F2F),
    onDestructive = Color.White,
    destructiveContainer = Color(0xFFFCE4EC),
    link = Color(0xFF1565C0),
    disabled = Color(0xFF9E9E9E),
    disabledContainer = Color(0xFFF5F5F5),
    scrim = Color(0x52000000),
    inverseSurface = Color(0xFF2C2C2C),
    inverseOnSurface = Color(0xFFF5F5F5),

    surfaceDim = Color(0xFFF5F5F5),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFFAFAFA),
    surfaceContainerLow = Color(0xFFF0F0F0),
    surfaceContainerHigh = Color(0xFFEEEEEE),

    categoryVegetable = Color(0xFF4CAF50),   // Green
    categoryFruit = Color(0xFFFF9800),        // Orange
    categoryNonVeg = Color(0xFFE53935),       // Red
    categoryGold = Color(0xFFFFD700),         // Gold
    categoryEgg = Color(0xFF8D6E63),          // Brown
)
```

---

### 1.3 Typography Fixes

**Add the missing `headlineLarge` slot:**

```kotlin
headlineLarge = TextStyle(
    fontSize = 32.sp,
    lineHeight = 40.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = (-0.5).sp
)
```

**Fix `bodyLarge` positive letter spacing** — change from `0.3.sp` to `0.sp` (or `-0.1.sp` for tighter feel). Positive letter spacing on body text looks loose in most scripts.

**Add `lineHeight` to `bodyMedium`, `bodySmall`, `labelMedium`** — currently relying on Material defaults inconsistently.

**Add a `headlineLarge` usage** — use it for the HeroTitle greeting on Home screen instead of `headlineMedium` which is slightly undersized for a hero element.

---

### 1.4 Spacing Scale Extension

Add larger values to `FarmerSpacing`:

```kotlin
object FarmerSpacing {
    val xs = 4.dp
    val s = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp

    // NEW
    val xxxl = 32.dp     // Section gaps
    val xxxxl = 40.dp    // Screen-level vertical spacing
    val section = 48.dp  // Between major sections
    val screen = 56.dp   // Top/bottom screen padding
}
```

---

### 1.5 Motion Tokens

Add a `FarmerMotion` object for consistent animation:

```kotlin
object FarmerMotion {
    // Durations
    val durationFast = 100
    val durationNormal = 200
    val durationSlow = 300
    val durationModal = 250

    // Curves (named from M3)
    val standardDecelerate = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)
    val standardAccelerate = CubicBezierEasing(0.3f, 0.0f, 1.0f, 1.0f)
    val emphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val emphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

    // Springs
    val springBouncy = spring<Float>(dampingRatio = 0.5f, stiffness = 1500f)
    val springSnappy = spring<Float>(dampingRatio = 0.75f, stiffness = 2000f)
    val springGentle = spring<Float>(dampingRatio = 0.85f, stiffness = 800f)
}
```

---

### 1.6 Iconography Tokens

Add an `FarmerIcons` object for consistent icon sizing:

```kotlin
object FarmerIcons {
    val sizeXs = 14.dp   // Inline badges
    val sizeSm = 18.dp   // Chip end-icons
    val sizeMd = 22.dp   // List item icons
    val sizeLg = 28.dp   // Section headers
    val sizeXl = 36.dp   // Empty state / hero icons

    val touchTarget = 48.dp  // Minimum touch target
}
```

Replace all scattered `Modifier.size(N.dp)` on icons with these tokens.

---

## 2. Multilingual Text Resilience

### 2.1 The Core Problem

Text length varies dramatically across languages:

| String Key | English | Tamil | Hindi | Telugu | Ratio (worst/EN) |
|-----------|---------|-------|-------|--------|-------------------|
| `feelLike` | 10 chars | 22 chars | 18 chars | 18 chars | **2.2x** |
| `retailPriceLabel` | 6 | 13 | 11 | 11 | **2.2x** |
| `rainChance` | 11 | 13 | 17 | 13 | **1.5x** |
| `termsOfUse` | 12 | 22 | 14 | 14 | **1.8x** |
| `weatherAlertLabel` | 13 | 16 | 11 | 15 | **1.2x** |
| Relative time ("1h ago") | 5 | 25+ | 20+ | 20+ | **5x** |

### 2.2 Fix: Global Overflow Rule

**Rule:** Every `Text` composable must have either `overflow = TextOverflow.Ellipsis` or `fillMaxWidth()` (which lets it wrap naturally). No exceptions.

Apply across these files:

| File | Component | Current | Fix |
|------|-----------|---------|-----|
| `CommonComponents.kt:74` | `HeroTitle` | No overflow | Add `maxLines = 2, overflow = Ellipsis` |
| `CommonComponents.kt:138` | `LocPill` text | No overflow | Add `maxLines = 1, overflow = Ellipsis` |
| `CommonComponents.kt:273` | `PillChip` text | No overflow | Add `maxLines = 1, overflow = Ellipsis` |
| `CommonComponents.kt:340` | `RowCard` title | `maxLines=2` only | Add `overflow = Ellipsis` |
| `CommonComponents.kt:340` | `RowCard` subtitle | `maxLines=2` only | Add `overflow = Ellipsis` |
| `CommonComponents.kt:431` | `AlertChip` text | No overflow | Add `maxLines = 1, overflow = Ellipsis` |
| `CommonComponents.kt:546` | `WeatherChipStat` | No overflow | Add `maxLines = 1, overflow = Ellipsis` |
| `FloatingTabBar.kt:94` | Tab label | `maxLines=1` only | Add `overflow = Ellipsis` |
| `HomeScreen.kt:448` | `QuickAccessTile` title | `maxLines=2` only | Add `overflow = Ellipsis` |
| `WeatherScreen.kt:364` | `WeatherStatTile` label | `maxLines=1` only | Add `overflow = Ellipsis` |
| `AlertsScreen.kt:239` | AlertItem message | `maxLines=3` only | Add `overflow = Ellipsis` |
| `AlertsScreen.kt:254` | Relative time label | No overflow | Add `maxLines = 1, overflow = Ellipsis` |
| `WeatherScreen.kt:233` | SelectedDayCard day | No overflow | Add `maxLines = 1, overflow = Ellipsis` |

### 2.3 Fix: Remove All Hardcoded Widths on Text Containers

**Problem:** `DayPill` uses `width(62.dp)` and `ForecastRangeList` uses `width(64.dp)` for day labels. Tamil "திங்கள்" and Telugu "సోమవారं" don't fit.

**Fix:** Replace fixed widths with `widthIn(min = N.dp, max = M.dp)` or `wrapContentWidth()`:

```kotlin
// BEFORE (DayPill.kt line 589)
Column(modifier = Modifier.width(62.dp) ...)

// AFTER
Column(modifier = Modifier.widthIn(min = 56.dp, max = 80.dp) ...)
```

```kotlin
// BEFORE (ForecastRangeList line 281)
Text(modifier = Modifier.width(64.dp), ...)

// AFTER
Text(
    modifier = Modifier.widthIn(min = 48.dp).wrapContentWidth(),
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    ...
)
```

### 2.4 Fix: WeatherChipStat Label Shortening

The `feelLike` label is 22 chars in Tamil vs 10 in English. The chip concatenates `"$label $value"` — too long for small pills.

**Fix strategy:** Use shorter labels for inline contexts:

```kotlin
// In AppStrings, add shortened variants for inline use:
val feelLikeShort: String     // "Feels" / "உணர்" / "ऐसा" / "అని"
val humidityShort: String     // "Humid" / "ஈரப்" / "नमी" / "తేమ"
```

Or use abbreviations with icons instead of text labels.

### 2.5 Fix: Relative Time Strings

Tamil relative time strings like "1 மணிநேரத்திற்கு முன்" are 5x longer than English "1h ago".

**Fix:** Use short-form relative time:

```kotlin
// BEFORE (English: "2 hours ago", Tamil: "2 மணிநேரத்திற்கு முன்")
// AFTER (English: "2h ago", Tamil: "2 முன்", Hindi: "2 घं पहले")
```

Create a `relativeTimeShort()` function that produces compact strings:

```kotlin
fun relativeTimeShort(timestamp: Long, strings: AppStrings): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / 60_000
    val hours = diff / 3_600_000
    val days = diff / 86_400_000
    return when {
        minutes < 1 -> strings.timeJustNow      // "Now" / "இப்போது"
        minutes < 60 -> "${minutes}${strings.timeMinShort}"  // "5m" / "5 நிம"
        hours < 24 -> "${hours}${strings.timeHourShort}"     // "2h" / "2 மணி"
        days < 7 -> "${days}${strings.timeDayShort}"         // "3d" / "3 நா"
        else -> relativeTimeLabel(timestamp, strings)         // fallback to full form
    }
}
```

### 2.6 Fix: QuickAccessTile Grid Sizing

The 3-column grid has tiles at `Modifier.weight(1f)` with `4.dp` horizontal padding. On a 360dp screen, each tile is ~102dp. Tamil "பயிர் குறிப்புகள்" at `labelMedium` wraps to 2 lines and may overflow.

**Fix:** Reduce tile text to `labelSmall` for non-English, OR use `fillMaxWidth()` + `wrapContentHeight()` with proper `maxLines = 2, overflow = Ellipsis`. The current 2-line max is fine — just add ellipsis.

---

## 3. Market Screen: Images, Categories & Coverage

### 3.1 The Problem

| Category | Markets | Has API Images | Thumbnail |
|----------|---------|----------------|-----------|
| Vegetables | 391 | ✅ Yes (ItemImageTable) | Real photos |
| Fruits | 6 | ❌ No | Generic ShoppingCart icon |
| Non-Veg | 10 | ❌ No | Generic ShoppingCart icon |
| Gold | 1 | ❌ No | Generic ShoppingCart icon |
| Egg | 20 | ❌ No | Generic ShoppingCart icon |

The visual gap is stark: vegetables look polished with real photos, everything else looks like a placeholder.

### 3.2 Fix: Category-Aware Fallback Icons

Replace the hardcoded `Icons.Default.ShoppingCart` in `CropThumbnail` with category-specific icons and colors:

```kotlin
@Composable
fun CropThumbnail(crop: Crop, modifier: Modifier = Modifier) {
    val colors = FarmerTheme.colors

    if (crop.imageUrl.isNotBlank()) {
        // Real photo (vegetables) — existing Coil logic
        SubcomposeAsyncImage(...)
    } else {
        // Category-aware fallback
        val (icon, tint, bg) = when (crop.category) {
            "vegetable" -> Triple(Icons.Default.Eco, colors.categoryVegetable, colors.softGreen)
            "fruit"     -> Triple(Icons.Default.ShoppingBasket, colors.categoryFruit, colors.softOrange)
            "nonveg"    -> Triple(Icons.Default.Restaurant, colors.categoryNonVeg, colors.softRed)
            "gold"      -> Triple(Icons.Default.Diamond, colors.categoryGold, colors.softLavender)
            "egg"       -> Triple(Icons.Default.Egg, colors.categoryEgg, colors.softBrown)
            else        -> Triple(Icons.Default.ShoppingCart, colors.textSecondary, colors.surfaceMuted)
        }

        Box(
            modifier = modifier
                .size(46.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        }
    }
}
```

**This alone makes every category visually distinct** without any new API data.

### 3.3 Fix: Category Color Consistency

Apply category colors throughout the market screen, not just thumbnails:

| Element | Current | Fix |
|---------|---------|-----|
| Filter chip active color | All use `primary` (green) | Use category color for active chip |
| Card leading icon bg | All `softMint` | Use category-specific `soft` color |
| Empty state icon tint | Generic | Use category color |

```kotlin
// FilterChip active color
FilterChip(
    selected = isSelected,
    colors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = categoryColorFor(filter),  // NEW
        selectedLabelColor = onCategoryColorFor(filter),
    ),
    ...
)
```

### 3.4 Fix: Market Picker Grouping by Coverage

The market picker currently shows a flat list. With 391 vegetable markets vs 1 gold market, users need context.

**Fix:** Group the market picker by tier with explanatory headers:

```
📍 Major Markets (20)
   Koyambedu, Chennai, Bangalore, Kolkata...

🗺 States (37)
   Tamil Nadu, Karnataka, Kerala...

🏙 Cities (334)
   Tenkasi, Coimbatore...
```

This already exists as `MarketGroup.MAJOR_MARKETS / STATES / CITIES` — just need to render group headers in the picker UI.

### 3.5 Fix: Empty State per Category

When a category has few markets, show a helpful empty state instead of a blank screen:

```kotlin
// When user selects "Gold" in a non-Chennai market:
EmptyState(
    icon = Icons.Default.Diamond,
    title = strings.goldOnlyChennai,
    subtitle = strings.goldOnlyChennaiDesc,
    action = {
        TextButton(onClick = { /* auto-switch to Chennai */ }) {
            Text(strings.switchToChennai)
        }
    }
)
```

### 3.6 ❌ NOT Feasible: Real Product Images for Non-Veg/Gold/Egg

The `vegetablemarketprice.com` API only provides `ItemImageTable` for vegetable items. Fruits, nonveg, gold, and egg DTOs do not have image fields. Getting real images would require:
- A new food image API integration
- Or a curated local image asset library
- Or ML-based image generation

**Recommendation:** The category-aware icon system (§3.2) is the right solution. It's consistent, fast, and doesn't depend on external image availability.

---

## 4. Shared Component Standards

### 4.1 The "One-Time Foundation" Principle

After this transformation, the following components are the **only shared building blocks** any screen should use. New screens compose from these; existing screens migrate to them gradually.

```
CommonComponents.kt
├── Layout
│   ├── CenteredMaxWidth          — adaptive content cap
│   └── adaptiveContentMaxWidth() — width breakpoints
│
├── Headers
│   ├── SubScreenHeader           — back button + title
│   ├── SectionHeader             — section title + optional action
│   └── HeroTitle                 — greeting / hero text
│
├── Cards
│   ├── RowCard                   — generic list item (icon + title + subtitle + end slot)
│   ├── CompactWeatherCard        — weather preview card
│   └── FarmTipCard               — tip of the day
│
├── Inputs
│   ├── SearchField               — text search with clear button
│   ├── PillChipGroup             — horizontal scrollable filter chips
│   └── PillChip                  — individual filter chip
│
├── Badges
│   ├── AlertChip                 — type label chip
│   ├── TrendTag                  — price change indicator
│   ├── FreshnessChip             — data freshness indicator
│   ├── MarketInfoChip            — market/category info pill
│   └── UnreadBadge               — notification count badge
│
├── Weather
│   ├── WeatherChipStat           — stat pill (temp, humidity, etc.)
│   └── DayPill                   — forecast day card
│
├── Market
│   ├── CropThumbnail             — product image / category icon
│   ├── MarketCropItem            — full market list item
│   └── MarketPreviewCard         — home screen preview card
│
├── States
│   ├── EmptyState                — no data with optional action
│   ├── ErrorState                — error with retry
│   ├── ShimmerList               — loading skeleton
│   └── NotificationPermissionBanner
│
├── Navigation
│   ├── FloatingTabBar            — bottom tab bar
│   ├── LocPill                   — location display pill
│   └── LocationPickerSheet       — location search bottom sheet
│
└── Utils
    ├── urlHostLabel()            — extract hostname from URL
    └── weatherIconFor()          — weather code → icon
```

### 4.2 Component Contract: Every Text Must Handle Overflow

Going forward, every new composable that renders text must follow this pattern:

```kotlin
Text(
    text = displayText,
    style = MaterialTheme.typography.bodyMedium,
    maxLines = 2,                          // ALWAYS set
    overflow = TextOverflow.Ellipsis,       // ALWAYS set
    modifier = Modifier.fillMaxWidth()      // OR fixed width with min/max
)
```

**Exception:** Numeric-only text (prices, counts) that is guaranteed short.

### 4.3 Component Contract: No Hardcoded Widths

No component may use `Modifier.width(N.dp)` for text containers. Use one of:

| Pattern | When |
|---------|------|
| `Modifier.fillMaxWidth()` | Text should take all available space |
| `Modifier.weight(1f)` | Text is in a Row/Column and should share space |
| `Modifier.widthIn(min, max)` | Text needs a size range |
| `Modifier.wrapContentWidth()` | Text should be its natural size |

### 4.4 Component Contract: Category Colors

Every component that deals with market data must accept or derive a category color:

```kotlin
// Utility function
fun categoryColor(category: String, colors: FarmerColors): Pair<Color, Color> {
    return when (category) {
        "vegetable" -> colors.categoryVegetable to colors.softGreen
        "fruit"     -> colors.categoryFruit to colors.softOrange
        "nonveg"    -> colors.categoryNonVeg to colors.softRed
        "gold"      -> colors.categoryGold to colors.softLavender
        "egg"       -> colors.categoryEgg to colors.softBrown
        else        -> colors.textSecondary to colors.surfaceMuted
    }
}
```

---

## 5. Screen-by-Screen Quick Reference

After the foundation work (§1-4), these are the specific changes per screen. Each entry is independent — a developer can pick any screen and implement it without touching others.

### Home Screen

| Change | Component | Data Source | Effort |
|--------|-----------|-------------|--------|
| Add `overflow = Ellipsis` to HeroTitle greeting | `HeroTitle` | System clock | 5 min |
| Make market preview a `LazyRow` (swipeable) | `MarketPreviewStrip` | `state.marketPreview` | 30 min |
| Weather-based dynamic farm tip | `FarmTipCard` | `WeatherInfo` fields | 1 hr |
| Time-of-day greeting variant | `HomeState.greeting` | System clock | 30 min |
| Add badge counts to quick-access grid | `QuickAccessGrid` | Alert count + list sizes | 1 hr |
| Expand weather card to 3-day mini-forecast | `CompactWeatherCard` | `WeatherInfo.forecast` | 1 hr |

### Market Screen

| Change | Component | Data Source | Effort |
|--------|-----------|-------------|--------|
| Category-aware fallback icons + colors | `CropThumbnail` | `crop.category` | 30 min |
| Category-colored filter chips | `PillChipGroup` | `FilterType` | 30 min |
| Sort options (price/name) | New bottom sheet | Client-side sort | 1 hr |
| Show retail price for veg/fruit | `MarketCropItem` | `crop.retailPrice` | 15 min |
| Egg price trend prominence | `MarketCropItem` | `crop.priceDiffPercent` | 15 min |
| Fix `PriceAlertWorker` hardcoded location | Worker | `LocationPrefs` | 15 min |
| Market picker with group headers | Picker sheet | `MarketGroup` | 1 hr |

### Weather Screen

| Change | Component | Data Source | Effort |
|--------|-----------|-------------|--------|
| Enable hourly forecast (new API param) | New `LazyRow` | Open-Meteo `hourly` | 2 hr |
| Sunrise/sunset arc | New composable | Open-Meteo `daily.sunrise/sunset` | 2 hr |
| UV index warning tile | `WeatherStatTile` | Open-Meteo `daily.uv_index_max` | 30 min |
| Wind-based spray advisory | `FarmTipCard` | `WeatherInfo.windSpeed` | 30 min |
| Agricultural weather insights | Dynamic tips | All weather fields | 1 hr |
| Fix day label hardcoded `width(64.dp)` | `ForecastRangeList` | Replace with `widthIn` | 15 min |
| Fix `WeatherStatTile` overflow | `WeatherStatTile` | Add `overflow = Ellipsis` | 5 min |

### Alerts Screen

| Change | Component | Data Source | Effort |
|--------|-----------|-------------|--------|
| Swipe-to-dismiss with undo | Alert list | `FirestoreAlertRepository` | 1 hr |
| Time-based grouping (Today/Yesterday/Earlier) | Alert list | `Alert.timestamp` | 1 hr |
| Alert priority visual treatment | Alert cards | `Alert.type` + message | 1 hr |
| "Mark all as read" button | Header | `AlertsState.unreadCount` | 15 min |
| Fix location staleness | `AlertsState` | `LocationPrefs` | 15 min |
| Fix relative time overflow | Alert cards | `relativeTimeShort()` | 15 min |

### Disease Screen

| Change | Component | Data Source | Effort |
|--------|-----------|-------------|--------|
| Add `excerpt` field to `Disease` model | Model + scraper | Wikipedia `description` | 1 hr |
| Inline excerpt preview (no browser open) | `DiseaseItem` | `Disease.excerpt` | 30 min |
| Crop-based category grouping + colors | Filter chips | `Disease.cropAffected` | 1 hr |

### Schemes Screen

| Change | Component | Data Source | Effort |
|--------|-----------|-------------|--------|
| Bookmark/save schemes locally | ViewModel + DataStore | Scheme title + URL | 2 hr |
| Inline excerpt display | `SchemeItem` | `Scheme.description` | 15 min |
| Remove dead `SchemeFilterType` | ViewModel | — | 10 min |

### Crop Notes Screen

| Change | Component | Data Source | Effort |
|--------|-----------|-------------|--------|
| Expandable note cards | `CropNoteItem` | `CropNote.content` | 1 hr |
| Emoji-enhanced crop filter chips | Filter chips | `CropNote.cropName` | 30 min |
| Hardcoded seasonal tips section | New composable | Calendar month | 1 hr |

### Profile Screen

| Change | Component | Data Source | Effort |
|--------|-----------|-------------|--------|
| Settings with icons + subtitles | Settings list | Hardcoded config | 1 hr |
| Fix location staleness | `ProfileState` | `LocationPrefs` | 15 min |
| Remove dead `tempEmail` field | ViewModel | — | 5 min |

### Splash Screen

| Change | Component | Data Source | Effort |
|--------|-----------|-------------|--------|
| Reduce hold + add tagline fade-in | `SplashScreen` | — | 30 min |

### Auth Screen

| Change | Component | Data Source | Effort |
|--------|-----------|-------------|--------|
| Google Sign-In button | `AuthScreen` | Firebase Auth | 4 hr |

---

## Implementation Order

The work is organized into **independent phases**. Each phase compiles and ships independently.

### Phase 1: Foundation Cleanup (do first)
- Remove 28 dead legacy color vals from `Color.kt`
- Migrate `TrendRed` usages to `FarmerTheme.colors.alertRed`
- Add new semantic tokens to `FarmerColors` (category colors, destructive, disabled, scrim, inverse, surface hierarchy)
- Add `FarmerMotion` and `FarmerIcons` objects
- Add `headlineLarge` to typography, fix `bodyLarge` letter spacing
- Extend `FarmerSpacing` with larger values

### Phase 2: Multilingual Hardening (do second)
- Add `overflow = TextOverflow.Ellipsis` to all 13 critical text locations
- Replace hardcoded `width(62.dp)` and `width(64.dp)` with flexible sizing
- Add `relativeTimeShort()` function + shortened label strings
- Fix `WeatherChipStat` concatenation with shorter labels

### Phase 3: Market Screen Visual Upgrade (do third)
- Category-aware `CropThumbnail` with distinct icons + colors
- Category-colored filter chips
- Retail price display for veg/fruit
- Egg price trend prominence
- Fix `PriceAlertWorker` location

### Phase 4: Screen-Specific Improvements (any order)
- Each screen's changes are independent
- A developer can pick any screen and implement without touching others

---

## Verification Checklist

After each phase:

- [ ] `gradlew compileDebugKotlin` passes
- [ ] Switch to Tamil → verify no text overflow on Home, Market, Weather, Alerts
- [ ] Switch to Telugu → verify tab bar labels fit
- [ ] Switch to Hindi → verify weather stat tiles fit
- [ ] Switch to Malayalam → verify tab bar labels fit (longest script)
- [ ] Check Market → Veg shows photos, Fruit shows orange basket, NonVeg shows red restaurant, Gold shows lavender diamond, Egg shows brown egg
- [ ] Check Weather → no hardcoded widths clipping text
- [ ] Check Alerts → relative time labels don't overflow
- [ ] Test at 200% font scale → no text clipping anywhere
- [ ] Test dark mode → all new tokens look correct
