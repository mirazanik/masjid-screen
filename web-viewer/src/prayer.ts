import {
  Coordinates,
  CalculationMethod,
  Madhab,
  PrayerTimes,
} from "adhan";
import type { PrayerTimesStrings, ShareJamaat } from "./types";

const BD_TZ = "Asia/Dhaka";

const METHOD_MAP: Record<string, () => ReturnType<(typeof CalculationMethod)["MuslimWorldLeague"]>> = {
  MWL: () => CalculationMethod.MuslimWorldLeague(),
  MuslimWorldLeague: () => CalculationMethod.MuslimWorldLeague(),
  ISNA: () => CalculationMethod.NorthAmerica(),
  NorthAmerica: () => CalculationMethod.NorthAmerica(),
  Egypt: () => CalculationMethod.Egyptian(),
  Egyptian: () => CalculationMethod.Egyptian(),
  Makkah: () => CalculationMethod.UmmAlQura(),
  UmmAlQura: () => CalculationMethod.UmmAlQura(),
  Karachi: () => CalculationMethod.Karachi(),
  Tehran: () => CalculationMethod.Tehran(),
  Jafari: () => CalculationMethod.Tehran(),
  Dubai: () => CalculationMethod.Dubai(),
  Kuwait: () => CalculationMethod.Kuwait(),
  Qatar: () => CalculationMethod.Qatar(),
  Singapore: () => CalculationMethod.Singapore(),
  Turkey: () => CalculationMethod.Turkey(),
  MoonsightingCommittee: () => CalculationMethod.MoonsightingCommittee(),
};

function resolveParams(methodName: string, madhabName: string) {
  const factory = METHOD_MAP[methodName] ?? METHOD_MAP.MWL;
  const params = factory();
  params.madhab =
    madhabName.toUpperCase() === "SHAFI" || madhabName.toUpperCase() === "SHAFI_HANAFI_FALSE"
      ? Madhab.Shafi
      : Madhab.Hanafi;
  return params;
}

function formatHm(date: Date): string {
  return new Intl.DateTimeFormat("en-GB", {
    timeZone: BD_TZ,
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  }).format(date);
}

export function nowInBangladesh(): Date {
  // Wall-clock in Asia/Dhaka for display; Date itself stays UTC-based.
  return new Date(
    new Date().toLocaleString("en-US", { timeZone: BD_TZ })
  );
}

/** Current HH:mm:ss parts in Bangladesh time from a real Date. */
export function bdClockParts(from: Date = new Date()): {
  hours: number;
  minutes: number;
  seconds: number;
  date: Date;
} {
  const parts = new Intl.DateTimeFormat("en-GB", {
    timeZone: BD_TZ,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  }).formatToParts(from);
  const get = (type: string) =>
    Number(parts.find((p) => p.type === type)?.value ?? 0);
  const year = get("year");
  const month = get("month");
  const day = get("day");
  return {
    hours: get("hour"),
    minutes: get("minute"),
    seconds: get("second"),
    date: new Date(year, month - 1, day, get("hour"), get("minute"), get("second")),
  };
}

export function calculatePrayerTimes(
  latitude: number,
  longitude: number,
  methodName: string,
  madhabName: string,
  forDate: Date = bdClockParts().date
): PrayerTimesStrings {
  try {
    const coords = new Coordinates(latitude, longitude);
    const params = resolveParams(methodName, madhabName);
    const times = new PrayerTimes(coords, forDate, params);
    return {
      fajr: formatHm(times.fajr),
      sunrise: formatHm(times.sunrise),
      dhuhr: formatHm(times.dhuhr),
      asr: formatHm(times.asr),
      maghrib: formatHm(times.maghrib),
      isha: formatHm(times.isha),
    };
  } catch {
    return {
      fajr: "--:--",
      sunrise: "--:--",
      dhuhr: "--:--",
      asr: "--:--",
      maghrib: "--:--",
      isha: "--:--",
    };
  }
}

export function parseTimeToMins(value: string): number {
  const m = /^(\d{1,2}):(\d{2})$/.exec(value.trim());
  if (!m) return -1;
  return Number(m[1]) * 60 + Number(m[2]);
}

