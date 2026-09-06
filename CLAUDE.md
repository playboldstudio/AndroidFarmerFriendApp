# CLAUDE.md — Farmer Friend (AndroidFarmerFriendApp)

Context for any agent working on this codebase, so a new requirement lands correctly the first time. Read this **before** starting any task.

---

## Mandatory design-system skills for any UI/UX work

**On ANY UI/UX task — designing, planning, reviewing, implementing, or critiquing a screen, flow, or component — the agent MUST load and apply the three locally installed design skills.** They are the project's design system source of truth for *how to design*, layered over the token system below (which is *what* to use). Load them via the Skill tool before producing UI output:

| Skill (local) | Role / when to apply |
|---------------|----------------------|
| **`android-design-guidelines`** | **Material 3 platform correctness.** Always on for Android: dynamic color, color roles, navigation (bottom bar/rail/drawer), responsive window-size classes, typography scale, component selection (FAB, chips, sheets, snackbar vs dialog), accessibility, gestures, predictive back, notifications. Follow its "Design Evaluation Checklist". |
| **`mobile-app-ui-design`** | **Polish + emotion.** Typography discipline (≤4 sizes / ≤2 weights), 60/30/10 color rule, 8-point grid relationship spacing, soft shadows, empty/error/loading/success states, peak-end rule, micro-animations as trust signals, thumb-zone CTA placement. |
| **`mobile-design-skill`** | **Workflow + rigor.** Starts every response with `Mode:`, `Platform scope:`, `Device class:`, `Assumptions:`, then its per-mode output contract. Reason per decision (name the alternative considered and why it lost). Enforce its hard constraints: no invented platform rules, no invented research, accessibility built in, never aesthetic-only. Follow its self-review before returning. |

**How to combine them (precedence):**
1. Project rules in this file and the real code/data (highest).
2. `android-design-guidelines` for any platform-convention call (Material 3).
3. `mobile-design-skill` for the response structure, rigor, and self-review; `mobile-app-ui-design` for visual-craft decisions.
4. Real constraints: this app is **11-language**, **token-first**, and **minSdk 24** — the skills' generic advice must be reconciled with those (e.g. keep `FontFamily.Default` for Indic scripts, keep every `Text` overflow-safe).

**Always apply the standing constraints even when a skill suggests otherwise:** token-first (no hardcoded color/spacing), no emojis in code/strings, overflow-safety on every Text, `imePadding` on search screens, 48dp touch targets, and never building UI on data the APIs don't supply.

---

## Improvement / revamp workflow (create-new, delete-old)

**Whenever the user asks for a `revamp`, `production ready`, or `further improvement` pass** (UI or otherwise):

1. **Create a NEW markdown plan/backlog file** documenting the improvement work. Use a fresh, dated/versioned filename in `docs/` (e.g. `docs/screen-wise-improvements-v2.md`, `docs/revamp-v3.md`).
2. **Delete the old related markdown file** if one exists for that same body of work — do not let stale plans accumulate alongside fresh ones. Remove the superseded file (and, if the old file is referenced from this CLAUDE.md, update that reference).
3. Confirm the current file is the **single source of truth** for that improvement track before writing.
4. Follow the new file's own "what/how + dependency/skill" pointers. Keep the newest plan file as the single **active working plan** for its track (the prior `design-system-audit.md` was superseded by `docs/screen-wise-improvements-v2.md`).

### How to write these improvement files — "what" and "how", current/production structure

Focus each file on **`what`** (the concrete deliverable) **and `how`** (the exact file/component/pattern to change) — not vague goals. Use the **current recent developer file structure and patterns** so every item is up-to-date and production-ready:

- **Ground every item in the real codebase.** Name the actual file (`ui/screens/<name>/XScreen.kt`), the composable, and the token(s) (`FarmerTheme.colors.*`, `FarmerSpacing.*`, `FarmerMotion.*`, `MaterialTheme.typography.*`). Never propose a pattern the project doesn't already use unless you flag it as new.
- **Follow the codebase's own patterns** (MVI per screen: `Screen + ViewModel + Event + State`; shared components in `ui/components/`; `UiState` for Loading/Error/Success; `AppStrings` for all user text). Match existing conventions rather than inventing parallel ones.
- **State each item as:** priority (🔴/🟡/⚪) · effort · `what · why · how`. Include the M3/mobile-design rationale and the skill that informs it.
- **Compute the cost honestly:** every new user-visible string means **11 language instances** (`AppStrings`); every new color/branding change flows through the `FarmerColors` tokens; release-worthiness means `assembleRelease` checks for R8-touching changes.
- **Structure the file** in waves/order so it reads as a build sequence (quick wins → gaps → data-rich value → structural), and end with discussion starters / open decisions.
- **Keep it current:** mark items ✅ as they complete and retire the file's completed items so it never serves stale guidance to the next agent.

