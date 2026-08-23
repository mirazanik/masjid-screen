package com.mirazanik.masjidscreen.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirazanik.masjidscreen.data.model.JamaatTimes
import com.mirazanik.masjidscreen.data.model.Prayer
import com.mirazanik.masjidscreen.data.model.PrayerTimesData
import com.mirazanik.masjidscreen.ui.theme.LocalMosqueColors
import com.mirazanik.masjidscreen.util.BanglaUtils

data class PrayerRow(
    val prayer: Prayer,
    val waqtStart: String,
    val waqtEnd: String,
    val jamaat: String,
    val note: String = "",
    val isCurrent: Boolean = false,
    val prohibitedStartLabel: String = "",
    val prohibitedStartTime: String = "",
    val prohibitedEndLabel: String = "",
    val prohibitedEndTime: String = ""
)

@Composable
fun PrayerTimesTable(
    prayerTimes: PrayerTimesData,
    jamaatTimes: JamaatTimes,
    nextPrayerName: String,
    currentPrayerName: String = "",
    language: String = "en",
    isFriday: Boolean = false,
    fontScale: Float = 1f,
    prayerColWeight: Float = 0.20f,
    waqtStartColWeight: Float = 0.25f,
    waqtEndColWeight: Float = 0.25f,
    jamaatColWeight: Float = 0.30f,
    resizeColumns: Boolean = false,
    onColumnSplitDelta: ((Int, Float) -> Unit)? = null,
    onColumnSplitCommit: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val c = LocalMosqueColors.current
    val isBangla = language == "bn"

    val ishraqTime = BanglaUtils.addMinutes(prayerTimes.sunrise, 20)
    val zawalTime = BanglaUtils.addMinutes(prayerTimes.dhuhr, -5)
    val makroohAsrTime = BanglaUtils.addMinutes(prayerTimes.maghrib, -15)
    val labelIshraq = if (isBangla) "ইশরাক" else "Ishraq"
    val labelZawal = if (isBangla) "যাওয়াল" else "Zawal"
    val labelMakrooh = if (isBangla) "মাকরূহ" else "Makrooh"

    val rows = listOf(
        PrayerRow(Prayer.FAJR, prayerTimes.fajr, prayerTimes.sunrise, jamaatTimes.fajr, jamaatTimes.fajrNote, currentPrayerName == "Fajr",
            prohibitedEndLabel = labelIshraq, prohibitedEndTime = ishraqTime),
        PrayerRow(Prayer.DHUHR, prayerTimes.dhuhr, prayerTimes.asr, jamaatTimes.dhuhr, jamaatTimes.dhuhrNote, currentPrayerName == "Dhuhr",
            prohibitedStartLabel = labelZawal, prohibitedStartTime = zawalTime),
        PrayerRow(Prayer.ASR, prayerTimes.asr, prayerTimes.maghrib, jamaatTimes.asr, jamaatTimes.asrNote, currentPrayerName == "Asr",
            prohibitedEndLabel = labelMakrooh, prohibitedEndTime = makroohAsrTime),
        PrayerRow(
            Prayer.MAGHRIB, prayerTimes.maghrib, prayerTimes.isha,
            if (jamaatTimes.maghrib == "auto") prayerTimes.maghrib else jamaatTimes.maghrib,
            jamaatTimes.maghribNote,
            currentPrayerName == "Maghrib"
        ),
        PrayerRow(Prayer.ISHA, prayerTimes.isha, prayerTimes.fajr, jamaatTimes.isha, jamaatTimes.ishaNote, currentPrayerName == "Isha")
    )

    val headerPrayer = if (isBangla) "নামাজ" else "Prayer"
    val headerWaqtStart = if (isBangla) "ওয়াক্ত শুরু" else "Waqt Start"
    val headerWaqtEnd = if (isBangla) "ওয়াক্ত শেষ" else "Waqt End"
    val headerJamaat = if (isBangla) "জামাত" else "Jamaat"

    BoxWithConstraints(modifier = modifier.padding(8.dp).clip(RoundedCornerShape(16.dp)).background(c.backgroundCard)) {
        val rowHeightValue = if (maxHeight.value.isFinite()) maxHeight.value / 6f else 80f
        val usableHeight = (rowHeightValue - 12f).coerceAtLeast(20f)
        val baseFontSize = (usableHeight * 0.66f * fontScale).sp
        val headerFontSize = (usableHeight * 0.50f * fontScale).sp
        val nameFontSize = (usableHeight * 0.57f * fontScale).sp
        val iconSizeDp = (usableHeight * 0.52f * fontScale).dp.coerceIn(10.dp, 48.dp)
        val colW = run {
            val raw = floatArrayOf(prayerColWeight, waqtStartColWeight, waqtEndColWeight, jamaatColWeight)
            val sum = raw.sum().takeIf { it > 0f } ?: 1f
            FloatArray(4) { raw[it] / sum }
        }
        val density = LocalDensity.current
        val hPad = 16.dp
        val contentW = (maxWidth - hPad * 2).coerceAtLeast(1.dp)
        val contentWpx = with(density) { contentW.toPx() }.coerceAtLeast(1f)

        Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(c.tableHeaderStart, c.tableHeaderMid, c.tableHeaderEnd)))
                .padding(vertical = (rowHeightValue * 0.03f).dp.coerceIn(2.dp, 6.dp), horizontal = hPad),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableHeaderCell(headerPrayer, Modifier.weight(colW[0]), headerFontSize)
            TableHeaderCell(headerWaqtStart, Modifier.weight(colW[1]), headerFontSize)
            TableHeaderCell(headerWaqtEnd, Modifier.weight(colW[2]), headerFontSize)
            TableHeaderCell(headerJamaat, Modifier.weight(colW[3]), headerFontSize)
        }

        Column(modifier = Modifier.weight(1f)) {
            rows.forEachIndexed { index, row ->
                val isJumuah = isFriday && row.prayer == Prayer.DHUHR
                val rowBg = when {
                    row.isCurrent -> c.highlightBg
                    index % 2 == 0 -> c.backgroundCard
                    else -> c.backgroundCardAlt
                }
                val primaryName = when {
                    isJumuah -> if (isBangla) "জুম'আ" else "Jumu'ah"
                    isBangla -> row.prayer.banglaName
                    else -> row.prayer.displayName
                }
                val waqtStart12 = BanglaUtils.to12Hour(row.waqtStart)
                val waqtEnd12 = BanglaUtils.to12Hour(row.waqtEnd)
                val jamaat12 = BanglaUtils.to12Hour(row.jamaat)
                val waqtStartDisplay = if (isBangla) BanglaUtils.toBangla(waqtStart12) else waqtStart12
                val waqtEndDisplay = if (isBangla) BanglaUtils.toBangla(waqtEnd12) else waqtEnd12
                val jamaatDisplay = if (isBangla) BanglaUtils.toBangla(jamaat12) else jamaat12
                val prohibitedStartDisplay = row.prohibitedStartTime.takeIf { it.isNotEmpty() }
                    ?.let { BanglaUtils.to12Hour(it).let { t -> if (isBangla) BanglaUtils.toBangla(t) else t } } ?: ""
                val prohibitedEndDisplay = row.prohibitedEndTime.takeIf { it.isNotEmpty() }
                    ?.let { BanglaUtils.to12Hour(it).let { t -> if (isBangla) BanglaUtils.toBangla(t) else t } } ?: ""
                val hasProhibited = row.prohibitedStartLabel.isNotEmpty() || row.prohibitedEndLabel.isNotEmpty()

                val nameColor = when {
                    row.isCurrent -> c.highlightNext
                    isJumuah -> c.primaryLight
                    else -> c.textPrimary
                }
                val (prayerIcon, prayerIconTint) = when (row.prayer) {
                    Prayer.FAJR -> Icons.Rounded.Bedtime to Color(0xFF81D4FA)
                    Prayer.DHUHR -> Icons.Rounded.WbSunny to Color(0xFFFFD54F)
                    Prayer.ASR -> Icons.Rounded.LightMode to Color(0xFFFFB74D)
                    Prayer.MAGHRIB -> Icons.Rounded.WbTwilight to Color(0xFFFF8A65)
                    Prayer.ISHA -> Icons.Rounded.NightsStay to Color(0xFFCE93D8)
                    else -> Icons.Rounded.WbSunny to c.primary
                }

                val noteText = row.note.trim()
                val hasNote = noteText.isNotEmpty()
                val activeScale = if (row.isCurrent) 1.22f else 1f
                Box(
                    modifier = Modifier
                        .weight(if (row.isCurrent) 1.35f else 1f)
                        .fillMaxWidth()
                        .background(rowBg),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (row.isCurrent) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight()
                                .background(c.highlightNext)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = if (hasNote || hasProhibited) 0.dp else (rowHeightValue * 0.08f).dp.coerceIn(4.dp, 14.dp),
                                bottom = if (hasNote || hasProhibited) 0.dp else (rowHeightValue * 0.08f).dp.coerceIn(4.dp, 14.dp),
                                start = if (row.isCurrent) 20.dp else 16.dp,
                                end = 16.dp
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(colW[0]),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = prayerIcon,
                                contentDescription = null,
                                tint = if (row.isCurrent) c.highlightNext else prayerIconTint,
                                modifier = Modifier.size(iconSizeDp * activeScale)
                            )
                            Spacer(modifier = Modifier.width(7.dp))
                            Text(
                                text = primaryName,
                                color = nameColor,
                                fontSize = (nameFontSize.value * activeScale).sp,
                                fontWeight = if (row.isCurrent || isJumuah) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                        TableTimeCell(waqtStartDisplay, row.isCurrent, isJumuah, Modifier.weight(colW[1]),
                            baseColor = c.jamaatColor, fontSize = (baseFontSize.value * activeScale).sp, prohibitedLabel = row.prohibitedStartLabel, prohibitedTime = prohibitedStartDisplay)
                        TableTimeCell(waqtEndDisplay, row.isCurrent, isJumuah, Modifier.weight(colW[2]),
                            baseColor = c.jamaatColor, fontSize = (baseFontSize.value * activeScale).sp, prohibitedLabel = row.prohibitedEndLabel, prohibitedTime = prohibitedEndDisplay)

                        Column(
                            modifier = Modifier.weight(colW[3]),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            TableTimeCell(
                                jamaatDisplay, row.isCurrent, isJumuah,
                                baseColor = c.primary,
                                fontSize = if (hasNote) (baseFontSize.value * 0.60f * activeScale).sp else (baseFontSize.value * activeScale).sp
                            )
                            if (hasNote) {
                                AutoSizeNoteText(
                                    text = noteText,
                                    color = c.primary.copy(alpha = 0.75f),
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Normal,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                    maxFontSize = (baseFontSize.value * 0.44f).sp
                                )
                            }
                        }
                    }
                }

                if (index < rows.lastIndex) {
                    HorizontalDivider(color = c.divider, thickness = 0.5.dp)
                }
            }
        }
        }
        if (resizeColumns) {
            var acc = 0f
            for (i in 0..2) {
                acc += colW[i]
                SplitHandle(
                    verticalBar = true,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = hPad + contentW * acc - 11.dp),
                    onDeltaPx = { dx -> onColumnSplitDelta?.invoke(i, dx / contentWpx) },
                    onCommit = { onColumnSplitCommit?.invoke() }
                )
            }
        }
    }
}

