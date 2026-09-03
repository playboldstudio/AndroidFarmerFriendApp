# Farmer Friend — Design System Audit

> Screen-by-screen improvement guide grounded in actual API data and current codebase.
> Every suggestion is verified against what the data sources return.
>
> **Last updated:** 2026-09-03 — after Phases 1–4a (`feature/ui-transformation-v2`).

---

## Part 1 — Design System Tokens

### Color Tokens (`Color.kt`) — 49 tokens via `FarmerTheme.colors`

| Group | Tokens | Purpose |
|-------|--------|---------|
| **Neutrals** (7) | `background`, `surface`, `surfaceMuted`, `textPrimary`, `textSecondary`, `textTertiary`, `outline` | Base surface + text hierarchy |
| **Brand** (4) | `primary`, `primaryDeep`, `primaryBright`, `onPrimary` | Farmer green accent |
| **Semantic accents** (8) | `weatherBlue`, `weatherYellow`, `alertRed`, `alertBlue`, `alertGreen`, `alertPurple`, `diseaseOrange`, `cropBrown` | Screen-specific accents |
| **Soft tint containers** (8) | `softGreen`, `softBlue`, `softRed`, `softPurple`, `softOrange`, `softBrown`, `softMint`, `softLavender` | Low-saturation backgrounds |
| **Gold / unlock** (7) | `goldBorder`, `goldTitle`, `goldBody`, `unlockTop`, `unlockBottom`, `unlockTileBg`, `unlockTileIcon` | Profile gold card |
| **Semantic roles** (9) | `destructive`, `onDestructive`, `destructiveContainer`, `link`, `disabled`, `disabledContainer`, `scrim`, `inverseSurface`, `inverseOnSurface` | M3 standard roles |
| **Surface hierarchy** (5) | `surfaceDim`, `surfaceBright`, `surfaceContainer`, `surfaceContainerLow`, `surfaceContainerHigh` | Tonal elevation |
| **Category-specific** (5) | `categoryVegetable`, `categoryFruit`, `categoryNonVeg`, `categoryGold`, `categoryEgg` | Market icon tints |

**Rule:** Never hardcode `Color(0xFF...)` — always use `FarmerTheme.colors.*`.

### Spacing, Motion, Icons (`Dimens.kt`)

| Object | Key Values |
|--------|-----------|
| `FarmerSpacing` | `xs=4, s=8, md=12, lg=16, xl=20, xxl=24, xxxl=32, xxxxl=40, section=48, screen=56` dp |
| `FarmerMotion` | `durationFast=100, durationNormal=200, durationSlow=300, durationModal=250` ms |
| `FarmerMotion` curves | `standardDecelerate`, `standardAccelerate`, `emphasizedDecelerate`, `emphasizedAccelerate` |
| `FarmerMotion` springs | `springBouncy(0.5, 1500)`, `springSnappy(0.75, 2000)`, `springGentle(0.85, 800)` |
| `FarmerIcons` | `sizeXs=14, sizeSm=18, sizeMd=22, sizeLg=28, sizeXl=36, touchTarget=48` dp |

### Typography (`Type.kt`) — 15 styles

| Role | Size | Weight | Usage |
|------|------|--------|-------|
| `displayLarge` | 66sp | Bold | Splash hero |
| `displayMedium` | 58sp | Bold | Onboarding hero |
| `displaySmall` | 28sp | ExtraBold | Large accent text |
| `headlineLarge` | 32sp | Bold | Screen titles |
| `headlineMedium` | 26sp | ExtraBold | Section headers |
| `headlineSmall` | 22sp | Bold | Card titles |
| `titleLarge` | 19sp | ExtraBold | Price text, top bar |
| `titleMedium` | 17sp | ExtraBold | Section headers, nav |
| `titleSmall` | 14.5sp | Bold | List item titles |
| `bodyLarge` | 16sp | Normal | Primary body |
| `bodyMedium` | 14.5sp | Normal | Secondary body |
| `bodySmall` | 12.5sp | Normal | Captions |
| `labelLarge` | 14.5sp | SemiBold | Buttons |
| `labelMedium` | 12.5sp | SemiBold | Chips, labels |
| `labelSmall` | 10.5sp | SemiBold | Timestamps |

