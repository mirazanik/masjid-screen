package com.mirazanik.masjidscreen.data.model

data class MosqueConfig(
    val id: String = "default",
    val name: String = "Masjid",
    val address: String = "",
    val latitude: Double = 23.777176,
    val longitude: Double = 90.399452,
    val calculationMethod: String = "MWL",
    val madhab: String = "HANAFI",
    val language: String = "en",
    val hadithInterval: Int = 30,
    val jamaatCountdownMins: Int = 3,
    val hijriDateOffset: Int = 0,
    val tableFontScale: Float = 1f,
    val activeTheme: String = "night_navy"
)

data class JamaatTimes(
    val fajr: String = "05:45",
    val dhuhr: String = "13:15",
    val asr: String = "17:30",
    val maghrib: String = "auto",
    val isha: String = "21:45",
    val fajrNote: String = "",
    val dhuhrNote: String = "",
    val asrNote: String = "",
    val maghribNote: String = "",
    val ishaNote: String = ""
)

data class Hadith(
    val id: String = "",
    val translation: String = "",
    val source: String = "",
    val narrator: String = "",
    val active: Boolean = true
)

data class Notice(
    val id: String = "",
    val text: String = "",
    val active: Boolean = true,
    val priority: Int = 0
)

data class PrayerTimesData(
    val fajr: String = "--:--",
    val sunrise: String = "--:--",
    val dhuhr: String = "--:--",
    val asr: String = "--:--",
    val maghrib: String = "--:--",
    val isha: String = "--:--"
)

data class HijriDate(
    val day: Int = 1,
    val monthIndex: Int = 0,
    val year: Int = 1446
) {
    val monthNameArabic: String get() = arabicMonths.getOrElse(monthIndex) { "" }
    val monthNameEnglish: String get() = englishMonths.getOrElse(monthIndex) { "" }
    val monthNameBangla: String get() = banglaMonths.getOrElse(monthIndex) { "" }

    companion object {
        val arabicMonths = listOf(
            "مُحَرَّم", "صَفَر", "رَبِيع الأوَّل", "رَبِيع الثَّانِي",
            "جُمَادَى الأُولَى", "جُمَادَى الآخِرَة", "رَجَب", "شَعْبَان",
            "رَمَضَان", "شَوَّال", "ذُو القَعْدَة", "ذُو الحِجَّة"
        )
        val englishMonths = listOf(
            "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
            "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
            "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
        )
        val banglaMonths = listOf(
            "মুহাররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি",
            "জুমাদাল আউয়াল", "জুমাদাস সানি", "রজব", "শাবান",
            "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
        )
    }
}

data class ScreenInfo(
    val screenId: String = "",
    val name: String = "",
    val ownerUserId: String = "",
    val pairingCode: String = "",
    val pairedDeviceId: String = "",
    val lastSeenMs: Long = 0L,
    val appVersion: String = "",
    val displayWidthDp: Float = 0f,
    val displayHeightDp: Float = 0f,
    val displayWidthPx: Int = 0,
    val displayHeightPx: Int = 0,
    val displayDensity: Float = 0f,
    val displayFontScale: Float = 0f,
    /** Unguessable public viewer token; empty when sharing is off. */
    val shareToken: String = "",
    val shareEnabled: Boolean = false,
) {
    val isOnline: Boolean get() = lastSeenMs > 0L &&
        (System.currentTimeMillis() - lastSeenMs) in 0L until 5 * 60_000L
    val isPaired: Boolean get() = pairedDeviceId.isNotBlank()
    val hasPublicShare: Boolean get() = shareEnabled && shareToken.isNotBlank()
}