export function resolveJamaatTimes(
  prayer: PrayerTimesStrings,
  jamaat: ShareJamaat
): ShareJamaat {
  return {
    ...jamaat,
    maghrib:
      jamaat.maghrib.trim().toLowerCase() === "auto" || !jamaat.maghrib.trim()
        ? prayer.maghrib
        : jamaat.maghrib,
  };
}

export function getCurrentPrayer(
  prayerTimes: PrayerTimesStrings,
  nowMins: number
): { name: string; minsLeft: number } {
  const fajr = parseTimeToMins(prayerTimes.fajr);
  const sunrise = parseTimeToMins(prayerTimes.sunrise);
  const dhuhr = parseTimeToMins(prayerTimes.dhuhr);
  const asr = parseTimeToMins(prayerTimes.asr);
  const maghrib = parseTimeToMins(prayerTimes.maghrib);
  const isha = parseTimeToMins(prayerTimes.isha);
  if (nowMins < fajr) return { name: "Isha", minsLeft: fajr - nowMins };
  if (nowMins < sunrise) return { name: "Fajr", minsLeft: sunrise - nowMins };
  if (nowMins < dhuhr) return { name: "", minsLeft: dhuhr - nowMins };
  if (nowMins < asr) return { name: "Dhuhr", minsLeft: asr - nowMins };
  if (nowMins < maghrib) return { name: "Asr", minsLeft: maghrib - nowMins };
  if (nowMins < isha) return { name: "Maghrib", minsLeft: isha - nowMins };
  return { name: "Isha", minsLeft: fajr + 1440 - nowMins };
}

export function getNextJamaat(
  prayerTimes: PrayerTimesStrings,
  jamaat: ShareJamaat,
  nowMins: number
): { name: string; minsLeft: number } {
  const resolved = resolveJamaatTimes(prayerTimes, jamaat);
  const schedule: Array<[string, number]> = [
    ["Fajr", parseTimeToMins(resolved.fajr)],
    ["Dhuhr", parseTimeToMins(resolved.dhuhr)],
    ["Asr", parseTimeToMins(resolved.asr)],
    ["Maghrib", parseTimeToMins(resolved.maghrib)],
    ["Isha", parseTimeToMins(resolved.isha)],
  ];
  const next = schedule.find(([, mins]) => mins > nowMins);
  if (next) return { name: next[0], minsLeft: next[1] - nowMins };
  const fajr = schedule[0][1];
  return { name: "Fajr", minsLeft: fajr + 1440 - nowMins };
}

const HIJRI_MONTHS_EN = [
  "Muharram",
  "Safar",
  "Rabi' al-Awwal",
  "Rabi' al-Thani",
  "Jumada al-Awwal",
  "Jumada al-Thani",
  "Rajab",
  "Sha'ban",
  "Ramadan",
  "Shawwal",
  "Dhu al-Qi'dah",
  "Dhu al-Hijjah",
];

export function formatGregorian(date: Date, language: string): string {
  const locale = language === "bn" ? "bn-BD" : "en-GB";
  return new Intl.DateTimeFormat(locale, {
    timeZone: BD_TZ,
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric",
  }).format(date);
}

export function formatHijri(date: Date, offsetDays: number, language: string): string {
  const shifted = new Date(date.getTime() + offsetDays * 86_400_000);
  try {
    const fmt = new Intl.DateTimeFormat(language === "bn" ? "bn-BD-u-ca-islamic" : "en-GB-u-ca-islamic", {
      timeZone: BD_TZ,
      day: "numeric",
      month: "long",
      year: "numeric",
    });
    return fmt.format(shifted);
  } catch {
    const approx = new Intl.DateTimeFormat("en-TN-u-ca-islamic", {
      day: "numeric",
      month: "numeric",
      year: "numeric",
    }).formatToParts(shifted);
    const day = approx.find((p) => p.type === "day")?.value ?? "";
    const monthIdx = Number(approx.find((p) => p.type === "month")?.value ?? "1") - 1;
    const year = approx.find((p) => p.type === "year")?.value ?? "";
    return `${day} ${HIJRI_MONTHS_EN[monthIdx] ?? ""} ${year}`;
  }
}

export function formatCountdown(totalMins: number): string {
  const h = Math.floor(totalMins / 60);
  const m = totalMins % 60;
  if (h > 0) return `${h}h ${m}m`;
  return `${m}m`;
}

export function pad2(n: number): string {
  return n.toString().padStart(2, "0");
}