### Overflow Rule

Every `Text` displaying user/localized content MUST have:
- `maxLines` (1 for pills, 2 for titles, 3 for messages)
- `overflow = TextOverflow.Ellipsis`
- Constrained width via `weight(1f)` or `widthIn(min, max)`

---

## Part 2 — API Reality

### Market Prices (`vegetablemarketprice.com`)

| Endpoint | Returns | Images | Trend |
|----------|---------|--------|-------|
| `/api/dataapi/market/{location}/daywisedata` | `VegetableItem` — name, price, units, imageUrl | ✅ | ❌ |
| `/api/dataapi/fruits/{location}/daywisedata` | `FruitItem` — name, price, units | ❌ | ❌ |
| `/api/dataapi/nonveg/{location}/daywisedata` | `NonVegItem` — name, price, units | ❌ | ❌ |
| `/api/dataapi/gold/{location}/daywisedata` | `GoldItem` — name, price, units | ❌ | ❌ |

**Critical:** Only today's data (or yesterday fallback). No historical endpoint. `Crop.trend = 0.0` for 4/5 categories.

### Egg Prices (`ncee-ten.vercel.app`)

| Endpoint | Returns | Trend |
|----------|---------|-------|
| `GET /api/egg-prices?month=MM&year=YYYY` | City, price, avg | ✅ (`priceDiffPercent` from `avg`) |

**Only category with trend data** — `avg` enables `((price - avg) / avg) * 100`.

### Open-Meteo Weather

**Fetched:** temp, humidity, feels-like, wind speed/direction, weather code, 5-day forecast.

**Free but unused:**

| Field | Farm Use | Effort |
|-------|----------|--------|
| `hourly` (temp, weather code) | Hourly forecast row | Medium |
| `daily.sunrise` / `daily.sunset` | Spray timing | Low |
| `daily.uv_index_max` | UV warnings | Low |
| `daily.wind_speed_10m_max` | Spray advisory | Low |
| `daily.soil_temperature_6cm_max/min` | Seed germination | Low |
| `daily.et0_fao_evapotranspiration` | Irrigation scheduling | Low |

### Wikipedia Scraper

Returns: title, ~200-char excerpt, source URL.
**Does NOT return:** disease symptoms/treatment, scheme eligibility, seasonal data, crop images.

### Firestore (Alerts)

| Type | Source | Issue |
|------|--------|-------|
| Price | `PriceAlertWorker` | ✅ Uses `LocationPrefs` + `resolveMarketSlug()` |
| Weather | `WeatherAlertWorker` | ✅ Uses user location |
| Crop | FCM / `AlertSeedData` | ✅ Push |

---

## Part 3 — Screen-by-Screen Audit

Each screen section includes: **Current State**, **✅ Done**, **Improvements** (prioritized), and **❌ Not Feasible** (API limits).

---

### 🏠 Home Screen

**File:** `ui/screens/home/HomeScreen.kt`
**Current state:** Header with logo + bell + profile, greeting, date, location pill, compact weather card, market preview strip (3 crops), quick-access grid (6 tiles), farm tip card.

#### ✅ Done
- Category-aware market preview thumbnails (Phase 4a)
- Text overflow hardening on all text elements
- Unread alert count badge on bell icon
- Weather-based dynamic farm tips (rain/wind/humidity/heat-aware) via `WeatherFarmTips`
- Swipeable market preview `LazyRow` with "See All" card

#### Improvements

| Priority | # | What | Effort | Data |
|----------|---|------|--------|------|
| 🟡 Med | 3 | **Time-of-day greeting** — contextual greeting based on clock (5–7AM "Early riser", 12–2PM "Markets are active", 8PM+ "Tomorrow's forecast") | Low | `java.util.Calendar` |
| 🟡 Med | 4 | **Expand weather card** — tap to reveal 3-day mini-forecast in-place using `animateContentSize` | Low | `WeatherInfo.forecast` |
| 🟡 Med | 5 | **Quick-access badge counts** — show unread alert count and market item count on grid tiles | Low | Already available |
| 🟡 Med | 6 | **Haptic feedback** — PTR, chip tap, FAB tap via `LocalHapticFeedback` | Low | None |

