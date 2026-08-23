package com.mirazanik.masjidscreen.admin.ui.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mirazanik.masjidscreen.data.model.MosqueConfig
import com.mirazanik.masjidscreen.ui.theme.AppTheme

private val calculationMethods = listOf(
    "MWL" to "Muslim World League",
    "ISNA" to "ISNA (North America)",
    "EGYPT" to "Egyptian",
    "MAKKAH" to "Umm al-Qura (Makkah)",
    "KARACHI" to "Karachi / Hanafi",
    "MOON_SIGHTING" to "Moon Sighting Committee",
    "DUBAI" to "Dubai",
)

private val languages = listOf("en" to "English", "bn" to "\u09ac\u09be\u0982\u09b2\u09be")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSettingsScreen(
    config: MosqueConfig,
    isSaving: Boolean,
    onSave: (MosqueConfig) -> Unit,
) {
    var mosqueName by remember(config) { mutableStateOf(config.name) }
    var mosqueAddress by remember(config) { mutableStateOf(config.address) }
    var lat by remember(config) { mutableStateOf(config.latitude.toString()) }
    var lng by remember(config) { mutableStateOf(config.longitude.toString()) }
    var method by remember(config) { mutableStateOf(config.calculationMethod) }
    var madhab by remember(config) { mutableStateOf(config.madhab) }
    var language by remember(config) { mutableStateOf(config.language) }
    var selectedTheme by remember(config) { mutableStateOf(AppTheme.fromKey(config.activeTheme)) }
    var fontScale by remember(config) { mutableFloatStateOf(config.tableFontScale) }
    var countdownMins by remember(config) { mutableIntStateOf(config.jamaatCountdownMins) }
    var hijriOffset by remember(config) { mutableIntStateOf(config.hijriDateOffset) }
    var hadithInterval by remember(config) { mutableStateOf(config.hadithInterval.toString()) }
    var methodExpanded by remember { mutableStateOf(false) }
    var madhabExpanded by remember { mutableStateOf(false) }
    var languageExpanded by remember { mutableStateOf(false) }
    var countdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Global defaults used by every screen unless that screen overrides them.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SettingsCard(icon = Icons.Default.Mosque, title = "Masjid Identity") {
            OutlinedTextField(
                value = mosqueName,
                onValueChange = { mosqueName = it },
                label = { Text("Masjid Name") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = mosqueAddress,
                onValueChange = { mosqueAddress = it },
                label = { Text("Address") },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )
        }

        SettingsCard(icon = Icons.Default.MyLocation, title = "Prayer Time Calculation") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = lat,
                    onValueChange = { lat = it },
                    label = { Text("Latitude") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = lng,
                    onValueChange = { lng = it },
                    label = { Text("Longitude") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.weight(1f)
                )
            }

            ExposedDropdownMenuBox(expanded = methodExpanded, onExpandedChange = { methodExpanded = it }) {
                OutlinedTextField(
                    value = calculationMethods.find { it.first == method }?.second ?: method,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Calculation Method") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(methodExpanded) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = methodExpanded, onDismissRequest = { methodExpanded = false }) {
                    calculationMethods.forEach { (key, label) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = { method = key; methodExpanded = false })
                    }
                }
            }

            ExposedDropdownMenuBox(expanded = madhabExpanded, onExpandedChange = { madhabExpanded = it }) {
                OutlinedTextField(
                    value = madhab,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Madhab (Asr calculation)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(madhabExpanded) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = madhabExpanded, onDismissRequest = { madhabExpanded = false }) {
                    listOf("HANAFI", "SHAFI").forEach { m ->
                        DropdownMenuItem(text = { Text(m) }, onClick = { madhab = m; madhabExpanded = false })
                    }
                }
            }
        }

        SettingsCard(icon = Icons.Default.Palette, title = "Default Display Theme") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppTheme.entries.forEach { theme ->
                    ThemeChip(
                        theme = theme,
                        selected = theme == selectedTheme,
                        onClick = { selectedTheme = theme }
                    )
                }
            }
        }

        SettingsCard(icon = Icons.Default.Translate, title = "Default Language") {
            ExposedDropdownMenuBox(expanded = languageExpanded, onExpandedChange = { languageExpanded = it }) {
                OutlinedTextField(
                    value = languages.find { it.first == language }?.second ?: language,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Language") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(languageExpanded) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = languageExpanded, onDismissRequest = { languageExpanded = false }) {
                    languages.forEach { (key, label) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = { language = key; languageExpanded = false })
                    }
                }
            }
        }

        SettingsCard(icon = Icons.Default.TextFields, title = "Prayer Table Font Size") {
            Text(
                "Scale: ${"%.0f".format(fontScale * 100)}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Slider(
                value = fontScale,
                onValueChange = { fontScale = it },
                valueRange = 0.5f..1.5f,
                steps = 19,
                modifier = Modifier.fillMaxWidth()
            )
        }

        SettingsCard(icon = Icons.Default.Timer, title = "Jamaat Countdown / Hadith Rotation") {
            ExposedDropdownMenuBox(expanded = countdownExpanded, onExpandedChange = { countdownExpanded = it }) {
                OutlinedTextField(
                    value = "$countdownMins minutes before Jamaat",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Show countdown timer") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(countdownExpanded) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = countdownExpanded, onDismissRequest = { countdownExpanded = false }) {
                    listOf(1, 2, 3, 5, 10, 15).forEach { mins ->
                        DropdownMenuItem(
                            text = { Text("$mins minutes before Jamaat") },
                            onClick = { countdownMins = mins; countdownExpanded = false }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = hadithInterval,
                onValueChange = { hadithInterval = it.filter { ch -> ch.isDigit() } },
                label = { Text("Hadith rotation (seconds)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )
        }

        SettingsCard(icon = Icons.Default.CalendarMonth, title = "Arabic Date Adjustment") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Hijri date offset", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        when {
                            hijriOffset > 0 -> "+$hijriOffset day${if (hijriOffset > 1) "s" else ""} from calculated"
                            hijriOffset < 0 -> "$hijriOffset day${if (hijriOffset < -1) "s" else ""} from calculated"
                            else -> "No adjustment (default)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilledTonalIconButton(
                        onClick = { if (hijriOffset > -3) hijriOffset-- },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    Text(
                        text = if (hijriOffset >= 0) "+$hijriOffset" else "$hijriOffset",
                        modifier = Modifier.widthIn(min = 40.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    FilledTonalIconButton(
                        onClick = { if (hijriOffset < 3) hijriOffset++ },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            }
        }

        Button(
            onClick = {
                onSave(
                    config.copy(
                        name = mosqueName,
                        address = mosqueAddress,
                        latitude = lat.toDoubleOrNull() ?: config.latitude,
                        longitude = lng.toDoubleOrNull() ?: config.longitude,
                        calculationMethod = method,
                        madhab = madhab,
                        language = language,
                        activeTheme = selectedTheme.key,
                        tableFontScale = fontScale,
                        jamaatCountdownMins = countdownMins,
                        hijriDateOffset = hijriOffset,
                        hadithInterval = hadithInterval.toIntOrNull()?.coerceAtLeast(5) ?: config.hadithInterval
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !isSaving,
            shape = MaterialTheme.shapes.medium,
        ) {
            if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Save Global Settings", fontWeight = FontWeight.Bold)
        }
    }
}
