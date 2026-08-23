package com.mirazanik.masjidscreen.prayer

import android.icu.util.IslamicCalendar
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.mirazanik.masjidscreen.data.model.HijriDate
import com.mirazanik.masjidscreen.data.model.PrayerTimesData
import com.mirazanik.masjidscreen.util.NtpTimeProvider
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.Locale
import java.util.TimeZone
import android.icu.util.TimeZone as IcuTimeZone

object PrayerCalculator {

    private val bdTimeZone = TimeZone.getTimeZone(NtpTimeProvider.bdZone)
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
        timeZone = bdTimeZone
    }

    fun calculate(
        latitude: Double,
        longitude: Double,
        methodName: String,
        madhabName: String,
        forDate: LocalDate = NtpTimeProvider.nowInBangladesh().toLocalDate()
    ): PrayerTimesData {
        return try {
            val coords = Coordinates(latitude, longitude)
            val date = DateComponents(
                forDate.year,
                forDate.monthValue,
                forDate.dayOfMonth
            )
            val params = resolveMethod(methodName).parameters.also { p ->
                p.madhab = resolveMadhab(madhabName)
            }
            val times = PrayerTimes(coords, date, params)
            PrayerTimesData(
                fajr = timeFormat.format(times.fajr),
                sunrise = timeFormat.format(times.sunrise),
                dhuhr = timeFormat.format(times.dhuhr),
                asr = timeFormat.format(times.asr),
                maghrib = timeFormat.format(times.maghrib),
                isha = timeFormat.format(times.isha)
            )
        } catch (e: Exception) {
            PrayerTimesData()
        }
    }

    fun getCurrentPrayer(
        prayerTimes: PrayerTimesData,
        now: ZonedDateTime = NtpTimeProvider.nowInBangladesh()
    ): Pair<String, Long> {
        return try {
            val nowMins = now.hour * 60 + now.minute
            val fajr    = parseTimeToMins(prayerTimes.fajr)
            val sunrise = parseTimeToMins(prayerTimes.sunrise)
            val dhuhr   = parseTimeToMins(prayerTimes.dhuhr)
            val asr     = parseTimeToMins(prayerTimes.asr)
            val maghrib = parseTimeToMins(prayerTimes.maghrib)
            val isha    = parseTimeToMins(prayerTimes.isha)
            when {
                nowMins < fajr          -> "Isha"    to (fajr - nowMins).toLong()
                nowMins < sunrise       -> "Fajr"    to (sunrise - nowMins).toLong()
                nowMins < dhuhr         -> ""        to (dhuhr - nowMins).toLong()
                nowMins < asr           -> "Dhuhr"   to (asr - nowMins).toLong()
                nowMins < maghrib       -> "Asr"     to (maghrib - nowMins).toLong()
                nowMins < isha          -> "Maghrib"  to (isha - nowMins).toLong()
                else                    -> "Isha"    to (fajr + 1440 - nowMins).toLong()
            }
        } catch (e: Exception) { "" to 0L }
    }

    fun getNextPrayer(
        prayerTimes: PrayerTimesData,
        jamaatFajr: String,
        jamaatDhuhr: String,
        jamaatAsr: String,
        jamaatMaghrib: String,
        jamaatIsha: String,
        now: ZonedDateTime = NtpTimeProvider.nowInBangladesh()
    ): Pair<String, Long> {
        val nowMins = now.hour * 60 + now.minute

        val schedule = listOf(
            "Fajr" to parseTimeToMins(jamaatFajr.ifBlank { prayerTimes.fajr }),
            "Dhuhr" to parseTimeToMins(jamaatDhuhr.ifBlank { prayerTimes.dhuhr }),
            "Asr" to parseTimeToMins(jamaatAsr.ifBlank { prayerTimes.asr }),
            "Maghrib" to parseTimeToMins(jamaatMaghrib.ifBlank { prayerTimes.maghrib }),
            "Isha" to parseTimeToMins(jamaatIsha.ifBlank { prayerTimes.isha })
        )

        val next = schedule.firstOrNull { (_, mins) -> mins > nowMins }
            ?: schedule.first()

        val diffMins = if (next.second > nowMins) next.second - nowMins
                       else next.second + 1440 - nowMins

        return next.first to diffMins.toLong()
    }

    fun getHijriDate(
        offsetDays: Int = 0,
        maghribTime: String = "",
        now: ZonedDateTime = NtpTimeProvider.nowInBangladesh()
    ): HijriDate {
        return try {
            val cal = IslamicCalendar(IcuTimeZone.getTimeZone(NtpTimeProvider.bdZone.id))
            cal.setTimeInMillis(now.toInstant().toEpochMilli())
            // Islamic day starts at sunset — advance by 1 if past Maghrib
            val sunsetOffset = if (maghribTime.isNotBlank()) {
                val nowMins = now.hour * 60 + now.minute
                val maghribMins = parseTimeToMins(maghribTime)
                if (maghribMins > 0 && nowMins >= maghribMins) 1 else 0
            } else 0
            val totalOffset = offsetDays + sunsetOffset
            if (totalOffset != 0) cal.add(IslamicCalendar.DAY_OF_MONTH, totalOffset)
            HijriDate(
                day = cal.get(IslamicCalendar.DAY_OF_MONTH),
                monthIndex = cal.get(IslamicCalendar.MONTH),
                year = cal.get(IslamicCalendar.YEAR)
            )
        } catch (e: Exception) {
            HijriDate()
        }
    }

    private fun parseTimeToMins(time: String): Int {
        return try {
            val parts = time.split(":")
            parts[0].toInt() * 60 + parts[1].toInt()
        } catch (e: Exception) { 0 }
    }

    private fun resolveMethod(name: String): CalculationMethod = when (name.uppercase()) {
        "ISNA" -> CalculationMethod.NORTH_AMERICA
        "MWL" -> CalculationMethod.MUSLIM_WORLD_LEAGUE
        "EGYPT" -> CalculationMethod.EGYPTIAN
        "MAKKAH" -> CalculationMethod.UMM_AL_QURA
        "KARACHI" -> CalculationMethod.KARACHI
        "MOON_SIGHTING" -> CalculationMethod.MOON_SIGHTING_COMMITTEE
        "DUBAI" -> CalculationMethod.DUBAI
        else -> CalculationMethod.MUSLIM_WORLD_LEAGUE
    }

    private fun resolveMadhab(name: String): Madhab = when (name.uppercase()) {
        "SHAFI", "SHAFI'I" -> Madhab.SHAFI
        else -> Madhab.HANAFI
    }
}