---

### 📊 Market Screen

**File:** `ui/screens/market/MarketScreen.kt`
**Current state:** Hero title, market picker pill, search, chip filters (Veg/Fruit/NonVeg/Gold/Egg), LazyColumn with crop cards (category-aware thumbnails), pull-to-refresh, share FAB.

#### ✅ Done
- Category-aware thumbnails + RowCard chips (Phase 3)
- Text overflow hardening
- Honest pull-to-refresh (spinner tracks real `UiState`)
- Scroll-aware FAB (hides on scroll down, shows on scroll up)
- Sort options (name A-Z / Z-A, price low-high / high-low) via sort icon
- `imePadding()` so keyboard doesn't cover the search field

#### Improvements

| Priority | # | What | Effort | Data |
|----------|---|------|--------|------|
| 🟡 Med | 3 | **Retail price comparison** — show `retailPrice` alongside wholesale for veg/fruit | Low | Already mapped |
| 🟡 Med | 4 | **Egg trend prominence** — make `priceDiffPercent` more visible (larger, colored arrow) | Low | Already computed |
| 🟡 Med | 5 | **Animated filter switch** — crossfade content when switching categories | Low | `AnimatedContent` |
| 🟡 Med | 6 | **Pull-to-refresh status messages** — "Fetching latest prices…" → "Market servers are slow…" | Low | Timer-based |
| 🟡 Med | 3 | **Retail price comparison** — show `retailPrice` alongside wholesale for veg/fruit | Low | Already mapped |
| 🟡 Med | 4 | **Egg trend prominence** — make `priceDiffPercent` more visible (larger, colored arrow) | Low | Already computed |
| 🟡 Med | 5 | **Animated filter switch** — crossfade content when switching categories | Low | `AnimatedContent` |
| 🟡 Med | 6 | **Pull-to-refresh status messages** — "Fetching latest prices…" → "Market servers are slow…" | Low | Timer-based |

#### ❌ Not Feasible
| Idea | Why |
|------|-----|
| Price history sparklines | No historical endpoint — only today's data |
| Price comparison across days | Same — no historical data |
| Product images for fruit/nonveg/gold/egg | API only returns images for vegetables |

---

### 🌤 Weather Screen

**File:** `ui/screens/weather/WeatherScreen.kt`
**Current state:** Hero title, location picker, compact weather card (gradient + 3 stat chips), 5-day forecast pills, selected-day detail card, min–max range chart, stat tiles (rain/humidity/wind/direction), farm tip card.

#### ✅ Done
- Text overflow hardening on all labels
- `widthIn(min, max)` on forecast day labels for multilingual safety
- Location picker with search
- Weather-based dynamic farm tips (same `WeatherFarmTips` logic as Home)

#### Improvements

| Priority | # | What | Effort | Data |
|----------|---|------|--------|------|
| 🔴 High | 1 | **Hourly forecast row** — add `hourly` query param + horizontal LazyRow below daily forecast | Medium | Free from Open-Meteo |
| 🟡 Med | 2 | **Sunrise/sunset times** — add `daily.sunrise,sunset` + visual arc for field work planning | Low | Free from Open-Meteo |
| 🟡 Med | 3 | **UV index warning** — add `daily.uv_index_max` + color-coded advisory chip | Low | Free from Open-Meteo |
| 🟡 Med | 4 | **Wind spray advisory** — based on `wind_speed_10m_max` (> 30 → "no spraying", < 10 → "ideal") | Low | Free from Open-Meteo |
| 🟡 Med | 5 | **Soil temperature + ET0** — add `daily.soil_temperature_6cm_max,min` and `et0_fao_evapotranspiration` for irrigation scheduling | Low | Free from Open-Meteo |

#### ❌ Not Feasible
| Idea | Why |
|------|-----|
| 7-day forecast | `forecast_days=5` is intentional for mobile density — extending to 7 is feasible but may clutter |

---

### 🔔 Alerts Screen

**File:** `ui/screens/alerts/AlertsScreen.kt`
**Current state:** Hero title with unread badge, "Mark all read" link, location pill, chip filters (All/Price/Weather/Crop), alert list with left accent bar + icon + title + message + timestamp + type chip.

