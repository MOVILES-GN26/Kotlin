package com.andeshub.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")

class ThemePreferences(private val context: Context) {

    companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        const val SYSTEM = "SYSTEM"
        const val LIGHT = "LIGHT"
        const val DARK = "DARK"
    }

    val themeMode: Flow<String> = context.themeDataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY] ?: SYSTEM
    }

    suspend fun saveThemeMode(mode: String) {
        context.themeDataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }
}
