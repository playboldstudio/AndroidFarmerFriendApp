# Farmer Friend — Design System Audit

> Living document: current design-system state, data-grounded improvement backlog,
> and API constraints. Updated as the design system evolves.
>
> **Last updated:** 2026-09-03 — after Phases 1–4a (v1.7.0-prep on `feature/ui-transformation-v2`).

---

## Part 1 — Current Design System State

### Color Tokens (`Color.kt`)

`FarmerColors` data class — 49 tokens, exposed via `FarmerTheme.colors`.

| Group | Tokens | Purpose |
|-------|--------|---------|
| **Neutrals** (7) | `background`, `surface`, `surfaceMuted`, `textPrimary`, `textSecondary`, `textTertiary`, `outline` | Base surface + text hierarchy |
| **Brand** (4) | `primary`, `primaryDeep`, `primaryBright`, `onPrimary` | Farmer green accent + contrast |
| **Semantic accents** (8) | `weatherBlue`, `weatherYellow`, `alertRed`, `alertBlue`, `alertGreen`, `alertPurple`, `diseaseOrange`, `cropBrown` | Screen-specific accent colors |
| **Soft tint containers** (8) | `softGreen`, `softBlue`, `softRed`, `softPurple`, `softOrange`, `softBrown`, `softMint`, `softLavender` | Low-saturation icon/bg backgrounds |
| **Gold / unlock** (6) | `goldBorder`, `goldTitle`, `goldBody`, `unlockTop`, `unlockBottom`, `unlockTileBg`, `unlockTileIcon` | Profile gold teaser card |
| **Semantic roles** (9) | `destructive`, `onDestructive`, `destructiveContainer`, `link`, `disabled`, `disabledContainer`, `scrim`, `inverseSurface`, `inverseOnSurface` | Standard M3 semantic roles |
| **Surface hierarchy** (5) | `surfaceDim`, `surfaceBright`, `surfaceContainer`, `surfaceContainerLow`, `surfaceContainerHigh` | Tonal elevation surfaces |
| **Category-specific** (5) | `categoryVegetable`, `categoryFruit`, `categoryNonVeg`, `categoryGold`, `categoryEgg` | Market item icon tint per category |

**Light/Dark:** Both palettes are fully defined (`LightFarmerColors` / `DarkFarmerColors`). Dark palette uses warm-tinted darks, not pure black.

**Access pattern:**
```kotlin
val colors = FarmerTheme.colors
Text(color = colors.textPrimary)
Icon(tint = colors.categoryVegetable)
Box(background = colors.softGreen)
```

**Anti-patterns to avoid:**
- Never use `Color(0xFF...)` directly in components — always reference `FarmerTheme.colors.*`
- Never use `MaterialTheme.colorScheme.*` — the app uses its own token set
- `soft*` tokens are for container backgrounds, not foreground text
- `category*` tokens are for icon tints inside their matching `soft*` containers

---

### Spacing, Motion, Icons (`Dimens.kt`)

```kotlin
FarmerSpacing   // 10-step scale: xs(4) → screen(56)
FarmerMotion    // 4 durations + 4 M3 curves + 3 springs
FarmerIcons     // 5 size tokens + 48dp touch target
```

| Object | Key Values |
|--------|-----------|
| `FarmerSpacing` | `xs=4, s=8, md=12, lg=16, xl=20, xxl=24, xxxl=32, xxxxl=40, section=48, screen=56` (all dp) |
| `FarmerMotion` | `durationFast=100ms, durationNormal=200ms, durationSlow=300ms, durationModal=250ms` |
| `FarmerMotion` curves | `standardDecelerate`, `standardAccelerate`, `emphasizedDecelerate`, `emphasizedAccelerate` |
| `FarmerMotion` springs | `springBouncy(damping=0.5, stiffness=1500)`, `springSnappy(0.75, 2000)`, `springGentle(0.85, 800)` |
| `FarmerIcons` | `sizeXs=14, sizeSm=18, sizeMd=22, sizeLg=28, sizeXl=36, touchTarget=48` (all dp) |

---

### Typography (`Type.kt`)

15 styles via `MaterialTheme.typography`:

