package com.mirazanik.masjidscreen.util

object BanglaUtils {

    private val digits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun toBangla(input: String): String =
        input.map { if (it.isDigit()) digits[it - '0'] else it }.joinToString("")

    fun toBangla(n: Long): String = toBangla(n.toString())

    val gregorianMonths = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    fun addMinutes(time: String, minutes: Int): String {
        return try {
            val parts = time.split(":")
            val totalMins = parts[0].trim().toInt() * 60 + parts[1].trim().toInt() + minutes
            val adjusted = ((totalMins % 1440) + 1440) % 1440
            "%02d:%02d".format(adjusted / 60, adjusted % 60)
        } catch (e: Exception) { time }
    }

    fun to12Hour(time: String): String {
        return try {
            val parts = time.split(":")
            val hour = parts[0].trim().toInt()
            val min = parts[1].trim()
            val amPm = if (hour < 12) "AM" else "PM"
            val h12 = if (hour % 12 == 0) 12 else hour % 12
            "$h12:$min $amPm"
        } catch (e: Exception) { time }
    }

    val weekDays = mapOf(
        "SATURDAY" to "শনিবার",
        "SUNDAY" to "রবিবার",
        "MONDAY" to "সোমবার",
        "TUESDAY" to "মঙ্গলবার",
        "WEDNESDAY" to "বুধবার",
        "THURSDAY" to "বৃহস্পতিবার",
        "FRIDAY" to "শুক্রবার"
    )
}