---

## Project at a glance

**Farmer Friend** is a Jetpack Compose Android app for Indian farmers, localized into **11 languages**, showing live market prices, multi-day weather, government schemes/disease reference lists, and push alerts.

- Application id: `com.playboldstudio.farmerfriend`
- `minSdk 24` · `compileSdk 37` · `targetSdk 37` · version `1.6.0`
- Stack: **Compose + Material3** (BOM 2026.02.01), Navigation Compose, Retrofit + Gson, Coil, Firebase (Auth / Firestore / Messaging / Analytics / Crashlytics), WorkManager, in-app updates (`play-app-update-ktx`).
- Release builds run **R8 minify + resource shrink** — see the R8 gotcha below.

---

## Build & verify

- **JDK 17** is required (Adoptium `C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot`).
- Compile only (fast loop):
  ```
  ./gradlew.bat compileDebugKotlin
  ```
- Do a full release check whenever a change touches R8-sensitive code (generic Gson/DTO classes, reflection):
  ```
  ./gradlew.bat assembleRelease
  ```

### Hard rules (do not break)

- **Never commit or push unless the user explicitly asks.** Report finished work as working-tree-only changes.
- **Never use emoji characters** in code comments or any user-facing string (11 languages). No `📊`, no `✅` in code/strings. Emojis are stripped from notification workers by design.
- **Never hardcode a color** — use `FarmerTheme.colors.*` tokens only. Never hardcode spacing/icon sizes — use `FarmerSpacing.*` / `FarmerIcons.*`. (Exception: literal `dp` inside an isolated composable where a token is not a fit, and `Color.White.copy(alpha=…)` overlays on gradients are fine.)
- Prefer the **Edit/Write tools**. Never strip emojis or rewrite Kotlin via PowerShell/`sed` regex — it has corrupted worker files before.
- Use `Icons.AutoMirrored.Filled.*` for ArrowBack / ArrowForward / Sort / List, not the deprecated `Icons.Filled.*`.

---

## Design system (token-first)

All tokens live under `ui/theme/`. There is **one** source of truth — never introduce inline values that duplicate them.

### Color — `ui/theme/Color.kt` (49 tokens, `FarmerTheme.colors`)
Two full palettes: `LightFarmerColors` (warm neutral `#F7F6F3` bg) and `DarkFarmerColors` (warm dark, green-tinted surfaces, never pure black). Groups:

| Group | Tokens | Use |
|-------|--------|-----|
| Neutrals | `background` `surface` `surfaceMuted` `textPrimary` `textSecondary` `textTertiary` `outline` | Base surfaces + text hierarchy |
| Brand | `primary` `primaryDeep` `primaryBright` `onPrimary` | Farmer green accent (gradients go deep→bright) |
| Semantic accents | `weatherBlue` `weatherYellow` `alertRed` `alertBlue` `alertGreen` `alertPurple` `diseaseOrange` `cropBrown` | Screen-specific accents |
| Soft tint containers | `softGreen` `softBlue` `softRed` `softPurple` `softOrange` `softBrown` `softMint` `softLavender` | Low-saturation icon/card backgrounds |
| Gold / unlock | `goldBorder` `goldTitle` `goldBody` `unlockTop` `unlockBottom` `unlockTileBg` `unlockTileIcon` | Profile gold unlock card |
| Semantic roles | `destructive` `onDestructive` `destructiveContainer` `link` `disabled` `disabledContainer` `scrim` `inverseSurface` `inverseOnSurface` | M3-standard roles |
| Surface hierarchy | `surfaceDim` `surfaceBright` `surfaceContainer` `surfaceContainerLow` `surfaceContainerHigh` | Tonal elevation |
| Category (market) | `categoryVegetable` `categoryFruit` `categoryNonVeg` `categoryGold` `categoryEgg` | Market icon tints |

