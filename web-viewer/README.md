# MasjidScreen Public Viewer PWA

Phone/browser viewer for a mosque screen. The site is hosted on **Cloudflare Pages** (free). **Firebase is only used for Firestore** (real-time content sync)—not for hosting.

- **Home** (`https://mosque.mirazanik.com/`) — Play Store download, how to use, and documentation.
- **Share links** (`/s/<token>`) — live public display. Admins enable **Public view** in the Android app, then share the QR code or link.

---

## Share URL format

```
https://mosque.mirazanik.com/s/<shareToken>
```

- `<shareToken>` is an unguessable value (not `screenId` or pairing code).
- The PWA reads **only** `mosques/default/publicShares/{token}` — never live `screens/{id}` docs (those get heartbeat writes every 15s).

---

## Admin: how to get the QR code or link

On the **same Android app** used to manage the mosque (phone or tablet):

1. Open **Admin** and sign in.
2. Go to **Screens** (bottom tab or menu).
3. Tap **Manage** (gear icon) on the screen you want to share (e.g. Main Hall).
4. Open the **Share** tab (last tab, QR icon).
5. Turn **Enable public view** **ON**.
6. After a moment you will see:
   - A **QR code**
   - The full **link** under the QR
   - **Copy link** — copies the URL to the clipboard (paste in WhatsApp, SMS, email, etc.)
   - **Share link** — opens your phone’s share sheet (WhatsApp, Messenger, Gmail, …)
   - **Download QR** — saves a print-ready PNG to **Pictures/MasjidScreen** (open it in Gallery to print or share)
   - **New QR** — invalidates the old link and creates a new one (use if the link was leaked)
   - **Revoke link permanently** — turns off public view and deletes the snapshot

### Ways to share with worshippers

| Method | What to do |
|--------|------------|
| **Show QR on your phone** | Open the Share tab; let people scan the QR with their camera |
| **Copy / Share link** | Tap **Copy link** or **Share link**; send to a group or post on social media |
| **Save QR as an image** | Tap **Download QR**; the PNG is saved to Pictures/MasjidScreen |
| **Print for the mosque** | Download the QR, open it in Gallery, then print it for a notice board |

### Turn off public access

- Toggle **Enable public view** **OFF**, or tap **Revoke link permanently**.

---

## Cost model

| Event | Firestore impact |
|-------|------------------|
| Viewer opens link | 1 listener on one snapshot doc |
| Admin saves content / toggles share | 1 write to that snapshot |
| Clock / prayer countdown | Local (Adhan JS) — **no** Firebase traffic |
| TV heartbeat | Unrelated; viewers do not subscribe |

Cloudflare Pages free tier serves the static PWA; Firebase billing is only Firestore reads/writes for snapshots.

---

## Cloudflare Pages / Workers build (new dashboard)

The new **Workers & Pages** UI often has **no** “Build output directory” field. Use this:

1. Keep [`wrangler.toml`](wrangler.toml) in `web-viewer/` with `[assets] directory = "./dist"` (already in this repo).
2. **Root directory** = `web-viewer` — **not** `/web-viewer` (no leading slash).
3. **Build command** = `npm run build`
4. **Deploy command** = `npx wrangler deploy` (default; needs `[assets]` in wrangler.toml)
5. Put all `VITE_*` keys under **Build** variables (not only Runtime). Vite embeds them at build time.
6. Optional Build variable: `NODE_VERSION` = `20`
7. Do **not** use `public/_redirects` with Workers assets — SPA routes are handled by `not_found_handling = "single-page-application"` in `wrangler.toml`.
8. Commit + push, then **Retry deployment**.

If the build succeeds but deploy fails with “Missing entry-point… or assets directory”, your `wrangler.toml` is missing `[assets]` or was not pushed yet.  
If deploy fails with “Invalid _redirects… Infinite loop”, remove `_redirects` and rely on `wrangler.toml` SPA handling.

---

## Local development

```bash
cd web-viewer
npm install
npm run dev
```

Open `http://localhost:5173/s/<token>` with a token from a screen that has public share enabled.

---

## Deploy to Cloudflare Pages (step by step)

### One-time setup

#### 1. Firebase (data only)

