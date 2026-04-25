package com.andeshub.data.local

import android.content.Context
import com.andeshub.data.model.Store
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object StoreLogger {

    private const val LOG_FILE = "store_log.json"
    private const val DRAFT_FILE = "store_draft.json"

    // ── Log de tiendas creadas ──
    fun logCreatedStore(context: Context, store: Store) {
        val file = File(context.filesDir, LOG_FILE)
        val jsonArray = if (file.exists()) JSONArray(file.readText()) else JSONArray()
        val entry = JSONObject().apply {
            put("id", store.id)
            put("name", store.name)
            put("category", store.category)
            put("created_at", store.created_at ?: "unknown")
        }
        jsonArray.put(entry)
        file.writeText(jsonArray.toString())
        android.util.Log.d("StoreLogger", "Tienda guardada en log: ${store.name}")
    }

    // ── Borrador del formulario ──
    fun saveDraft(context: Context, name: String, description: String, category: String) {
        val file = File(context.filesDir, DRAFT_FILE)
        val draft = JSONObject().apply {
            put("name", name)
            put("description", description)
            put("category", category)
        }
        file.writeText(draft.toString())
        android.util.Log.d("StoreLogger", "Borrador guardado")
    }

    fun loadDraft(context: Context): Triple<String, String, String>? {
        val file = File(context.filesDir, DRAFT_FILE)
        if (!file.exists()) return null
        return try {
            val json = JSONObject(file.readText())
            Triple(
                json.optString("name", ""),
                json.optString("description", ""),
                json.optString("category", "")
            )
        } catch (e: Exception) {
            null
        }
    }

    fun clearDraft(context: Context) {
        val file = File(context.filesDir, DRAFT_FILE)
        if (file.exists()) file.delete()
        android.util.Log.d("StoreLogger", "Borrador eliminado")
    }
}