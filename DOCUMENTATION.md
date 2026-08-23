# MasjidScreen — Full Technical Documentation

> **App ID:** `com.mirazanik.masjidscreen`  
> **Min SDK:** 26 (Android 8) · **Target SDK:** 35 (Android 15)  
> **Language:** Kotlin · **UI:** Jetpack Compose (Material 3)  
> **Backend:** Firebase Firestore + Firebase Auth  
> **Local DB:** Room  
> **Prayer Engine:** Adhan library  

---

## Table of Contents

1. [What the App Does](#1-what-the-app-does)
2. [Project Structure](#2-project-structure)
3. [Architecture Overview](#3-architecture-overview)
4. [Data Models](#4-data-models)
5. [Local Database (Room)](#5-local-database-room)
6. [Remote Backend (Firestore)](#6-remote-backend-firestore)
7. [Complete Data Flow](#7-complete-data-flow)
8. [Offline Mode — How It Works](#8-offline-mode--how-it-works)
9. [Prayer Time Calculation](#9-prayer-time-calculation)
10. [ViewModel & State Management](#10-viewmodel--state-management)
11. [UI Screens](#11-ui-screens)
12. [Admin Panel](#12-admin-panel)
13. [Auto-Update System](#13-auto-update-system)
14. [Localization (Bengali / English)](#14-localization-bengali--english)
15. [Theme & Colors](#15-theme--colors)
16. [Permissions & Security](#16-permissions--security)
17. [Dependencies](#17-dependencies)

---

## 1. What the App Does

MasjidScreen is a **mosque display screen application** intended to run on a landscape-mounted TV or monitor inside a mosque. It shows:

- Live prayer times (auto-calculated from location + method)
- Jamaat (congregation) times set by the admin
- Islamic (Hijri) and Gregorian dates
- Current and next prayer with countdown
- Prohibited (makrooh) prayer time warnings
- Rotating hadiths (Islamic sayings)
- Scrolling announcements / notices
- Network connectivity status

A **password-protected admin panel** (Firebase Auth) lets mosque administrators manage all data remotely — changes reflect on the screen in real time.

---

## 2. Project Structure

```
MasjidScreen/
├── app/src/main/java/com/mirazanik/masjidscreen/
│   │
│   ├── MosqueApplication.kt          ← App entry point, Firebase init
│   ├── MainActivity.kt               ← Hosts all screens, OTA updates, window config
│   │
│   ├── data/
│   │   ├── model/
│   │   │   └── MosqueModels.kt       ← All data classes (MosqueConfig, Hadith, etc.)
│   │   ├── local/
│   │   │   ├── AppDatabase.kt        ← Room database singleton
│   │   │   ├── entity/Entities.kt    ← Room entity classes
│   │   │   └── dao/Daos.kt           ← DAO interfaces (query/insert)
│   │   └── remote/
│   │       └── FirestoreRepository.kt ← Syncs Firestore → Room (read only)
│   │
│   ├── prayer/
│   │   └── PrayerCalculator.kt       ← Wraps Adhan; calculates prayer/Hijri times
│   │
│   ├── ui/
│   │   ├── screen/DisplayScreen.kt   ← Main mosque display (landscape fullscreen)
│   │   ├── component/
│   │   │   ├── PrayerTimesTable.kt   ← Prayer times grid
│   │   │   ├── HadithCard.kt         ← Rotating hadith display
│   │   │   └── NoticeTicker.kt       ← Scrolling notice banner
│   │   └── theme/
│   │       ├── Color.kt              ← Color palette
│   │       └── Theme.kt              ← Material 3 dark theme
│   │
│   ├── util/
│   │   └── BanglaUtils.kt            ← Bengali numeral conversion, time helpers
│   │
│   └── admin/
│       ├── data/AdminRepository.kt   ← Firestore read + write (admin only)
│       ├── viewmodel/
│       │   ├── AdminAuthViewModel.kt ← Firebase Auth login state
│       │   └── AdminPanelViewModel.kt ← Admin CRUD state
│       └── ui/screen/
│           ├── AdminPanel.kt         ← Tab host
│           ├── LoginScreen.kt
│           ├── DashboardScreen.kt
│           ├── JamaatScreen.kt
│           ├── HadithScreen.kt
│           ├── NoticeScreen.kt
│           └── SettingsScreen.kt
│
├── gradle/libs.versions.toml         ← Version catalog for all dependencies
└── app/build.gradle.kts
```

---

## 3. Architecture Overview

The app follows **MVVM with a Repository pattern** and a **single-source-of-truth** strategy: the Room database is the only thing the UI ever reads from.

```
┌─────────────────────────────────────────────────────────────┐
│                         FIREBASE                            │
│  Firestore (Config, Jamaat, Hadiths, Notices, Version)     │
│  Auth (Admin email/password)                                │
└──────────────┬──────────────────────────────────────────────┘
               │  Snapshot listeners (real-time)
               ▼
┌─────────────────────────────────────────────────────────────┐
│               FirestoreRepository                           │
│  Listens → maps to Entity → writes to Room                 │
└──────────────┬──────────────────────────────────────────────┘
               │  DAO upserts
               ▼
┌─────────────────────────────────────────────────────────────┐
│               Room Database (mosque_db)                     │
│  mosque_config · jamaat_times · hadith · notices            │
└──────────────┬──────────────────────────────────────────────┘
               │  Flow<T> (reactive streams)
               ▼
┌─────────────────────────────────────────────────────────────┐
│               DisplayViewModel                              │
│  Combines DB flows + clock tick + prayer calculations       │
│  → single DisplayState                                      │
└──────────────┬──────────────────────────────────────────────┘
               │  collectAsState()
               ▼
┌─────────────────────────────────────────────────────────────┐
│               DisplayScreen (Compose UI)                    │
│  Recomposes on every state change                           │
└─────────────────────────────────────────────────────────────┘
```

**Admin writes** go directly to Firestore via `AdminRepository`. Those changes are then picked up by the Firestore snapshot listener and flow back down through Room to the UI automatically.

---

## 4. Data Models

All defined in `data/model/MosqueModels.kt`.

### MosqueConfig
| Field | Type | Description |
|-------|------|-------------|
| `name` | String | Mosque name shown on header |
| `address` | String | Address (display only) |
| `latitude` | Double | Used for prayer calculation |
| `longitude` | Double | Used for prayer calculation |
| `calculationMethod` | String | MWL, ISNA, EGYPT, MAKKAH, KARACHI, MOON_SIGHTING, DUBAI |
| `madhab` | String | HANAFI or SHAFI (affects Asr time) |
| `language` | String | `en` or `bn` (Bengali) |
| `hadithInterval` | Int | Seconds between hadith rotations |
| `jamaatCountdownMinutes` | Int | Minutes before jamaat to show countdown |
| `hijriDateOffset` | Int | Days added/subtracted to Hijri date |

### JamaatTimes
| Field | Type | Description |
|-------|------|-------------|
| `fajrTime` | String | e.g. `"05:30"` |
| `fajrNote` | String? | Optional note (e.g. "Winter: 06:00") |
| `dhuhrTime` | String | |
| `dhuhrNote` | String? | |
| `asrTime` | String | |
| `asrNote` | String? | |
| `maghribTime` | String | `"auto"` = use calculated sunset |
| `maghribNote` | String? | |
| `ishaTime` | String | |
| `ishaNote` | String? | |

### Hadith
| Field | Type | Description |
|-------|------|-------------|
| `id` | String | Firestore document ID |
| `translation` | String | Full hadith text |
| `source` | String | e.g. "Sahih Bukhari 1234" |
| `narrator` | String | e.g. "Abu Hurairah (R.A.)" |
| `isActive` | Boolean | Whether to include in rotation |

### Notice
| Field | Type | Description |
|-------|------|-------------|
| `id` | String | Firestore document ID |
| `text` | String | Announcement text |
| `priority` | Int | Lower = shown first (0 = highest) |
| `isActive` | Boolean | Whether to display |

### Prayer (Enum)
```
FAJR, SUNRISE, DHUHR, ASR, MAGHRIB, ISHA
```
Each has `displayName`, `arabicName`, and `banglaName`.

---

## 5. Local Database (Room)

**Database:** `mosque_db` (version 9)  
**Migration strategy:** Destructive — database is wiped on schema upgrade (acceptable since Firestore is the source of truth and re-syncs on startup).

### Tables

| Table | Entity | Key | Notes |
|-------|--------|-----|-------|
| `mosque_config` | `MosqueConfigEntity` | `id = "default"` | Single row |
| `jamaat_times` | `JamaatTimesEntity` | `id = "default"` | Single row |
| `hadith` | `HadithEntity` | Firestore doc ID | Many rows |
| `notices` | `NoticeEntity` | Firestore doc ID | Many rows |

### DAOs

**MosqueConfigDao**
- `observe(): Flow<MosqueConfigEntity?>` — emits whenever config changes
- `upsert(entity)` — insert or replace

**JamaatTimesDao**
- `observe(): Flow<JamaatTimesEntity?>` — emits whenever jamaat times change
- `upsert(entity)` — insert or replace

**HadithDao**
- `observeActive(): Flow<List<HadithEntity>>` — only active hadiths
- `upsertAll(list)` — full sync (delete old, insert new)
- `deleteAll()` — called before re-sync

**NoticeDao**
- `observeActive(): Flow<List<NoticeEntity>>` — active notices sorted by priority
- `upsertAll(list)` — full sync
- `deleteAll()`

---

## 6. Remote Backend (Firestore)

### Firestore Document Structure

```
mosques/
└── default/                        ← Single mosque document
    ├── data/
    │   ├── config                  ← MosqueConfig fields
    │   ├── jamaat_times            ← JamaatTimes fields
    │   └── app_version             ← AppVersionInfo (versionCode, apkUrl, etc.)
    ├── hadiths/
    │   ├── {hadith_id}             ← Individual hadith documents
    │   └── ...
    └── notices/
        ├── {notice_id}             ← Individual notice documents
        └── ...
```

### FirestoreRepository (read side)

`FirestoreRepository` is responsible for **one-way sync: Firestore → Room**.

It sets up **snapshot listeners** (Firestore real-time listeners) for:
1. `data/config` → writes `MosqueConfigEntity` to Room
2. `data/jamaat_times` → writes `JamaatTimesEntity` to Room
3. `hadiths` collection → replaces all `HadithEntity` rows in Room
4. `notices` collection → replaces all `NoticeEntity` rows in Room

Each listener runs in a `CoroutineScope(SupervisorJob + Dispatchers.IO)` so failures in one listener don't crash others.

### AdminRepository (read + write side)

Used exclusively by the admin panel. It:
- Observes the same Firestore paths (directly, without going through Room)
- Writes config, jamaat times, hadiths, and notices back to Firestore
- Writes `app_version` document for OTA update distribution

Write operations use Kotlin `suspendCoroutine` to bridge the Firestore callback API with coroutines.

---

## 7. Complete Data Flow

### Display Screen Data Flow

```
App starts
    │
    ├─ MosqueApplication.onCreate()
    │       └─ Firebase.initializeApp()
    │
    ├─ MainActivity.onCreate()
    │       ├─ FirestoreRepository starts snapshot listeners
    │       │       └─ On each Firestore event:
    │       │               map fields → Room entity → DAO.upsert()
    │       │
    │       └─ DisplayViewModel created
    │               ├─ Collects Room Flows (config, jamaat, hadiths, notices)
    │               ├─ Starts 1-second clock ticker
    │               └─ Starts 30-second prayer countdown ticker
    │
    └─ DisplayScreen (Compose)
            └─ collectAsState(DisplayViewModel.state)
                    └─ Recomposes whenever state changes

Every second:
    DisplayViewModel clock tick
        ├─ Updates time/date
        ├─ Recalculates prayer times (via PrayerCalculator)
        ├─ Recalculates Hijri date (sunset-aware)
        ├─ Checks makrooh (prohibited) time windows
        ├─ Checks jamaat countdown
        └─ Updates DisplayState → UI recomposes
```

### Admin Write Flow

```
Admin edits data in AdminPanel
    │
    └─ AdminPanelViewModel.saveXxx()
            └─ AdminRepository.saveXxx()
                    └─ Firestore.set(documentRef, data)
                            │
                            └─ Firestore snapshot listener fires
                                    └─ FirestoreRepository maps → Room.upsert()
                                            └─ DisplayViewModel Flow emits
                                                    └─ DisplayScreen recomposes
                                                       (change appears on screen)
```

---

## 8. Offline Mode — How It Works

The app is fully functional offline because **the UI reads from Room, not directly from Firestore**.

### What happens when internet is lost:

| Step | What happens |
|------|-------------|
| 1 | Network disconnects |
| 2 | Firestore snapshot listeners pause (no new events fired) |
| 3 | Room database still contains the last synced data |
| 4 | `DisplayViewModel` continues reading from Room Flows |
| 5 | Prayer times are calculated locally (no network needed) |
| 6 | Hadiths, notices, jamaat times all served from Room cache |
| 7 | UI shows a "No Internet" indicator (network status badge) |

### What happens when internet returns:

| Step | What happens |
|------|-------------|
| 1 | Network reconnects |
| 2 | Firestore snapshot listeners resume automatically |
| 3 | Firestore sends latest data (or "no change" if nothing changed) |
| 4 | Room is updated with any changes that happened while offline |
| 5 | UI reflects updated data immediately |
| 6 | "No Internet" badge disappears |

### Network monitoring

`DisplayViewModel` uses Android's `ConnectivityManager.NetworkCallback` to track connectivity. It exposes `isOnline: Boolean` in `DisplayState`, which `DisplayScreen` uses to show/hide the offline badge.

### What does NOT work offline:

- Admin panel logins (Firebase Auth requires network for first login; stays logged in after)
- Admin writes (Firestore writes will be queued by Firestore SDK and sent when back online)
- App auto-updates

### Data persistence across restarts

Since Room is a SQLite database on device storage, all cached data **survives app restarts and device reboots**. The app can run indefinitely without a network connection as long as:
- It has synced at least once
- No database schema upgrade forces a wipe

---

## 9. Prayer Time Calculation

**Library:** [Adhan by Batoul Apps](https://github.com/batoulapps/adhan-java) v1.2.1

### How prayer times are calculated

```kotlin
PrayerCalculator.calculate(
    latitude = config.latitude,
    longitude = config.longitude,
    method = config.calculationMethod,   // e.g. "KARACHI"
    madhab = config.madhab               // "HANAFI" or "SHAFI"
)
```

Returns `PrayerTimesData`:
```
Fajr     → String (HH:mm)
Sunrise  → String (HH:mm)
Dhuhr    → String (HH:mm)
Asr      → String (HH:mm)
Maghrib  → String (HH:mm)
Isha     → String (HH:mm)
```

Prayer times are recalculated **every second** (as part of the clock tick) because the date may change at midnight.

### Makrooh (Prohibited) Time Detection

Three windows are detected and displayed as a warning banner:

| Name | Rule | Shown until |
|------|------|------------|
| Ishraq (Sunrise) | Fajr → Fajr + 20 min | 20 min after Fajr |
| Zawal | Dhuhr − 5 min → Dhuhr | Dhuhr time |
| Makrooh (Sunset) | Maghrib − 15 min → Maghrib | Maghrib time |

### Next Prayer / Countdown

`getNextPrayer()` compares current time against jamaat times (falling back to calculated times if jamaat is not set). Returns the next upcoming prayer name and minutes remaining.

`getCurrentPrayer()` returns the current waqt (prayer window) name and minutes until it ends.

### Hijri Date

Calculated using Android's `android.icu.util.IslamicCalendar`. The date automatically advances **after Maghrib** (sunset), matching the Islamic convention that the new day begins at sunset. A configurable `hijriDateOffset` (in days) allows the mosque to match their local moon-sighting decision.

---

## 10. ViewModel & State Management

### DisplayViewModel

Holds a single `StateFlow<DisplayState>` that the UI consumes.

**DisplayState fields:**

| Field | Type | Source |
|-------|------|--------|
| `currentTime` | String | System clock |
| `currentDate` | String | System clock |
| `hijriDate` | HijriDate | PrayerCalculator |
| `prayerTimes` | PrayerTimesData? | PrayerCalculator |
| `jamaatTimes` | JamaatTimesEntity? | Room |
| `nextPrayerName` | String | PrayerCalculator |
| `nextPrayerMinutes` | Long | PrayerCalculator |
| `currentPrayerName` | String | PrayerCalculator |
| `currentPrayerMinutes` | Long | PrayerCalculator |
| `currentHadithIndex` | Int | Cycled on timer |
| `hadiths` | List<HadithEntity> | Room |
| `notices` | List<NoticeEntity> | Room |
| `config` | MosqueConfigEntity? | Room |
| `isOnline` | Boolean | ConnectivityManager |
| `appUpdateInfo` | AppVersionInfo? | Firestore |
| `prohibitedTime` | ProhibitedTimeInfo? | PrayerCalculator |
| `jamaatCountdown` | JamaatCountdownInfo? | PrayerCalculator |

### AdminAuthViewModel

Manages Firebase Auth state:
```
Unauthenticated → [login(email, pw)] → Loading → Authenticated
                                                ↓ (on error)
                                              Error(message)
```

### AdminPanelViewModel

Holds `AdminUiState` with all admin-editable data. Each save method calls `AdminRepository`, which writes to Firestore. The UI is reactive: it observes `AdminRepository` Flows.

---

## 11. UI Screens

### DisplayScreen (Main)

Full landscape layout split into three zones:

```
┌──────────────────────────────────────────────────┐  ← 25% height
│  Mosque Name     |  Hijri Date  |  Gregorian  |  │  HEADER
│  (offline badge) |              |  Day+Date   |  │
└───────────────────────────────────────────────────┘
┌─────────────────┬────────────────────────────────┐  ← 75% height
│                 │                                │
│  CLOCK PANEL    │    PRAYER TIMES TABLE          │
│  40% width      │    60% width                   │
│                 │                                │
│  HH:MM:SS AM    │  Prayer | Waqt | Jamaat | Note │
│                 │  Fajr   | ...  | 05:30  |      │
│  Sunrise 05:10  │  Dhuhr  | ...  | 13:30  |      │
│  Sunset  18:45  │  Asr    | ...  | 16:30  |      │
│                 │  Maghrib| ...  | auto   |      │
│  Next: Asr      │  Isha   | ...  | 21:00  |      │
│  in 45 min      │                                │
└─────────────────┴────────────────────────────────┘
┌──────────────────────────┬───────────────────────┐  ← 25% height
│  HADITH CARD             │  NOTICE TICKER        │  FOOTER
│  "Hadith text..."        │  → scrolling notices  │
│  — Source, Narrator      │                       │
└──────────────────────────┴───────────────────────┘
```

### Special Display States

**Jamaat Countdown Banner** — overlays the bottom section when jamaat is within `jamaatCountdownMinutes` (configured) of starting:
```
┌─────────────────────────────────────────────────┐
│  🕌  Asr Jamaat starting in  02:45              │
└─────────────────────────────────────────────────┘
```

**Prohibited Time Banner** — shown during makrooh windows:
```
┌─────────────────────────────────────────────────┐
│  ⚠  Nafl Salah not permitted until 05:45        │
└─────────────────────────────────────────────────┘
```

**Friday (Jumu'ah)** — Dhuhr row automatically shows "Jumu'ah" label on Fridays.

---

## 12. Admin Panel

Accessible by swiping or tapping a hidden area on the display screen header (triggers orientation change to portrait + shows login).

### Login
- Firebase email/password authentication
- Errors shown inline

### Dashboard (tabs)

| Tab | Screen | What you can do |
|-----|--------|-----------------|
| Jamaat | JamaatScreen | Set jamaat times for each prayer, add notes |
| Hadith | HadithScreen | Add/edit/delete hadiths, toggle active, set rotation interval |
| Notices | NoticeScreen | Add/edit/delete notices, set priority, toggle active |
| Settings | SettingsScreen | Mosque name/address, lat/long, prayer method, madhab, language, Hijri offset, upload APK for OTA |

All saves go to Firestore and propagate to the display screen within seconds.

---

## 13. Auto-Update System

The app supports **over-the-air (OTA) APK updates** without the Play Store.

### How it works

1. Admin uploads a new APK URL + version code to Firestore via `SettingsScreen`
2. `FirestoreRepository` listens to `data/app_version` document
3. `MainActivity` checks: if Firestore `versionCode > BuildConfig.VERSION_CODE`:
   - Shows an update banner on the display screen
   - User (or automatic) triggers download
4. Download uses `DownloadManager` (Android system service)
5. A `BroadcastReceiver` listens for `ACTION_DOWNLOAD_COMPLETE`
6. On completion: installs APK via `Intent.ACTION_VIEW` + `FileProvider` URI
   - FileProvider provides safe URI for the APK file
   - `REQUEST_INSTALL_PACKAGES` permission allows silent-ish install prompt

### APK storage
APK is saved to `getExternalFilesDir(null)/update.apk` (app-specific external storage, no special permissions needed).

---

## 14. Localization (Bengali / English)

Language is set per mosque in `MosqueConfig.language` (`"en"` or `"bn"`).

### Bengali Support (`BanglaUtils.kt`)

| Function | Description |
|----------|-------------|
| `toBangla(String)` | Converts `0-9` digits to `০-৯` |
| `to12Hour(String)` | Converts `"14:30"` → `"2:30"` |
| `addMinutes(time, minutes)` | Time arithmetic with midnight wrapping |
| `gregorianMonths` | Bengali month names array |
| `weekDays` | Bengali day names array |

All UI strings (prayer names, month names, day names) have Bengali equivalents defined in the data models and utility files.

---

## 15. Theme & Colors

### Display Theme (Dark, Islamic)

| Token | Hex | Usage |
|-------|-----|-------|
| Primary (Gold) | `#D4AF37` | Headers, borders, accents |
| Background | `#050D1A` | Screen background |
| Card | `#0D1B2A` | Prayer table, hadith card |
| Accent Green | `#1A6B3A` | Next prayer highlight |
| Prayer Yellow | `#FFD700` | Current prayer highlight |
| Prohibited Red | `#FF7043` | Makrooh time warning |
| Jamaat Cyan | `#4FC3F7` | Jamaat time cells |

### Admin Theme

| Token | Hex | Usage |
|-------|-----|-------|
| Primary (Green) | `#1B8B5A` | Buttons, active states |
| Secondary (Gold) | `#D4AF37` | Accents |
| Background | `#0F1923` | Admin panel background |

---

## 16. Permissions & Security

### Android Permissions

| Permission | Why |
|-----------|-----|
| `INTERNET` | Firestore sync |
| `ACCESS_NETWORK_STATE` | Offline/online badge |
| `REQUEST_INSTALL_PACKAGES` | OTA APK installs |

### Window Flags

- `FLAG_KEEP_SCREEN_ON` — Display never sleeps
- Fullscreen (no status bar, no navigation bar)
- System bars swipe-to-reveal (for admin access)

### Screen Orientation

| Mode | Orientation |
|------|------------|
| Display screen | Landscape |
| Admin panel | Portrait |

Orientation is changed programmatically in `MainActivity` when switching modes.

### Firebase Auth

Admin panel is protected by Firebase Authentication (email/password). Firestore security rules (server-side, in the Firebase console) should be set to allow reads by all (for display) and writes only by authenticated users.

---

## 17. Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Jetpack Compose BOM | 2024.12.01 | UI framework |
| Material 3 | (via BOM) | Design components |
| Room | 2.8.4 | Local SQLite cache |
| Firebase BOM | 33.7.0 | Firestore + Auth |
| Adhan | 1.2.1 | Prayer time calculation |
| DataStore Preferences | 1.1.1 | Key-value storage |
| WorkManager | 2.10.0 | Background tasks |
| Coroutines | 1.9.0 | Async programming |
| Lifecycle ViewModel | 2.8.7 | MVVM state management |
| KSP | 2.3.8 | Code generation for Room |

---

## Quick Reference: Adding New Data Types

To add a new type of mosque content (e.g., "Events"):

1. **Add model** in `MosqueModels.kt`
2. **Add entity** in `data/local/entity/Entities.kt`
3. **Add DAO** in `data/local/dao/Daos.kt`
4. **Register entity** in `AppDatabase.kt`, bump version
5. **Add Firestore listener** in `FirestoreRepository.kt`
6. **Add write method** in `AdminRepository.kt`
7. **Expose Flow** in `DisplayViewModel.kt`
8. **Add admin UI** screen under `admin/ui/screen/`
9. **Add display UI** component under `ui/component/`