1. Open [Firebase Console](https://console.firebase.google.com/) → project **mosque-live-screen** (prod).
2. Ensure **Firestore** is enabled.
3. Deploy security rules from this repo (once, or after rule changes):

   ```bash
   firebase deploy --only firestore:rules --project prod
   ```

4. Optional: add a **Web** app in Project settings → Your apps (for a proper `VITE_FIREBASE_APP_ID`). Firestore works with the API key from `google-services.json` if you use the values in `.env.production`.

#### 2. Push code to GitHub

Cloudflare Pages deploys from Git. Ensure your repo (with the `web-viewer/` folder) is on GitHub.

#### 3. Create Cloudflare Pages project

1. Log in to [Cloudflare Dashboard](https://dash.cloudflare.com/) → **Workers & Pages** → **Create** → **Pages** → **Connect to Git**.
2. Select your **masjid-screen-live** repository.
3. **Build settings**:

   | Field | Value |
   |--------|--------|
   | Production branch | `main` (or your default branch) |
   | Root directory | `web-viewer` (no leading `/`) |
   | Build command | `npm run build` |
   | Build output | set by [`wrangler.toml`](wrangler.toml) → `pages_build_output_dir = "dist"` |

   New Cloudflare UI may not show “Build output directory”. The `wrangler.toml` in this folder tells Cloudflare to use `dist`.

   Put `VITE_*` under **Build variables** (not only Runtime). Vite embeds them at build time.

4. **Environment variables** (Production) — add each name exactly:

   | Variable | Example / source |
   |----------|------------------|
   | `VITE_FIREBASE_API_KEY` | From `app/src/prod/google-services.json` → `api_key.current_key` |
   | `VITE_FIREBASE_AUTH_DOMAIN` | `mosque-live-screen.firebaseapp.com` |
   | `VITE_FIREBASE_PROJECT_ID` | `mosque-live-screen` |
   | `VITE_FIREBASE_STORAGE_BUCKET` | `mosque-live-screen.firebasestorage.app` |
   | `VITE_FIREBASE_MESSAGING_SENDER_ID` | Project number from Firebase |
   | `VITE_FIREBASE_APP_ID` | Web app ID (or placeholder from `.env.production`) |
   | `VITE_MOSQUE_ID` | `default` |

5. Click **Save and Deploy**. Wait for the first build to finish.

SPA routing (`/s/:token`) is handled by `not_found_handling = "single-page-application"` in [`wrangler.toml`](wrangler.toml). Do not add a `public/_redirects` file for Workers assets (it causes an infinite-loop deploy error).

#### 4. Custom domain (mosque.mirazanik.com)

1. In Pages → your project → **Custom domains** → **Set up a custom domain**.
2. Enter `mosque.mirazanik.com`.
3. Cloudflare will show DNS records. If the domain is already on Cloudflare, confirm/add the CNAME as instructed.
4. Wait for SSL (usually a few minutes).

#### 5. Match the Android app URL

In [`app/build.gradle.kts`](../app/build.gradle.kts), both flavors should use your live URL:

```kotlin
buildConfigField("String", "PUBLIC_VIEWER_BASE_URL", "\"https://mosque.mirazanik.com\"")
```

Rebuild and install the app so QR codes and links use this domain.

---

### Every update to the web viewer

1. Commit and push changes under `web-viewer/`.
2. Cloudflare Pages **auto-rebuilds** on push (or trigger **Retry deployment** in the dashboard).

Manual build (optional):

```bash
cd web-viewer
npm install
npm run build
```

Then upload `dist/` via **Direct Upload** in Pages if you are not using Git deploy.

---

## Firebase env files (local builds)

- [`.env.production`](.env.production) — prod Firestore project
- [`.env.development`](.env.development) — dev Firestore project

These are used by `npm run build` on your PC. On Cloudflare, set the same keys as **Environment variables** in the dashboard.

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `/s/abc123` shows 404 | Confirm `wrangler.toml` has `not_found_handling = "single-page-application"` and redeploy |
| “Unavailable” on phone | Enable public view in admin; check Firestore rules deployed |
| Wrong mosque data | Confirm `VITE_MOSQUE_ID=default` and prod Firebase project |
| QR opens wrong site | Rebuild Android app after changing `PUBLIC_VIEWER_BASE_URL` |