| Role | Size | Weight | Line Height | Usage |
|------|------|--------|-------------|-------|
| `displayLarge` | 66sp | Bold | 70sp | Splash hero |
| `displayMedium` | 58sp | Bold | 64sp | Onboarding hero |
| `displaySmall` | 28sp | ExtraBold | 36sp | Large accent text |
| `headlineLarge` | 32sp | Bold | 40sp | Screen titles (new) |
| `headlineMedium` | 26sp | ExtraBold | 32sp | Section headers |
| `headlineSmall` | 22sp | Bold | 28sp | Card titles |
| `titleLarge` | 19sp | ExtraBold | 24sp | Price text, top bar |
| `titleMedium` | 17sp | ExtraBold | 22sp | Section headers, nav |
| `titleSmall` | 14.5sp | Bold | 19sp | List item titles |
| `bodyLarge` | 16sp | Normal | 24sp | Primary body text |
| `bodyMedium` | 14.5sp | Normal | 21sp | Secondary body |
| `bodySmall` | 12.5sp | Normal | 18sp | Captions |
| `labelLarge` | 14.5sp | SemiBold | 20sp | Buttons, prominent labels |
| `labelMedium` | 12.5sp | SemiBold | 17sp | Chips, small labels |
| `labelSmall` | 10.5sp | SemiBold | 15sp | Timestamps, annotations |

**All text uses `sp` for user font-scaling.** Line heights are ~1.3–1.5× font size.

---

### Shared Components (`CommonComponents.kt`)

| Component | Signature | Notes |
|-----------|-----------|-------|
| `HeroTitle` | `(text, accent?, modifier)` | Bold title with optional green accent substring |
| `LocPill` | `(text, onClick?, modifier)` | Location pill with map icon |
| `PillChipGroup` | `(filters, selectedFilter, onFilterSelected)` | Horizontal scrollable filter chips |
| `RowCard` | `(title, subtitle?, icon?, iconTint, iconContainer, leading?, end?, onClick?, modifier, unread?)` | List row — 46dp icon tile, 20dp rounded, 1dp shadow |
| `TintIconCircle` | `(icon, tint, container, modifier, size, cornerRadius)` | Reusable icon tile (46dp default) |
| `CompactWeatherCard` | `(weather, strings, modifier, onClick?)` | Gradient weather card with stat chips |
| `WeatherChipStat` | `(icon, label, value)` | Glass pill inside weather card |
| `SearchField` | `(value, onValueChange, placeholder)` | Text field with search/clear icons |
| `EmptyState` | `(icon, title, subtitle?)` | Centered empty state |
| `ErrorState` | `(message, onRetry, modifier)` | Error with retry button |
| `OfflineState` | `(title, body, onRetry, modifier)` | Offline-specific empty state |
| `SlowNetworkState` | `(title, body, modifier)` | Loading hint for slow connections |
| `ShimmerList` | `(rowCount, rowHeight, modifier)` | Skeleton loading rows |
| `TrendTag` | `(percent?)` | Up/down percentage pill |
| `FarmTipCard` | `(title, body)` | Green gradient tip card |
| `SectionHeaderCompat` | `(title, modifier)` | Section divider title |
| `DayPill` | `(day, weatherCode, temp, selected, onClick)` | Forecast day selector pill |
| `AlertChip` | `(text, color, container)` | Small colored chip |

---

### Multilingual Overflow Hardening

All user-facing text across 10+ files now has:
- `maxLines = 1` or `2` (context-dependent)
- `overflow = TextOverflow.Ellipsis`
- `widthIn(min, max)` for constrained containers (e.g., `DayPill`, `ForecastRangeList`)

This prevents long Tamil/Hindi/Bengali strings from breaking layout.

---

### Category-Aware Market Thumbnails

Market and Home screens use per-category visual identity:

| Category | Icon | Tint | Container |
|----------|------|------|-----------|
| vegetable | `Eco` | `categoryVegetable` | `softGreen` |
| fruit | `ShoppingBasket` | `categoryFruit` | `softOrange` |
| nonveg | `Restaurant` | `categoryNonVeg` | `softRed` |
| gold | `Diamond` | `categoryGold` | `softLavender` |
| egg | `Egg` | `categoryEgg` | `softBrown` |

**API note:** Only vegetables carry image URLs from the API. Other categories fall back to these themed icon tiles.

---

## Part 2 — API Reality

Before suggesting improvements, here's what each data source provides.

### Market Prices (`vegetablemarketprice.com`)

| Endpoint | Returns | Has Images | Has Trend |
|----------|---------|------------|-----------|
| `/api/dataapi/market/{location}/daywisedata` | `VegetableItem` — name, price, units, imageUrl | ✅ | ❌ |
| `/api/dataapi/fruits/{location}/daywisedata` | `FruitItem` — name, price, units | ❌ | ❌ |
| `/api/dataapi/nonveg/{location}/daywisedata` | `NonVegItem` — name, price, units | ❌ | ❌ |
| `/api/dataapi/gold/{location}/daywisedata` | `GoldItem` — name, price, units | ❌ | ❌ |

