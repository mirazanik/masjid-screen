package com.mirazanik.masjidscreen.admin.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mirazanik.masjidscreen.admin.viewmodel.AdminUiState
import com.mirazanik.masjidscreen.data.model.MosqueConfig
import com.mirazanik.masjidscreen.data.model.ScreenConfig
import com.mirazanik.masjidscreen.data.model.ScreenInfo
import com.mirazanik.masjidscreen.data.model.ScreenLayout
import com.mirazanik.masjidscreen.prayer.PrayerCalculator
import com.mirazanik.masjidscreen.ui.component.DisplayEditActions
import com.mirazanik.masjidscreen.ui.screen.DisplayScreen
import com.mirazanik.masjidscreen.ui.theme.AppTheme
import com.mirazanik.masjidscreen.ui.theme.MosqueTheme
import com.mirazanik.masjidscreen.util.NtpTimeProvider
import com.mirazanik.masjidscreen.util.TimePreview
import com.mirazanik.masjidscreen.viewmodel.DisplayState
import kotlinx.coroutines.delay

@Composable
fun ScreenDisplayPreview(
    screen: ScreenInfo,
    state: AdminUiState,
    editActions: DisplayEditActions? = null,
    layoutOverride: ScreenLayout? = null,
    timePreview: TimePreview? = null,
    modifier: Modifier = Modifier,
) {
    val mosqueConfig = remember(state.screenConfig, state.config) {
        mergeScreenMosqueConfig(state.screenConfig, state.config)
    }
    val theme = remember(mosqueConfig.activeTheme) { AppTheme.fromKey(mosqueConfig.activeTheme) }
    val activeHadiths = remember(state.screenHadiths) { state.screenHadiths.filter { it.active } }
    val activeNotices = remember(state.screenNotices) { state.screenNotices.filter { it.active } }
    val jamaat = state.screenJamaat
    val initialNow = timePreview?.now() ?: NtpTimeProvider.nowInBangladesh()

    var currentTime by remember { mutableStateOf(initialNow.toLocalTime()) }
    var currentDate by remember { mutableStateOf(initialNow.toLocalDate()) }
    var hadithIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(timePreview) {
        while (true) {
            val now = timePreview?.now() ?: NtpTimeProvider.nowInBangladesh()
            currentTime = now.toLocalTime()
            currentDate = now.toLocalDate()
            delay(1_000)
        }
    }

    LaunchedEffect(mosqueConfig.hadithInterval, activeHadiths.size) {
        if (activeHadiths.size <= 1) return@LaunchedEffect
        while (true) {
            delay(mosqueConfig.hadithInterval.coerceAtLeast(5) * 1_000L)
            hadithIndex = (hadithIndex + 1) % activeHadiths.size
        }
    }

    val nowZoned = remember(currentDate, currentTime) {
        currentDate.atTime(currentTime).atZone(NtpTimeProvider.bdZone)
    }
    val prayerTimes = remember(
        currentDate,
        mosqueConfig.latitude,
        mosqueConfig.longitude,
        mosqueConfig.calculationMethod,
        mosqueConfig.madhab
    ) {
        PrayerCalculator.calculate(
            mosqueConfig.latitude,
            mosqueConfig.longitude,
            mosqueConfig.calculationMethod,
            mosqueConfig.madhab,
            currentDate
        )
    }
    val safeHadithIndex = if (activeHadiths.isEmpty()) 0 else hadithIndex % activeHadiths.size
    val (nextName, nextMins) = remember(prayerTimes, jamaat, nowZoned) {
        PrayerCalculator.getNextPrayer(
            prayerTimes,
            jamaat.fajr, jamaat.dhuhr, jamaat.asr, jamaat.maghrib, jamaat.isha,
            now = nowZoned
        )
    }
    val (currentName, currentMins) = remember(prayerTimes, nowZoned) {
        PrayerCalculator.getCurrentPrayer(prayerTimes, now = nowZoned)
    }
    val hijri = remember(mosqueConfig.hijriDateOffset, prayerTimes.maghrib, nowZoned) {
        PrayerCalculator.getHijriDate(
            offsetDays = mosqueConfig.hijriDateOffset,
            maghribTime = prayerTimes.maghrib,
            now = nowZoned
        )
    }

    val displayState = DisplayState(
        currentTime = currentTime,
        currentDate = currentDate,
        hijriDate = hijri,
        prayerTimes = prayerTimes,
        jamaatTimes = jamaat,
        nextPrayerName = nextName,
        nextPrayerMinsLeft = nextMins,
        currentPrayerName = currentName,
        currentPrayerMinsLeft = currentMins,
        hadiths = activeHadiths,
        currentHadithIndex = safeHadithIndex,
        notices = activeNotices,
        mosqueConfig = mosqueConfig,
        isNetworkAvailable = screen.isOnline,
        activeTheme = theme,
        tableFontScale = mosqueConfig.tableFontScale,
        layout = (layoutOverride ?: state.screenConfig.layout).coerced(),
    )

    MosqueTheme(theme = theme, bangla = mosqueConfig.language == "bn") {
        DisplayScreen(
            state = displayState,
            onEnterAdmin = {},
            editActions = editActions,
            modifier = modifier.fillMaxSize()
        )
    }
}

internal fun mergeScreenMosqueConfig(screen: ScreenConfig, global: MosqueConfig): MosqueConfig {
    return MosqueConfig(
        id = global.id,
        name = screen.mosqueName.takeIf { it.isNotBlank() } ?: global.name,
        address = screen.mosqueAddress.takeIf { it.isNotBlank() } ?: global.address,
        latitude = screen.latitude.takeIf { it != 0.0 } ?: global.latitude,
        longitude = screen.longitude.takeIf { it != 0.0 } ?: global.longitude,
        calculationMethod = screen.calculationMethod.takeIf { it.isNotBlank() } ?: global.calculationMethod,
        madhab = screen.madhab.takeIf { it.isNotBlank() } ?: global.madhab,
        language = screen.language.ifBlank { global.language },
        hadithInterval = if (screen.hadithInterval > 0) screen.hadithInterval else global.hadithInterval,
        jamaatCountdownMins = if (screen.jamaatCountdownMins > 0) screen.jamaatCountdownMins else global.jamaatCountdownMins,
        hijriDateOffset = screen.hijriDateOffset,
        tableFontScale = if (screen.tableFontScale > 0f) screen.tableFontScale else global.tableFontScale,
        activeTheme = screen.activeTheme.ifBlank { global.activeTheme }
    )
}
