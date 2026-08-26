export const PLAY_STORE_URL =
  "https://play.google.com/store/apps/details?id=com.mirazanik.masjidscreen";
export const DOCS_URL =
  "https://mirazanik.github.io/masjid-screen/MasjidScreen-Documentation.html";
export const PRIVACY_URL =
  "https://mirazanik.github.io/masjid-screen/privacy-policy.html";

type Lang = "en" | "bn";
type ShotKind = "phone" | "wide";

const SHOT = {
  banner: "/images/tv-banner-1280x720.png",
  connect: "/images/main_config_screen.jpg",
  login: "/images/login.jpg",
  screens: "/images/admin-list.jpg",
  jamaat: "/images/jamat.jpg",
  hadith: "/images/hadith.jpg",
  notices: "/images/notice.jpg",
  language: "/images/change-language.jpg",
  share: "/images/public-url.jpg",
  display: "/images/time.jpg",
  jamaatLive: "/images/jamat-time.jpg",
  makruh: "/images/makruh-time.jpg",
} as const;

type Copy = {
  langLabel: string;
  title: string;
  tagline: string;
  subtitle: string;
  getOnPlay: string;
  playStore: string;
  openPlay: string;
  continueLast: string;
  navHow: string;
  navDocs: string;
  whatTitle: string;
  whatItems: { title: string; text: string; src: string; alt: string; kind: ShotKind }[];
  howTitle: string;
  howIntro: string;
  howSteps: {
    title: string;
    steps: string[];
    src: string;
    alt: string;
    kind: ShotKind;
  }[];
  docsTitle: string;
  docsIntro: string;
  docs: {
    title: string;
    text: string;
    src: string;
    alt: string;
    kind: ShotKind;
  }[];
  fullDocs: string;
  privacy: string;
  github: string;
  footer: string;
};

