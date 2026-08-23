package com.mirazanik.masjidscreen.util

import android.os.SystemClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.TimeZone

object NtpTimeProvider {
    val bdZone: ZoneId = ZoneId.of("Asia/Dhaka")

    private const val NTP_PORT = 123
    private const val NTP_PACKET_SIZE = 48
    private const val NTP_MODE_CLIENT = 3
    private const val NTP_VERSION = 3
    private const val TRANSMIT_TIME_OFFSET = 40
    private const val NTP_TO_UNIX_EPOCH_OFFSET_SECONDS = 2_208_988_800L

    private val servers = listOf(
        "time.google.com",
        "pool.ntp.org",
        "time.cloudflare.com"
    )

    @Volatile
    private var offsetFromElapsedRealtimeMs: Long? = null

    fun currentTimeMillis(): Long {
        return offsetFromElapsedRealtimeMs?.let { SystemClock.elapsedRealtime() + it }
            ?: System.currentTimeMillis()
    }

    fun nowInBangladesh(): ZonedDateTime {
        return Instant.ofEpochMilli(currentTimeMillis()).atZone(bdZone)
    }

    fun calendarInBangladesh(): Calendar {
        return Calendar.getInstance(TimeZone.getTimeZone(bdZone)).apply {
            timeInMillis = currentTimeMillis()
        }
    }

    suspend fun sync() = withContext(Dispatchers.IO) {
        for (server in servers) {
            val serverTime = runCatching { requestTime(server) }.getOrNull()
            if (serverTime != null) {
                offsetFromElapsedRealtimeMs = serverTime - SystemClock.elapsedRealtime()
                return@withContext
            }
        }
    }

    private fun requestTime(server: String): Long {
        val buffer = ByteArray(NTP_PACKET_SIZE)
        buffer[0] = ((NTP_VERSION shl 3) or NTP_MODE_CLIENT).toByte()

        DatagramSocket().use { socket ->
            socket.soTimeout = 5_000
            val address = InetAddress.getByName(server)
            val request = DatagramPacket(buffer, buffer.size, address, NTP_PORT)
            socket.send(request)

            val response = DatagramPacket(buffer, buffer.size)
            socket.receive(response)
        }

        return readTimestamp(buffer, TRANSMIT_TIME_OFFSET)
    }

    private fun readTimestamp(buffer: ByteArray, offset: Int): Long {
        val seconds = readUnsignedInt(buffer, offset) - NTP_TO_UNIX_EPOCH_OFFSET_SECONDS
        val fraction = readUnsignedInt(buffer, offset + 4)
        return (seconds * 1_000L) + ((fraction * 1_000L) / 0x1_0000_0000L)
    }

    private fun readUnsignedInt(buffer: ByteArray, offset: Int): Long {
        return ((buffer[offset].toLong() and 0xffL) shl 24) or
            ((buffer[offset + 1].toLong() and 0xffL) shl 16) or
            ((buffer[offset + 2].toLong() and 0xffL) shl 8) or
            (buffer[offset + 3].toLong() and 0xffL)
    }
}
