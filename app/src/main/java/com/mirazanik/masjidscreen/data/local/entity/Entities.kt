package com.mirazanik.masjidscreen.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mosque_config")
data class MosqueConfigEntity(
    @PrimaryKey val id: String = "default",
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

@Entity(tableName = "jamaat_times")
data class JamaatTimesEntity(
    @PrimaryKey val id: String = "default",
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

@Entity(tableName = "hadith")
data class HadithEntity(
    @PrimaryKey val id: String,
    val translation: String = "",
    val source: String = "",
    val narrator: String = "",
    val active: Boolean = true
)

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey val id: String,
    val text: String = "",
    val active: Boolean = true,
    val priority: Int = 0
)

@Entity(tableName = "screen_info")
data class ScreenInfoEntity(
    @PrimaryKey val screenId: String,
    val name: String = "",
    val pairingCode: String = "",
    val pairedDeviceId: String = "",
    val lastSeenMs: Long = 0L,
    val appVersion: String = "",
    val activeTheme: String = "night_navy",
    val language: String = "en",
    val tableFontScale: Float = 1f,
    val hadithInterval: Int = 30,
    val jamaatCountdownMins: Int = 3,
    val hijriDateOffset: Int = 0,
    // Per-screen mosque identity (empty = use global)
    val mosqueName: String = "",
    val mosqueAddress: String = "",
    // Per-screen prayer calc (0.0 / empty = use global)
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val calculationMethod: String = "",
    val madhab: String = "",
    val headerScale: Float = 1f,
    val clockScale: Float = 1f,
    val sunGridScale: Float = 1f,
    val clockWidthFraction: Float = 0.40f,
    val mainRowWeight: Float = 0.75f,
    val noticeHeightFraction: Float = 0.20f,
    val jamaatColPrayer: Float = 0.20f,
    val jamaatColWaqtStart: Float = 0.25f,
    val jamaatColWaqtEnd: Float = 0.25f,
    val jamaatColJamaat: Float = 0.30f,
)
