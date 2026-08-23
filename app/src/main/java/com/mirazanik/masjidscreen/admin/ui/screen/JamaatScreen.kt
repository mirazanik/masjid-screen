package com.mirazanik.masjidscreen.admin.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mirazanik.masjidscreen.data.model.JamaatTimes
import com.mirazanik.masjidscreen.data.model.PrayerTimesData
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JamaatScreen(
    initial: JamaatTimes,
    prayerTimes: PrayerTimesData,
    isSaving: Boolean,
    waqtDate: LocalDate? = null,
    onSave: (JamaatTimes) -> Unit,
) {
    var fajr by remember(initial) { mutableStateOf(initial.fajr) }
    var dhuhr by remember(initial) { mutableStateOf(initial.dhuhr) }
    var asr by remember(initial) { mutableStateOf(initial.asr) }
    var maghrib by remember(initial) { mutableStateOf(if (initial.maghrib == "auto") "19:00" else initial.maghrib) }
    var maghribAuto by remember(initial) { mutableStateOf(initial.maghrib == "auto") }
    var isha by remember(initial) { mutableStateOf(initial.isha) }

    var fajrNote by remember(initial) { mutableStateOf(initial.fajrNote) }
    var dhuhrNote by remember(initial) { mutableStateOf(initial.dhuhrNote) }
    var asrNote by remember(initial) { mutableStateOf(initial.asrNote) }
    var maghribNote by remember(initial) { mutableStateOf(initial.maghribNote) }
    var ishaNote by remember(initial) { mutableStateOf(initial.ishaNote) }

    var pickerTarget by remember { mutableStateOf<Pair<String, (String) -> Unit>?>(null) }

    val fajrRange = WaqtRange(prayerTimes.fajr, prayerTimes.sunrise)
    val dhuhrRange = WaqtRange(prayerTimes.dhuhr, prayerTimes.asr)
    val asrRange = WaqtRange(prayerTimes.asr, prayerTimes.maghrib)
    val maghribRange = WaqtRange(prayerTimes.maghrib, prayerTimes.isha)
    val ishaRange = WaqtRange(prayerTimes.isha, prayerTimes.fajr)

    val fajrError = jamaatRangeError(fajr, fajrRange)
    val dhuhrError = jamaatRangeError(dhuhr, dhuhrRange)
    val asrError = jamaatRangeError(asr, asrRange)
    val maghribError = if (maghribAuto) null else jamaatRangeError(maghrib, maghribRange)
    val ishaError = jamaatRangeError(isha, ishaRange)
    val hasValidationError = listOf(fajrError, dhuhrError, asrError, maghribError, ishaError).any { it != null }

    pickerTarget?.let { (initTime, onPick) ->
        JamaatTimePickerDialog(
            initialTime = initTime,
            onDismiss = { pickerTarget = null },
        ) { t -> onPick(t); pickerTarget = null }
    }

    Column(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column {
                Text(
                    "Jamaat Times",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    waqtDate?.let { "Waqt for ${it.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy"))}" }
                        ?: "Set congregation prayer times",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (waqtDate != null)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        PrayerJamaatCard(
            label = "Fajr", arabicLabel = "الفجر",
            time = fajr, waqtRange = fajrRange, error = fajrError, note = fajrNote,
            onPickTime = { pickerTarget = fajr to { fajr = it } },
            onNoteChange = { fajrNote = it }
        )
        PrayerJamaatCard(
            label = "Dhuhr", arabicLabel = "الظهر",
            time = dhuhr, waqtRange = dhuhrRange, error = dhuhrError, note = dhuhrNote,
            onPickTime = { pickerTarget = dhuhr to { dhuhr = it } },
            onNoteChange = { dhuhrNote = it }
        )
        PrayerJamaatCard(
            label = "Asr", arabicLabel = "العصر",
            time = asr, waqtRange = asrRange, error = asrError, note = asrNote,
            onPickTime = { pickerTarget = asr to { asr = it } },
            onNoteChange = { asrNote = it }
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PrayerCardHeader(label = "Maghrib", arabicLabel = "المغرب")
                WaqtRangeText(maghribRange)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = maghribAuto,
                        onCheckedChange = { maghribAuto = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                        )
                    )
                    Text(
                        "Auto (use calculated adhan time)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!maghribAuto) {
                    TimePickerButton(time = maghrib, onPick = { pickerTarget = maghrib to { maghrib = it } })
                    ValidationErrorText(maghribError)
                }
                NoteField(note = maghribNote, onNoteChange = { maghribNote = it })
            }
        }

        PrayerJamaatCard(
            label = "Isha", arabicLabel = "العشاء",
            time = isha, waqtRange = ishaRange, error = ishaError, note = ishaNote,
            onPickTime = { pickerTarget = isha to { isha = it } },
            onNoteChange = { ishaNote = it }
        )

        Spacer(Modifier.height(8.dp))
    }
    StickySaveBar(
        label = "Save Jamaat Times",
        isSaving = isSaving,
        enabled = !hasValidationError,
        onClick = {
            onSave(JamaatTimes(
                fajr = fajr, dhuhr = dhuhr, asr = asr,
                maghrib = if (maghribAuto) "auto" else maghrib,
                isha = isha,
                fajrNote = fajrNote, dhuhrNote = dhuhrNote, asrNote = asrNote,
                maghribNote = maghribNote, ishaNote = ishaNote
            ))
        }
    )
    }
}