### Category theming (market identity)
Each market category has a **distinct icon + tint + soft container** and must keep that identity:
vegetable → green, fruit → orange, nonveg → red, gold → amber, egg → brown. Match against `category*` + `soft*` tokens, never a hardcoded color.

### Spacing — `ui/theme/Dimens.kt` (`FarmerSpacing`)
8-point grid: `xs=4 s=8 md=12 lg=16 xl=20 xxl=24 xxxl=32 xxxxl=40 section=48 screen=56` dp. Groups of related UI sit closer together than unrelated groups (relationship-based spacing).

### Motion — `ui/theme/Dimens.kt` (`FarmerMotion`)
Durations: `durationFast=100` `durationNormal=200` `durationSlow=300` `durationModal=250` ms. Curves: `standardDecelerate` `standardAccelerate` `emphasizedDecelerate` `emphasizedAccelerate`. Springs: `springBouncy(0.5,1500)` `springSnappy(0.75,2000)` `springGentle(0.85,800)`. Use these, not ad-hoc `tween`s.

### Icons — `ui/theme/Dimens.kt` (`FarmerIcons`)
`sizeXs=14 sizeSm=18 sizeMd=22 sizeLg=28 sizeXl=36` dp + `touchTarget=48` dp.

### Typography — `ui/theme/Type.kt` (15 styles)
Full M3 scale, `FontFamily.Default`. Use `MaterialTheme.typography.*` roles, never raw `sp` **except** sizes already implicit in a token or a local override that matches a role. Key: `displayLarge 66` `headlineMedium 26` `headlineSmall 22` `titleMedium 17` `titleSmall 14.5` `bodyMedium 14.5` `labelLarge 14.5` `labelSmall 10.5`.

### Overflow rule (critical, 11 languages)
Every `Text` showing localized/live content **must** have `maxLines` + `TextOverflow.Ellipsis` + a constrained width (`weight(1f)` or `widthIn(min,max)`). Titles max 2 lines, body 3, pills 1. Test mentally at 200% font scale and in the longest language (often Tamil/Malayalam).

---

## Localization (11 languages)

- `data/localization/AppStrings.kt` holds a single `AppStrings` **data class with ~197 fields**, populated by companion-object instances for **Tamil, English, Hindi, Telugu, Malayalam, Kannada, Marathi, Bengali, Punjabi, Gujarati, Odia**.
- **When you add any new user-visible string:**
  1. Add the field to `AppStrings` (grouped with related fields).
  2. Populate it in **every one of the 11 language instances** — never leave one missing.
  3. This is repetitive; delegating the 11-language fill to a subagent is acceptable, but **verify by counting occurrences** of the new field across the file (11 expected) before moving on.
- Read current strings via `LocalAppStrings.current`.
- `LanguagePrefs` is the persisted language source.

---

## Architecture

**MVI per screen** under `ui/screens/<name>/`: `XScreen.kt` (Compose) + `XViewModel.kt` + `XEvent.kt` + `XState.kt`. Screens expose `(onBack)` / `(onNavigate)` callbacks. State flows via `collectAsStateWithLifecycle()`. Loading/Error/Success live in `data/util/UiState.kt`.

- **Nav:** `ui/navigation/Screen.kt` routes; `ui/screens/MainScreen.kt` hosts the `Scaffold` + `NavHost` + `FloatingTabBar` (top-level routes only). Sub-screens use `SubScreenHeader` (back arrow + title).
- **Screens:** Home, Market, Weather, Alerts, Profile (top-level); Schemes, Disease, Crop Notes, Language, Auth, Privacy, Terms (sub).
- **Shared components** in `ui/components/`: `RowCard`, `TintIconCircle`, `PillChipGroup`/`PillChip`, `SearchField`, `LocPill`, `ShimmerList`, `EmptyState`, `ErrorState`, `SlowNetworkState`, `OfflineState`, `SubScreenHeader`, `HeroTitle`, `CompactWeatherCard`, `FloatingTabBar`, `CenteredMaxWidth`, `WeatherFarmTips`, `UpdateBannerCard`, `WeatherLoader`.
- **Data:** Retrofit APIs + `WebDataScraper` (Wikipedia lists) → `FarmerRepository` → ViewModels. Alerts from Firestore. Network errors recorded via `data/api/NetworkErrors.kt` (`record` → Crashlytics, `friendlyMessage` → UI).
- **Background:** `notifications/*Worker.kt` (WorkManager) for price/weather/digest alerts; keep titles emoji-free.

