export interface ShareConfig {
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  calculationMethod: string;
  madhab: string;
  language: string;
  hadithInterval: number;
  jamaatCountdownMins: number;
  hijriDateOffset: number;
  tableFontScale: number;
  activeTheme: string;
}

export interface ShareJamaat {
  fajr: string;
  dhuhr: string;
  asr: string;
  maghrib: string;
  isha: string;
  fajrNote: string;
  dhuhrNote: string;
  asrNote: string;
  maghribNote: string;
  ishaNote: string;
}

export interface ShareHadith {
  id: string;
  translation: string;
  source: string;
  narrator: string;
  active: boolean;
}

export interface ShareNotice {
  id: string;
  text: string;
  active: boolean;
  priority: number;
}

export interface PublicShareSnapshot {
  enabled: boolean;
  screenId: string;
  screenName: string;
  updatedAt: number;
  config: ShareConfig;
  jamaat: ShareJamaat;
  hadiths: ShareHadith[];
  notices: ShareNotice[];
}

export interface PrayerTimesStrings {
  fajr: string;
  sunrise: string;
  dhuhr: string;
  asr: string;
  maghrib: string;
  isha: string;
}
