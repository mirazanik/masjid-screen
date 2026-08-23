package com.mirazanik.masjidscreen.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import com.mirazanik.masjidscreen.data.model.HijriDate
import com.mirazanik.masjidscreen.data.model.ScreenLayout
import com.mirazanik.masjidscreen.ui.component.DisplayEditActions
import com.mirazanik.masjidscreen.ui.component.EditBadge
import com.mirazanik.masjidscreen.ui.component.EditableRegion
import com.mirazanik.masjidscreen.ui.component.EmptyEditPlaceholder
import com.mirazanik.masjidscreen.ui.component.HadithCard
import com.mirazanik.masjidscreen.ui.component.NoticeTicker
import com.mirazanik.masjidscreen.ui.component.PrayerTimesTable
import com.mirazanik.masjidscreen.ui.component.SplitHandle
import com.mirazanik.masjidscreen.ui.component.splitHandleHitSize
import com.mirazanik.masjidscreen.ui.theme.LocalMosqueColors
import com.mirazanik.masjidscreen.util.BanglaUtils
import com.mirazanik.masjidscreen.viewmodel.DisplayState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.LocalDining
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import com.mirazanik.masjidscreen.data.model.JamaatTimes
import com.mirazanik.masjidscreen.data.model.Prayer
import com.mirazanik.masjidscreen.data.model.PrayerTimesData
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity

private data class ProhibitedInfo(
    val labelEn: String,
    val labelBn: String,
    val endsAtMins: Int,
    val noteEn: String = "",
    val noteBn: String = ""
)

private data class JamaatCountdownInfo(
    val secsLeft: Long,
    val prayer: Prayer
)