**Critical:** Only today's data (or yesterday as fallback). No historical endpoint. `Crop.trend` is hardcoded to `0.0` for 4 of 5 categories.

### Egg Prices (`ncee-ten.vercel.app`)

| Endpoint | Returns | Has Trend |
|----------|---------|-----------|
| `GET /api/egg-prices?month=MM&year=YYYY` | City, price, avg | ✅ (`priceDiffPercent` from `avg`) |

**Only category with trend data** — `avg` (historical average) enables `((price - avg) / avg) * 100`.

### Open-Meteo Weather

**Currently fetched:** temperature, humidity, feels-like, wind speed/direction, weather code, 5-day daily forecast (high/low, rain probability).

**Available but unused (free, no new API):**

| Field | Farm Use | Effort |
|-------|----------|--------|
| `hourly` (temperature, weather code) | Hourly forecast row | Medium |
| `daily.sunrise` / `daily.sunset` | Spray timing, field work windows | Low |
| `daily.uv_index_max` | UV safety warnings | Low |
| `daily.wind_speed_10m_max` | Wind-based spray advisory | Low |
| `daily.soil_temperature_6cm_max/min` | Seed germination timing | Low |
| `daily.et0_fao_evapotranspiration` | Irrigation scheduling | Low |

### Wikipedia Scraper

Returns: article title, ~200-char cleaned excerpt, source URL.

**Does NOT return:** Structured disease data (symptoms, treatment), scheme eligibility, seasonal relevance, crop-specific images.

### Firestore (Alerts)

| Type | Source | Issue |
|------|--------|-------|
| Price alerts | `PriceAlertWorker` | ⚠️ Hardcodes `"koyambedu"` — ignores user location |
| Weather alerts | `WeatherAlertWorker` | ✅ Uses user's saved location |
| Crop alerts | FCM / `AlertSeedData` | ✅ Push notifications |

---

## Part 3 — Completed Improvements

These were implemented in Phases 1–4a (`feature/ui-transformation-v2` branch):

| Phase | What | Files Changed |
|-------|------|---------------|
| **1** | Dead code cleanup — removed 28 legacy standalone color vals | `Color.kt` |
| **1** | Added 19 new semantic tokens to `FarmerColors` | `Color.kt` |
| **1** | Added `FarmerMotion` and `FarmerIcons` objects | `Dimens.kt` |
| **1** | Typography fixes — `headlineLarge`, `displaySmall` lineHeight, `bodyLarge` letterSpacing | `Type.kt` |
| **2** | Multilingual overflow hardening — `maxLines` + `TextOverflow.Ellipsis` across 10 files | `CommonComponents.kt`, `AlertsScreen.kt`, `WeatherScreen.kt`, `HomeScreen.kt`, `FloatingTabBar.kt`, `MarketScreen.kt` |
| **2** | Added `relativeTimeShort()` for compact timestamps | `RelativeTime.kt` |
| **3** | Market screen category-aware thumbnails + RowCard chips | `MarketScreen.kt` |
| **4a** | Home screen `MarketPreviewStrip` category-aware thumbnails | `HomeScreen.kt` |

---

## Part 4 — Remaining Improvements (Backlog)

Prioritized by impact × effort. Each item links to the relevant screen section.

### High Impact, Low Effort

| # | Improvement | Screen | Data Needed |
|---|-------------|--------|-------------|
| 1 | **Fix `PriceAlertWorker` hardcoded location** — read from `LocationPrefs` like `WeatherAlertWorker` | Notifications | Already available |
| 2 | **Keyboard padding** — add `imePadding()` on search screens (Market, Disease, Schemes, CropNotes) | All search screens | None |
| 3 | **Swipe-to-dismiss alerts** — mark read on swipe with undo snackbar | Alerts | `SwipeToDismissBox` |
| 4 | **Time-based alert grouping** — Today / Yesterday / Earlier sections | Alerts | `Alert.timestamp` |
| 5 | **Disease inline excerpt preview** — show Wikipedia excerpt before opening browser | Disease | Add `excerpt` field to `Disease` model |
| 6 | **Scheme inline excerpt preview** — show `description` in card | Schemes | Already in model |
| 7 | **Expandable note cards** — collapse/expand `CropNote.content` | CropNotes | Already in model |

### High Impact, Medium Effort