#### ✅ Done
- Compact `relativeTimeShort()` timestamps ("5m", "3h", "2d")
- Text overflow hardening on all elements
- Honest pull-to-refresh (tracks realtime listener)
- Unread count badge
- "Mark all read" button
- `PriceAlertWorker` reads user location from `LocationPrefs` (was hardcoded koyambedu)
- Time-based grouping — Today / Yesterday / Earlier sections
- Swipe-to-dismiss marks unread alerts read

#### Improvements

| Priority | # | What | Effort | Data |
|----------|---|------|--------|------|
| 🟡 Med | 3 | **Alert priority visual treatment** — critical (red border), warning (orange), info (gray) based on message content analysis | Low | `Alert.message` |

---

### 🐛 Disease Screen

**File:** `ui/screens/disease/DiseaseScreen.kt`
**Current state:** SubScreenHeader, search field, RowCard list (BugReport icon, orange tint), source chips, tapping opens Wikipedia in browser.

#### ✅ Done
- Category-appropriate theming (`diseaseOrange` / `softOrange`)
- Text overflow hardening
- Inline Wikipedia excerpt preview in card body (added `excerpt` field to `Disease` model)
- `imePadding()` on search field

#### Improvements

| Priority | # | What | Effort | Data |
|----------|---|------|--------|------|
| 🟡 Med | 3 | **Disease category color coding** — group by crop type with colored chips (`Disease.cropAffected`) | Low | Already in model |

#### ❌ Not Feasible
| Idea | Why |
|------|-----|
| Disease detail (symptoms/treatment) | Wikipedia returns only title + excerpt — no structured disease data |
| Camera-based detection | ML/TFLite model — significant new feature, out of scope |

---

### 🏛 Schemes Screen

**File:** `ui/screens/schemes/SchemesScreen.kt`
**Current state:** SubScreenHeader, search, RowCard list (AccountBalance icon, green tint), source chips, tapping opens Wikipedia.

#### ✅ Done
- Category-appropriate theming (`alertGreen` / `softLavender`)
- Text overflow hardening
- Inline excerpt preview — `Scheme.description` shown as the card subtitle
- `imePadding()` on search field

#### Improvements

| Priority | # | What | Effort | Data |
|----------|---|------|--------|------|
| 🟡 Med | 3 | **Bookmark/save schemes** — local storage with heart icon + "Saved" filter tab | Medium | `DataStore` or Room |

#### ❌ Not Feasible
| Idea | Why |
|------|-----|
| Scheme eligibility quick-check | Wikipedia excerpts have no eligibility fields |
| Scheme status (active/deadline) | No status data in Wikipedia |

---

### 📝 Crop Notes Screen

**File:** `ui/screens/cropnotes/CropNotesScreen.kt`
**Current state:** SubScreenHeader, location pill, search, horizontal crop filter chips, LazyColumn of CropNoteItems (title + content + source chip).

#### ✅ Done
- Category-appropriate theming (`cropBrown` / `softBrown`)
- Text overflow hardening
- Expandable note cards — tap to collapse/expand `CropNote.content` with `animateContentSize`
- `imePadding()` on search field

#### Improvements

| Priority | # | What | Effort | Data |
|----------|---|------|--------|------|
| 🟡 Med | 3 | **Seasonal tips section** — hardcoded calendar keyed to `Calendar.MONTH` | Low | `java.util.Calendar` |

---

### 👤 Profile Screen

**File:** `ui/screens/profile/ProfileScreen.kt`
**Current state:** Identity card, settings sections (language, notifications, appearance, share, logout), gold unlock teaser.

#### ✅ Done
- Per-item themed icons (Person/green, Landscape/green, Logout/red, Language/blue, Notifications/purple)
- Text overflow hardening

#### Improvements

| Priority | # | What | Effort | Data |
|----------|---|------|--------|------|
| 🟡 Med | 1 | **Settings with icons + descriptions** — upgrade plain text rows to icon + title + subtitle | Low | Hardcoded |
| 🟡 Med | 2 | **Fix profile location staleness** — re-read from `LocationPrefs` on resume | Low | Already available |
| 🟡 Med | 3 | **Fix TempEmail dead field** — remove or add email edit capability | Low | `ProfileState.tempEmail` |

