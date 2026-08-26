# Farmer Friend — Complete UI Redesign Plan

> Status: **Approved plan — not yet implemented**
> Scope: all screens (tabs, sub-screens, sheets, splash) + approved data-display enhancements + new Firebase Auth flow.
> Hard rule for Phases 0–2: **UI-only**. No ViewModel / State / Event / Repository / prefs / navigation-graph logic changes.

---

## Ground rules

Never touched in Phases 0–2:
- All `*ViewModel`, `*State`, `*Event` classes
- `FarmerRepository`, `FirestoreAlertRepository`, `WebDataScraper`
- Workers (`notifications/*`), `UserPrefs`, `LocationPrefs`, `LanguagePrefs`
- Route names, `NavHost` graph shape, Firestore user-sync, alert seeding

Phase 3 relaxes this for three explicitly approved additive data mappings.
Phase 4 (Firebase Auth) is **new feature work** and defines its own logic.

---

## Phase 0 — Design foundations (shared)

| Item | Today | Plan |
|---|---|---|
| Typography | Only `bodyLarge` defined; 40+ hardcoded `fontSize = X.sp` call sites | Full scale in `Type.kt` (display/headline/title/body/label); migrate call sites |
| Spacing | Ad-hoc 4–24dp values everywhere | Token constants (4/8/12/16/20/24) |
| Cards | `FarmerCard` + ~5 hand-built `Card(RoundedCornerShape(24.dp))` duplicates | All surfaces via `FarmerCard`; single shadow/radius source |
| Insets | `ProfileScreen` uses `WindowInsets(0,0,0,0)` hack | One consistent edge-to-edge rule for all tabs |
| Hardcoded English | `"Retry"`, banner copy, `"Chennai only"`, empty-state subtitles, CropNotes fake tags | Route through `LocalAppStrings` (additive string keys only) |

---

## Phase 1 — Tab screens

### Home (`HomeScreen.kt`)
- Merge logo row + greeting into one header; tappable avatar chip → Profile tab
- Weather hero gains **feels-like** line (`WeatherInfo.feelsLike` — fetched today, never rendered); hero becomes tap-target → Weather tab
- Quick-access grid: keep 3×2; refine tile (hairline border, press ripple, consistent 52dp orb)
- `NotificationPermissionBanner`: pass localized strings instead of hardcoded defaults

### Market (`MarketScreen.kt`)
- Crop rows: wholesale price stays bold/right; add second line **“Retail: ₹39 – 46”** (`Crop.retailPrice` — already mapped, never displayed)
- `MarketTrendCard`: design for real behavior — price-range mode (min–max) becomes primary; rising/falling branch kept but de-emphasized
- Gold “Chennai only” note → styled info chip, localized
- Loading state: shimmer placeholders instead of spinner (visual only)

### Weather (`WeatherScreen.kt`)
- Details list → 2×2 stat-tile grid (Rain / Humidity / Wind / Direction)
- **Feels-like added to hero**
- `DayPill` row functionally unchanged; stronger selected-state elevation

### Alerts (`AlertsScreen.kt`)
- Replace full red unread border with type-colored left accent bar (calmer); red dot retained
- “N new” badge moves next to section title; time chip right-aligned
- Pull-to-refresh + mark-read flows untouched

### Profile (`ProfileScreen.kt`)
- Identity hero card: soft-green gradient header, 84dp initials avatar (derived in UI from `state.userName`), name, `+91 •••` phone, inline location pill (already read via `LocationPrefs` here), Edit pill top-right
- Edit mode: filled rounded inputs matching `SearchField` aesthetic; Save = primary pill, Cancel = ghost — same events, same validation messages
- Menu regrouped with section headers:
  - **Account**: My Details, My Lands
  - **Preferences**: Language (+ current value trailing), Notifications
  - **Legal & About**: Privacy, Terms, Settings
- Coming-soon snackbar path unchanged; version + tagline footer kept

---

## Phase 2 — Sub-screens, sheets, shell

- **Disease / Schemes / CropNotes**
  - Add `SubScreenHeader` back affordance (wiring identical to Privacy/Terms — `popBackStack` only)
  - RowCards: maxLines=3 descriptions; keep OpenInNew affordance
  - Remove fake metadata (“ACTIVE” chip, “💧 Watered 🧪 Fertilized”, “Today” — static decorations with no data behind them)
  - Blank Wikipedia excerpt → fallback subtitle text
