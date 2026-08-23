package com.mirazanik.masjidscreen.admin.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mirazanik.masjidscreen.data.model.JamaatTimes
import com.mirazanik.masjidscreen.data.model.MosqueConfig
import com.mirazanik.masjidscreen.prayer.PrayerCalculator
import com.mirazanik.masjidscreen.util.NtpTimeProvider
import com.mirazanik.masjidscreen.util.TimePreview
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePreviewBar(
    mosqueConfig: MosqueConfig,
    jamaat: JamaatTimes,
    preview: TimePreview?,
    onPreviewChange: (TimePreview?) -> Unit,
    modifier: Modifier = Modifier,
    onHide: (() -> Unit)? = null,
) {
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(preview) {
        if (preview == null) return@LaunchedEffect
        while (true) {
            delay(1_000)
            tick++
        }
    }
    val now = remember(preview, tick) {
        preview?.now() ?: NtpTimeProvider.nowInBangladesh()
    }
    val date = now.toLocalDate()
    val time = now.toLocalTime()
    val prayerTimes = remember(date, mosqueConfig.latitude, mosqueConfig.longitude, mosqueConfig.calculationMethod, mosqueConfig.madhab) {
        PrayerCalculator.calculate(
            mosqueConfig.latitude,
            mosqueConfig.longitude,
            mosqueConfig.calculationMethod,
            mosqueConfig.madhab,
            date
        )
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    fun jumpTo(date: LocalDate, time: LocalTime) {
        onPreviewChange(TimePreview.at(date, time.withSecond(0).withNano(0)))
    }

    val shortcuts = remember(date, time, prayerTimes, jamaat) {
        buildPreviewShortcuts(date, time, mosqueConfig, prayerTimes, jamaat)
    }

    Surface(
        modifier = modifier
            .widthIn(max = 820.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 10.dp,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = ButtonDefaults.ContentPadding
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        date.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy")),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                FilledTonalButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(0.72f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        time.format(DateTimeFormatter.ofPattern("h:mm a")),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (preview != null) {
                    FilledTonalButton(
                        onClick = { onPreviewChange(null) },
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Real time", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Now", fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
                if (onHide != null) {
                    IconButton(onClick = onHide) {
                        Icon(Icons.Default.VisibilityOff, contentDescription = "Hide test time")
                    }
                }
            }
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(shortcuts, key = { it.label }) { shortcut ->
                    AssistChip(
                        onClick = { shortcut.onClick(::jumpTo) { onPreviewChange(null) } },
                        label = { Text(shortcut.label, fontWeight = FontWeight.Medium) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        var pickedDate by remember(date) { mutableStateOf(date) }
        Dialog(
            onDismissRequest = { showDatePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = 6.dp,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .padding(12.dp)
                        .widthIn(max = 560.dp)
                        .fillMaxWidth(0.96f)
                        .fillMaxHeight(0.92f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "Select date",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp)
                        )
                        CompactMonthCalendar(
                            selected = pickedDate,
                            onSelected = { pickedDate = it },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    jumpTo(pickedDate, time)
                                    showDatePicker = false
                                },
                                shape = MaterialTheme.shapes.medium
                            ) { Text("Set date") }
                        }
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        val pickerState = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = false
        )
        Dialog(
            onDismissRequest = { showTimePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .widthIn(min = 300.dp, max = 400.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Select time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                    TimeInput(state = pickerState)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                jumpTo(date, LocalTime.of(pickerState.hour, pickerState.minute))
                                showTimePicker = false
                            },
                            shape = MaterialTheme.shapes.medium
                        ) { Text("Set time") }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactMonthCalendar(
    selected: LocalDate,
    onSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var visibleMonth by remember { mutableStateOf(YearMonth.from(selected)) }
    val today = remember { NtpTimeProvider.nowInBangladesh().toLocalDate() }
    val firstDayOfWeek = remember { WeekFields.of(Locale.getDefault()).firstDayOfWeek }
    val weekDays = remember(firstDayOfWeek) {
        List(7) { firstDayOfWeek.plus(it.toLong()) }
    }
    val firstOfMonth = visibleMonth.atDay(1)
    val startOffset = (firstOfMonth.dayOfWeek.value - firstDayOfWeek.value + 7) % 7
    val monthLength = visibleMonth.lengthOfMonth()
    val colors = MaterialTheme.colorScheme

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { visibleMonth = visibleMonth.minusMonths(1) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
            }
            Text(
                text = visibleMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { visibleMonth = visibleMonth.plusMonths(1) }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
            weekDays.forEach { day ->
                Text(
                    text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
            repeat(6) { row ->
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    repeat(7) { col ->
                        val dayNum = row * 7 + col - startOffset + 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (dayNum in 1..monthLength) {
                                val day = visibleMonth.atDay(dayNum)
                                val isSelected = day == selected
                                val isToday = day == today
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            when {
                                                isSelected -> colors.primary
                                                isToday -> colors.primary.copy(alpha = 0.18f)
                                                else -> colors.surfaceContainerHighest
                                            }
                                        )
                                        .clickable { onSelected(day) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayNum.toString(),
                                        color = when {
                                            isSelected -> colors.onPrimary
                                            else -> colors.onSurface
                                        },
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class PreviewShortcut(
    val label: String,
    val onClick: (jumpTo: (LocalDate, LocalTime) -> Unit, reset: () -> Unit) -> Unit,
)

private fun buildPreviewShortcuts(
    date: LocalDate,
    time: LocalTime,
    mosqueConfig: MosqueConfig,
    prayerTimes: com.mirazanik.masjidscreen.data.model.PrayerTimesData,
    jamaat: JamaatTimes,
): List<PreviewShortcut> {
    val items = mutableListOf(
        PreviewShortcut("Now") { _, reset -> reset() },
        PreviewShortcut("-1 day") { jumpTo, _ -> jumpTo(date.minusDays(1), time) },
        PreviewShortcut("+1 day") { jumpTo, _ -> jumpTo(date.plusDays(1), time) },
        PreviewShortcut("Friday") { jumpTo, _ ->
            val days = (DayOfWeek.FRIDAY.value - date.dayOfWeek.value + 7) % 7
            val friday = date.plusDays(days.toLong())
            val fridayTimes = PrayerCalculator.calculate(
                mosqueConfig.latitude,
                mosqueConfig.longitude,
                mosqueConfig.calculationMethod,
                mosqueConfig.madhab,
                friday
            )
            val jumuah = parseLocalTime(fridayTimes.dhuhr) ?: LocalTime.of(12, 15)
            jumpTo(friday, jumuah.plusMinutes(1))
        },
    )
    parseLocalTime(prayerTimes.sunrise)?.let { sunrise ->
        items += PreviewShortcut("Sunrise") { jumpTo, _ -> jumpTo(date, sunrise) }
    }
    parseLocalTime(prayerTimes.dhuhr)?.let { dhuhr ->
        items += PreviewShortcut("Zawal") { jumpTo, _ -> jumpTo(date, dhuhr.minusMinutes(4)) }
    }
    parseLocalTime(prayerTimes.maghrib)?.let { maghrib ->
        items += PreviewShortcut("Sunset") { jumpTo, _ -> jumpTo(date, maghrib.minusMinutes(10)) }
    }
    jamaatTime(jamaat.fajr, prayerTimes.fajr)?.let { fajrJamaat ->
        items += PreviewShortcut("Fajr jamaat") { jumpTo, _ -> jumpTo(date, fajrJamaat.minusMinutes(2)) }
    }
    return items
}

private fun parseLocalTime(value: String): LocalTime? {
    val parts = value.split(":")
    if (parts.size < 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return runCatching { LocalTime.of(hour, minute) }.getOrNull()
}

private fun jamaatTime(jamaat: String, prayerTime: String): LocalTime? {
    val source = if (jamaat.trim().equals("auto", ignoreCase = true)) prayerTime else jamaat
    return parseLocalTime(source)
}