---

### 🎬 Splash Screen

**File:** `ui/screens/splash/SplashScreen.kt`
**Current state:** Logo scale animation (0.7→1.0, 450ms), text fade (400ms), 900ms hold, then navigate.

#### Improvements

| Priority | # | What | Effort | Data |
|----------|---|------|--------|------|
| 🟡 Med | 1 | **Reduce dead time** — add tagline fade-in during 900ms hold, navigate sooner | Low | None |
| ⚪ Low | 2 | **Shared element transition to Home** — logo animates into header (requires Nav Compose 2.8+) | Medium | Check dep version |

---

### 🔐 Auth Screen

**File:** `ui/screens/auth/` (AuthBridge, login/signup)
**Current state:** Full-screen sign in/up with email/password.

#### Improvements

| Priority | # | What | Effort | Data |
|----------|---|------|--------|------|
| 🟡 Med | 1 | **Google Sign-In** — one-tap option above email/password | Medium | Firebase Auth |

---

### 🧭 Navigation (FloatingTabBar)

**File:** `ui/components/FloatingTabBar.kt`
**Current state:** 5-tab floating bar (Home, Market, Weather, Alerts, Profile).

#### Improvements

| Priority | # | What | Effort | Data |
|----------|---|------|--------|------|
| 🟡 Med | 1 | **Tab switch crossfade** — 200ms crossfade when switching tabs | Low | `AnimatedContent` |
| 🟡 Med | 2 | **Sub-screen slide transitions** — 300ms enter, 200ms exit for push navigation | Low | `enterTransition`/`exitTransition` |
| 🟡 Med | 3 | **Predictive back gesture** — Android 13+ live preview on edge swipe | Low | `enableOnBackInvokedCallback` |

---

## Part 4 — Global Improvements

These apply across all screens:

| Priority | # | What | Effort |
|----------|---|------|--------|
| 🔴 High | 1 | **Dynamic Color (Android 12+)** — wallpaper-derived palette as opt-in toggle in Profile → Appearance | Medium |
| 🟡 Med | 2 | **Snackbar feedback** — share/location/language change confirmations | Low |
| 🟡 Med | 3 | **Edge-to-edge display** — `enableEdgeToEdge()` in MainActivity | Low |

---

## Part 5 — Accessibility Checklist

| # | Change | Status |
|---|--------|--------|
| 1 | `contentDescription` on all icon-only composables | ⬜ |
| 2 | 48dp touch targets (HeaderIconButton is 44dp) | ⬜ |
| 3 | `textTertiary` contrast ratio (darken both palettes) | ⬜ |
| 4 | `mergeDescendants` on RowCard items for TalkBack | ⬜ |
| 5 | Test at 200% font scale | ⬜ |
| 6 | `imePadding()` on all search screens | ✅ |

---

## Part 6 — Dark Mode Checklist

| Area | Check |
|------|-------|
| CompactWeatherCard gradient | Green gradient on dark surface — verify text readability |
| WeatherChipStat glass pill | `White.copy(0.18f)` on gradient — verify dark mode |
| Bottom sheet background | Use `surface` token, not hardcoded |
| Splash gradient | `softMint → background` — verify both look intentional |
| SearchField clear button | 32dp target visibility in dark |
| SourceChip | `surfaceMuted` contrast with `textTertiary` |
| Category icon tiles | `softGreen`/`softOrange` etc. — verify icon legibility |

---

## Quick Reference

| Principle | Rule |
|-----------|------|
| **60/30/10 Color** | 60% neutral base, 30% text/dark, 10% accent |
| **8-Point Grid** | All spacing via `FarmerSpacing` |
| **48dp Touch Target** | `FarmerIcons.touchTarget` |
| **One FAB Per Screen** | Market screen only (share) |
| **Honest Loading** | PullToRefresh tied to real `UiState` |
| **Category Identity** | Each market category = distinct icon + tint + container |
| **Overflow Safety** | Every Text has `maxLines` + `Ellipsis` — 11 languages |
| **Token-First** | Never hardcode — `FarmerTheme.colors.*` / `FarmerSpacing.*` |