| # | Improvement | Screen | Data Needed |
|---|-------------|--------|-------------|
| 8 | **Hourly forecast row** — add `hourly` query param + LazyRow | Weather | Free from Open-Meteo |
| 9 | **Weather-based farm tips** — replace static tip with data-driven advice | Home, Weather | `WeatherInfo` fields |
| 10 | **Dynamic Color (Android 12+)** — wallpaper-derived palette as opt-in | Global | `dynamicDarkColorScheme`/`dynamicLightColorScheme` |
| 11 | **Navigation transitions** — slide-push for sub-screens, crossfade for tabs | Navigation | `enterTransition`/`exitTransition` |
| 12 | **Predictive back gesture** — Android 13+ live preview on edge swipe | Navigation | `enableOnBackInvokedCallback` |
| 13 | **Swipeable market preview** — `LazyRow` with "See All" card | Home | Already available |
| 14 | **Sort options** — price low→high, high→low, name A→Z | Market | `Crop.priceValue`, `Crop.nameEng` |

### Medium Impact, Low Effort

| # | Improvement | Screen | Data Needed |
|---|-------------|--------|-------------|
| 15 | **Snackbar feedback** — share/location/language change confirmations | Global | None |
| 16 | **Haptic feedback** — PTR, chip tap, FAB tap | Global | `LocalHapticFeedback` |
| 17 | **Time-of-day greeting** — contextual greeting based on system clock | Home | `java.util.Calendar` |
| 18 | **Sunrise/sunset times** — add `daily.sunrise,sunset` to weather query | Weather | Free from Open-Meteo |
| 19 | **UV index warning** — add `daily.uv_index_max` | Weather | Free from Open-Meteo |
| 20 | **Wind spray advisory** — based on `wind_speed_10m_max` | Weather | Free from Open-Meteo |
| 21 | **Soil temp + ET0** — add `daily.soil_temperature_6cm_max,min,et0_fao_evapotranspiration` | Weather | Free from Open-Meteo |
| 22 | **Quick-access grid badge counts** — unread alerts, market item count | Home | Already available |
| 23 | **Egg trend prominence** — make `priceDiffPercent` more visible in egg cards | Market | Already computed |
| 24 | **Expand weather card** — reveal 3-day mini-forecast on tap | Home | `WeatherInfo.forecast` |
| 25 | **Emoji-enhanced crop filter chips** — 🌾 Rice, 🥬 Vegetables, etc. | CropNotes | `CropNote.cropName` |
| 26 | **Seasonal tips section** — hardcoded calendar keyed to month | CropNotes | `java.util.Calendar` |

### Medium Impact, Medium Effort

| # | Improvement | Screen | Data Needed |
|---|-------------|--------|-------------|
| 27 | **Animated filter switch** — crossfade content on filter change | Market | `AnimatedContent` |
| 28 | **Retail price comparison** — show `retailPrice` alongside wholesale | Market | Already mapped for veg/fruit |
| 29 | **Bookmark/save schemes** — local storage with heart icon | Schemes | `DataStore` or Room |
| 30 | **Disease category color coding** — group by crop type | Disease | `Disease.cropAffected` |
| 31 | **Settings with icons + descriptions** — upgrade profile settings rows | Profile | Hardcoded |
| 32 | **Social login** — Google Sign-In one-tap option | Auth | Firebase Auth |

### NOT Feasible (API Limitations)

| Idea | Why Not | What Would Be Needed |
|------|---------|---------------------|
| Price history sparklines | No historical endpoint | Backend that stores daily snapshots |
| Price comparison across days | No historical data | Same as above |
| Disease detail (symptoms/treatment) | Wikipedia returns only title + excerpt | Dedicated disease API or on-device ML |
| Camera-based disease detection | ML/TFLite model | Significant new feature, out of scope |
| Scheme eligibility quick-check | Wikipedia excerpts have no eligibility fields | MyScheme.gov.in API |
| Scheme status indicators (active/deadline) | No status data in Wikipedia | Dedicated schemes API |
| Hourly forecast for 7+ days | `forecast_days=5` intentional for density | Could extend to 7 but mobile UX tradeoff |

---

## Part 5 — Architecture Notes

### Screen-by-Screen Token Usage

