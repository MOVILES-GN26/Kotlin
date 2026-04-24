package com.andeshub.data.local

import android.content.Context
import com.andeshub.data.model.Store
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object StoreLogger {

    private const val FILE_NAME = "store_log.json"

    fun logCreatedStore(context: Context, store: Store) {
        val file = File(context.filesDir, FILE_NAME)

        // Lee el contenido actual o crea un array vacío
        val jsonArray = if (file.exists()) {
            JSONArray(file.readText())
        } else {
            JSONArray()
        }

        // Crea el nuevo registro
        val entry = JSONObject().apply {
            put("id", store.id)
            put("name", store.name)
            put("category", store.category)
            put("created_at", store.created_at ?: "unknown")
        }

        jsonArray.put(entry)

        // Guarda el archivo actualizado
        file.writeText(jsonArray.toString())

        //Aca es para ver si se me guardo el json con logcat
        android.util.Log.d("StoreLogger", "Log guardado: ${jsonArray.toString()}")
    }

    fun getLog(context: Context): String {
        val file = File(context.filesDir, FILE_NAME)
        return if (file.exists()) file.readText() else "[]"
    }
}