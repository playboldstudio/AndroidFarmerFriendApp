# Farmer Friend — Screen-by-Screen Further UI Improvements (v2 pass)

> **Purpose:** the deepening of `docs/design-system-audit.md`. That audit records *what exists + high-level backlog*; this file is the **screening-by-screen, implementation-ready** pass — every item is grounded in the real code (from the exploration below) and in the three local design skills (`android-design-guidelines` = Material 3 platform correctness, `mobile-app-ui-design` = polish/emotion, `mobile-design-skill` = workflow rigor). Items are grouped per screen, each with effort and why it matters. **This is a discussion document** — nothing here is committed to code until we agree.

**Format of each item:** `[screen] | [priority] | [effort] | what · why · how`.

Priorities: 🔴 High (clear win) · 🟡 Med (solid) · ⚪ Low (polish / optional).

---

## 0. Current state — the gaps a fresh pass found

From reading every screen and component, the codebase today is *structurally sound and overflow-safe*, but **feels static**:

- **No screen/nav transitions** — `MainScreen` NavHost uses default `composable()`; all screen changes are instant.
- **No animated selection** — `PillChip`, `DayPill`, market filter, sort all swap state instantly (no color animation, no crossfade).
- **No crossfade on state swaps** — every `when(UiState)` blinks Loading↔Error↔Success.
- **No haptics** anywhere.
- **Only Market + Alerts have pull-to-refresh**; Home/Weather/Profile don't.
- **`ShimmerList` is one pulsing box**, not a shaped skeleton.
- **No shared-element transitions**, no animated counters, no empty-state illustration.
- **`FloatingTabBar` active state changes color instantly** — no indicator animation.

These are the cross-cutting themes. Which are also echoed in `design-system-audit.md` Parts 4–5.

---

## 1. 🌐 Global / cross-cutting (do first — they lift every screen)

#### ✅ Done (Wave 1)
- **G1 — NavHost enter/exit transitions.** `MainScreen.kt` NavHost now has slide-in-from-start (`it/4`) + fade for push, reversed for pop; IntOffset/Flow-tuned `tween`s driven by `FarmerMotion` curves. Screen changes no longer snap.
- **G2 — Animated chip/pill selection.** `PillChip` (shared by Market/Alerts/Crop Notes filters) now `animateColorAsState`-animates bg/fg/border (200ms `standardDecelerate`) — selection visibly settles.
- **G3 — Crossfade state blocks.** `CrossfadeUiState<T>` helper in `CommonComponents.kt` wraps any `UiState` in `AnimatedContent` crossfade (~200ms, `standardDecelerate`). Applied to **all 7 screens** with `when(UiState)`: Weather, Market, Alerts, Disease, Schemes, Crop Notes, Home weather card. Loading↔Error↔Success transitions now fade smoothly instead of blinking.
- **G4 — Haptics.** `LocalHapticFeedback` wired on: filter-chip taps (`TextHandleMove`), tab switch in `FloatingTabBar` (`TextHandleMove`), and Alerts swipe-to-mark-read (`Confirm`).

