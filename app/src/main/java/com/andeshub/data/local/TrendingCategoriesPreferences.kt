package com.andeshub.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.andeshub.data.model.TrendingCategory
import kotlinx.coroutines.flow.first

private val Context.trendingDataStore by preferencesDataStore(name = "trending_prefs")

class TrendingCategoriesPreferences(private val context: Context) {

    companion object {
        val TRENDING_KEY = stringPreferencesKey("trending_categories")
    }

    suspend fun save(categories: List<TrendingCategory>) {
        context.trendingDataStore.edit { prefs ->
            // Guardamos como "categoria:count|categoria:count"
            prefs[TRENDING_KEY] = categories.joinToString("|") { "${it.category}:${it.count}" }
        }
    }

    suspend fun load(): List<TrendingCategory> {
        val prefs = context.trendingDataStore.data.first()
        val raw = prefs[TRENDING_KEY] ?: return emptyList()
        return raw.split("|").filter { it.isNotEmpty() }.mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size >= 2) TrendingCategory(parts[0], parts[1].toIntOrNull() ?: 0)
            else null
        }
    }
}