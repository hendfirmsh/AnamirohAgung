package com.agunganamiroh.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.agunganamiroh.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemePreferenceManager(private val context: Context) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("theme_mode")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            val theme = preferences[THEME_KEY] ?: ThemeMode.SYSTEM.name
            ThemeMode.valueOf(theme)
        }

    suspend fun saveTheme(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = themeMode.name
        }
    }
}
