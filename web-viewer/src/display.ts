import type { PublicShareSnapshot, PrayerTimesStrings } from "./types";
import {
  bdClockParts,
  calculatePrayerTimes,
  formatCountdown,
  formatGregorian,
  formatHijri,
  getCurrentPrayer,
  getNextJamaat,
  pad2,
  resolveJamaatTimes,
} from "./prayer";

const PRAYER_ROWS: Array<{
  key: keyof PrayerTimesStrings;
  label: string;
  labelBn: string;
  jamaatKey?: "fajr" | "dhuhr" | "asr" | "maghrib" | "isha";
}> = [
  { key: "fajr", label: "Fajr", labelBn: "ফজর", jamaatKey: "fajr" },
  { key: "sunrise", label: "Sunrise", labelBn: "সূর্যোদয়" },
  { key: "dhuhr", label: "Dhuhr", labelBn: "জোহর", jamaatKey: "dhuhr" },
  { key: "asr", label: "Asr", labelBn: "আসর", jamaatKey: "asr" },
  { key: "maghrib", label: "Maghrib", labelBn: "মাগরিব", jamaatKey: "maghrib" },
  { key: "isha", label: "Isha", labelBn: "ইশা", jamaatKey: "isha" },
];

function escapeHtml(text: string): string {
  return text
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
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
      <div class="waqt" id="waqtLine"></div>
      <div class="next-jamaat" id="nextJamaat"></div>
    </section>
    <section class="table-wrap">
      <table class="prayer-table">
        <thead>
          <tr>
            <th>Prayer</th>
            <th>Start</th>
            <th>Jamaat</th>
          </tr>
        </thead>
        <tbody id="prayerBody"></tbody>
      </table>
    </section>
    <section class="hadith-card" id="hadithCard" hidden>
      <div class="hadith-label">Hadith</div>
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
    document.documentElement.dataset.theme = share.config.activeTheme || "night_navy";
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
    const nameEl = this.root.querySelector("#mosqueName");
    if (nameEl) nameEl.textContent = config.name || this.share.screenName || "Masjid";

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
    const { hours, minutes } = bdClockParts();
    const nowMins = hours * 60 + minutes;
    const current = getCurrentPrayer(this.prayerTimes, nowMins).name;

    body.innerHTML = PRAYER_ROWS.map((row) => {
      const start = this.prayerTimes![row.key];
      const jamaatVal = row.jamaatKey ? jamaat[row.jamaatKey] : "—";
      const label = bn ? row.labelBn : row.label;
      const active = current && row.label === current ? " class=\"active\"" : "";
      return `<tr${active}><td>${escapeHtml(label)}</td><td>${escapeHtml(start)}</td><td>${escapeHtml(jamaatVal)}</td></tr>`;
    }).join("");
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
    const parts = bdClockParts();
    const clock = this.root.querySelector("#clock");
    if (clock) {
      clock.textContent = `${pad2(parts.hours)}:${pad2(parts.minutes)}:${pad2(parts.seconds)}`;
    }

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

    // Recalculate prayer times at midnight BD
    if (parts.hours === 0 && parts.minutes === 0 && parts.seconds < 2) {
      this.updateStatic();
    }

    const nowMins = parts.hours * 60 + parts.minutes;
    const current = getCurrentPrayer(this.prayerTimes, nowMins);
    const next = getNextJamaat(this.prayerTimes, this.share.jamaat, nowMins);

    const waqt = this.root.querySelector("#waqtLine");
    if (waqt) {
      waqt.textContent = current.name
        ? `Current: ${current.name} · ends in ${formatCountdown(current.minsLeft)}`
        : `Next waqt in ${formatCountdown(current.minsLeft)}`;
    }
    const nextEl = this.root.querySelector("#nextJamaat");
    if (nextEl) {
      nextEl.textContent = `Next Jamaat: ${next.name} in ${formatCountdown(next.minsLeft)}`;
    }

    // Refresh active row highlight each minute
    if (parts.seconds === 0) this.renderTable();
  }
}

export function parseShareTokenFromPath(pathname: string): string | null {
  const match = pathname.match(/\/s\/([A-Za-z0-9_-]+)/);
  return match?.[1] ?? null;
}
