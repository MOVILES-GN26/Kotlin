package com.andeshub.data.local

import android.util.LruCache
import com.andeshub.data.model.Product

object HomeLruCache {

    // Máximo 5 búsquedas en memoria
    private val cache = LruCache<String, List<Product>>(5)

    fun get(query: String): List<Product>? {
        return cache.get(query.lowercase().trim())
    }

    fun put(query: String, products: List<Product>) {
        cache.put(query.lowercase().trim(), products)
        android.util.Log.d("HomeLruCache", "Guardado en caché: '$query' — ${products.size} productos")
    }

    fun clear() {
        cache.evictAll()
    }
}