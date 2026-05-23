package com.andeshub.data.local

import android.util.LruCache
import com.andeshub.data.model.Product

object RecentlyViewedLruCache {

    // Máximo 10 productos vistos en memoria
    private val cache = LruCache<String, Product>(10)
    private val order = mutableListOf<String>()

    fun put(product: Product) {
        val key = product.id
        order.remove(key)
        order.add(0, key)
        if (order.size > 10) {
            val removed = order.removeLast()
            cache.remove(removed)
        }
        cache.put(key, product)
        android.util.Log.d("RecentlyViewedLruCache", "Guardado: ${product.title}")
    }

    fun getAll(): List<Product> {
        return order.mapNotNull { cache.get(it) }
    }

    fun clear() {
        cache.evictAll()
        order.clear()
    }
}