| # | Item | Pri | Effort | What · Why · How |
|---|------|-----|--------|------------------|
| G1 | **NavHost enter/exit transitions** | 🔴 | Low | ✅ Done — see above. |
| G2 | **Animated chip/pill selection** | 🔴 | Low | `PillChip`, `DayPill`, filter, sort change colors instantly. Wrap selected-state colors in `animateColorAsState` (150–200ms `FarmerMotion.durationNormal`) so selection visibly settles; consider an `AnimatedContent` crossfade when the filter actually swaps the whole list. *mobile-app-ui-design: micro-animations as trust signals.* |
| G3 | **Crossfade state blocks** | 🟡 | Med | ✅ Done — `CrossfadeUiState` helper applied to all 7 data screens **except Weather**, which uses plain `when()` because `AnimatedContent` inside a `verticalScroll` Column causes vertical overlap of outgoing/incoming content (weather card hidden, stat rows + farm tip stacked on top). The day-pill detail crossfade inside `WeatherDetailedView` is bounded and safe. |
| G4 | **Haptics on key actions** | 🟡 | Low | None today. Add `LocalHapticFeedback` on pull-to-refresh, tab switch, swipe-to-dismiss, filter taps. *android-design-guidelines §7.3 + checklist.* |
| G5 | **Shaped skeleton loaders** | 🟡 | Med | `ShimmerList` is one pulsing bar. Make it mirror content: leading icon circle + 2–3 text lines, subtle `softMint`/`surfaceMuted` blocks, `infiniteRepeatable` alpha. Loading reads as "content about to appear". |
| G6 | **Animated numeric values** | ⚪ | Low | Prices + big weather temp render static. A brief count-up / crossfade on load/refresh (via `AnimatedContent`) adds life at the peak data moment. *mobile-app-ui-design peak-end rule.* |
| G7 | **Shared-element transitions** | ⚪ | Med | ⚠️ Tried then reverted for the weather card. A shared element inside the weather screen's `verticalScroll` column renders on its own overlay layer and overlaps the stat sections below — a persistent vertical overlap. The weather card now renders inline (no overlay), restoring clean alignment; nav transition + chip animations remain. `SharedTransitionLayout` stays in `MainScreen` for any future shared element that sits in a stable (non-scrolled) parent. Also discovered: `CrossfadeUiState` (G3) cannot be used inside `verticalScroll` columns — same overlap symptom — so Weather uses plain `when()` instead. |
| G8 | **FloatingTabBar indicator** | ⚪ | Low | Animate the active-tab background (move/crossfade) instead of an instant color swap; keep labels always visible (M3 rule). *android-design-guidelines §2.1.* |
| G9 | **Edge-to-edge + insets** | 🟡 | Low | ✅ Done — `enableEdgeToEdge()` in `MainActivity` + `Scaffold` innerPadding already applied. |
| G10 | **Dynamic Color opt-in** | 🟡 | Med | ✅ Done — Profile → Appearance toggle (API 31+ guard), `UserPrefs.dynamicColorEnabled`, `AndroidFarmerFriendTheme(dynamicColor=...)` switches to `dynamicDarkColorScheme`/`dynamicLightColorScheme`; falls back to static `FarmerColors`. |

---

## 2. 🏠 Home Screen

#### ✅ Done
- **H2 — Time-of-day greeting.** `HomeViewModel.refreshGreeting()` now picks `goodMorning` / `goodAfternoon` / `goodEvening` (5–11 / 12–16 / else) via `Calendar.HOUR_OF_DAY` instead of a static "Welcome back". 3 new `AppStrings` fields × 11 languages added. HeroTitle reads e.g. "Good morning, Ravi".

| # | Item | Pri | Effort | What · Why · How |
|---|------|-----|--------|------------------|
| H1 | **Pull-to-refresh** | 🔴 | Med | ✅ Done — `PullToRefreshBox` tied to real `UiState` refresh (honest spinner). |
| H2 | **Time-of-day greeting** | 🟡 | Low | ✅ Done — see above. |
| H3 | **Expandable weather card** | 🟡 | Low | ✅ Done — `ExpandableWeatherCard` with `animateContentSize`, chevron overlay, 3 `MiniForecastTile`s revealed on expand. |
| H4 | **Quick-access badge counts** | 🟡 | Low | ✅ Done — `QuickAccessGrid(alertBadge=unreadCount, marketBadge=state.marketPreview.size)` with red circle badge on `QuickAccessTile`. |
| H5 | **Market strip "See All" polish** | ⚪ | Low | Ensure the new `LazyRow` See-All card animates its arrow on press and has a full 48dp touch target. |
| H6 | **Empty/error state on missing weather+market** | ⚪ | Low | If both fail, Home falls to an empty card region; add a single friendly `ErrorState`-style strip with retry rather than two silent gaps. |

---

## 3. 📊 Market Screen

#### ✅ Done
- **M0 —  Combine search + sort into one row** (user suggestion, v2). `SearchField` gained an optional `trailing` slot; the sort `IconButton` + `DropdownMenu` now live inside the search field, Google-search style. Sort icon tints `primary` when a non-default sort is active (default `NAME_ASC` → `textSecondary`). One less full-width row above the list.

| # | Item | Pri | Effort | What · Why · How |
|---|------|-----|--------|------------------|
| M1 | **Animated filter/sort switch** | 🟡 | Med | Category chips + sort swap the list instantly. Crossfade content with `AnimatedContent` + animate the selected chip (`G2`). *mobile-app-ui-design visual hierarchy.* |
| M2 | **Retail price comparison** | 🟡 | Low | Show `retailPrice` beside wholesale for veg/fruit (already mapped in model) — farmers compare retail margin. From audit #3. |
| M3 | **Egg trend prominence** | 🟡 | Low | `priceDiffPercent` is the *only* trend data — make it a large colored arrow (up/down/stable + %) instead of a small label, since it's the sole "signal". From audit #4. |
| M4 | **Pull-to-refresh status messages** | ⚪ | Low | Timer-based captions ("Fetching latest prices…" → "Market servers are slow…"). Humanizes the honest spinner. From audit #6. |
| M5 | **RowCard press interaction** | ⚪ | Low | RowCard has no press feedback beyond default ripple. Add a subtle press-state (slight scale / color) for tactile feel. *mobile-app-ui-design interaction polish.* |

