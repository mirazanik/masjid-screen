package com.mirazanik.masjidscreen.util

import android.os.SystemClock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

/** Super-admin display preview clock. Ticks forward from the chosen moment. */
data class TimePreview(
    val anchor: ZonedDateTime,
    val elapsedRealtimeAtSetMs: Long = SystemClock.elapsedRealtime(),
) {
    fun now(): ZonedDateTime {
        val elapsedMs = (SystemClock.elapsedRealtime() - elapsedRealtimeAtSetMs).coerceAtLeast(0L)
        return anchor.plusNanos(elapsedMs * 1_000_000L)
    }

    companion object {
        fun at(dateTime: ZonedDateTime): TimePreview = TimePreview(anchor = dateTime.withNano(0))

        fun at(date: LocalDate, time: LocalTime): TimePreview =
            at(date.atTime(time).atZone(NtpTimeProvider.bdZone))
    }
}