const copy: Record<Lang, Copy> = {
  en: {
    langLabel: "বাংলা",
    title: "MasjidScreen",
    tagline: "Live prayer display for mosque TVs, tablets, and phones.",
    subtitle:
      "Show prayer times, jamaat, Hijri date, hadiths, and notices on a mounted screen. Manage everything from the same app.",
    getOnPlay: "Get it on",
    playStore: "Google Play",
    openPlay: "Open in Play Store",
    continueLast: "Open last mosque display",
    navHow: "How to use",
    navDocs: "Documentation",
    whatTitle: "What it does",
    whatItems: [
      {
        title: "Mosque TV",
        text: "Fullscreen landscape display: clock, next prayer, waqt and jamaat table, hadith, and scrolling notices.",
        src: SHOT.display,
        alt: "Mosque display showing clock, prayer table, hadith, and notice",
        kind: "wide",
      },
      {
        title: "Admin phone",
        text: "Sign in on the same app to set jamaat times, hadiths, notices, language, and mosque settings. Changes appear live.",
        src: SHOT.login,
        alt: "Masjid Admin sign-in screen",
        kind: "phone",
      },
      {
        title: "Public view",
        text: "Share a QR or link so worshippers can open the same display in a browser on their phone.",
        src: SHOT.share,
        alt: "Public view share screen with QR code and link",
        kind: "phone",
      },
    ],
    howTitle: "How to use",
    howIntro:
      "Install the same app on the mosque TV and on the admin phone. Pair once, then edit content from the phone — the TV updates live.",
    howSteps: [
      {
        title: "1. Connect the mosque screen",
        src: SHOT.connect,
        alt: "Connect Screen with QR code, pairing code, Connect my Masjid, and Admin",
        kind: "wide",
        steps: [
          "Install MasjidScreen from Google Play on the TV box, tablet, or phone you will mount. Keep it in landscape.",
          "An unpaired device shows Connect Screen: a QR on the left, plus Connect my Masjid, a pairing-code box, Admin, and Connect.",
          "Let an admin scan the QR, or type the 6-digit pairing code and tap Connect.",
          "To watch a public share on this device instead, tap Connect my Masjid and scan a public-view QR (Admin → Screens → Share).",
        ],
      },
      {
        title: "2. Sign in as admin",
        src: SHOT.login,
        alt: "Masjid Admin login with email, password, and Continue with Google",
        kind: "phone",
        steps: [
          "On the TV tap Admin, or install the same app on your phone and open Admin sign in.",
          "Sign in with email and password, or Continue with Google.",
          "Need access? Register here. Use Forgot password if you cannot sign in.",
        ],
      },
      {
        title: "3. Pair and manage screens",
        src: SHOT.screens,
        alt: "Admin Panel Screens list with online status and gear settings",
        kind: "phone",
        steps: [
          "After login you land on Admin Panel → Screens. Each card is one TV (name, online/offline, version).",
          "Tap Check Online to refresh status. Tap the gear on a screen to manage it. Use + to add a screen.",
          "Scan the TV QR from your phone, or type the pairing code, so that TV is attached to a screen such as Main Hall.",
        ],
      },
      {
        title: "4. Set jamaat times",
        src: SHOT.jamaat,
        alt: "Jamaat Times editor with waqt range and gold jamaat time buttons",
        kind: "phone",
        steps: [
          "Open the Jamaat tab. Each prayer shows its waqt window (for example Fajr 4:21 AM – 5:39 AM).",
          "Tap the gold time button to set the congregation time. Add an optional note if needed.",
          "Tap Save Jamaat Times. The TV table updates at once.",
        ],
      },
      {
        title: "5. Manage hadiths",
        src: SHOT.hadith,
        alt: "Hadiths screen with rotation seconds and hide, edit, delete on each card",
        kind: "phone",
        steps: [
          "Open the Hadith tab. Set Rotation (seconds) — for example 30 — then Save.",
          "Tap + to add a hadith (text and source). Use the eye to hide it from the TV, the pencil to edit, or the trash to delete.",
        ],
      },
      {
        title: "6. Add notices",
        src: SHOT.notices,
        alt: "Notices screen with announcement cards and add button",
        kind: "phone",
        steps: [
          "Open the Notices tab. Tap + to add a scrolling announcement for the TV ticker.",
          "Hide or delete a notice from its card. Priority tags (such as P0) control order on the display.",
        ],
      },
      {
        title: "7. Language and settings",
        src: SHOT.language,
        alt: "Settings with display language Bangla, jamaat countdown, and Hijri date adjustment",
        kind: "phone",
        steps: [
          "Open Settings. Choose Display Language: English or বাংলা. This language is used on the TV and on public phone viewers.",
          "Set when the jamaat countdown appears (for example 3 minutes before Jamaat).",
          "Adjust the Hijri date if local moon sighting differs, and drag layout sliders if the TV columns need resizing. Tap Save Settings.",
        ],
      },
      {
        title: "8. Share with worshippers",
        src: SHOT.share,
        alt: "Public view enabled with QR code, copy link, share, download QR, and new QR",
        kind: "phone",
        steps: [
          "Open the Share tab on a screen. Turn Enable public view on.",
          "Anyone with the link can see this screen on their phone. Use Copy link, Share link, or Download QR.",
          "Tap New QR / link if the old link leaked. Tap Revoke link permanently to stop sharing.",
        ],
      },
    ],
    docsTitle: "Documentation",
    docsIntro:
      "What each part of the mosque display and admin app does. Switch language above to read this page in বাংলা.",
    docs: [
      {
        title: "Prayer display",
        src: SHOT.display,
        alt: "Live display with Hijri and Gregorian dates, clock, sun times, current waqt, prayer table, hadith, and notice",
        kind: "wide",
        text: "The TV stays on this screen. Header: Hijri date, Gregorian date. Left: clock, sunrise, sunset, sahri, iftar, and the current waqt with time remaining. Right: prayer table (start, end, jamaat). Bottom: rotating hadith and a scrolling notice ticker. Friday Dhuhr is shown as Jumu'ah.",
      },
      {
        title: "Jamaat countdown",
        src: SHOT.jamaatLive,
        alt: "Display overlay counting down to Fajr jamaat with a silent-phone reminder",
        kind: "wide",
        text: "When jamaat is close, the left panel switches to a countdown (for example “Fajr jamaat starts in …”) and a Please silent your mobile reminder. The current prayer row stays highlighted in the table. You choose how many minutes before jamaat this appears in Settings.",
      },
      {
        title: "Makruh (prohibited) times",
        src: SHOT.makruh,
        alt: "Makruh sunset warning on the display with Asr still allowed until sunset",
        kind: "wide",
        text: "During sunrise, zawal, and sunset the display shows a warning: prayer is not allowed in that window. For sunset it notes that today’s Asr may still be performed until the listed end time. Ishraq, zawal, and makruh times also appear as small notes in the table.",
      },
      {
        title: "Connect Screen",
        src: SHOT.connect,
        alt: "Unpaired TV Connect Screen",
        kind: "wide",
        text: "First launch (or after unpair) shows Connect Screen. Three ways in: admin scans the QR, someone types a pairing code, or Connect my Masjid opens a public share. Admin on this screen signs in without pairing.",
      },
      {
        title: "Admin sign-in",
        src: SHOT.login,
        alt: "Admin login screen",
        kind: "phone",
        text: "Same APK as the TV. Email/password or Google. Only signed-in mosque admins can change content. Privacy policy is linked on this screen.",
      },
      {
        title: "Screens list",
        src: SHOT.screens,
        alt: "Admin Panel listing paired screens",
        kind: "phone",
        text: "Super Admin sees all TVs, online status, last seen, and app version. Gear opens that screen’s Jamaat / Hadith / Notices / Settings / Share tabs. Users, Groups, and Global are for account and mosque-wide settings.",
      },
      {
        title: "Jamaat editor",
        src: SHOT.jamaat,
        alt: "Jamaat times editor",
        kind: "phone",
        text: "Waqt is calculated on the device from the coordinates and method in Settings (not GPS). Jamaat times are whatever you type. Optional notes can appear on the TV. Save writes to the cloud; the display updates in real time and keeps working offline after the first sync.",
      },
      {
        title: "Hadiths",
        src: SHOT.hadith,
        alt: "Hadith manager",
        kind: "phone",
        text: "Hadiths rotate on the TV at the interval you set. Hide a hadith without deleting it. Text can be English or Bangla — the display shows what you entered.",
      },
      {
        title: "Notices",
        src: SHOT.notices,
        alt: "Notices manager",
        kind: "phone",
        text: "Notices scroll along the bottom of the TV. Add, hide, or delete from this tab. Keep them short so they read well on a ticker.",
      },
      {
        title: "Language and layout",
        src: SHOT.language,
        alt: "Settings for language, countdown, Hijri offset, and layout",
        kind: "phone",
        text: "Display Language switches the whole TV (and public viewer) between English and Bangla, including dates and numerals. Jamaat countdown, Hijri ± days, and column width sliders live here. Reset layout to default restores the stock split.",
      },
      {
        title: "Public viewer",
        src: SHOT.share,
        alt: "Public view QR and link",
        kind: "phone",
        text: "The share URL is not the mosque ID. Clock and prayer times update on the phone; content refreshes when you save. New QR invalidates the old link. Viewers do not subscribe to TV heartbeat documents, so many phones do not raise Firebase cost.",
      },
      {
        title: "Requirements",
        src: SHOT.banner,
        alt: "MasjidScreen mosque TV display banner",
        kind: "wide",
        text: "Android 8.0 (API 26) or newer. A landscape tablet, TV box, or monitor is best for the mosque screen. Admin works on a phone. After the first sync the display keeps clock, waqt, and countdown without internet.",
      },
    ],
    fullDocs: "Technical documentation",
    privacy: "Privacy policy",
    github: "GitHub",
    footer: "MasjidScreen · mosque.mirazanik.com",
  },
  bn: {
    langLabel: "English",
    title: "MasjidScreen",
    tagline: "মসজিদের টিভি, ট্যাবলেট ও ফোনের জন্য লাইভ নামাজের ডিসপ্লে।",
    subtitle:
      "মাউন্ট করা স্ক্রিনে নামাজের সময়, জামাত, হিজরি তারিখ, হাদিস ও নোটিশ দেখান। একই অ্যাপ থেকে সব পরিচালনা করুন।",
    getOnPlay: "ডাউনলোড করুন",
    playStore: "Google Play",
    openPlay: "Play Store-এ খুলুন",
    continueLast: "শেষ মসজিদ ডিসপ্লে খুলুন",
    navHow: "কীভাবে ব্যবহার করবেন",
    navDocs: "ডকুমেন্টেশন",
    whatTitle: "এটি কী করে",
    whatItems: [
      {
        title: "মসজিদ টিভি",
        text: "ফুলস্ক্রিন ল্যান্ডস্কেপ: ঘড়ি, পরবর্তী নামাজ, ওয়াক্ত ও জামাতের টেবিল, হাদিস এবং স্ক্রলিং নোটিশ।",
        src: SHOT.display,
        alt: "ঘড়ি, নামাজের টেবিল, হাদিস ও নোটিশসহ মসজিদ ডিসপ্লে",
        kind: "wide",
      },
      {
        title: "অ্যাডমিন ফোন",
        text: "একই অ্যাপে সাইন ইন করে জামাত, হাদিস, নোটিশ, ভাষা ও মসজিদের সেটিংস বদলান। পরিবর্তন তাৎক্ষণিক দেখা যায়।",
        src: SHOT.login,
        alt: "মসজিদ অ্যাডমিন সাইন-ইন স্ক্রিন",
        kind: "phone",
      },
      {
        title: "পাবলিক ভিউ",
        text: "কিউআর বা লিংক শেয়ার করুন — মুসল্লিরা ফোনের ব্রাউজারে একই ডিসপ্লে খুলতে পারেন।",
        src: SHOT.share,
        alt: "কিউআর ও লিংকসহ পাবলিক ভিউ শেয়ার স্ক্রিন",
        kind: "phone",
      },
    ],
    howTitle: "কীভাবে ব্যবহার করবেন",
    howIntro:
      "মসজিদের টিভি এবং অ্যাডমিন ফোনে একই অ্যাপ ইনস্টল করুন। একবার পেয়ার করুন — তারপর ফোন থেকে কনটেন্ট বদলালে টিভি লাইভে আপডেট হয়।",
    howSteps: [
      {
        title: "১. মসজিদের স্ক্রিন সংযোগ করুন",
        src: SHOT.connect,
        alt: "কানেক্ট স্ক্রিন — কিউআর, পেয়ারিং কোড, Connect my Masjid ও Admin",
        kind: "wide",
        steps: [
          "যে টিভি বক্স, ট্যাবলেট বা ফোন মাউন্ট করবেন, সেখানে Google Play থেকে MasjidScreen ইনস্টল করুন। ল্যান্ডস্কেপে রাখুন।",
          "আনপেয়ারড ডিভাইসে Connect Screen দেখায়: বামে কিউআর, সাথে Connect my Masjid, পেয়ারিং কোড, Admin এবং Connect।",
          "অ্যাডমিন কিউআর স্ক্যান করুন, অথবা ৬-সংখ্যার কোড লিখে Connect চাপুন।",
          "এই ডিভাইসে পাবলিক শেয়ার দেখতে Connect my Masjid চাপুন এবং পাবলিক-ভিউ কিউআর স্ক্যান করুন (Admin → Screens → Share)।",
        ],
      },
      {
        title: "২. অ্যাডমিন হিসেবে সাইন ইন",
        src: SHOT.login,
        alt: "ইমেইল, পাসওয়ার্ড ও Google দিয়ে অ্যাডমিন লগইন",
        kind: "phone",
        steps: [
          "টিভিতে Admin চাপুন, অথবা ফোনে একই অ্যাপ ইনস্টল করে Admin sign in খুলুন।",
          "ইমেইল-পাসওয়ার্ড বা Continue with Google দিয়ে সাইন ইন করুন।",
          "অ্যাক্সেস লাগলে Register here। সাইন ইন না গেলে Forgot password ব্যবহার করুন।",
        ],
      },
      {
        title: "৩. স্ক্রিন পেয়ার ও পরিচালনা",
        src: SHOT.screens,
        alt: "অনলাইন স্ট্যাটাস ও গিয়ার সেটিংসসহ অ্যাডমিন প্যানেল স্ক্রিন তালিকা",
        kind: "phone",
        steps: [
          "লগইনের পর Admin Panel → Screens। প্রতিটি কার্ড একটি টিভি (নাম, অনলাইন/অফলাইন, ভার্সন)।",
          "স্ট্যাটাস রিফ্রেশ করতে Check Online চাপুন। স্ক্রিন ম্যানেজ করতে গিয়ার আইকনে চাপুন। নতুন স্ক্রিন যোগ করতে + চাপুন।",
          "ফোন থেকে টিভির কিউআর স্ক্যান করুন, অথবা পেয়ারিং কোড টাইপ করুন — টিভি একটি স্ক্রিনের সাথে যুক্ত হবে (যেমন মেইন হল)।",
        ],
      },
      {
        title: "৪. জামাতের সময় সেট করুন",
        src: SHOT.jamaat,
        alt: "ওয়াক্ত রেঞ্জ ও সোনালি জামাত সময় বাটনসহ জামাত এডিটর",
        kind: "phone",
        steps: [
          "Jamaat ট্যাব খুলুন। প্রতি নামাজে ওয়াক্তের উইন্ডো দেখায় (যেমন ফজর ৪:২১ AM – ৫:৩৯ AM)।",
          "জামাতের সময় সেট করতে সোনালি সময়ের বাটনে চাপুন। প্রয়োজনে নোট যোগ করুন।",
          "Save Jamaat Times চাপুন। টিভির টেবিল সাথে সাথে আপডেট হয়।",
        ],
      },
      {
        title: "৫. হাদিস পরিচালনা",
        src: SHOT.hadith,
        alt: "ঘূর্ণন সেকেন্ড ও লুকান/এডিট/ডিলিটসহ হাদিস স্ক্রিন",
        kind: "phone",
        steps: [
          "Hadith ট্যাব খুলুন। Rotation (seconds) সেট করুন — যেমন ৩০ — তারপর Save।",
          "নতুন হাদিস যোগ করতে + চাপুন (টেক্সট ও সূত্র)। চোখ দিয়ে টিভি থেকে লুকান, পেন্সিল দিয়ে এডিট, ট্র্যাশ দিয়ে মুছুন।",
        ],
      },
      {
        title: "৬. নোটিশ যোগ করুন",
        src: SHOT.notices,
        alt: "ঘোষণা কার্ড ও অ্যাড বাটনসহ নোটিশ স্ক্রিন",
        kind: "phone",
        steps: [
          "Notices ট্যাব খুলুন। টিভির টিকারের জন্য স্ক্রলিং ঘোষণা যোগ করতে + চাপুন।",
          "কার্ড থেকে নোটিশ লুকান বা মুছুন। প্রায়োরিটি ট্যাগ (যেমন P0) ডিসপ্লের ক্রম নিয়ন্ত্রণ করে।",
        ],
      },
      {
        title: "৭. ভাষা ও সেটিংস",
        src: SHOT.language,
        alt: "ডিসপ্লে ভাষা বাংলা, জামাত কাউন্টডাউন ও হিজরি তারিখ সমন্বয়সহ সেটিংস",
        kind: "phone",
        steps: [
          "Settings খুলুন। Display Language বেছে নিন: English বা বাংলা। এই ভাষা টিভি এবং পাবলিক ফোন ভিউয়ারে ব্যবহৃত হয়।",
          "জামাত কাউন্টডাউন কখন দেখাবে সেট করুন (যেমন জামাতের ৩ মিনিট আগে)।",
          "চাঁদ দেখা ভিন্ন হলে হিজরি তারিখ সমন্বয় করুন। টিভির কলাম চওড়া করতে স্লাইডার ব্যবহার করুন। Save Settings চাপুন।",
        ],
      },
      {
        title: "৮. মুসল্লিদের সাথে শেয়ার",
        src: SHOT.share,
        alt: "পাবলিক ভিউ চালু — কিউআর, লিংক কপি, শেয়ার, কিউআর ডাউনলোড ও নতুন কিউআর",
        kind: "phone",
        steps: [
          "স্ক্রিনের Share ট্যাব খুলুন। Enable public view চালু করুন।",
          "লিংক থাকলে যে কেউ ফোনে এই স্ক্রিন দেখতে পারবেন। Copy link, Share link বা Download QR ব্যবহার করুন।",
          "পুরনো লিংক ফাঁস হলে New QR / link চাপুন। শেয়ার বন্ধ করতে Revoke link permanently চাপুন।",
        ],
      },
    ],
    docsTitle: "ডকুমেন্টেশন",
    docsIntro:
      "মসজিদ ডিসপ্লে ও অ্যাডমিন অ্যাপের প্রতিটি অংশ কী করে। ইংরেজিতে পড়তে উপরে English চাপুন।",
    docs: [
      {
        title: "নামাজের ডিসপ্লে",
        src: SHOT.display,
        alt: "হিজরি ও গ্রেগরিয়ান তারিখ, ঘড়ি, সূর্যের সময়, বর্তমান ওয়াক্ত, নামাজের টেবিল, হাদিস ও নোটিশসহ লাইভ ডিসপ্লে",
        kind: "wide",
        text: "টিভিতে এই স্ক্রিনই থাকে। হেডার: হিজরি তারিখ, গ্রেগরিয়ান তারিখ। বামে: ঘড়ি, সূর্যোদয়, সূর্যাস্ত, সাহরি, ইফতার এবং বর্তমান ওয়াক্ত ও অবশিষ্ট সময়। ডানে: নামাজের টেবিল (শুরু, শেষ, জামাত)। নিচে: ঘুরন্ত হাদিস ও স্ক্রলিং নোটিশ। শুক্রবার জোহর জুম'আ হিসেবে দেখায়।",
      },
      {
        title: "জামাত কাউন্টডাউন",
        src: SHOT.jamaatLive,
        alt: "ফজরের জামাতের কাউন্টডাউন ও মোবাইল সাইলেন্ট করার অনুস্মারক",
        kind: "wide",
        text: "জামাত কাছে এলে বাম প্যানেলে কাউন্টডাউন আসে (যেমন “ফজরের জামাত শুরু হতে বাকি …”) এবং অনুগ্রহ করে মোবাইল সাইলেন্ট করুন। টেবিলে বর্তমান নামাজ হাইলাইট থাকে। কত মিনিট আগে এটি দেখাবে সেটি Settings-এ সেট করেন।",
      },
      {
        title: "মাকরূহ (নিষিদ্ধ) সময়",
        src: SHOT.makruh,
        alt: "সূর্যাস্তের মাকরূহ সতর্কতা — আসর এখনও আদায় করা যাবে",
        kind: "wide",
        text: "সূর্যোদয়, যাওয়াল ও সূর্যাস্তের সময় ডিসপ্লে সতর্কতা দেখায়: সেই উইন্ডোতে নামাজ পড়া নিষেধ। সূর্যাস্তে জানায় আজকের আসর নির্ধারিত শেষ সময় পর্যন্ত আদায় করা যাবে। ইশরাক, যাওয়াল ও মাকরূহ সময় টেবিলে ছোট নোট হিসেবেও থাকে।",
      },
      {
        title: "কানেক্ট স্ক্রিন",
        src: SHOT.connect,
        alt: "আনপেয়ারড টিভির কানেক্ট স্ক্রিন",
        kind: "wide",
        text: "প্রথম চালুতে (বা আনপেয়ারের পর) Connect Screen। তিনভাবে ঢোকা যায়: অ্যাডমিন কিউআর স্ক্যান, পেয়ারিং কোড টাইপ, অথবা Connect my Masjid দিয়ে পাবলিক শেয়ার। এই স্ক্রিনের Admin পেয়ার ছাড়াই সাইন ইন করে।",
      },
      {
        title: "অ্যাডমিন সাইন-ইন",
        src: SHOT.login,
        alt: "অ্যাডমিন লগইন স্ক্রিন",
        kind: "phone",
        text: "টিভির মতো একই অ্যাপ। ইমেইল/পাসওয়ার্ড বা Google। শুধু সাইন-ইন করা অ্যাডমিন কনটেন্ট বদলাতে পারেন। এই স্ক্রিনে গোপনীয়তা নীতির লিংক আছে।",
      },
      {
        title: "স্ক্রিন তালিকা",
        src: SHOT.screens,
        alt: "পেয়ার করা স্ক্রিনের অ্যাডমিন প্যানেল তালিকা",
        kind: "phone",
        text: "সুপার অ্যাডমিন সব টিভি, অনলাইন স্ট্যাটাস, শেষ দেখা এবং অ্যাপ ভার্সন দেখেন। গিয়ার সেই স্ক্রিনের Jamaat / Hadith / Notices / Settings / Share ট্যাব খোলে। Users, Groups ও Global অ্যাকাউন্ট ও মসজিদ-ব্যাপী সেটিংসের জন্য।",
      },
      {
        title: "জামাত এডিটর",
        src: SHOT.jamaat,
        alt: "জামাত সময় এডিটর",
        kind: "phone",
        text: "ওয়াক্ত ডিভাইসেই হিসাব হয় — সেটিংসের স্থানাঙ্ক ও পদ্ধতি থেকে (জিপিএস নয়)। জামাতের সময় আপনি যা টাইপ করেন। ঐচ্ছিক নোট টিভিতে দেখা যেতে পারে। Save ক্লাউডে লেখে; ডিসপ্লে রিয়েল টাইমে আপডেট হয় এবং প্রথম সিঙ্কের পর অফলাইনেও চলে।",
      },
      {
        title: "হাদিস",
        src: SHOT.hadith,
        alt: "হাদিস ম্যানেজার",
        kind: "phone",
        text: "আপনার সেট করা বিরতিতে টিভিতে হাদিস ঘোরে। মুছে না ফেলে লুকাতে পারেন। টেক্সট ইংরেজি বা বাংলা — ডিসপ্লে যা লিখেছেন তাই দেখায়।",
      },
      {
        title: "নোটিশ",
        src: SHOT.notices,
        alt: "নোটিশ ম্যানেজার",
        kind: "phone",
        text: "নোটিশ টিভির নিচে স্ক্রল করে। এই ট্যাব থেকে যোগ, লুকান বা মুছুন। টিকারে পড়ার সুবিধায় সংক্ষিপ্ত রাখুন।",
      },
      {
        title: "ভাষা ও লেআউট",
        src: SHOT.language,
        alt: "ভাষা, কাউন্টডাউন, হিজরি অফসেট ও লেআউটের সেটিংস",
        kind: "phone",
        text: "Display Language পুরো টিভি (ও পাবলিক ভিউয়ার) ইংরেজি ও বাংলার মধ্যে বদলায় — তারিখ ও সংখ্যাসহ। জামাত কাউন্টডাউন, হিজরি ± দিন এবং কলামের চওড়া এখানে। Reset layout to default আগের স্প্লিট ফেরায়।",
      },
      {
        title: "পাবলিক ভিউয়ার",
        src: SHOT.share,
        alt: "পাবলিক ভিউ কিউআর ও লিংক",
        kind: "phone",
        text: "শেয়ার ইউআরএল মসজিদ আইডি নয়। ফোনে ঘড়ি ও নামাজের সময় আপডেট হয়; আপনি সেভ করলে কনটেন্ট রিফ্রেশ হয়। New QR পুরনো লিংক বাতিল করে। ভিউয়াররা টিভির হার্টবিট ডকুমেন্ট শোনে না, তাই অনেক ফোনেও Firebase খরচ বাড়ে না।",
      },
      {
        title: "প্রয়োজনীয়তা",
        src: SHOT.banner,
        alt: "MasjidScreen মসজিদ টিভি ডিসপ্লে ব্যানার",
        kind: "wide",
        text: "Android 8.0 (API 26) বা নতুন। মসজিদ স্ক্রিনের জন্য ল্যান্ডস্কেপ ট্যাবলেট, টিভি বক্স বা মনিটর ভালো। অ্যাডমিন ফোনে চলে। প্রথম সিঙ্কের পর ইন্টারনেট ছাড়াই ঘড়ি, ওয়াক্ত ও কাউন্টডাউন চলে।",
      },
    ],
    fullDocs: "টেকনিক্যাল ডকুমেন্টেশন",
    privacy: "গোপনীয়তা নীতি",
    github: "GitHub",
    footer: "MasjidScreen · mosque.mirazanik.com",
  },
};

