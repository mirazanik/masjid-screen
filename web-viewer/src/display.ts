import type { PublicShareSnapshot, PrayerTimesStrings } from "./types";
import {
  addMinutes,
  bdClockParts,
  calculatePrayerTimes,
  formatClock,
  formatCountdown,
  formatDisplayTime,
  formatGregorian,
  formatHijri,
  formatMmSs,
  getCurrentPrayer,
  getJamaatCountdown,
  getMakruhInfo,
  getNextJamaat,
  isFridayBd,
  minsToHm,
  resolveJamaatTimes,
} from "./prayer";

const PRAYER_BN: Record<string, string> = {
  Fajr: "ফজর",
  Dhuhr: "জোহর",
  Asr: "আসর",
  Maghrib: "মাগরিব",
  Isha: "ইশা",
  Sunrise: "সূর্যোদয়",
};

const JAMAAT_BN: Record<string, string> = {
  Fajr: "ফজরের",
  Dhuhr: "জোহরের",
  Asr: "আসরের",
  Maghrib: "মাগরিবের",
  Isha: "ইশার",
};

const MAKRUH = {
  sunrise: { en: "Makrooh (Sunrise)", bn: "মাকরূহ (সূর্যোদয়)" },
  zawal: { en: "Makrooh (Zawal)", bn: "মাকরূহ (যাওয়াল)" },
  sunset: {
    en: "Makrooh (Sunset)",
    bn: "মাকরূহ (সূর্যাস্ত)",
    noteEn: "Today's Asr prayer may still be performed",
    noteBn: "আজকের আসরের নামাজ আদায় করা যাবে",
  },
};