---

## State-handling conventions

Every screen content area branches on `UiState`:

```
Loading  → ShimmerList / WeatherLoader
Error    → ErrorState (retry) — but branch on NetworkUtil.isOnline() to
           show OfflineState instead when offline
Success  → empty → EmptyState(icon, title); else the list/content
```

Reuse these shared states — do not hand-roll per-screen `when` blocks.

---

## Motion & polish baseline (standard for new work)

The audit (below) defines where animations belong. Baseline expectations:
- **Screen navigation transitions** in the `NavHost`: `enterTransition`/`exitTransition` (+ `pop` variants) using `FarmerMotion` durations/curves.
- **Selection/filter changes** animate: `animateColorAsState` for chip/pill selection, `AnimatedContent` crossfade when swapping whole content blocks.
- **State swaps** (Loading↔Error↔Success) crossfade rather than blink.
- **Haptic feedback** on key interactions: pull-to-refresh, tab switch, swipe-to-dismiss, filter taps (`LocalHapticFeedback`).
- **Shared elements** where a card navigates to a detail surface.
- Respect `prefers-reduced-motion`; every animation has a resting state (never parked at `opacity: 0`).

---

## Accessibility (audit checklist — chips not yet all checked)

| Rule | Status |
|------|--------|
| `contentDescription` on all icon-only composables | ⬜ (some done) |
| 48dp touch targets (HeaderIconButton is 44dp) | ⬜ |
| `textTertiary` contrast (darken both palettes) | ⬜ |
| `mergeDescendants` on RowCard items for TalkBack | ⬜ |
| Test at 200% font scale | ⬜ |
| `imePadding()` on all search screens | ✅ done |
| Non-color-only signaling (pair color with icon/text) | ⬜ |

Default starting point for any touch target: `FarmerIcons.touchTarget` = 48dp.

---

## R8 gotcha (release builds)

R8 strips generic signatures/reflection targets. Historical crash: `OfflineCache` (generic `LruCache`-style DTOs) broke release; removed in v1.5.0.
- DTOs and Gson wrappers (`data.api.*`, `data.scraper.*`, `data.model.*`) are kept in `proguard-rules.pro`. If you add a new generic/reflection-based class, add a matching `-keep`.
- Always `assembleRelease` before shipping when you touch data/parsing layers.

---

## API data reality (do not promise what data can't deliver)

- **Market:** only *today's* prices (or yesterday fallback). No historical endpoint → no price-history sparklines, no day-over-day comparison. Only **vegetables** return images.
- **Egg:** the *only* category with trend data (`priceDiffPercent` from `avg`).
- **Weather:** Open-Meteo — many free fields unused: `hourly` (hourly forecast), `sunrise/sunset`, `uv_index_max`, `wind_speed_10m_max`, `soil_temperature`, `et0_fao_evapotranspiration`.
- **Reference lists** (Schemes/Disease/CropNotes): Wikipedia returns title + ~200-char excerpt + URL only. No symptoms/treatment, no eligibility, no images. Fall back to English Wikipedia when a language host fails (see `WebDataScraper`).

Do not build UI on data the APIs cannot supply. When a requested feature depends on missing data, say so and propose the honest alternative.

---

## Current UI state (feature/ui-transformation-v2)

Already delivered: category-aware market thumbnails, weather-based farm tips (`WeatherFarmTips`), swipeable market preview `LazyRow` + "See All", market **sort** (name/price) + `imePadding`, alerts **time grouping** + swipe-to-dismiss, disease/scheme **excerpts**, expandable crop notes, redesigned **update dialog**, and emoji-stripped notification workers.

Working-tree only (uncommitted until asked): the `UpdateBannerCard.kt` redesign.

The active improvement track lives in **`docs/screen-wise-improvements-v2.md`** — read it before proposing UI work; keep it updated (mark ✅ / append / retire completed items) as tasks complete. Per the improvement workflow it superseded `docs/design-system-audit.md` (deleted).
