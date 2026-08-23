# MasjidScreen — Setup Guide

## Git: production vs development

Treat branches as environments so the mosque TV never runs unfinished work.

| Branch | Purpose |
|--------|---------|
| `main` | Production only. The APK on the TV is built from this branch. |
| `miraz-dev` | Daily development. Feature branches start from here. |

Workflow:

1. Keep the mosque TV on a **prod** release APK built from `main`. Do not uninstall it.
2. Do all new work on `miraz-dev` (or `feature/...` branched from it).
3. When a feature is ready: merge into `main`, bump `versionCode` / `versionName` in `app/build.gradle.kts`, then ship **prodRelease** from Play Console (`.aab`) or sideload an APK.

Never publish a development build to production devices. Play Store updates replace the old OTA APK installer; do not re-enable self-update in the Play build.

### Before you make GitHub public

These stay on your PC only (gitignored): signing keystores (`.jks` / `.keystore`), `google-services.json`, and `local.properties`.

**Old commits still contain those files.** Switching the existing repo to Public exposes the whole history. To hide them for real:

1. Commit the current tree (secrets untracked).
2. Create a **new empty GitHub repo**, copy this project in, and push **without** the old history — **or** rewrite history (`git filter-repo`) on a backup clone.
3. Then turn the public repo on and enable GitHub Pages from `main` / `docs`.

Do not push `local.properties`. It has your SDK path and signing passwords.

---

## Project Structure

```
MasjidScreen/
├── app/                          ← TV display + in-app admin (single APK)
│   ├── src/prod/google-services.json          ← local only (gitignored)
│   ├── src/prod/google-services.json.example
│   ├── src/dev/google-services.json           ← local only (gitignored)
│   └── src/dev/google-services.json.example
├── local.properties.example      ← copy to local.properties on your PC
├── firestore.rules
├── PLAY_STORE.md
└── SETUP.md
```

Product flavors:

| Flavor | Package | Firebase | When to use |
|--------|---------|----------|-------------|
| `prod` | `com.mirazanik.masjidscreen` | `mosque-live-screen` | Mosque TV / live admin |
| `dev` | `com.mirazanik.masjidscreen.dev` | `mosque-live-screen-dev` | Local work; can sit next to prod on the same device |

---

## 1. Firebase Setup (Required)

You need **two** Firebase projects. Prod and dev must never share a database.

### Production — `mosque-live-screen` (live TV)

This project already exists. Keep the Android app registered as:

- Package name: `com.mirazanik.masjidscreen`
- `google-services.json` → `app/src/prod/google-services.json` (gitignored; see `app/src/prod/google-services.json.example`)

### Development — `mosque-live-screen-dev` (required for dev builds)