data class ScreenLayout(
    val headerScale: Float = DEFAULT_HEADER,
    val clockScale: Float = DEFAULT_CLOCK,
    val sunGridScale: Float = DEFAULT_SUN,
    val clockWidthFraction: Float = DEFAULT_CLOCK_WIDTH,
    val mainRowWeight: Float = DEFAULT_MAIN_ROW,
    val noticeHeightFraction: Float = DEFAULT_NOTICE,
    val jamaatColPrayer: Float = DEFAULT_COL_PRAYER,
    val jamaatColWaqtStart: Float = DEFAULT_COL_WAQT_START,
    val jamaatColWaqtEnd: Float = DEFAULT_COL_WAQT_END,
    val jamaatColJamaat: Float = DEFAULT_COL_JAMAAT,
) {
    fun jamaatCols(): FloatArray {
        val raw = floatArrayOf(jamaatColPrayer, jamaatColWaqtStart, jamaatColWaqtEnd, jamaatColJamaat)
        val sum = raw.sum()
        if (sum <= 0f) {
            return floatArrayOf(DEFAULT_COL_PRAYER, DEFAULT_COL_WAQT_START, DEFAULT_COL_WAQT_END, DEFAULT_COL_JAMAAT)
        }
        return FloatArray(4) { (raw[it] / sum).coerceAtLeast(0.01f) }.let { normalized ->
            val nSum = normalized.sum()
            FloatArray(4) { normalized[it] / nSum }
        }
    }

    fun withJamaatCols(cols: FloatArray) = copy(
        jamaatColPrayer = cols[0],
        jamaatColWaqtStart = cols[1],
        jamaatColWaqtEnd = cols[2],
        jamaatColJamaat = cols[3],
    )

    fun adjustJamaatSplit(splitIndex: Int, delta: Float): ScreenLayout {
        val cols = jamaatCols()
        val a = splitIndex.coerceIn(0, 2)
        val b = a + 1
        val pair = cols[a] + cols[b]
        val min = MIN_JAMAAT_COL.coerceAtMost(pair / 2f)
        cols[a] = (cols[a] + delta).coerceIn(min, pair - min)
        cols[b] = pair - cols[a]
        return withJamaatCols(cols)
    }

    fun setJamaatCol(index: Int, value: Float): ScreenLayout {
        val cols = jamaatCols()
        val i = index.coerceIn(0, 3)
        val others = (0..3).filter { it != i }
        val otherSum = others.sumOf { cols[it].toDouble() }.toFloat().coerceAtLeast(0.001f)
        val newVal = value.coerceIn(MIN_JAMAAT_COL, 1f - MIN_JAMAAT_COL * 3)
        val remain = (1f - newVal).coerceAtLeast(MIN_JAMAAT_COL * 3)
        cols[i] = newVal
        others.forEach { cols[it] = (cols[it] / otherSum * remain).coerceAtLeast(MIN_JAMAAT_COL) }
        val sum = cols.sum()
        return withJamaatCols(FloatArray(4) { cols[it] / sum })
    }

    fun coerced(): ScreenLayout {
        val cols = jamaatCols()
        return copy(
            headerScale = headerScale.coerceIn(MIN_SCALE, MAX_SCALE),
            clockScale = clockScale.coerceIn(MIN_SCALE, MAX_SCALE),
            sunGridScale = sunGridScale.coerceIn(MIN_SCALE, MAX_SCALE),
            clockWidthFraction = clockWidthFraction.coerceIn(MIN_CLOCK_WIDTH, MAX_CLOCK_WIDTH),
            mainRowWeight = mainRowWeight.coerceIn(MIN_MAIN_ROW, MAX_MAIN_ROW),
            noticeHeightFraction = noticeHeightFraction.coerceIn(MIN_NOTICE, MAX_NOTICE),
            jamaatColPrayer = cols[0],
            jamaatColWaqtStart = cols[1],
            jamaatColWaqtEnd = cols[2],
            jamaatColJamaat = cols[3],
        )
    }

    fun toMap(): Map<String, Any> = mapOf(
        "headerScale" to headerScale,
        "clockScale" to clockScale,
        "sunGridScale" to sunGridScale,
        "clockWidthFraction" to clockWidthFraction,
        "mainRowWeight" to mainRowWeight,
        "noticeHeightFraction" to noticeHeightFraction,
        "jamaatColPrayer" to jamaatColPrayer,
        "jamaatColWaqtStart" to jamaatColWaqtStart,
        "jamaatColWaqtEnd" to jamaatColWaqtEnd,
        "jamaatColJamaat" to jamaatColJamaat,
    )

    companion object {
        const val DEFAULT_HEADER = 1f
        const val DEFAULT_CLOCK = 1f
        const val DEFAULT_SUN = 1f
        const val DEFAULT_CLOCK_WIDTH = 0.40f
        const val DEFAULT_MAIN_ROW = 0.75f
        const val DEFAULT_NOTICE = 0.20f
        const val DEFAULT_COL_PRAYER = 0.20f
        const val DEFAULT_COL_WAQT_START = 0.25f
        const val DEFAULT_COL_WAQT_END = 0.25f
        const val DEFAULT_COL_JAMAAT = 0.30f

        const val MIN_SCALE = 0.6f
        const val MAX_SCALE = 1.8f
        const val MIN_CLOCK_WIDTH = 0.28f
        const val MAX_CLOCK_WIDTH = 0.55f
        const val MIN_MAIN_ROW = 0.50f
        const val MAX_MAIN_ROW = 0.88f
        const val MIN_NOTICE = 0.12f
        const val MAX_NOTICE = 0.45f
        const val MIN_JAMAAT_COL = 0.12f
        const val MAX_JAMAAT_COL = 0.50f

        fun fromMap(d: Map<String, Any>): ScreenLayout = ScreenLayout(
            headerScale = (d["headerScale"] as? Number)?.toFloat()?.takeIf { it > 0f } ?: DEFAULT_HEADER,
            clockScale = (d["clockScale"] as? Number)?.toFloat()?.takeIf { it > 0f } ?: DEFAULT_CLOCK,
            sunGridScale = (d["sunGridScale"] as? Number)?.toFloat()?.takeIf { it > 0f } ?: DEFAULT_SUN,
            clockWidthFraction = (d["clockWidthFraction"] as? Number)?.toFloat()?.takeIf { it > 0f } ?: DEFAULT_CLOCK_WIDTH,
            mainRowWeight = (d["mainRowWeight"] as? Number)?.toFloat()?.takeIf { it > 0f } ?: DEFAULT_MAIN_ROW,
            noticeHeightFraction = (d["noticeHeightFraction"] as? Number)?.toFloat()?.takeIf { it > 0f } ?: DEFAULT_NOTICE,
            jamaatColPrayer = (d["jamaatColPrayer"] as? Number)?.toFloat()?.takeIf { it > 0f } ?: DEFAULT_COL_PRAYER,
            jamaatColWaqtStart = (d["jamaatColWaqtStart"] as? Number)?.toFloat()?.takeIf { it > 0f } ?: DEFAULT_COL_WAQT_START,
            jamaatColWaqtEnd = (d["jamaatColWaqtEnd"] as? Number)?.toFloat()?.takeIf { it > 0f } ?: DEFAULT_COL_WAQT_END,
            jamaatColJamaat = (d["jamaatColJamaat"] as? Number)?.toFloat()?.takeIf { it > 0f } ?: DEFAULT_COL_JAMAAT,
        ).coerced()
    }
}

