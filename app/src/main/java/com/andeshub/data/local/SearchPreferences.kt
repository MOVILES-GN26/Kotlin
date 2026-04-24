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
        val SEARCH_HISTORY_KEY = stringPreferencesKey("search_history")
    }

    // Devuelve la lista de búsquedas separadas por "|"
    val searchHistory: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val raw = preferences[SEARCH_HISTORY_KEY] ?: ""
        if (raw.isEmpty()) emptyList()
        else raw.split("|").filter { it.isNotEmpty() }
    }

    suspend fun saveSearch(query: String) {
        if (query.isBlank()) return
        context.dataStore.edit { preferences ->
            val raw = preferences[SEARCH_HISTORY_KEY] ?: ""
            val current = if (raw.isEmpty()) mutableListOf()
            else raw.split("|").toMutableList()
            current.remove(query) // evita duplicados
            current.add(0, query) // agrega al inicio
            val trimmed = current.take(5) // máximo 5
            preferences[SEARCH_HISTORY_KEY] = trimmed.joinToString("|")
        }
        android.util.Log.d("DataStore", "Historial guardado: $query")
    }

    suspend fun clearHistory() {
        context.dataStore.edit { it.remove(SEARCH_HISTORY_KEY) }
    }
}