- **Language**: radio list → selectable cards (native script prominent, checkmark); immediate-apply note kept
- **Privacy / Terms**: thin linear progress while WebView loads
- **FloatingTabBar**: tiny labels + `contentDescription` (currently null — accessibility bug); reserved badge slot on Alerts tab
- **MarketPickerSheet**: section headers using existing `MarketGroup` enum (Major Markets / States / Cities)
- **LocationPickerSheet / NotificationPermissionBanner**: unify drag-handle/search styling with tokens
- **Splash**: neutral gradient → brand green wash (cosmetic)

---

## Phase 3 — Approved data enhancements ✅

Small **additive** data-layer changes, user-approved:

1. **Product images on market rows**
   - `VegetableItem`/DTO: add `table.imageUrl` mapping (prepend `https://vegetablemarketprice.com/`)
   - Map into `Crop.imageUrl` (field already exists); render in market rows replacing generic cart icon
   - Applies to vegetables (fruits/nonveg/gold return `null` images → fall back to icon)
2. **Egg “vs month avg” indicator**
   - `NcecEggPriceItem.avg` → carry monthly average through `toCrop()` (e.g., into `prevPrice` slot)
   - Egg rows show ▲/▼ chip computed against month avg (only genuine trend signal available)
3. **Yesterday-fallback when today’s sheet is empty**
   - Market load: if response `data` empty → retry once with `date = today-1`
   - Label list with the **response’s own `date` field** so users see which day prices are from
   - Explicitly approved ViewModel touch; isolated to `MarketViewModel.loadData()`

API field → UI summary (from live tests):

| API | Field | Use |
|---|---|---|
| vegetablemarketprice.com | `retailprice` ("39 - 46") | Retail line on veg/fruit rows |
| vegetablemarketprice.com | `table.imageUrl` | Product thumbnails |
| vegetablemarketprice.com | `date` + past-date queries | Freshness label + fallback |
| NCEE egg | `avg` | vs-month-avg trend chip |
| Open-Meteo | `apparent_temperature` | Feels-like display |

---

## Phase 4 — Firebase Auth (Sign Up / Sign In) 🆕

New feature. Adds logic by design; everything else keeps working.

### Dependencies & setup
- Add `libs.firebase.auth` under existing `firebase-bom` platform in `app/build.gradle.kts`
- `google-services` plugin already applied — no config change needed
- Enable Email/Password provider in Firebase console

### New screens & navigation
- `Screen.Auth` route (`Screen` sealed class already has unused `Onboarding`/`Location` precedents)
- `AuthScreen`: segmented toggle **Sign In / Sign Up**, email + password fields, name + phone extra on Sign-Up, primary pill button, error text slot
- `MainActivity`/`MainScreen`: gate start destination on `Firebase.auth.currentUser != null`; listen to `AuthStateChanged` to navigate on sign-out
- Back button disabled on Auth when signed out (single top-level destination)

### New `AuthViewModel` (MVI, matches house style)
- State: `authMode`, `email`, `password`, `name`, `phone`, `isLoading`, `error`
- Events: `ToggleMode`, `UpdateEmail/Password/Name/Phone`, `Submit`, `SignOut`
- Logic: `createUserWithEmailAndPassword` / `signInWithEmailAndPassword`; on success write `users/{uid} = {name, phone, email, createdAt}` (replaces FCM-token-keyed doc over time)

### Profile integration
- Signed-in card shows Firebase user (initials avatar, name/email, “Verified” chip)
- **Logout**: confirmation dialog using existing `AppStrings` keys (`logoutTitle`, `logoutMessage`, `yes`, `no`, `logout`) — they already exist unused in all 11 languages
- `ProfileViewModel.saveUserToFirestore` migrates doc key from `fcmToken` → `uid` behind the auth flag
- Optional later: anonymous auth → link-credential upgrade so farmers can browse first, sign up later

### Localization
- Additive string keys only: `emailField`, `passwordField`, `signIn`, `signUp`, `forgotPassword`, `authError*` variants across all languages in `AppStrings`

---

## Suggested implementation order

1. Phase 0 foundations → compile check
2. Phase 1 tabs (Profile first) → compile check per screen
3. Phase 2 sub-screens/sheets/shell
4. Phase 3 data enhancements (images → egg avg → date fallback)
5. Phase 4 Auth (deps → AuthScreen/VM → nav gating → Profile logout → i18n)

Each step leaves the app releasable.
