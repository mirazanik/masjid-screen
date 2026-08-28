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

const LANDSCAPE_SLIDES = [
  { src: SHOT.banner, key: "banner" },
  { src: SHOT.display, key: "display" },
  { src: SHOT.jamaatLive, key: "jamaat" },
  { src: SHOT.makruh, key: "makruh" },
  { src: SHOT.connect, key: "connect" },
] as const;

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
  carouselPrev: string;
  carouselNext: string;
  carouselSlides: { alt: string; caption: string }[];
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
    carouselPrev: "Previous image",
    carouselNext: "Next image",
    carouselSlides: [
      { alt: "MasjidScreen mosque TV display", caption: "Mosque TV display" },
      {
        alt: "Live display with Hijri and Gregorian dates, clock, sun times, current waqt, prayer table, hadith, and notice",
        caption: "Prayer display",
      },
      {
        alt: "Display overlay counting down to Fajr jamaat with a silent-phone reminder",
        caption: "Jamaat countdown",
      },
      {
        alt: "Makruh sunset warning on the display with Asr still allowed until sunset",
        caption: "Makruh (prohibited) times",
      },
      {
        alt: "Connect Screen with QR code, pairing code, Connect my Masjid, and Admin",
        caption: "Connect Screen",
      },
    ],
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
      // {
      //   title: "Connect Screen",
      //   src: SHOT.connect,
      //   alt: "Unpaired TV Connect Screen",
      //   kind: "wide",
      //   text: "First launch (or after unpair) shows Connect Screen. Three ways in: admin scans the QR, someone types a pairing code, or Connect my Masjid opens a public share. Admin on this screen signs in without pairing.",
      // },
      // {
      //   title: "Admin sign-in",
      //   src: SHOT.login,
      //   alt: "Admin login screen",
      //   kind: "phone",
      //   text: "Same APK as the TV. Email/password or Google. Only signed-in mosque admins can change content. Privacy policy is linked on this screen.",
      // },
      // {
      //   title: "Screens list",
      //   src: SHOT.screens,
      //   alt: "Admin Panel listing paired screens",
      //   kind: "phone",
      //   text: "Super Admin sees all TVs, online status, last seen, and app version. Gear opens that screen’s Jamaat / Hadith / Notices / Settings / Share tabs. Users, Groups, and Global are for account and mosque-wide settings.",
      // },
      // {
      //   title: "Jamaat editor",
      //   src: SHOT.jamaat,
      //   alt: "Jamaat times editor",
      //   kind: "phone",
      //   text: "Waqt is calculated on the device from the coordinates and method in Settings (not GPS). Jamaat times are whatever you type. Optional notes can appear on the TV. Save writes to the cloud; the display updates in real time and keeps working offline after the first sync.",
      // },
      // {
      //   title: "Hadiths",
      //   src: SHOT.hadith,
      //   alt: "Hadith manager",
      //   kind: "phone",
      //   text: "Hadiths rotate on the TV at the interval you set. Hide a hadith without deleting it. Text can be English or Bangla — the display shows what you entered.",
      // },
      // {
      //   title: "Notices",
      //   src: SHOT.notices,
      //   alt: "Notices manager",
      //   kind: "phone",
      //   text: "Notices scroll along the bottom of the TV. Add, hide, or delete from this tab. Keep them short so they read well on a ticker.",
      // },
      // {
      //   title: "Language and layout",
      //   src: SHOT.language,
      //   alt: "Settings for language, countdown, Hijri offset, and layout",
      //   kind: "phone",
      //   text: "Display Language switches the whole TV (and public viewer) between English and Bangla, including dates and numerals. Jamaat countdown, Hijri ± days, and column width sliders live here. Reset layout to default restores the stock split.",
      // },
      // {
      //   title: "Public viewer",
      //   src: SHOT.share,
      //   alt: "Public view QR and link",
      //   kind: "phone",
      //   text: "The share URL is not the mosque ID. Clock and prayer times update on the phone; content refreshes when you save. New QR invalidates the old link. Viewers do not subscribe to TV heartbeat documents, so many phones do not raise Firebase cost.",
      // },
      {
        title: "Requirements",
        src: SHOT.banner,
        alt: "MasjidScreen mosque TV display banner",
        kind: "wide",
        text: "Android 8.0 (API 26) or newer. A landscape tablet, TV box, or monitor is best for the mosque screen. Admin works on a phone. After the first sync the display keeps clock, waqt, and countdown without internet.",
      },
    ],
    privacy: "Privacy policy",
    github: "GitHub",
    footer: "MasjidScreen · mosque.mirazanik.com",
  },
  bn: {
    langLabel: "English",
    title: "MasjidScreen",
    tagline: "মসজিদের টিভি, ট্যাবলেট আর ফোনে নামাজের সময় লাইভে দেখান।",
    subtitle:
      "পর্দায় ঘড়ি, নামাজের সময়, জামাত, হিজরি তারিখ, হাদিস আর নোটিশ থাকে। ফোন থেকেই সব সেট করা যায়।",
    getOnPlay: "ডাউনলোড করুন",
    playStore: "Google Play",
    openPlay: "প্লে স্টোরে খুলুন",
    continueLast: "আগের মসজিদের পর্দা খুলুন",
    navHow: "কীভাবে ব্যবহার করবেন",
    navDocs: "ডকুমেন্টেশন",
    carouselPrev: "আগের ছবি",
    carouselNext: "পরের ছবি",
    carouselSlides: [
      { alt: "MasjidScreen মসজিদের টিভি পর্দা", caption: "মসজিদের টিভি পর্দা" },
      {
        alt: "হিজরি ও ইংরেজি তারিখ, ঘড়ি, সূর্যের সময়, চলতি ওয়াক্ত, নামাজের তালিকা, হাদিস ও নোটিশসহ লাইভ পর্দা",
        caption: "নামাজের পর্দা",
      },
      {
        alt: "ফজরের জামাতের কাউন্টডাউন ও মোবাইল সাইলেন্ট রাখার অনুরোধ",
        caption: "জামাতের কাউন্টডাউন",
      },
      {
        alt: "সূর্যাস্তের মাকরূহ সতর্কতা — আসর এখনও আদায় করা যাবে",
        caption: "মাকরূহ সময়",
      },
      {
        alt: "কানেক্ট স্ক্রিন — কিউআর, পেয়ারিং কোড, Connect my Masjid ও Admin",
        caption: "কানেক্ট স্ক্রিন",
      },
    ],
    whatTitle: "এটি কী করে",
    whatItems: [
      {
        title: "মসজিদের টিভি",
        text: "পুরো পর্দায় ঘড়ি, পরের নামাজ, ওয়াক্ত ও জামাতের তালিকা, হাদিস আর নিচে চলমান নোটিশ।",
        src: SHOT.display,
        alt: "ঘড়ি, নামাজের তালিকা, হাদিস ও নোটিশসহ মসজিদের পর্দা",
        kind: "wide",
      },
      {
        title: "অ্যাডমিনের ফোন",
        text: "ফোনে অ্যাডমিন হিসেবে ঢুকে জামাত, হাদিস, নোটিশ, ভাষা আর মসজিদের সেটিংস বদলান। সাথে সাথে টিভিতে দেখা যায়।",
        src: SHOT.login,
        alt: "মসজিদ অ্যাডমিন সাইন-ইন স্ক্রিন",
        kind: "phone",
      },
      {
        title: "পাবলিক ভিউ",
        text: "কিউআর কোড বা লিংক দিয়ে শেয়ার করুন। মুসল্লিরা নিজের ফোনে একই পর্দা দেখতে পারবেন।",
        src: SHOT.share,
        alt: "কিউআর ও লিংকসহ পাবলিক ভিউ শেয়ার স্ক্রিন",
        kind: "phone",
      },
    ],
    howTitle: "কীভাবে ব্যবহার করবেন",
    howIntro:
      "মসজিদের টিভি আর অ্যাডমিনের ফোনে একই অ্যাপ বসান। একবার জোড়া লাগালে, ফোন থেকে যা বদলাবেন তা টিভিতে সঙ্গে সঙ্গে চলে আসবে।",
    howSteps: [
      {
        title: "১. মসজিদের পর্দা জোড়া লাগান",
        src: SHOT.connect,
        alt: "কানেক্ট স্ক্রিন — কিউআর, পেয়ারিং কোড, Connect my Masjid ও Admin",
        kind: "wide",
        steps: [
          "যে টিভি বক্স, ট্যাবলেট বা ফোন মসজিদে লাগাবেন, সেখানে গুগল প্লে থেকে MasjidScreen বসান। পর্দা আড়াআড়ি রাখুন।",
          "জোড়া না লাগানো ডিভাইসে Connect Screen আসে—বামে কিউআর কোড, পাশে Connect my Masjid, পেয়ারিং কোড, Admin আর Connect।",
          "অ্যাডমিন কিউআর কোড স্ক্যান করুন, অথবা ছয় অঙ্কের কোড লিখে Connect চাপুন।",
          "এই ডিভাইসে পাবলিক লিংক খুলতে চাইলে Connect my Masjid চাপুন, তারপর Admin → Screens → Share থেকে কিউআর স্ক্যান করুন।",
        ],
      },
      {
        title: "২. অ্যাডমিন হিসেবে ঢুকুন",
        src: SHOT.login,
        alt: "ইমেইল, পাসওয়ার্ড ও Google দিয়ে অ্যাডমিন লগইন",
        kind: "phone",
        steps: [
          "টিভিতে Admin চাপুন, অথবা ফোনে একই অ্যাপ খুলে অ্যাডমিন লগইন করুন।",
          "ইমেইল-পাসওয়ার্ড দিয়ে ঢুকুন, অথবা Continue with Google চাপুন।",
          "অ্যাকাউন্ট না থাকলে Register here। পাসওয়ার্ড ভুলে গেলে Forgot password ব্যবহার করুন।",
        ],
      },
      {
        title: "৩. স্ক্রিন জোড়া লাগান ও দেখভাল করুন",
        src: SHOT.screens,
        alt: "অনলাইন অবস্থা ও গিয়ার সেটিংসসহ অ্যাডমিন প্যানেল স্ক্রিন তালিকা",
        kind: "phone",
        steps: [
          "লগইনের পর Admin Panel → Screens খুলবে। প্রতিটি কার্ড একটা টিভি—নাম, অনলাইন/অফলাইন, ভার্সন।",
          "অবস্থা হালনাগাদ করতে Check Online চাপুন। কোনো স্ক্রিন দেখভাল করতে গিয়ার আইকনে চাপুন। নতুন স্ক্রিন যোগ করতে + চাপুন।",
          "ফোন থেকে টিভির কিউআর স্ক্যান করুন, অথবা কোড টাইপ করুন। তাতে টিভি একটি স্ক্রিনের সাথে যুক্ত হবে, যেমন মেইন হল।",
        ],
      },
      {
        title: "৪. জামাতের সময় দিন",
        src: SHOT.jamaat,
        alt: "ওয়াক্তের সময়সীমা ও সোনালি জামাত সময় বাটনসহ জামাত এডিটর",
        kind: "phone",
        steps: [
          "Jamaat ট্যাব খুলুন। প্রতি নামাজের ওয়াক্ত দেখা যায়, যেমন ফজর ৪:২১ AM – ৫:৩৯ AM।",
          "সোনালি সময়ের বাটনে চেপে জামাতের সময় দিন। চাইলে নোটও যোগ করতে পারেন।",
          "Save Jamaat Times চাপুন। টিভির তালিকা সাথে সাথে বদলে যায়।",
        ],
      },
      {
        title: "৫. হাদিস রাখুন",
        src: SHOT.hadith,
        alt: "ঘূর্ণন সেকেন্ড ও লুকান/সম্পাদনা/মুছুনসহ হাদিস স্ক্রিন",
        kind: "phone",
        steps: [
          "Hadith ট্যাব খুলুন। কত সেকেন্ড পর পর ঘুরবে সেট করুন—যেমন ৩০—তারপর Save চাপুন।",
          "নতুন হাদিস যোগ করতে + চাপুন (লেখা ও সূত্র)। চোখের আইকনে চেপে টিভি থেকে লুকান, পেন্সিল দিয়ে সম্পাদনা, ডাস্টবিন দিয়ে মুছুন।",
        ],
      },
      {
        title: "৬. নোটিশ দিন",
        src: SHOT.notices,
        alt: "ঘোষণার কার্ড ও যোগ বাটনসহ নোটিশ স্ক্রিন",
        kind: "phone",
        steps: [
          "Notices ট্যাব খুলুন। টিভির নিচে চলমান ঘোষণা যোগ করতে + চাপুন।",
          "কার্ড থেকে নোটিশ লুকান বা মুছুন। P0-এর মতো ট্যাগ দিয়ে কোনটা আগে দেখাবে সেটা ঠিক হয়।",
        ],
      },
      {
        title: "৭. ভাষা ও সেটিংস",
        src: SHOT.language,
        alt: "ডিসপ্লে ভাষা বাংলা, জামাত কাউন্টডাউন ও হিজরি তারিখ সমন্বয়সহ সেটিংস",
        kind: "phone",
        steps: [
          "Settings খুলুন। Display Language থেকে English বা বাংলা বেছে নিন। এই ভাষা টিভি আর ফোনের পাবলিক ভিউ—দুই জায়গাতেই চলে।",
          "জামাতের কতক্ষণ আগে কাউন্টডাউন দেখাবে সেট করুন, যেমন ৩ মিনিট আগে।",
          "চাঁদ দেখা আলাদা হলে হিজরি তারিখ এদিক-ওদিক করতে পারেন। কলামের চওড়া বদলাতে স্লাইডার টানুন। শেষে Save Settings চাপুন।",
        ],
      },
      {
        title: "৮. মুসল্লিদের সাথে শেয়ার করুন",
        src: SHOT.share,
        alt: "পাবলিক ভিউ চালু — কিউআর, লিংক কপি, শেয়ার, কিউআর ডাউনলোড ও নতুন কিউআর",
        kind: "phone",
        steps: [
          "স্ক্রিনের Share ট্যাব খুলুন। Enable public view চালু করুন।",
          "লিংক থাকলে যে কেউ ফোনে এই পর্দা দেখতে পারবেন। Copy link, Share link বা Download QR ব্যবহার করুন।",
          "পুরনো লিংক ফাঁস হলে New QR / link চাপুন। শেয়ার বন্ধ করতে Revoke link permanently চাপুন।",
        ],
      },
    ],
    docsTitle: "ডকুমেন্টেশন",
    docsIntro:
      "মসজিদের পর্দা আর অ্যাডমিন অ্যাপের প্রতিটি অংশ কী করে। ইংরেজিতে পড়তে উপরে English চাপুন।",
    docs: [
      {
        title: "নামাজের পর্দা",
        src: SHOT.display,
        alt: "হিজরি ও ইংরেজি তারিখ, ঘড়ি, সূর্যের সময়, চলতি ওয়াক্ত, নামাজের তালিকা, হাদিস ও নোটিশসহ লাইভ পর্দা",
        kind: "wide",
        text: "টিভিতে এই পর্দাই থাকে। উপরে হিজরি ও ইংরেজি তারিখ। বামে ঘড়ি, সূর্যোদয়, সূর্যাস্ত, সাহরি, ইফতার আর চলতি ওয়াক্ত কতক্ষণ বাকি। ডানে নামাজের তালিকা—শুরু, শেষ, জামাত। নিচে হাদিস ঘোরে আর নোটিশ চলে। শুক্রবারে জোহরের জায়গায় জুমআ দেখায়।",
      },
      {
        title: "জামাতের কাউন্টডাউন",
        src: SHOT.jamaatLive,
        alt: "ফজরের জামাতের কাউন্টডাউন ও মোবাইল সাইলেন্ট রাখার অনুরোধ",
        kind: "wide",
        text: "জামাত কাছে এলে বাম পাশে কাউন্টডাউন আসে, যেমন «ফজরের জামাত শুরু হতে বাকি …» আর মোবাইল সাইলেন্ট রাখার অনুরোধ। তালিকায় চলতি নামাজ হাইলাইট থাকে। কত মিনিট আগে দেখাবে তা Settings থেকে ঠিক করেন।",
      },
      {
        title: "মাকরূহ (নিষিদ্ধ) সময়",
        src: SHOT.makruh,
        alt: "সূর্যাস্তের মাকরূহ সতর্কতা — আসর এখনও আদায় করা যাবে",
        kind: "wide",
        text: "সূর্যোদয়, যাওয়াল আর সূর্যাস্তের সময় পর্দায় সতর্কতা আসে—এই সময়ে নামাজ পড়া যায় না। সূর্যাস্তে জানিয়ে দেয়, আজকের আসর নির্ধারিত শেষ সময় পর্যন্ত আদায় করা যাবে। ইশরাক, যাওয়াল ও মাকরূহ সময় তালিকায় ছোট করেও লেখা থাকে।",
      },
      {
        title: "কানেক্ট স্ক্রিন",
        src: SHOT.connect,
        alt: "জোড়া না লাগানো টিভির কানেক্ট স্ক্রিন",
        kind: "wide",
        text: "প্রথমবার চালু করলে (বা জোড়া খুলে দিলে) Connect Screen আসে। তিনভাবে ঢোকা যায়—অ্যাডমিন কিউআর স্ক্যান করবেন, কেউ কোড টাইপ করবেন, অথবা Connect my Masjid দিয়ে পাবলিক লিংক খুলবেন। এখানকার Admin বাটন জোড়া না লাগিয়েই লগইন করে।",
      },
      {
        title: "অ্যাডমিন লগইন",
        src: SHOT.login,
        alt: "অ্যাডমিন লগইন স্ক্রিন",
        kind: "phone",
        text: "টিভির মতোই একই অ্যাপ। ইমেইল-পাসওয়ার্ড বা গুগল দিয়ে ঢোকা যায়। শুধু লগইন করা অ্যাডমিন সময় ও নোটিশ বদলাতে পারেন। এই পর্দায় গোপনীয়তা নীতির লিংকও আছে।",
      },
      {
        title: "স্ক্রিনের তালিকা",
        src: SHOT.screens,
        alt: "জোড়া লাগানো স্ক্রিনের অ্যাডমিন প্যানেল তালিকা",
        kind: "phone",
        text: "সুপার অ্যাডমিন সব টিভি দেখেন—অনলাইন আছে কি না, শেষ কবে দেখা গেছে, অ্যাপের ভার্সন কত। গিয়ার চাপলে সেই স্ক্রিনের Jamaat, Hadith, Notices, Settings আর Share ট্যাব খোলে। Users, Groups আর Global দিয়ে অ্যাকাউন্ট ও পুরো মসজিদের সেটিংস হয়।",
      },
      {
        title: "জামাত এডিটর",
        src: SHOT.jamaat,
        alt: "জামাত সময় এডিটর",
        kind: "phone",
        text: "ওয়াক্ত অ্যাপেই হিসাব হয়—সেটিংসে দেওয়া স্থান ও নিয়ম অনুসারে, জিপিএস লাগে না। জামাতের সময় আপনি যা দেবেন তাই। চাইলে নোট টিভিতেও দেখা যায়। Save চাপলে ক্লাউডে যায়; পর্দা সাথে সাথে বদলায়, আর একবার সিঙ্ক হলে ইন্টারনেট ছাড়াও চলে।",
      },
      {
        title: "হাদিস",
        src: SHOT.hadith,
        alt: "হাদিস ম্যানেজার",
        kind: "phone",
        text: "আপনি যত সেকেন্ড ধরবেন, ততক্ষণ পর পর টিভিতে হাদিস বদলায়। মুছে না ফেলে শুধু লুকিয়ে রাখা যায়। ইংরেজি বা বাংলা যা লিখবেন, পর্দায় তাই দেখাবে।",
      },
      {
        title: "নোটিশ",
        src: SHOT.notices,
        alt: "নোটিশ ম্যানেজার",
        kind: "phone",
        text: "নোটিশ টিভির নিচে চলে। এই ট্যাব থেকে যোগ করুন, লুকান বা মুছুন। যাতে সহজে পড়া যায়, ছোট করে লিখুন।",
      },
      {
        title: "ভাষা ও লেআউট",
        src: SHOT.language,
        alt: "ভাষা, কাউন্টডাউন, হিজরি অফসেট ও লেআউটের সেটিংস",
        kind: "phone",
        text: "Display Language দিয়ে পুরো টিভি আর পাবলিক ভিউ ইংরেজি বা বাংলায় যায়—তারিখ ও সংখ্যাসহ। জামাতের কাউন্টডাউন, হিজরি কয়েকদিন এদিক-ওদিক আর কলামের চওড়া এখান থেকেই। আগের মাপে ফিরতে Reset layout to default চাপুন।",
      },
      {
        title: "পাবলিক ভিউ",
        src: SHOT.share,
        alt: "পাবলিক ভিউ কিউআর ও লিংক",
        kind: "phone",
        text: "শেয়ার লিংকটা মসজিদের আইডি নয়। ফোনে ঘড়ি ও নামাজের সময় চলতে থাকে; আপনি সেভ করলে নতুন তথ্য আসে। New QR চাপলে পুরনো লিংক আর খুলবে না। অনেকজন ফোনে খুললেও খরচ খুব একটা বাড়ে না।",
      },
      {
        title: "কী লাগবে",
        src: SHOT.banner,
        alt: "MasjidScreen মসজিদের টিভি পর্দার ব্যানার",
        kind: "wide",
        text: "অ্যান্ড্রয়েড ৮.০ বা তার নতুন লাগবে। মসজিদের পর্দার জন্য আড়াআড়ি ট্যাবলেট, টিভি বক্স বা মনিটর ভালো। অ্যাডমিন ফোনেই চলে। একবার সিঙ্ক হয়ে গেলে ইন্টারনেট না থাকলেও ঘড়ি, ওয়াক্ত আর কাউন্টডাউন চলতে থাকে।",
      },
    ],
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

function landscapeCarousel(t: Copy): string {
  const slides = LANDSCAPE_SLIDES.map((slide, index) => {
    const meta = t.carouselSlides[index] ?? { alt: "", caption: "" };
    return `
      <figure class="carousel-slide${index === 0 ? " is-active" : ""}" data-slide="${index}">
        <img src="${escapeHtml(slide.src)}" alt="${escapeHtml(meta.alt)}" draggable="false" ${index === 0 ? "" : 'loading="lazy"'} />
      </figure>`;
  }).join("");

  const dots = t.carouselSlides
    .map(
      (meta, index) =>
        `<button type="button" class="carousel-dot${index === 0 ? " is-active" : ""}" data-goto="${index}" aria-label="${escapeHtml(meta.caption)}"></button>`
    )
    .join("");

  return `
    <div class="hero-carousel" data-carousel aria-roledescription="carousel">
      <div class="carousel-frame">
        <div class="carousel-track">
          ${slides}
        </div>
        <button type="button" class="carousel-btn carousel-prev" data-carousel-prev aria-label="${escapeHtml(t.carouselPrev)}">‹</button>
        <button type="button" class="carousel-btn carousel-next" data-carousel-next aria-label="${escapeHtml(t.carouselNext)}">›</button>
      </div>
      <p class="carousel-caption" data-carousel-caption aria-live="polite">${escapeHtml(t.carouselSlides[0]?.caption ?? "")}</p>
      <div class="carousel-dots" role="tablist">${dots}</div>
    </div>
  `;
}

function bindCarousel(root: HTMLElement): () => void {
  const el = root.querySelector<HTMLElement>("[data-carousel]");
  const track = el?.querySelector<HTMLElement>(".carousel-track");
  if (!el || !track) return () => undefined;

  const slides = [...el.querySelectorAll<HTMLElement>("[data-slide]")];
  const dots = [...el.querySelectorAll<HTMLButtonElement>("[data-goto]")];
  const caption = el.querySelector<HTMLElement>("[data-carousel-caption]");
  const captions = dots.map((dot) => dot.getAttribute("aria-label") ?? "");
  const total = slides.length;
  let index = 0;
  let timer: number | null = null;
  let pointerId: number | null = null;
  let startX = 0;
  const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  const go = (next: number) => {
    index = (next + total) % total;
    track.style.transform = `translateX(-${index * 100}%)`;
    slides.forEach((slide, i) => slide.classList.toggle("is-active", i === index));
    dots.forEach((dot, i) => {
      dot.classList.toggle("is-active", i === index);
      dot.setAttribute("aria-selected", i === index ? "true" : "false");
    });
    if (caption) caption.textContent = captions[index] ?? "";
  };

  const stop = () => {
    if (timer != null) window.clearInterval(timer);
    timer = null;
  };

  const play = () => {
    stop();
    if (reduceMotion || total < 2) return;
    timer = window.setInterval(() => go(index + 1), 5000);
  };

  const onPrev = () => {
    go(index - 1);
    play();
  };
  const onNext = () => {
    go(index + 1);
    play();
  };

  el.querySelector("[data-carousel-prev]")?.addEventListener("click", onPrev);
  el.querySelector("[data-carousel-next]")?.addEventListener("click", onNext);
  dots.forEach((dot) => {
    dot.addEventListener("click", () => {
      go(Number(dot.dataset.goto));
      play();
    });
  });

  const onPointerDown = (event: PointerEvent) => {
    pointerId = event.pointerId;
    startX = event.clientX;
    track.setPointerCapture(event.pointerId);
    stop();
  };
  const onPointerUp = (event: PointerEvent) => {
    if (pointerId !== event.pointerId) return;
    pointerId = null;
    const delta = event.clientX - startX;
    if (delta > 40) go(index - 1);
    else if (delta < -40) go(index + 1);
    play();
  };

  track.addEventListener("pointerdown", onPointerDown);
  track.addEventListener("pointerup", onPointerUp);
  track.addEventListener("pointercancel", onPointerUp);
  el.addEventListener("mouseenter", stop);
  el.addEventListener("mouseleave", play);
  el.addEventListener("focusin", stop);
  el.addEventListener("focusout", play);

  const onVisibility = () => {
    if (document.hidden) stop();
    else play();
  };
  document.addEventListener("visibilitychange", onVisibility);

  play();

  return () => {
    stop();
    document.removeEventListener("visibilitychange", onVisibility);
  };
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

    ${landscapeCarousel(t)}

    

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
  let stopCarousel: (() => void) | null = null;

  const paint = () => {
    const y = window.scrollY;
    stopCarousel?.();
    root.innerHTML = `<div class="landing">${renderBody(lang, options.lastSharePath ?? null)}</div>`;
    document.documentElement.lang = lang;
    document.documentElement.dir = "ltr";
    stopCarousel = bindCarousel(root);
    root.querySelector("#langToggle")?.addEventListener("click", () => {
      lang = lang === "en" ? "bn" : "en";
      localStorage.setItem("ms-landing-lang", lang);
      paint();
    });
    window.scrollTo(0, y);
  };

  paint();
}
