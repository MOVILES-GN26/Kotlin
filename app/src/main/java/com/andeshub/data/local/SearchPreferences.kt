package com.andeshub.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "search_prefs")

class SearchPreferences(private val context: Context) {

    companion object {
        val LAST_SEARCH_KEY = stringPreferencesKey("last_search")
    }

    val lastSearch: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LAST_SEARCH_KEY] ?: ""
    }

    suspend fun saveLastSearch(query: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SEARCH_KEY] = query
        }
        android.util.Log.d("DataStore", "Búsqueda guardada: $query")
    }
}