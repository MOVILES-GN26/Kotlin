package com.andeshub.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.notificationDataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_prefs")

class NotificationPreferences(private val context: Context) {

    companion object {
        val NOTIFY_VIEWS_KEY     = booleanPreferencesKey("notify_views")
        val NOTIFY_FAVORITES_KEY = booleanPreferencesKey("notify_favorites")
        val PREV_VIEW_COUNTS_KEY = stringPreferencesKey("prev_view_counts")
        val PREV_FAV_COUNTS_KEY  = stringPreferencesKey("prev_fav_counts")
    }

    private val gson = Gson()

    // ── Toggles ──────────────────────────────────────────────────────────────

    val notifyViews: Flow<Boolean> = context.notificationDataStore.data.map { prefs ->
        prefs[NOTIFY_VIEWS_KEY] ?: true
    }

    val notifyFavorites: Flow<Boolean> = context.notificationDataStore.data.map { prefs ->
        prefs[NOTIFY_FAVORITES_KEY] ?: true
    }

    suspend fun saveNotifyViews(enabled: Boolean) {
        context.notificationDataStore.edit { prefs ->
            prefs[NOTIFY_VIEWS_KEY] = enabled
        }
    }

    suspend fun saveNotifyFavorites(enabled: Boolean) {
        context.notificationDataStore.edit { prefs ->
            prefs[NOTIFY_FAVORITES_KEY] = enabled
        }
    }

    // ── Previous counts (Map<productId, count>) ───────────────────────────────

    suspend fun getPreviousViewCounts(): Map<String, Int> {
        val prefs = context.notificationDataStore.data.first()
        val json = prefs[PREV_VIEW_COUNTS_KEY] ?: return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, Int>>() {}.type
            gson.fromJson(json, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun getPreviousFavCounts(): Map<String, Int> {
        val prefs = context.notificationDataStore.data.first()
        val json = prefs[PREV_FAV_COUNTS_KEY] ?: return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, Int>>() {}.type
            gson.fromJson(json, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun savePreviousViewCounts(counts: Map<String, Int>) {
        context.notificationDataStore.edit { prefs ->
            prefs[PREV_VIEW_COUNTS_KEY] = gson.toJson(counts)
        }
    }

    suspend fun savePreviousFavCounts(counts: Map<String, Int>) {
        context.notificationDataStore.edit { prefs ->
            prefs[PREV_FAV_COUNTS_KEY] = gson.toJson(counts)
        }
    }
}