---

## 4. 🌤 Weather Screen

| # | Item | Pri | Effort | What · Why · How |
|---|------|-----|--------|------------------|
| W1 | **Hourly forecast row** | 🔴 | Med | ✅ Done — `buildHourly()` in `FarmerRepository` pulls next 8 hours from `OpenMeteoHourly`, `HourlyForecastStrip` LazyRow of `HourlyTile` (icon + temp + rain%). |
| W2 | **Sunrise/sunset + spray advisory** | 🟡 | Low | ✅ Done — `daily.sunrise`/`daily.sunrise` parsed to HH:mm, `SunTimeTile` pair (sunrise + sunset with `TintIconCircle`). Spray advisory pending — `wind_speed_10m_max` not yet queried. |
| W3 | **UV + soil/ET0 chips** | ⚪ | Low | `uv_index_max` color-coded advisory chip; optionally `soil_temperature`+`et0` for irrigation scheduling. Audit #3/#5. |
| W4 | **Pull-to-refresh** | 🟡 | Med | Weather data goes stale; add PTR here too (consistent with G/H). |
| W5 | **Animated temp across day pills** | ⚪ | Low | `DayPill` selection swaps instantly; animate selected state (G2) + brief temp count-up (G6). |

---

## 5. 🔔 Alerts Screen

| # | Item | Pri | Effort | What · Why · How |
|---|------|-----|--------|------------------|
| A1 | **Swipe undo / snackbar** | 🟡 | Low | Swipe-to-dismiss marks read with no undo. M3 says destructive/persistent swipes need undo or confirmation. Add a snackbar "Marked as read · Undo" for 3s. *android-design-guidelines §7.2/R7.3.* |
| A2 | **Priority visual treatment** | 🟡 | Low | Critical (red border), warning (orange), info (gray) — via content analysis of `Alert.message`. Users scan for severity. Audit #3. |
| A3 | **Haptic on swipe** | ⚪ | Low | Tie into G4 — a light confirm haptic when swipe crosses the threshold. |
| A4 | **Filter switch crossfade** | ⚪ | Low | Alert type chips swap the list instantly; apply G2 crossfade. |

---

## 6. 🐛 Disease Screen

| # | Item | Pri | Effort | What · Why · How |
|---|------|-----|--------|------------------|
| D1 | **Crop-type color coding** | 🟡 | Low | Group by `Disease.cropAffected` with colored chips (leaf vs fruit vs general). From audit #3. |
| D2 | **Excerpt line clamp polish** | ⚪ | Low | Currently snippet is up to 2 lines; ensure the `excerpt` + `cropAffected` fallback reads well in all 11 langs (test Tamil). |
| D3 | **Source-chip interaction** | ⚪ | Low | Whole card opens Wikipedia; make the source chip itself tappable/labeled so the intent is clear (icon + host), non-color cue. |

---

## 7. 🏛 Schemes Screen

| # | Item | Pri | Effort | What · Why · How |
|---|------|-----|--------|------------------|
| S1 | **Bookmark / save schemes** | 🟡 | Med | ✅ Done — `SchemesViewModel` converted to `AndroidViewModel`, SharedPreferences keyed on `sourceUrl` (stable; `Scheme.id` is unstable positional index). `SchemeItem` heart toggle (Favorite/FavoriteBorder), `PillChipGroup` ALL/SAVED filter, `noSavedSchemes` EmptyState. |
| S2 | **Central vs State filter color** | ⚪ | Low | `filterCentral`/`filterState` chips swap instantly; apply G2 animated selection. |

---

## 8. 📝 Crop Notes Screen

| # | Item | Pri | Effort | What · Why · How |
|---|------|-----|--------|------------------|
| C1 | **Seasonal tips section** | 🟡 | Low | Calendar-keyed to `Calendar.MONTH` (sowing/harvest notes per month). From audit #3. |
| C2 | **Expanded note action affordance** | ⚪ | Low | The source link only appears when expanded; add a clear "Read full article ↗" button label rather than just the host text, so the tap affordance is obvious. |
| C3 | **Filter chip animated selection** | ⚪ | Low | Applies G2. |