@Composable
fun DisplayScreen(
    state: DisplayState,
    onEnterAdmin: () -> Unit = {},
    editActions: DisplayEditActions? = null,
    onViewportMeasured: ((widthPx: Int, heightPx: Int, density: Float, fontScale: Float) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isFriday = state.currentDate.dayOfWeek == DayOfWeek.FRIDAY
    val c = LocalMosqueColors.current
    val editing = editActions != null
    val layout = state.layout.coerced()
    val canResize = editActions?.onLayoutChange != null
    val currentLayout = rememberUpdatedState(layout)
    val onLayoutChange = rememberUpdatedState(editActions?.onLayoutChange)
    val onLayoutCommit = rememberUpdatedState(editActions?.onLayoutCommit)

    fun changeLayout(block: ScreenLayout.() -> ScreenLayout) {
        onLayoutChange.value?.invoke(currentLayout.value.block().coerced())
    }
    fun commitLayout() {
        onLayoutCommit.value?.invoke(currentLayout.value.coerced())
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(c.screenTop, c.backgroundDeep, c.screenBottom)))
    ) {
        val screenW = maxWidth
        val screenWpx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val handleHalf = splitHandleHitSize() / 2
        val scaleDragPx = with(LocalDensity.current) { 80.dp.toPx() }.coerceAtLeast(1f)
        val density = LocalDensity.current
        val viewportWpx = constraints.maxWidth
        val viewportHpx = constraints.maxHeight
        LaunchedEffect(viewportWpx, viewportHpx, density.density, density.fontScale, onViewportMeasured) {
            if (onViewportMeasured != null && viewportWpx > 80 && viewportHpx > 80) {
                onViewportMeasured(viewportWpx, viewportHpx, density.density, density.fontScale)
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Box {
                EditableRegion(
                    enabled = editing,
                    label = "Settings",
                    onEdit = { editActions?.onHeaderClick?.invoke() }
                ) {
                    DisplayHeader(
                        mosqueName = state.mosqueConfig.name,
                        currentDate = state.currentDate,
                        hijriDate = state.hijriDate,
                        language = state.mosqueConfig.language,
                        isNetworkAvailable = state.isNetworkAvailable,
                        onAdminClick = if (editing) ({}) else onEnterAdmin,
                        scale = layout.headerScale
                    )
                }
                if (canResize) {
                    SplitHandle(
                        verticalBar = false,
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onDeltaPx = { dy -> changeLayout { copy(headerScale = headerScale + dy / scaleDragPx) } },
                        onCommit = { commitLayout() }
                    )
                }
            }

            HorizontalDivider(color = c.primary.copy(alpha = 0.4f), thickness = 1.dp)

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val bodyH = maxHeight
                val bodyHpx = constraints.maxHeight.toFloat().coerceAtLeast(1f)
                val mainW = layout.mainRowWeight
                val bottomW = (1f - mainW).coerceAtLeast(0.12f)
                val lang = state.mosqueConfig.language
                val currentHadith = state.hadiths.getOrNull(state.currentHadithIndex)
                val showHadith = currentHadith != null || editing
                val showNotice = state.notices.isNotEmpty() || editing

                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(mainW)
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                ClockPanel(
                                    currentTime = state.currentTime,
                                    currentPrayerName = state.currentPrayerName,
                                    currentPrayerMinsLeft = state.currentPrayerMinsLeft,
                                    language = lang,
                                    isFriday = isFriday,
                                    prayerTimes = state.prayerTimes,
                                    jamaatTimes = state.jamaatTimes,
                                    jamaatCountdownMins = state.mosqueConfig.jamaatCountdownMins,
                                    clockScale = layout.clockScale,
                                    sunGridScale = layout.sunGridScale,
                                    resizeEnabled = canResize,
                                    onClockScaleChange = { scale -> changeLayout { copy(clockScale = scale) } },
                                    onClockScaleCommit = { commitLayout() },
                                    onSunGridScaleChange = { scale -> changeLayout { copy(sunGridScale = scale) } },
                                    onSunGridScaleCommit = { commitLayout() },
                                    modifier = Modifier
                                        .width(screenW * layout.clockWidthFraction)
                                        .fillMaxHeight()
                                )

                                VerticalDivider(color = c.divider, thickness = 1.dp)

                                EditableRegion(
                                    enabled = editing,
                                    label = "Jamaat",
                                    onEdit = { editActions?.onJamaatClick?.invoke() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    PrayerTimesTable(
                                        prayerTimes = state.prayerTimes,
                                        jamaatTimes = state.jamaatTimes,
                                        nextPrayerName = state.nextPrayerName,
                                        currentPrayerName = state.currentPrayerName,
                                        language = lang,
                                        isFriday = isFriday,
                                        fontScale = state.tableFontScale,
                                        prayerColWeight = layout.jamaatColPrayer,
                                        waqtStartColWeight = layout.jamaatColWaqtStart,
                                        waqtEndColWeight = layout.jamaatColWaqtEnd,
                                        jamaatColWeight = layout.jamaatColJamaat,
                                        resizeColumns = canResize,
                                        onColumnSplitDelta = { split, delta ->
                                            changeLayout { adjustJamaatSplit(split, delta) }
                                        },
                                        onColumnSplitCommit = { commitLayout() },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            if (canResize) {
                                SplitHandle(
                                    verticalBar = true,
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .offset(x = screenW * layout.clockWidthFraction - handleHalf),
                                    onDeltaPx = { dx ->
                                        changeLayout { copy(clockWidthFraction = clockWidthFraction + dx / screenWpx) }
                                    },
                                    onCommit = { commitLayout() }
                                )
                            }
                        }

                        HorizontalDivider(color = c.divider, thickness = 1.dp)

                        Box(
                            modifier = Modifier
                                .weight(bottomW)
                                .fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            ) {
                                val cardRadius = 16.dp
                                val hadithShape = if (showNotice) {
                                    RoundedCornerShape(topStart = cardRadius, topEnd = cardRadius)
                                } else {
                                    RoundedCornerShape(cardRadius)
                                }
                                val noticeShape = if (showHadith) {
                                    RoundedCornerShape(bottomStart = cardRadius, bottomEnd = cardRadius)
                                } else {
                                    RoundedCornerShape(cardRadius)
                                }
                                if (showHadith) {
                                    EditableRegion(
                                        enabled = editing,
                                        label = "Hadith",
                                        onEdit = { editActions?.onHadithClick?.invoke() },
                                        shape = hadithShape,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(if (showNotice) (1f - layout.noticeHeightFraction).coerceAtLeast(0.35f) else 1f)
                                    ) {
                                        if (currentHadith != null) {
                                            HadithCard(
                                                hadith = currentHadith,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            EmptyEditPlaceholder(
                                                text = "Tap to add Hadith",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }

                                if (showNotice) {
                                    EditableRegion(
                                        enabled = editing,
                                        label = "Notice",
                                        onEdit = { editActions?.onNoticeClick?.invoke() },
                                        shape = noticeShape,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(if (showHadith) layout.noticeHeightFraction else 1f)
                                    ) {
                                        if (state.notices.isNotEmpty()) {
                                            NoticeTicker(
                                                notices = state.notices,
                                                language = state.mosqueConfig.language,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            EmptyEditPlaceholder(
                                                text = "Tap to add Notice",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }
                            if (canResize && showHadith && showNotice) {
                                SplitHandle(
                                    verticalBar = false,
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .offset(y = bodyH * bottomW * (1f - layout.noticeHeightFraction) - handleHalf),
                                    onDeltaPx = { dy ->
                                        val bottomPx = (bodyHpx * bottomW).coerceAtLeast(1f)
                                        changeLayout {
                                            copy(noticeHeightFraction = noticeHeightFraction - dy / bottomPx)
                                        }
                                    },
                                    onCommit = { commitLayout() }
                                )
                            }
                        }
                    }
                    if (canResize) {
                        SplitHandle(
                            verticalBar = false,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = bodyH * mainW - handleHalf),
                            onDeltaPx = { dy ->
                                changeLayout { copy(mainRowWeight = mainRowWeight + dy / bodyHpx) }
                            },
                            onCommit = { commitLayout() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DisplayHeader(
    mosqueName: String,
    currentDate: LocalDate,
    hijriDate: HijriDate,
    language: String,
    isNetworkAvailable: Boolean = true,
    onAdminClick: () -> Unit = {},
    scale: Float = 1f,
) {
    val c = LocalMosqueColors.current
    val isBangla = language == "bn"

    val gregorianDate = if (isBangla) {
        val dayName = BanglaUtils.weekDays[currentDate.dayOfWeek.name] ?: currentDate.dayOfWeek.name
        val day = BanglaUtils.toBangla(currentDate.dayOfMonth.toString())
        val month = BanglaUtils.gregorianMonths.getOrElse(currentDate.monthValue - 1) { "" }
        val year = BanglaUtils.toBangla(currentDate.year.toString())
        "$dayName, $day $month $year"
    } else {
        currentDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH))
    }

    val hijriLine = if (isBangla) {
        "${BanglaUtils.toBangla(hijriDate.day.toString())} ${hijriDate.monthNameBangla} ${BanglaUtils.toBangla(hijriDate.year.toString())}"
    } else {
        "${hijriDate.day} ${hijriDate.monthNameEnglish} ${hijriDate.year} AH"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(c.headerTop, c.backgroundCard)))
            .padding(horizontal = (20 * scale).dp, vertical = (10 * scale).dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1f)) {
            Text(text = hijriLine, color = c.primaryLight, fontSize = (15 * scale).sp, fontWeight = FontWeight.Medium)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.2f)) {
            Text(
                text = mosqueName,
                color = c.primary,
                fontSize = (24 * scale).sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                letterSpacing = (2 * scale).sp
            )
        }

        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (isNetworkAvailable) c.networkOnline else Color(0xFFE53935))
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (isNetworkAvailable) "" else "",
                    color = if (isNetworkAvailable) c.networkOnline else Color(0xFFE53935),
                    fontSize = 10.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(text = gregorianDate, color = c.textPrimary, fontSize = (13 * scale).sp, textAlign = TextAlign.End)
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onAdminClick),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(c.textPrimary.copy(alpha = 0.25f))
                    )
                }
            }
        }
    }
}

@Composable
private fun ClockPanel(
    currentTime: LocalTime,
    currentPrayerName: String,
    currentPrayerMinsLeft: Long,
    language: String,
    isFriday: Boolean,
    prayerTimes: PrayerTimesData,
    jamaatTimes: JamaatTimes = JamaatTimes(),
    jamaatCountdownMins: Int = 3,
    clockScale: Float = 1f,
    sunGridScale: Float = 1f,
    resizeEnabled: Boolean = false,
    onClockScaleChange: ((Float) -> Unit)? = null,
    onClockScaleCommit: (() -> Unit)? = null,
    onSunGridScaleChange: ((Float) -> Unit)? = null,
    onSunGridScaleCommit: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val c = LocalMosqueColors.current
    val isBangla = language == "bn"
    val scaleDragPx = with(LocalDensity.current) { 80.dp.toPx() }.coerceAtLeast(1f)

    val amPm = if (currentTime.hour < 12) "AM" else "PM"
    val hour12 = if (currentTime.hour % 12 == 0) 12 else currentTime.hour % 12
    val minuteStr = currentTime.format(DateTimeFormatter.ofPattern("mm"))
        .let { if (isBangla) BanglaUtils.toBangla(it) else it }
    val hour12Str = if (isBangla) BanglaUtils.toBangla(hour12.toString()) else hour12.toString()

    val displayCurrentName = when {
        isFriday && currentPrayerName == "Dhuhr" -> if (isBangla) "জুম'আ" else "Jumu'ah"
        isBangla -> com.mirazanik.masjidscreen.data.model.Prayer.entries
            .firstOrNull { it.displayName == currentPrayerName }?.banglaName ?: currentPrayerName
        else -> currentPrayerName
    }

    val hours = currentPrayerMinsLeft / 60
    val mins = currentPrayerMinsLeft % 60
    val remainingStr = if (isBangla) {
        if (hours > 0) "${BanglaUtils.toBangla(hours)} ঘণ্টা ${BanglaUtils.toBangla(mins)} মিনিট"
        else "${BanglaUtils.toBangla(mins)} মিনিট বাকি"
    } else {
        if (hours > 0) "${hours} hour ${mins} minutes left" else "${mins} minutes left"
    }
    val currentLabel = if (isBangla) "শেষ হতে বাকি" else "Waqt ends in"

    val nowMins = currentTime.hour * 60 + currentTime.minute
    val sunriseMins = parseTimeMins(prayerTimes.sunrise)
    val ishraqMins = if (sunriseMins >= 0) sunriseMins + 20 else -1
    val dhuhrMins = parseTimeMins(prayerTimes.dhuhr)
    val zawalMins = if (dhuhrMins >= 0) dhuhrMins - 5 else -1
    val maghribMins = parseTimeMins(prayerTimes.maghrib)
    val makroohMins = if (maghribMins >= 0) maghribMins - 15 else -1

    val prohibitedInfo: ProhibitedInfo? = when {
        sunriseMins >= 0 && nowMins in sunriseMins until ishraqMins ->
            ProhibitedInfo("Makrooh (Sunrise)", "মাকরূহ (সূর্যোদয়)", ishraqMins)
        zawalMins >= 0 && nowMins in zawalMins until dhuhrMins ->
            ProhibitedInfo("Makrooh (Zawal)", "মাকরূহ (যাওয়াল)", dhuhrMins)
        makroohMins >= 0 && nowMins in makroohMins until maghribMins ->
            ProhibitedInfo(
                labelEn = "Makrooh (Sunset)",
                labelBn = "মাকরূহ (সূর্যাস্ত)",
                endsAtMins = maghribMins,
                noteEn = "Today's Asr prayer may still be performed",
                noteBn = "আজকের আসরের নামাজ আদায় করা যাবে"
            )
        else -> null
    }

    val jamaatCountdownInfo: JamaatCountdownInfo? = run {
        val pairs = listOf(
            Triple(Prayer.FAJR, jamaatTimes.fajr, prayerTimes.fajr),
            Triple(Prayer.DHUHR, jamaatTimes.dhuhr, prayerTimes.dhuhr),
            Triple(Prayer.ASR, jamaatTimes.asr, prayerTimes.asr),
            Triple(Prayer.MAGHRIB, jamaatTimes.maghrib, prayerTimes.maghrib),
            Triple(Prayer.ISHA, jamaatTimes.isha, prayerTimes.isha)
        )
        val nowSecs = currentTime.hour * 3600L + currentTime.minute * 60 + currentTime.second
        pairs
            .mapNotNull { (prayer, jamaat, prayerTime) ->
                val timeStr = if (jamaat.trim().equals("auto", ignoreCase = true)) prayerTime else jamaat
                val tMins = parseTimeMins(timeStr)
                if (tMins < 0) null else JamaatCountdownInfo(tMins * 60L - nowSecs, prayer)
            }
            .filter { it.secsLeft in 0L..(jamaatCountdownMins * 60L) }
            .minByOrNull { it.secsLeft }
    }

    BoxWithConstraints(
        modifier = modifier.background(
            Brush.verticalGradient(listOf(c.clockTop, c.clockMid, c.backgroundDeep))
        )
    ) {
        val h = maxHeight.value
        val w = maxWidth.value.takeIf { it.isFinite() && it > 0f } ?: (h * 0.55f)
        val usableW = (w - 16f).coerceAtLeast(80f)
        val scale = (h / 300f).coerceIn(0.8f, 3.0f)
        val sectionGap = (h * 0.008f).coerceIn(1f, 4f).dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight((0.32f * clockScale).coerceIn(0.14f, 0.55f))
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, c.primary.copy(alpha = 0.13f), RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(c.clockBoxBg),
                    contentAlignment = Alignment.Center
                ) {
                    val boxH = maxHeight.value
                    val boxW = maxWidth.value.takeIf { it.isFinite() && it > 0f } ?: usableW
                    val isShort = boxH < 56f
                    val lineH = if (isShort) 1f else 1.05f
                    val padV = (boxH * 0.06f).coerceIn(1f, 6f)
                    val clockFontSize = minOf(boxH * 0.70f, boxW * 0.28f).coerceIn(22f, 110f)
                    val amPmFontSize = (clockFontSize * 0.62f).coerceIn(12f, 56f)
                    val timeText = buildAnnotatedString {
                        withStyle(SpanStyle(fontSize = clockFontSize.sp, fontWeight = FontWeight.Thin, letterSpacing = (-1).sp)) {
                            append("$hour12Str:$minuteStr")
                        }
                        withStyle(SpanStyle(fontSize = amPmFontSize.sp, fontWeight = FontWeight.Thin, color = c.textPrimary.copy(alpha = 0.75f))) {
                            append(" $amPm")
                        }
                    }
                    Text(
                        text = timeText,
                        color = c.textPrimary,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center,
                        lineHeight = (clockFontSize * lineH).sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = padV.dp)
                    )
                }
                if (resizeEnabled) {
                    EditBadge(
                        label = "Time",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    )
                    SplitHandle(
                        verticalBar = false,
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onDeltaPx = { dy -> onClockScaleChange?.invoke(clockScale + dy / scaleDragPx) },
                        onCommit = { onClockScaleCommit?.invoke() }
                    )
                }
            }

            Spacer(Modifier.height(sectionGap))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight((0.24f * sunGridScale).coerceIn(0.10f, 0.45f))
            ) {
                SunSahriIftarGrid(
                    sunriseTime = prayerTimes.sunrise,
                    sunsetTime = prayerTimes.maghrib,
                    fajrTime = prayerTimes.fajr,
                    isBangla = isBangla,
                    modifier = Modifier.fillMaxSize()
                )
                if (resizeEnabled) {
                    EditBadge(
                        label = "Sunrise",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    )
                    SplitHandle(
                        verticalBar = false,
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onDeltaPx = { dy -> onSunGridScaleChange?.invoke(sunGridScale + dy / scaleDragPx) },
                        onCommit = { onSunGridScaleCommit?.invoke() }
                    )
                }
            }

            Spacer(Modifier.height(sectionGap))
            HorizontalDivider(color = c.divider, thickness = 1.dp)

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (prohibitedInfo != null) {
                    ProhibitedAlertBanner(prohibitedInfo, isBangla, scale)
                } else if (jamaatCountdownInfo != null) {
                    JamaatCountdownBanner(jamaatCountdownInfo, isFriday, isBangla, scale)
                } else if (currentPrayerName.isNotBlank()) {
                    val statusHeight = maxHeight.value
                    val compactScale = (statusHeight / 180f).coerceIn(0.55f, 1.5f)
                    val isShort = statusHeight < 120f
                    val lineH = if (isShort) 1f else 1.1f
                    val padV = (statusHeight * 0.04f).coerceIn(1f, 8f)
                    val prayerNameFontSize = (statusHeight * 0.28f).coerceIn(16f, 72f)
                    val statusLabelFontSize = (statusHeight * 0.11f).coerceIn(10f, 24f)
                    val remainFontSizePx = (statusHeight * 0.16f).coerceIn(12f, 40f)
                    val badgePadV = if (isShort) 1.dp else (statusHeight * 0.02f).coerceIn(2f, 5f).dp

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = padV.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = (6 * compactScale).coerceIn(4f, 10f).dp)
                                .border(1.dp, c.highlightNext.copy(alpha = 0.35f), RoundedCornerShape(50.dp))
                                .clip(RoundedCornerShape(50.dp))
                                .background(c.highlightNext.copy(alpha = 0.10f))
                                .padding(
                                    horizontal = (8 * compactScale).coerceIn(6f, 12f).dp,
                                    vertical = badgePadV
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayCurrentName,
                                color = c.highlightNext,
                                fontSize = prayerNameFontSize.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = (prayerNameFontSize * lineH).sp,
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Text(
                            text = currentLabel,
                            color = c.textDim,
                            fontSize = statusLabelFontSize.sp,
                            letterSpacing = 1.sp,
                            lineHeight = (statusLabelFontSize * lineH).sp,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = remainingStr,
                            color = c.primaryLight,
                            fontSize = remainFontSizePx.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = (remainFontSizePx * lineH).sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private fun parseTimeMins(time: String): Int {
    return try {
        val parts = time.split(":")
        parts[0].trim().toInt() * 60 + parts[1].trim().toInt()
    } catch (e: Exception) { -1 }
}

@Composable
private fun ProhibitedAlertBanner(info: ProhibitedInfo, isBangla: Boolean, scale: Float = 1f) {
    val c = LocalMosqueColors.current
    val label = if (isBangla) info.labelBn else info.labelEn
    val endsTimeStr = "%02d:%02d".format(info.endsAtMins / 60, info.endsAtMins % 60)
    val endsDisplay = BanglaUtils.to12Hour(endsTimeStr).let { t ->
        if (isBangla) BanglaUtils.toBangla(t) else t
    }
    val endsLabel = if (isBangla) "শেষ হবে:" else "Ends at:"
    val prohibitedLine = if (isBangla) "নামাজ পড়া নিষেধ" else "Prayer is prohibited"
    val note = if (isBangla) info.noteBn else info.noteEn

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val statusHeight = maxHeight.value
        val compactScale = (statusHeight / 180f).coerceIn(0.55f, 1.5f)
        val isShort = statusHeight < 120f
        val lineH = if (isShort) 1f else 1.1f

        val padV = (statusHeight * 0.04f).coerceIn(1f, 8f)
        val padH = (6f * compactScale).coerceIn(4f, 10f)
        val titleFontSize = (statusHeight * 0.18f).coerceIn(12f, 28f)
        val bodyFontSize = (statusHeight * 0.13f).coerceIn(10f, 20f)
        val endsFontSize = (statusHeight * 0.15f).coerceIn(11f, 22f)
        val iconSize = (statusHeight * 0.18f).coerceIn(12f, 26f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, c.prohibitedColor.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(c.prohibitedColor.copy(alpha = 0.12f))
                .padding(vertical = padV.dp, horizontal = padH.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = c.prohibitedColor,
                    modifier = Modifier.size(iconSize.dp)
                )
                Spacer(Modifier.width((4f * compactScale).coerceIn(3f, 6f).dp))
                Text(
                    text = label,
                    color = c.prohibitedColor,
                    fontSize = titleFontSize.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = (titleFontSize * lineH).sp,
                    maxLines = 1
                )
            }
            Text(
                text = prohibitedLine,
                color = c.textSecondary,
                fontSize = bodyFontSize.sp,
                lineHeight = (bodyFontSize * lineH).sp,
                maxLines = 1
            )
            if (note.isNotBlank()) {
                Text(
                    text = note,
                    color = c.primaryLight.copy(alpha = 0.85f),
                    fontSize = bodyFontSize.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    lineHeight = (bodyFontSize * lineH).sp,
                    maxLines = 1
                )
            }
            Text(
                text = styledAmPmTime("$endsLabel $endsDisplay", endsFontSize.sp),
                color = c.primaryLight,
                fontSize = endsFontSize.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = (endsFontSize * lineH).sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun JamaatCountdownBanner(info: JamaatCountdownInfo, isFriday: Boolean, isBangla: Boolean, scale: Float = 1f) {
    val c = LocalMosqueColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "jamaat_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_alpha"
    )

    val mins = info.secsLeft / 60
    val secs = info.secsLeft % 60
    val timerStr = "%d:%02d".format(mins, secs)
    val timerDisplay = if (isBangla) BanglaUtils.toBangla(timerStr) else timerStr
    val prayerName = when {
        isFriday && info.prayer == Prayer.DHUHR -> if (isBangla) "জুম'আর" else "Jumu'ah"
        isBangla -> when (info.prayer) {
            Prayer.FAJR -> "ফজরের"
            Prayer.DHUHR -> "জোহরের"
            Prayer.ASR -> "আসরের"
            Prayer.MAGHRIB -> "মাগরিবের"
            Prayer.ISHA -> "ইশার"
            else -> info.prayer.banglaName
        }
        else -> info.prayer.displayName
    }
    val label = if (isBangla) "$prayerName জামাত শুরু হতে বাকি" else "$prayerName Jamaat starts in"

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val statusHeight = maxHeight.value
        val compactScale = (statusHeight / 180f).coerceIn(0.55f, 1.5f)
        val padV = (statusHeight * 0.04f).coerceIn(2f, 8f)
        val padH = (6f * compactScale).coerceIn(4f, 10f)
        val contentH = (statusHeight - padV * 2f).coerceAtLeast(24f)
        val labelSlot = contentH * 0.22f
        val timerSlot = contentH * 0.50f
        val muteSlot = contentH * 0.28f
        val labelFontSize = (labelSlot * 0.55f).coerceIn(9f, 20f)
        val timerFontSize = (timerSlot * 0.78f).coerceIn(18f, 88f)
        val muteFontSize = (muteSlot * 0.36f).coerceIn(8f, 14f)
        val iconSize = (muteSlot * 0.38f).coerceIn(10f, 16f)
        val mutePadV = (muteSlot * 0.08f).coerceIn(1f, 4f).dp
        val mutePadH = (8f * compactScale).coerceIn(6f, 12f).dp
        val tightStyle = TextStyle(
            platformStyle = PlatformTextStyle(includeFontPadding = false),
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.Both
            )
        )
        val heightKey = statusHeight.toInt()
        var fittedLabelSize by remember(label, heightKey) { mutableStateOf(labelFontSize.sp) }
        LaunchedEffect(labelFontSize, label, heightKey) { fittedLabelSize = labelFontSize.sp }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(2.dp, c.jamaatColor.copy(alpha = pulseAlpha), RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.verticalGradient(listOf(c.jamaatColor.copy(alpha = 0.16f), c.jamaatColor.copy(alpha = 0.08f))))
                .padding(vertical = padV.dp, horizontal = padH.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(0.22f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = c.jamaatColor.copy(alpha = 0.85f),
                    fontSize = fittedLabelSize,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.4.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = fittedLabelSize,
                    style = tightStyle,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    onTextLayout = { result ->
                        if (result.hasVisualOverflow && fittedLabelSize.value > 8f) {
                            fittedLabelSize = (fittedLabelSize.value * 0.9f).sp
                        }
                    }
                )
            }
            Box(
                modifier = Modifier
                    .weight(0.50f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timerDisplay,
                    color = c.jamaatColor,
                    fontSize = timerFontSize.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    lineHeight = timerFontSize.sp,
                    style = tightStyle,
                    maxLines = 1,
                    softWrap = false
                )
            }
            Box(
                modifier = Modifier
                    .weight(0.28f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(c.jamaatColor.copy(alpha = 0.12f))
                        .padding(horizontal = mutePadH, vertical = mutePadV)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VolumeOff,
                        contentDescription = null,
                        tint = c.jamaatColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(iconSize.dp)
                    )
                    Spacer(Modifier.width((5 * compactScale).coerceIn(3f, 6f).dp))
                    Text(
                        text = if (isBangla) "অনুগ্রহ করে মোবাইল সাইলেন্ট করুন" else "Please silence your mobile",
                        color = c.jamaatColor.copy(alpha = 0.85f),
                        fontSize = muteFontSize.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.3.sp,
                        lineHeight = muteFontSize.sp,
                        style = tightStyle,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun to12hr(time: String): String {
    return try {
        val parts = time.split(":")
        val hour = parts[0].trim().toInt()
        val min = parts[1].trim()
        val amPm = if (hour < 12) "AM" else "PM"
        val h12 = if (hour % 12 == 0) 12 else hour % 12
        "$h12:$min $amPm"
    } catch (e: Exception) { time }
}

private fun styledAmPmTime(time: String, fontSize: TextUnit) = buildAnnotatedString {
    val suffixMatch = Regex("\\s+(AM|PM)\\s*$", RegexOption.IGNORE_CASE).find(time)
    if (suffixMatch != null) {
        append(time.substring(0, suffixMatch.range.first).trimEnd())
        withStyle(
            SpanStyle(
                fontSize = (fontSize.value * 0.62f).sp,
                fontWeight = FontWeight.SemiBold
            )
        ) {
            append(" ")
            append(suffixMatch.groupValues[1].uppercase())
        }
    } else {
        append(time)
    }
}

@Composable
private fun SunSahriIftarGrid(
    sunriseTime: String,
    sunsetTime: String,
    fajrTime: String,
    isBangla: Boolean,
    modifier: Modifier = Modifier
) {
    val c = LocalMosqueColors.current
    val sunrise12 = to12hr(sunriseTime)
    val sunset12 = to12hr(sunsetTime)
    val fajr12 = to12hr(fajrTime)
    val sunriseDisplay = if (isBangla) BanglaUtils.toBangla(sunrise12) else sunrise12
    val sunsetDisplay = if (isBangla) BanglaUtils.toBangla(sunset12) else sunset12
    val fajrDisplay = if (isBangla) BanglaUtils.toBangla(fajr12) else fajr12

    val sunriseLabel = if (isBangla) "সূর্যোদয়" else "Sunrise"
    val sunsetLabel = if (isBangla) "সূর্যাস্ত" else "Sunset"
    val sehriLabel = if (isBangla) "সাহরি" else "Sehri"
    val iftarLabel = if (isBangla) "ইফতার" else "Iftar"

    BoxWithConstraints(modifier = modifier) {
        val statusHeight = maxHeight.value
        val compactScale = (statusHeight / 56f).coerceIn(0.55f, 1.8f)
        val padV = (statusHeight * 0.08f).coerceIn(1f, 8f)
        val padH = (4f * compactScale).coerceIn(2f, 6f)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, c.primary.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.horizontalGradient(listOf(c.sunStripEdge, c.sunStripBg, c.sunStripEdge)))
                .padding(vertical = padV.dp, horizontal = padH.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeInfoCell(Icons.Rounded.WbSunny, sunriseLabel, sunriseDisplay, c.primaryLight)
            Box(modifier = Modifier.width(1.dp).fillMaxHeight(0.72f).background(c.divider))
            TimeInfoCell(Icons.Rounded.WbTwilight, sunsetLabel, sunsetDisplay, Color(0xFFFF8A65))
            Box(modifier = Modifier.width(1.dp).fillMaxHeight(0.72f).background(c.divider))
            TimeInfoCell(Icons.Rounded.Bedtime, sehriLabel, fajrDisplay, Color(0xFF90CAF9))
            Box(modifier = Modifier.width(1.dp).fillMaxHeight(0.72f).background(c.divider))
            TimeInfoCell(Icons.Rounded.LocalDining, iftarLabel, sunsetDisplay, Color(0xFF81C784))
        }
    }
}

@Composable
private fun RowScope.TimeInfoCell(
    icon: ImageVector,
    label: String,
    timeDisplay: String,
    color: Color
) {
    val c = LocalMosqueColors.current
    BoxWithConstraints(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
    ) {
        val cellH = maxHeight.value
        val isShort = cellH < 48f
        val lineH = if (isShort) 1f else 1.1f
        val labelSize = (cellH * 0.22f).coerceIn(8f, 14f)
        val timeSize = (cellH * 0.42f).coerceIn(12f, 28f)
        val iconSize = (cellH * 0.24f).coerceIn(10f, 18f)

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(iconSize.dp)
                )
                Spacer(Modifier.width((cellH * 0.06f).coerceIn(2f, 5f).dp))
                Text(
                    text = label,
                    color = c.textDim,
                    fontSize = labelSize.sp,
                    letterSpacing = 0.8.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = (labelSize * lineH).sp,
                    maxLines = 1
                )
            }
            Text(
                text = styledAmPmTime(timeDisplay, timeSize.sp),
                color = color,
                fontSize = timeSize.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = (timeSize * lineH).sp,
                maxLines = 1
            )
        }
    }
}