data class ScreenConfig(
    val screenId: String = "",
    val activeTheme: String = "night_navy",
    val language: String = "en",
    val tableFontScale: Float = 1f,
    val hadithInterval: Int = 30,
    val jamaatCountdownMins: Int = 3,
    val hijriDateOffset: Int = 0,
    val mosqueName: String = "",
    val mosqueAddress: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val calculationMethod: String = "",
    val madhab: String = "",
    val layout: ScreenLayout = ScreenLayout(),
)

enum class UserRole { SUPER_ADMIN, ADMIN }
enum class UserStatus { PENDING, ACTIVE, DISABLED }

data class AppSettings(
    val autoApprovalEnabled: Boolean = false
)

data class PendingUserNotification(
    val id: String = "",
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val createdAt: Long = 0L,
    val read: Boolean = false
)

data class MosqueUser(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.ADMIN,
    val status: UserStatus = UserStatus.PENDING,
    val createdAt: Long = 0L,
    val invitedByUserId: String = ""
)

data class ScreenGroup(
    val groupId: String = "",
    val name: String = "",
    val screenIds: List<String> = emptyList(),
    val memberUserIds: List<String> = emptyList(),
    val ownerUserId: String = ""
)

enum class Prayer(val displayName: String, val arabicName: String, val banglaName: String) {
    FAJR("Fajr", "الفجر", "ফজর"),
    SUNRISE("Sunrise", "الشروق", "সূর্যোদয়"),
    DHUHR("Dhuhr", "الظهر", "জোহর"),
    ASR("Asr", "العصر", "আসর"),
    MAGHRIB("Maghrib", "المغرب", "মাগরিব"),
    ISHA("Isha", "العشاء", "ইশা")
}
