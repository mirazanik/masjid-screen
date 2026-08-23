package com.mirazanik.masjidscreen.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mirazanik.masjidscreen.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore("theme_prefs")

private val THEME_KEY = stringPreferencesKey("active_theme")

class ThemePreferences(private val context: Context) {

    val themeFlow: Flow<AppTheme> = context.themeDataStore.data.map { prefs ->
        AppTheme.fromKey(prefs[THEME_KEY] ?: AppTheme.NIGHT_NAVY.key)
    }

    suspend fun saveTheme(theme: AppTheme) {
        context.themeDataStore.edit { it[THEME_KEY] = theme.key }
    }

    suspend fun clearTheme() {
        context.themeDataStore.edit { it.remove(THEME_KEY) }
    }
}