@Composable
private fun TableHeaderCell(text: String, modifier: Modifier = Modifier, fontSize: TextUnit = 15.sp) {
    val c = LocalMosqueColors.current
    Text(
        text = text,
        modifier = modifier,
        color = c.textPrimary,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun TableTimeCell(
    time: String,
    isCurrent: Boolean,
    isJumuah: Boolean,
    modifier: Modifier = Modifier,
    baseColor: Color = Color.Unspecified,
    fontSize: TextUnit = 18.sp,
    prohibitedLabel: String = "",
    prohibitedTime: String = ""
) {
    val c = LocalMosqueColors.current
    val resolvedBase = if (baseColor == Color.Unspecified) c.primary else baseColor
    val hasProhibited = prohibitedLabel.isNotEmpty() && prohibitedTime.isNotEmpty()
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val effectiveFontSize = if (hasProhibited) (fontSize.value * 0.60f).sp else fontSize
        val effectiveFontWeight = if (isCurrent || isJumuah) FontWeight.Bold else FontWeight.SemiBold
        Text(
            text = styledAmPmTime(time, effectiveFontSize),
            color = when {
                isCurrent -> c.highlightNext
                isJumuah -> c.primaryLight
                else -> resolvedBase
            },
            fontSize = effectiveFontSize,
            fontWeight = effectiveFontWeight,
            textAlign = TextAlign.Center,
            lineHeight = effectiveFontSize
        )
        if (hasProhibited) {
            val prohibitedFontSize = (fontSize.value * 0.44f).sp
            Text(
                text = styledAmPmTime("$prohibitedLabel: $prohibitedTime", prohibitedFontSize),
                color = c.prohibitedColor,
                fontSize = prohibitedFontSize,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = (fontSize.value * 0.72f).sp
            )
        }
    }
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
private fun AutoSizeNoteText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    maxFontSize: TextUnit = 12.sp,
    minFontSize: TextUnit = 8.sp,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null
) {
    var fontSize by remember(text) { mutableStateOf(maxFontSize) }
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = 2,
        softWrap = true,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { result ->
            if (result.hasVisualOverflow && fontSize > minFontSize) {
                fontSize = maxOf(fontSize.value - 0.5f, minFontSize.value).sp
            }
        }
    )
}