---

## 9. 👤 Profile Screen

| # | Item | Pri | Effort | What · Why · How |
|---|------|-----|--------|------------------|
| P1 | **Settings rows with icon + subtitle** | 🟡 | Med | Upgrade plain text rows to icon + title + subtitle (per-item themed icons already exist). Audit #1. |
| P2 | **Fix tempEmail dead field / profile staleness** | 🟡 | Low | Re-read `LocationPrefs` on resume; resolve `tempEmail`. Audit #2/#3. |
| P3 | **Appearance section → theme + dynamic color** | 🟡 | Med | ✅ Done — `ThemeSection` in ProfileScreen with API 31+ guard, `Switch` toggles `ProfileEvent.ToggleDynamicColor` → `UserPrefs.dynamicColorEnabled` → `AndroidFarmerFriendTheme(dynamicColor=...)`. System/Light/Dark override still pending. |
| P4 | **Unlock teaser polish** | ⚪ | Low | The gold `UnlockTeaserCard` is a good peak moment; add a gentle entrance animation. |
| P5 | **Pull-to-refresh** | ⚪ | Low | Profile is static but identity may drift; PTR is optional here. |

---

## 10. 🎬 Animation / Auth / Nav components

#### ✅ Done
- **N3 — Predictive back.** `AndroidManifest.xml` already sets `android:enableOnBackInvokedCallback="true"`; Navigation Compose (BOM 2026.02.01) pops the stack natively and no screen intercepts back (no custom `BackHandler`), so Android 13+ shows the live predictive-back preview, and the G1 nav transitions are driven by its progress. No code change was required.

| # | Item | Pri | Effort | What · Why · How |
|---|------|-----|--------|------------------|
| N1 | **Splash tagline** | 🟡 | Low | Fade a tagline in during the 900ms hold to reduce dead time; navigate sooner. Audit #1. |
| N2 | **Sub-screen slide transitions** | 🟡 | Low | 300ms enter / 200ms exit for push navigation to sub-screens. Audit #2. |
| N3 | **Predictive back** | 🟡 | Low | ✅ Done — manifest already opts in; Nav Compose drives the live preview. |
| N4 | **Google Sign-In one-tap** | 🟡 | Med | Already has credentials/google-id deps; surface one-tap above email/password. Audit #1. |

---

## Premium/vision adds (possibly later product scope)

- **Home "Today at a glance"** — a single hero strip uniting temp + top price + alerts count, tappable to each. Use `mobile-design-skill` Mode 1 screen-concept process before building.
- **Skeleton-first launch** — shaped skeletons on Home/Wx/Market (`G5`).
- **Dark-mode polish audit** — verify each `DarkFarmerColors` surface against `CompactWeatherCard` gradient + glass pills (from audit Part 6).

---

## Recommended build order (for discussion)

**Wave 1 — instant polish (all low effort, cross-cutting): ✅ COMPLETE.** G1 (nav transitions) → G2 (animated selection) → G4 (haptics) → H2 (greeting) → N3 (predictive back). One compile after each, all green.

**Wave 2 — feel + gaps:** ✅ G3 (crossfade) → G5 (skeleton) → H1/W4 (PTR on Home+Weather) → M2/M3 (retail + egg trend) → A2 (alert priority).

**Wave 3 — data-driven value (weather is the farmer's planning tool):** ✅ W1 (hourly) → W2 (sunrise/sunset) → H3 (expandable weather) → H4 (badges).

**Wave 4 — structural:** ✅ G9 (edge-to-edge) → G10 (dynamic color) → G7 (shared elements) → S1 (bookmarks).

---

## Discussion starters

1. **Scope:** do we want all four waves, or start with Wave 1 (quick wins) and decide Wave 3 (data-rich weather work) together?
2. **Motion level:** the app is intentionally flat-green. How much motion is "enough" — G1/G2 yes, but do we want shared elements (G7) and counters (G6) or keep it restrained per the skills' "avoid overuse" warning?
3. **New strings:** every text addition (greeting, status messages, spray advisory, seasonal tips) means editing **11 language instances**. Fine to proceed wave-by-wave?
4. **Commit policy:** working-tree only unless you ask — confirm as we go.

Tell me which screens/items you want to tackle first and we'll implement. Nothing is committed yet.
