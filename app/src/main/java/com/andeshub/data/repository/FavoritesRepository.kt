package com.andeshub.data.repository

import android.content.Context
import com.andeshub.data.local.AppDatabase
import com.andeshub.data.local.ProductEntity
import com.andeshub.data.model.Product
import com.andeshub.data.remote.RetrofitClient

class FavoritesRepository(context: Context) {

    private val api = RetrofitClient.apiService
    private val productDao = AppDatabase.getInstance(context).productDao()

    suspend fun getLocalFavorites(): List<Product> {
        return productDao.getFavoriteProducts().map { entity ->
            Product(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                price = entity.price,
                category = entity.category,
                condition = entity.condition,
                building_location = entity.location,
                image_urls = entity.imageUrl?.let { listOf(it) } ?: emptyList(),
                seller_id = entity.sellerId,
                store_id = entity.storeId,
                created_at = entity.createdAt
            )
        }
    }

    suspend fun syncFavorites(): List<Product> {
        val remoteFavorites = api.getFavorites()

        val favoriteEntities = remoteFavorites.map { product ->
            ProductEntity(
                id = product.id,
                title = product.title,
                description = product.description,
                price = product.price,
                category = product.category,
                condition = product.condition,
                location = product.building_location,
                imageUrl = product.image_urls.firstOrNull(),
                sellerId = product.seller_id,
                storeId = product.store_id,
                createdAt = product.created_at,
                isFavorite = true
            )
        }

        productDao.insertProducts(favoriteEntities)

        return remoteFavorites
    }

    suspend fun removeFavorite(productId: String) {
        productDao.unmarkAsFavorite(productId)

        try {
            api.removeFavorite(productId)
        } catch (_: Exception) {
            // Queda actualizado localmente aunque falle internet
        }
    }

    suspend fun addFavorite(product: Product) {
        val entity = ProductEntity(
            id = product.id,
            title = product.title,
            description = product.description,
            price = product.price,
            category = product.category,
            condition = product.condition,
            location = product.building_location,
            imageUrl = product.image_urls.firstOrNull(),
            sellerId = product.seller_id,
            storeId = product.store_id,
            createdAt = product.created_at,
            isFavorite = true
        )

        productDao.insertProduct(entity)

        try {
            api.addFavorite(product.id)
        } catch (_: Exception) {
            // Queda guardado localmente aunque falle internet
        }
    }
}