function escapeHtml(text: string): string {
  return text
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function playBadge(): string {
  return `
    <svg class="play-icon" viewBox="0 0 24 24" aria-hidden="true">
      <path fill="#32BBFF" d="M1.05.9C.88 1.1.75 1.4.75 1.8v20.4c0 .4.13.7.3.9l.08.06 11.5-11.5v-.22L1.13.84z"/>
      <path fill="#FFCE00" d="m16.3 16.1-4.67-4.67v-.22l4.67-4.67.1.06 5.54 3.15c1.58.9 1.58 2.37 0 3.27l-5.54 3.15z"/>
      <path fill="#00F076" d="m16.4 16.04-4.77-4.77L1.05 23.1c.5.54 1.32.6 2.24.08z"/>
      <path fill="#FF3A44" d="M16.4 7.96 3.29.82C2.37.3 1.55.36 1.05.9l10.58 10.37z"/>
    </svg>
  `;
}

function shot(src: string, alt: string, kind: ShotKind, extraClass = ""): string {
  return `<figure class="shot shot-${kind} ${extraClass}">
    <img src="${escapeHtml(src)}" alt="${escapeHtml(alt)}" loading="lazy" />
  </figure>`;
}

function stepsList(items: readonly string[]): string {
  return `<ol class="how-steps">${items.map((item) => `<li>${escapeHtml(item)}</li>`).join("")}</ol>`;
}

function renderBody(lang: Lang, lastSharePath: string | null): string {
  const t = copy[lang];
  const continueCard = lastSharePath
    ? `<a class="continue-card" href="${escapeHtml(lastSharePath)}">${escapeHtml(t.continueLast)} →</a>`
    : "";

  return `
    <header class="landing-hero">
      <div class="landing-top">
        <img class="landing-logo" src="/icons/icon-192.png" width="56" height="56" alt="" />
        <button type="button" class="lang-toggle" id="langToggle" aria-label="${lang === "en" ? "Switch to Bangla" : "Switch to English"}">${escapeHtml(t.langLabel)}</button>
      </div>
      <h1>${escapeHtml(t.title)}</h1>
      <p class="landing-tagline">${escapeHtml(t.tagline)}</p>
      <p class="landing-sub">${escapeHtml(t.subtitle)}</p>
      <a class="play-btn" href="${PLAY_STORE_URL}" target="_blank" rel="noopener noreferrer">
        ${playBadge()}
        <span>
          <small>${escapeHtml(t.getOnPlay)}</small>
          <strong>${escapeHtml(t.playStore)}</strong>
        </span>
      </a>
      <p class="play-fallback"><a href="${PLAY_STORE_URL}" target="_blank" rel="noopener noreferrer">${escapeHtml(t.openPlay)}</a></p>
      <nav class="landing-nav">
        <a href="#how-to">${escapeHtml(t.navHow)}</a>
        <a href="#docs">${escapeHtml(t.navDocs)}</a>
        <a href="${PRIVACY_URL}" target="_blank" rel="noopener noreferrer">${escapeHtml(t.privacy)}</a>
      </nav>
      ${continueCard}
    </header>

    ${shot(SHOT.banner, lang === "bn" ? "MasjidScreen মসজিদ টিভি ডিসপ্লে" : "MasjidScreen mosque TV display", "wide", "hero-shot")}

    <section class="landing-section">
      <h2>${escapeHtml(t.whatTitle)}</h2>
      <div class="feature-grid">
        ${t.whatItems
          .map(
            (item) => `
          <article class="feature-card">
            ${shot(item.src, item.alt, item.kind)}
            <h3>${escapeHtml(item.title)}</h3>
            <p>${escapeHtml(item.text)}</p>
          </article>`
          )
          .join("")}
      </div>
    </section>

    <section class="landing-section" id="how-to">
      <h2>${escapeHtml(t.howTitle)}</h2>
      <p class="docs-intro">${escapeHtml(t.howIntro)}</p>
      <div class="guide-list">
        ${t.howSteps
          .map(
            (item, index) => `
          <article class="guide-step${index % 2 === 1 ? " guide-step-flip" : ""}">
            ${shot(item.src, item.alt, item.kind)}
            <div class="guide-copy">
              <h3>${escapeHtml(item.title)}</h3>
              ${stepsList(item.steps)}
            </div>
          </article>`
          )
          .join("")}
      </div>
    </section>

    <section class="landing-section" id="docs">
      <h2>${escapeHtml(t.docsTitle)}</h2>
      <p class="docs-intro">${escapeHtml(t.docsIntro)}</p>
      <div class="docs-illustrated">
        ${t.docs
          .map(
            (item) => `
          <article class="docs-card docs-card-shot">
            ${shot(item.src, item.alt, item.kind)}
            <div>
              <h3>${escapeHtml(item.title)}</h3>
              <p>${escapeHtml(item.text)}</p>
            </div>
          </article>`
          )
          .join("")}
      </div>
      <div class="doc-links">
        <a href="${DOCS_URL}" target="_blank" rel="noopener noreferrer">${escapeHtml(t.fullDocs)}</a>
        <a href="${PRIVACY_URL}" target="_blank" rel="noopener noreferrer">${escapeHtml(t.privacy)}</a>
        <a href="https://github.com/mirazanik/masjid-screen" target="_blank" rel="noopener noreferrer">${escapeHtml(t.github)}</a>
      </div>
    </section>

    <footer class="landing-footer">
      <a class="play-btn play-btn-sm" href="${PLAY_STORE_URL}" target="_blank" rel="noopener noreferrer">
        ${playBadge()}
        <span>
          <small>${escapeHtml(t.getOnPlay)}</small>
          <strong>${escapeHtml(t.playStore)}</strong>
        </span>
      </a>
      <p>${escapeHtml(t.footer)}</p>
    </footer>
  `;
}

export function renderLanding(
  root: HTMLElement,
  options: { lastSharePath?: string | null } = {}
): void {
  document.body.classList.add("is-landing");
  document.title = "MasjidScreen — Mosque live display";
  const stored = localStorage.getItem("ms-landing-lang");
  let lang: Lang = stored === "bn" ? "bn" : "en";
  document.documentElement.lang = lang;

  const paint = () => {
    const y = window.scrollY;
    root.innerHTML = `<div class="landing">${renderBody(lang, options.lastSharePath ?? null)}</div>`;
    document.documentElement.lang = lang;
    document.documentElement.dir = "ltr";
    root.querySelector("#langToggle")?.addEventListener("click", () => {
      lang = lang === "en" ? "bn" : "en";
      localStorage.setItem("ms-landing-lang", lang);
      paint();
    });
    window.scrollTo(0, y);
  };

  paint();
}
