package com.andeshub.data.local

import android.graphics.Bitmap
import android.util.LruCache

/**
 * REQUERIMIENTO [C]: Estrategia de Caché
 * Implementación de un caché en memoria para las miniaturas de los borradores recientes.
 */
object DraftImageCache {
    // Calculamos el tamaño del caché (ej. 1/8 de la memoria disponible)
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8

    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    fun put(key: String, bitmap: Bitmap) {
        if (get(key) == null) {
            memoryCache.put(key, bitmap)
        }
    }

    fun get(key: String): Bitmap? {
        return memoryCache.get(key)
    }

    fun clear() {
        memoryCache.evictAll()
    }
}