Create this in the [Firebase Console](https://console.firebase.google.com) before running dev variants. Until `app/src/dev/google-services.json` exists, Gradle disables dev variants; prod builds still work.

1. Create a new project named **mosque-live-screen-dev** (Google Analytics optional).
2. Add an Android app:
   - Package name: **`com.mirazanik.masjidscreen.dev`**
   - App nickname: MasjidScreen DEV
   - SHA-1: print it from your local keystore (see below). Do not publish the keystore or its password.
3. Download `google-services.json` and save it as **`app/src/dev/google-services.json`** (not the `.example` file). This file is gitignored. The package inside the JSON must be `com.mirazanik.masjidscreen.dev`. See `app/src/dev/google-services.json.example` for the expected shape.
4. Enable **Firestore** (production mode) and paste the rules from [`firestore.rules`](firestore.rules).
5. Enable **Authentication**: Email/Password and Google. For Google Sign-In, the OAuth client is created when you add the SHA-1 above; download a fresh `google-services.json` after that and replace `app/src/dev/google-services.json`.
6. Create a test admin account in the **dev** Auth project. Do not reuse live mosque passwords if you can avoid it.
7. Open the **dev** app once, sign in, and seed a test mosque. Do not copy live prayer times unless you explicitly export a snapshot.

Leave `GOOGLE_WEB_CLIENT_ID` unset in `gradle.properties` / `local.properties` so each flavor uses `default_web_client_id` from its own `google-services.json`.

### Local signing key (not in git)

Keep `app/masjidscreen.keystore` (and any `.jks`) on your PC only. Copy `local.properties.example` to `local.properties` and set `KEYSTORE_PASSWORD` / `KEY_ALIAS` / `KEY_PASSWORD`.

Print fingerprints anytime with:

```bash
keytool -list -v -keystore app/masjidscreen.keystore -alias androiddebugkey
```

Never commit the keystore, `google-services.json`, or signing passwords.

---

## 2. Firestore Data Structure

The apps use this structure (auto-created when admin saves first time):

```
mosques/
  default/
    data/
      config        ← Masjid name, location, calc method
      jamaat_times  ← Congregation times
      hadith        ← Daily hadith
    notices/        ← Collection of notice documents
```

---

## 3. Build & Deploy

Daily work — **dev** app (does not replace the live TV app):

```bash
./gradlew :app:installDevDebug
```

In Android Studio, select the **devDebug** run configuration.

Ship to the mosque TV — prod only, from `main`:

```bash
./gradlew :app:bundleProdRelease
```

- Play Store upload: `app/build/outputs/bundle/prodRelease/app-prod-release.aab` (required format; do not upload an APK).
- Sideload APK if a device is not on Play: `./gradlew :app:assembleProdRelease` → `app/build/outputs/apk/prod/release/`
- App auto-starts in immersive full-screen mode; the screen stays on.

Windows: use `gradlew.bat` instead of `./gradlew`.

Privacy policy URL for Play Console (enable GitHub Pages on this repo, **docs** folder):

`https://mirazanik.github.io/masjid-screen/privacy-policy.html`

Full Play Store + GitHub Pages steps: [`PLAY_STORE.md`](PLAY_STORE.md).

After the first Play upload, copy **App signing** SHA-1 / SHA-256 from Play Console into Firebase Android app `com.mirazanik.masjidscreen`. Google Sign-In fails on Play installs until those fingerprints are added.

---

## 4. Prayer Time Calculation Methods

Set in Admin App → Settings → Calculation Method:

| Method | Used By |
|--------|---------|
| MWL | Muslim World League (default) |
| ISNA | North America |
| EGYPT | Egypt |
| MAKKAH | Saudi Arabia (Umm al-Qura) |
| KARACHI | Pakistan / Bangladesh |
| MOON_SIGHTING | UK Moon Sighting Committee |
| DUBAI | UAE |

---

## 5. Offline Behavior

- TV app caches all data in local Room database
- If Firebase is unreachable, last cached data is displayed
- Prayer times are **always** calculated locally from device location/settings
- Clock, dates, and prayer calculations work fully offline

---

## 6. TV App Display Layout

```
┌────────────────────────────────────────────────────────┐
│  ١٤ رَمَضَان ١٤٤٦هـ    MASJID NAME      Monday, 21 May 2025 │
├────────────────────────────────────────────────────────┤
│  ┌──────────────┐  │  Prayer   │ Adhan │ Jamaat       │
│  │   17:34:22   │  ├───────────┼───────┼──────────    │
│  │   5:34 PM    │  │ Fajr الفجر│ 04:52 │  05:15       │
│  │              │  │ Sunrise   │ 06:18 │   —           │
│  │  Next Prayer │  │ Dhuhr     │ 13:02 │  13:15       │
│  │   Maghrib    │  │ Asr       │ 17:12 │  17:30  ◄NEXT│
│  │   in 23m     │  │ Maghrib   │ 20:44 │  20:44       │
│  └──────────────┘  │ Isha      │ 22:10 │  22:15       │
├────────────────────────────────────────────────────────┤
│  ▌ Hadith of the Day                                   │
│    "The best of you is the one who learns the Quran…"  │
│    — Sahih al-Bukhari • Uthman ibn Affan RA            │
├────────────────────────────────────────────────────────┤
│ NOTICE  Important announcement scrolling here...  ➜   │
└────────────────────────────────────────────────────────┘
```