| Screen | Icon | Tint | Container | Shape |
|--------|------|------|-----------|-------|
| Market (veg) | `Eco` | `categoryVegetable` | `softGreen` | 15dp rounded |
| Market (fruit) | `ShoppingBasket` | `categoryFruit` | `softOrange` | 15dp rounded |
| Market (nonveg) | `Restaurant` | `categoryNonVeg` | `softRed` | 15dp rounded |
| Market (gold) | `Diamond` | `categoryGold` | `softLavender` | 15dp rounded |
| Market (egg) | `Egg` | `categoryEgg` | `softBrown` | 15dp rounded |
| Disease | `BugReport` | `diseaseOrange` | `softOrange` | 15dp rounded |
| Schemes | `AccountBalance` | `alertGreen` | `softLavender` | 15dp rounded |
| CropNotes | `MenuBook` | `cropBrown` | `softBrown` | 15dp rounded |
| Alerts (price) | `Paid` | `alertGreen` | `softGreen` | 15dp rounded |
| Alerts (weather) | `WbCloudy` | `alertBlue` | `softBlue` | 15dp rounded |
| Alerts (crop) | `Eco` | `alertPurple` | `softPurple` | 15dp rounded |
| Weather (rain) | `WaterDrop` | `weatherBlue` | `softBlue` | 11dp rounded |
| Weather (humidity) | `Opacity` | `primary` | `softMint` | 11dp rounded |
| Weather (wind) | `Air` | `alertPurple` | `softLavender` | 11dp rounded |
| Weather (direction) | `Explore` | `diseaseOrange` | `softOrange` | 11dp rounded |

### RowCard Standard

All list rows follow:
- 46dp icon tile (`TintIconCircle`, 15dp corner radius)
- 14dp inner padding
- 13dp gap between icon and text
- 20dp card corner radius
- 1dp shadow elevation
- Title: `titleSmall` (14.5sp Bold), subtitle: `bodySmall` (12.5sp, 12sp font size)

### Overflow Hardening Rule

Every `Text` composable that displays user-generated or localized content MUST have:
- `maxLines` set (1 for pills/chips, 2 for titles, 3 for messages)
- `overflow = TextOverflow.Ellipsis`
- Constrained width via `Modifier.weight(1f)` or `widthIn(min, max)` when in a Row

### Adding New Tokens

When adding a new color token:
1. Add field to `FarmerColors` data class
2. Add light value to `LightFarmerColors`
3. Add dark value to `DarkFarmerColors`
4. Use in components via `FarmerTheme.colors.newToken`

When adding a new spacing/motion/icon token:
1. Add to the appropriate object (`FarmerSpacing`, `FarmerMotion`, `FarmerIcons`)
2. Reference via `FarmerSpacing.newToken` (not hardcoded dp)

---

## Part 6 — Accessibility Checklist

| # | Change | Status |
|---|--------|--------|
| 1 | `contentDescription` on all icon-only composables | ⬜ Pending |
| 2 | 48dp touch targets (HeaderIconButton is 44dp — borderline) | ⬜ Pending |
| 3 | `textTertiary` contrast ratio (darken in both palettes) | ⬜ Pending |
| 4 | `mergeDescendants` on RowCard items for TalkBack grouping | ⬜ Pending |
| 5 | Test at 200% font scale — no clipping/overlap | ⬜ Pending |
| 6 | Add `imePadding()` on search screens | ⬜ Pending |

---

## Part 7 — Dark Mode Polish

| Area | Check |
|------|-------|
| CompactWeatherCard gradient | Green gradient on green-tinted dark surface — verify text readability |
| WeatherChipStat glass pill | `Color.White.copy(alpha=0.18f)` on gradient — verify in dark mode |
| Bottom sheet background | Should use `surface` token, not hardcoded white |
| Splash screen gradient | `softMint → background` — verify both look intentional in dark |
| SearchField clear button | 32dp touch target in dark mode — verify visibility |
| SourceChip in DiseaseScreen | `surfaceMuted` in dark — verify enough contrast with `textTertiary` |
| Category icon tiles | `softGreen`/`softOrange`/etc. in dark — verify icons are legible |

---

## Quick Reference

| Principle | Rule |
|-----------|------|
| **60/30/10 Color** | 60% neutral base, 30% text/dark, 10% accent (green) |
| **8-Point Grid** | All spacing via `FarmerSpacing` (divisible by 4) |
| **48dp Touch Target** | `FarmerIcons.touchTarget` — Android minimum |
| **One FAB Per Screen** | Market screen only (share) |
| **Honest Loading** | Real timing, not fake instant (PullToRefresh tied to UiState) |
| **Category Identity** | Each market category has distinct icon + tint + container |
| **Overflow Safety** | Every Text has `maxLines` + `Ellipsis` — works across 11 languages |
| **Token-First** | Never hardcode colors/sizes — always use `FarmerTheme.colors.*` or `FarmerSpacing.*` |