function escapeHtml(text: string): string {
  return text
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function prayerLabel(name: string, bangla: boolean, friday: boolean): string {
  if (friday && name === "Dhuhr") return bangla ? "জুম'আ" : "Jumu'ah";
  if (bangla) return PRAYER_BN[name] ?? name;
  return name;
}

function styledTime(hm: string, bangla: boolean): string {
  const display = formatDisplayTime(hm, bangla);
  const match = display.match(/^(.*)\s(AM|PM)$/i);
  if (!match) return escapeHtml(display);
  return `${escapeHtml(match[1])} <span class="ampm">${escapeHtml(match[2].toUpperCase())}</span>`;
}

export function renderShell(root: HTMLElement): void {
  root.innerHTML = `
    <div class="status" id="status" hidden></div>
    <header class="header">
      <div class="mosque-name" id="mosqueName">MasjidScreen</div>
      <div class="dates">
        <span id="gregorianDate"></span>
        <span class="sep">·</span>
        <span id="hijriDate"></span>
      </div>
    </header>
    <section class="clock-panel">
      <div class="clock" id="clock">--:--:--</div>
      <div class="status-stack" id="defaultStatus">
        <div class="waqt" id="waqtLine"></div>
        <div class="next-jamaat" id="nextJamaat"></div>
      </div>
      <div class="makruh-banner" id="makruhBanner" hidden>
        <div class="banner-title" id="makruhTitle"></div>
        <div class="banner-body" id="makruhBody"></div>
        <div class="banner-note" id="makruhNote" hidden></div>
        <div class="banner-ends" id="makruhEnds"></div>
      </div>
      <div class="jamaat-banner" id="jamaatBanner" hidden>
        <div class="banner-title" id="jamaatTitle"></div>
        <div class="jamaat-timer" id="jamaatTimer"></div>
        <div class="jamaat-mute" id="jamaatMute"></div>
      </div>
    </section>
    <section class="table-wrap">
      <table class="prayer-table" id="prayerTable">
        <thead>
          <tr>
            <th id="thPrayer">Prayer</th>
            <th id="thStart">Waqt Start</th>
            <th id="thEnd">Waqt End</th>
            <th id="thJamaat">Jamaat</th>
          </tr>
        </thead>
        <tbody id="prayerBody"></tbody>
      </table>
    </section>
    <section class="hadith-card" id="hadithCard" hidden>
      <div class="hadith-label" id="hadithLabel">Hadith</div>
      <p class="hadith-text" id="hadithText"></p>
      <p class="hadith-meta" id="hadithMeta"></p>
    </section>
    <footer class="ticker" id="ticker" hidden>
      <div class="ticker-track" id="tickerTrack"></div>
    </footer>
  `;
}

export function renderLoading(root: HTMLElement, message: string): void {
  root.innerHTML = `<div class="center-msg"><p>${escapeHtml(message)}</p></div>`;
}

export function renderError(root: HTMLElement, message: string): void {
  root.innerHTML = `<div class="center-msg error"><h1>Unavailable</h1><p>${escapeHtml(message)}</p></div>`;
}

export class DisplayController {
  private share: PublicShareSnapshot | null = null;
  private hadithIndex = 0;
  private hadithTimer: number | null = null;
  private clockTimer: number | null = null;
  private prayerTimes: PrayerTimesStrings | null = null;

  constructor(private readonly root: HTMLElement) {}

  setShare(share: PublicShareSnapshot): void {
    const hadithChanged =
      JSON.stringify(this.share?.hadiths) !== JSON.stringify(share.hadiths);
    this.share = share;
    const bn = share.config.language === "bn";
    document.documentElement.dataset.theme = share.config.activeTheme || "night_navy";
    document.documentElement.lang = bn ? "bn" : "en";
    document.title = `${share.config.name} · MasjidScreen`;
    if (!this.root.querySelector(".header")) {
      renderShell(this.root);
    }
    this.updateStatic();
    if (hadithChanged) {
      this.hadithIndex = 0;
      this.restartHadithRotation();
    }
    this.ensureClock();
    this.tick();
  }

  destroy(): void {
    if (this.clockTimer != null) window.clearInterval(this.clockTimer);
    if (this.hadithTimer != null) window.clearInterval(this.hadithTimer);
  }

  private ensureClock(): void {
    if (this.clockTimer != null) return;
    this.clockTimer = window.setInterval(() => this.tick(), 1000);
  }

  private restartHadithRotation(): void {
    if (this.hadithTimer != null) window.clearInterval(this.hadithTimer);
    const intervalSec = Math.max(5, this.share?.config.hadithInterval ?? 30);
    this.renderHadith();
    this.hadithTimer = window.setInterval(() => {
      const active = this.activeHadiths();
      if (active.length > 1) {
        this.hadithIndex = (this.hadithIndex + 1) % active.length;
        this.renderHadith();
      }
    }, intervalSec * 1000);
  }

  private activeHadiths() {
    return (this.share?.hadiths ?? []).filter((h) => h.active && h.translation.trim());
  }

  private activeNotices() {
    return (this.share?.notices ?? []).filter((n) => n.active && n.text.trim());
  }

  private updateStatic(): void {
    if (!this.share) return;
    const { config } = this.share;
    const bn = config.language === "bn";
    const nameEl = this.root.querySelector("#mosqueName");
    if (nameEl) nameEl.textContent = config.name || this.share.screenName || "Masjid";

    const setTh = (id: string, en: string, bnText: string) => {
      const el = this.root.querySelector(id);
      if (el) el.textContent = bn ? bnText : en;
    };
    setTh("#thPrayer", "Prayer", "নামাজ");
    setTh("#thStart", "Waqt Start", "ওয়াক্ত শুরু");
    setTh("#thEnd", "Waqt End", "ওয়াক্ত শেষ");
    setTh("#thJamaat", "Jamaat", "জামাত");

    const hadithLabel = this.root.querySelector("#hadithLabel");
    if (hadithLabel) hadithLabel.textContent = bn ? "হাদিস" : "Hadith";

    const table = this.root.querySelector<HTMLElement>("#prayerTable");
    if (table) {
      const scale = Math.max(0.8, config.tableFontScale || 1);
      table.style.fontSize = `${scale}em`;
    }

    this.prayerTimes = calculatePrayerTimes(
      config.latitude,
      config.longitude,
      config.calculationMethod,
      config.madhab
    );
    this.renderTable();
    this.renderTicker();
  }

  private renderTable(): void {
    if (!this.share || !this.prayerTimes) return;
    const body = this.root.querySelector("#prayerBody");
    if (!body) return;
    const jamaat = resolveJamaatTimes(this.prayerTimes, this.share.jamaat);
    const bn = this.share.config.language === "bn";
    const friday = isFridayBd();
    const { hours, minutes } = bdClockParts();
    const nowMins = hours * 60 + minutes;
    const current = getCurrentPrayer(this.prayerTimes, nowMins).name;
    const ishraq = addMinutes(this.prayerTimes.sunrise, 20);
    const zawal = addMinutes(this.prayerTimes.dhuhr, -5);
    const makrooh = addMinutes(this.prayerTimes.maghrib, -15);
    const labelIshraq = bn ? "ইশরাক" : "Ishraq";
    const labelZawal = bn ? "যাওয়াল" : "Zawal";
    const labelMakrooh = bn ? "মাকরূহ" : "Makrooh";

    const rows: Array<{
      name: string;
      start: string;
      end: string;
      jamaat: string;
      note: string;
      subStart?: { label: string; time: string };
      subEnd?: { label: string; time: string };
    }> = [
      {
        name: "Fajr",
        start: this.prayerTimes.fajr,
        end: this.prayerTimes.sunrise,
        jamaat: jamaat.fajr,
        note: this.share.jamaat.fajrNote,
        subEnd: { label: labelIshraq, time: ishraq },
      },
      {
        name: "Dhuhr",
        start: this.prayerTimes.dhuhr,
        end: this.prayerTimes.asr,
        jamaat: jamaat.dhuhr,
        note: this.share.jamaat.dhuhrNote,
        subStart: { label: labelZawal, time: zawal },
      },
      {
        name: "Asr",
        start: this.prayerTimes.asr,
        end: this.prayerTimes.maghrib,
        jamaat: jamaat.asr,
        note: this.share.jamaat.asrNote,
        subEnd: { label: labelMakrooh, time: makrooh },
      },
      {
        name: "Maghrib",
        start: this.prayerTimes.maghrib,
        end: this.prayerTimes.isha,
        jamaat: jamaat.maghrib,
        note: this.share.jamaat.maghribNote,
      },
      {
        name: "Isha",
        start: this.prayerTimes.isha,
        end: this.prayerTimes.fajr,
        jamaat: jamaat.isha,
        note: this.share.jamaat.ishaNote,
      },
    ];

    body.innerHTML = rows
      .map((row) => {
        const classes = [
          current && row.name === current ? "active" : "",
          friday && row.name === "Dhuhr" ? "jumuah" : "",
        ]
          .filter(Boolean)
          .join(" ");
        const classAttr = classes ? ` class="${classes}"` : "";
        const note = row.note.trim();
        const jamaatCell = `<div class="time-main">${styledTime(row.jamaat, bn)}</div>${
          note ? `<div class="jamaat-note">${escapeHtml(note)}</div>` : ""
        }`;
        return `<tr${classAttr}>
          <td>${escapeHtml(prayerLabel(row.name, bn, friday))}</td>
          <td>${timeCell(row.start, bn, row.subStart)}</td>
          <td>${timeCell(row.end, bn, row.subEnd)}</td>
          <td>${jamaatCell}</td>
        </tr>`;
      })
      .join("");
  }

  private renderHadith(): void {
    const card = this.root.querySelector("#hadithCard") as HTMLElement | null;
    const text = this.root.querySelector("#hadithText");
    const meta = this.root.querySelector("#hadithMeta");
    if (!card || !text || !meta) return;
    const active = this.activeHadiths();
    if (active.length === 0) {
      card.hidden = true;
      return;
    }
    card.hidden = false;
    const h = active[this.hadithIndex % active.length];
    text.textContent = h.translation;
    const bits = [h.narrator, h.source].filter(Boolean);
    meta.textContent = bits.length ? bits.join(" · ") : "";
  }

  private renderTicker(): void {
    const ticker = this.root.querySelector("#ticker") as HTMLElement | null;
    const track = this.root.querySelector("#tickerTrack");
    if (!ticker || !track) return;
    const notices = this.activeNotices();
    if (notices.length === 0) {
      ticker.hidden = true;
      track.textContent = "";
      return;
    }
    ticker.hidden = false;
    const joined = notices.map((n) => n.text).join("   •   ");
    track.textContent = `${joined}   •   ${joined}`;
  }

  private tick(): void {
    if (!this.share) return;
    const bn = this.share.config.language === "bn";
    const friday = isFridayBd();
    const parts = bdClockParts();
    const clock = this.root.querySelector("#clock");
    if (clock) clock.textContent = formatClock(parts, bn);

    const greg = this.root.querySelector("#gregorianDate");
    const hijri = this.root.querySelector("#hijriDate");
    if (greg) greg.textContent = formatGregorian(new Date(), this.share.config.language);
    if (hijri) {
      hijri.textContent = formatHijri(
        new Date(),
        this.share.config.hijriDateOffset,
        this.share.config.language
      );
    }

    if (!this.prayerTimes) {
      this.prayerTimes = calculatePrayerTimes(
        this.share.config.latitude,
        this.share.config.longitude,
        this.share.config.calculationMethod,
        this.share.config.madhab
      );
    }

    if (parts.hours === 0 && parts.minutes === 0 && parts.seconds < 2) {
      this.updateStatic();
    }

    const nowMins = parts.hours * 60 + parts.minutes;
    const nowSecs = parts.hours * 3600 + parts.minutes * 60 + parts.seconds;
    const current = getCurrentPrayer(this.prayerTimes, nowMins);
    const next = getNextJamaat(this.prayerTimes, this.share.jamaat, nowMins);
    const makruh = getMakruhInfo(this.prayerTimes, nowMins);
    const jamat = getJamaatCountdown(
      this.prayerTimes,
      this.share.jamaat,
      nowSecs,
      this.share.config.jamaatCountdownMins || 3
    );

    this.renderStatus(bn, friday, current, next, makruh, jamat);

    if (parts.seconds === 0) this.renderTable();
  }

  private renderStatus(
    bn: boolean,
    friday: boolean,
    current: { name: string; minsLeft: number },
    next: { name: string; minsLeft: number },
    makruh: ReturnType<typeof getMakruhInfo>,
    jamat: ReturnType<typeof getJamaatCountdown>
  ): void {
    const defaultStatus = this.root.querySelector<HTMLElement>("#defaultStatus");
    const makruhBanner = this.root.querySelector<HTMLElement>("#makruhBanner");
    const jamaatBanner = this.root.querySelector<HTMLElement>("#jamaatBanner");
    if (!defaultStatus || !makruhBanner || !jamaatBanner) return;

    if (makruh) {
      defaultStatus.hidden = true;
      jamaatBanner.hidden = true;
      makruhBanner.hidden = false;
      const copy = MAKRUH[makruh.key];
      const title = this.root.querySelector("#makruhTitle");
      const body = this.root.querySelector("#makruhBody");
      const note = this.root.querySelector<HTMLElement>("#makruhNote");
      const ends = this.root.querySelector("#makruhEnds");
      if (title) title.textContent = bn ? copy.bn : copy.en;
      if (body) body.textContent = bn ? "নামাজ পড়া নিষেধ" : "Prayer is prohibited";
      if (note) {
        const noteText = "noteBn" in copy ? (bn ? copy.noteBn : copy.noteEn) : "";
        note.hidden = !noteText;
        note.textContent = noteText;
      }
      if (ends) {
        const endsLabel = bn ? "শেষ হবে:" : "Ends at:";
        ends.innerHTML = `${escapeHtml(endsLabel)} ${styledTime(minsToHm(makruh.endsAtMins), bn)}`;
      }
      return;
    }

    if (jamat) {
      defaultStatus.hidden = true;
      makruhBanner.hidden = true;
      jamaatBanner.hidden = false;
      const title = this.root.querySelector("#jamaatTitle");
      const timer = this.root.querySelector("#jamaatTimer");
      const mute = this.root.querySelector("#jamaatMute");
      const prayerName = friday && jamat.name === "Dhuhr"
        ? bn ? "জুম'আর" : "Jumu'ah"
        : bn
          ? JAMAAT_BN[jamat.name] ?? jamat.name
          : jamat.name;
      if (title) {
        title.textContent = bn
          ? `${prayerName} জামাত শুরু হতে বাকি`
          : `${prayerName} Jamaat starts in`;
      }
      if (timer) timer.textContent = formatMmSs(jamat.secsLeft, bn);
      if (mute) {
        mute.textContent = bn
          ? "অনুগ্রহ করে মোবাইল সাইলেন্ট করুন"
          : "Please silence your mobile";
      }
      return;
    }

    makruhBanner.hidden = true;
    jamaatBanner.hidden = true;
    defaultStatus.hidden = false;

    const waqt = this.root.querySelector("#waqtLine");
    const nextEl = this.root.querySelector("#nextJamaat");
    const currentName = prayerLabel(current.name, bn, friday);
    const nextName = prayerLabel(next.name, bn, friday);
    if (waqt) {
      if (current.name) {
        waqt.textContent = bn
          ? `বর্তমান: ${currentName} · শেষ হতে বাকি ${formatCountdown(current.minsLeft, true)}`
          : `Current: ${currentName} · ends in ${formatCountdown(current.minsLeft)}`;
      } else {
        waqt.textContent = bn
          ? `পরবর্তী ওয়াক্ত ${formatCountdown(current.minsLeft, true)}`
          : `Next waqt in ${formatCountdown(current.minsLeft)}`;
      }
    }
    if (nextEl) {
      nextEl.textContent = bn
        ? `পরবর্তী জামাত: ${nextName} ${formatCountdown(next.minsLeft, true)}`
        : `Next Jamaat: ${nextName} in ${formatCountdown(next.minsLeft)}`;
    }
  }
}

function timeCell(
  hm: string,
  bangla: boolean,
  sub?: { label: string; time: string }
): string {
  const main = `<div class="time-main">${styledTime(hm, bangla)}</div>`;
  if (!sub) return main;
  return `${main}<div class="time-sub">${escapeHtml(sub.label)}: ${styledTime(sub.time, bangla)}</div>`;
}

export function parseShareTokenFromPath(pathname: string): string | null {
  const match = pathname.match(/\/s\/([A-Za-z0-9_-]+)/);
  return match?.[1] ?? null;
}