@Composable
private fun PrayerCardHeader(label: String, arabicLabel: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
        Text(
            arabicLabel,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PrayerJamaatCard(
    label: String,
    arabicLabel: String,
    time: String,
    waqtRange: WaqtRange,
    error: String?,
    note: String,
    onPickTime: () -> Unit,
    onNoteChange: (String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        color = if (error != null) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.extraSmall
                    )
            )
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PrayerCardHeader(label = label, arabicLabel = arabicLabel)
                WaqtRangeText(waqtRange)
                TimePickerButton(time = time, onPick = onPickTime)
                ValidationErrorText(error)
                NoteField(note = note, onNoteChange = onNoteChange)
            }
        }
    }
}

@Composable
private fun WaqtRangeText(range: WaqtRange) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
    ) {
        Text(
            text = "Waqt: ${to12Hour(range.start)} – ${to12Hour(range.end)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ValidationErrorText(error: String?) {
    if (error != null) {
        Text(
            text = error,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun TimePickerButton(time: String, onPick: () -> Unit) {
    FilledTonalButton(
        onClick = onPick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    ) {
        Icon(Icons.Rounded.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            to12Hour(time),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun NoteField(note: String, onNoteChange: (String) -> Unit) {
    OutlinedTextField(
        value = note,
        onValueChange = onNoteChange,
        label = { Text("Note (optional)") },
        placeholder = { Text("e.g. 2nd jamaat at 4:45 PM") },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        minLines = 1,
        maxLines = 3
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JamaatTimePickerDialog(
    initialTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val (initH, initM) = parseHHmm(initialTime)
    val state = rememberTimePickerState(initialHour = initH, initialMinute = initM, is24Hour = false)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Select Time",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                )
                TimePicker(state = state)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm("%02d:%02d".format(state.hour, state.minute)) },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

private data class WaqtRange(val start: String, val end: String)

private fun jamaatRangeError(time: String, range: WaqtRange): String? {
    if (!isKnownTime(range.start) || !isKnownTime(range.end) || !isKnownTime(time)) return null

    val jamaatMins = parseTimeToMinutes(time)
    val startMins = parseTimeToMinutes(range.start)
    val endMins = parseTimeToMinutes(range.end)
    val isInRange = if (startMins <= endMins) {
        jamaatMins in startMins..endMins
    } else {
        jamaatMins >= startMins || jamaatMins <= endMins
    }

    return if (isInRange) null else "Jamaat must be between ${to12Hour(range.start)} and ${to12Hour(range.end)}"
}

private fun isKnownTime(time: String): Boolean = parseTimeToMinutes(time) >= 0

private fun parseTimeToMinutes(time: String): Int {
    val parts = time.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: return -1
    val m = parts.getOrNull(1)?.toIntOrNull() ?: return -1
    return if (h in 0..23 && m in 0..59) h * 60 + m else -1
}

private fun parseHHmm(time: String): Pair<Int, Int> {
    val parts = time.split(":")
    return (parts.getOrNull(0)?.toIntOrNull() ?: 0) to (parts.getOrNull(1)?.toIntOrNull() ?: 0)
}

private fun to12Hour(time: String): String {
    val parts = time.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: return time
    val m = parts.getOrNull(1)?.toIntOrNull() ?: return time
    val suffix = if (h < 12) "AM" else "PM"
    val h12 = when (h) { 0 -> 12; in 1..12 -> h; else -> h - 12 }
    return "%d:%02d %s".format(h12, m, suffix)
}
