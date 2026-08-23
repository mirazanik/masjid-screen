package com.mirazanik.masjidscreen.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mirazanik.masjidscreen.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.deviceDataStore by preferencesDataStore(name = "device_prefs")

object DeviceManager {

    private val KEY_DEVICE_ID = stringPreferencesKey("device_id")
    private val KEY_SCREEN_ID = stringPreferencesKey("screen_id")

    suspend fun getOrCreateDeviceId(context: Context): String {
        var id = ""
        context.deviceDataStore.edit { prefs ->
            val existing = prefs[KEY_DEVICE_ID]
            if (existing.isNullOrBlank()) {
                val newId = UUID.randomUUID().toString()
                prefs[KEY_DEVICE_ID] = newId
                id = newId
            } else {
                id = existing
            }
        }
        return id
    }

    fun screenIdFlow(context: Context): Flow<String?> =
        context.deviceDataStore.data.map { prefs ->
            prefs[KEY_SCREEN_ID]?.takeIf { it.isNotBlank() }
        }

    suspend fun saveScreenId(context: Context, screenId: String) {
        context.deviceDataStore.edit { prefs ->
            prefs[KEY_SCREEN_ID] = screenId
        }
    }

    suspend fun clearScreenId(context: Context) {
        context.deviceDataStore.edit { prefs ->
            prefs.remove(KEY_SCREEN_ID)
        }
    }

    fun appVersionName(): String = BuildConfig.VERSION_NAME
}
