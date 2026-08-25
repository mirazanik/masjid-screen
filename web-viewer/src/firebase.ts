import { initializeApp, type FirebaseApp } from "firebase/app";
import { getFirestore, doc, getDoc, onSnapshot, type Firestore, type Unsubscribe } from "firebase/firestore";
import type { PublicShareSnapshot } from "./types";

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY as string,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN as string,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID as string,
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET as string,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID as string,
  appId: import.meta.env.VITE_FIREBASE_APP_ID as string,
};

const mosqueId = (import.meta.env.VITE_MOSQUE_ID as string) || "default";

let app: FirebaseApp | null = null;
let db: Firestore | null = null;

function getDb(): Firestore {
  if (!db) {
    app = initializeApp(firebaseConfig);
    db = getFirestore(app);
  }
  return db;
}

function shareDocRef(token: string) {
  return doc(getDb(), "mosques", mosqueId, "publicShares", token);
}

function mapSnapshot(data: Record<string, unknown>): PublicShareSnapshot {
  const config = (data.config ?? {}) as Record<string, unknown>;
  const jamaat = (data.jamaat ?? {}) as Record<string, unknown>;
  const hadiths = Array.isArray(data.hadiths) ? data.hadiths : [];
  const notices = Array.isArray(data.notices) ? data.notices : [];

  return {
    enabled: data.enabled === true,
    screenId: String(data.screenId ?? ""),
    screenName: String(data.screenName ?? ""),
    updatedAt: Number(data.updatedAt ?? 0),
    config: {
      name: String(config.name ?? "Masjid"),
      address: String(config.address ?? ""),
      latitude: Number(config.latitude ?? 23.777176),
      longitude: Number(config.longitude ?? 90.399452),
      calculationMethod: String(config.calculationMethod ?? "MWL"),
      madhab: String(config.madhab ?? "HANAFI"),
      language: String(config.language ?? "en"),
      hadithInterval: Number(config.hadithInterval ?? 30),
      jamaatCountdownMins: Number(config.jamaatCountdownMins ?? 3),
      hijriDateOffset: Number(config.hijriDateOffset ?? 0),
      tableFontScale: Number(config.tableFontScale ?? 1),
      activeTheme: String(config.activeTheme ?? "night_navy"),
    },
    jamaat: {
      fajr: String(jamaat.fajr ?? "05:45"),
      dhuhr: String(jamaat.dhuhr ?? "13:15"),
      asr: String(jamaat.asr ?? "17:30"),
      maghrib: String(jamaat.maghrib ?? "auto"),
      isha: String(jamaat.isha ?? "21:45"),
      fajrNote: String(jamaat.fajrNote ?? ""),
      dhuhrNote: String(jamaat.dhuhrNote ?? ""),
      asrNote: String(jamaat.asrNote ?? ""),
      maghribNote: String(jamaat.maghribNote ?? ""),
      ishaNote: String(jamaat.ishaNote ?? ""),
    },
    hadiths: hadiths.map((h) => {
      const row = h as Record<string, unknown>;
      return {
        id: String(row.id ?? ""),
        translation: String(row.translation ?? ""),
        source: String(row.source ?? ""),
        narrator: String(row.narrator ?? ""),
        active: row.active !== false,
      };
    }),
    notices: notices
      .map((n) => {
        const row = n as Record<string, unknown>;
        return {
          id: String(row.id ?? ""),
          text: String(row.text ?? ""),
          active: row.active !== false,
          priority: Number(row.priority ?? 0),
        };
      })
      .sort((a, b) => a.priority - b.priority),
  };
}

/** One-shot read — preferred for short visits (1 read). */
export async function fetchShare(token: string): Promise<PublicShareSnapshot | null> {
  const snap = await getDoc(shareDocRef(token));
  if (!snap.exists()) return null;
  const mapped = mapSnapshot(snap.data() as Record<string, unknown>);
  return mapped.enabled ? mapped : null;
}

/**
 * Listen to a single publicShares/{token} doc only.
 * Never attach listeners to screens/{id} (heartbeat would bill every viewer).
 */
export function listenShare(
  token: string,
  onData: (share: PublicShareSnapshot | null) => void,
  onError?: (err: Error) => void
): Unsubscribe {
  return onSnapshot(
    shareDocRef(token),
    (snap) => {
      if (!snap.exists()) {
        onData(null);
        return;
      }
      const mapped = mapSnapshot(snap.data() as Record<string, unknown>);
      onData(mapped.enabled ? mapped : null);
    },
    (err) => onError?.(err)
  );
}

const CACHE_KEY = "masjidscreen.share.cache";

export function cacheShare(token: string, share: PublicShareSnapshot): void {
  try {
    localStorage.setItem(CACHE_KEY, JSON.stringify({ token, share, savedAt: Date.now() }));
  } catch {
    /* ignore quota */
  }
}

export function readCachedShare(token: string): PublicShareSnapshot | null {
  try {
    const raw = localStorage.getItem(CACHE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as { token: string; share: PublicShareSnapshot };
    if (parsed.token !== token) return null;
    return parsed.share?.enabled ? parsed.share : null;
  } catch {
    return null;
  }
}
