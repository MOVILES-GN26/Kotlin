package com.andeshub.data.repository

import com.andeshub.data.model.Product
import com.andeshub.data.remote.RetrofitClient

class FavoritesRepository {

    private val api = RetrofitClient.apiService

    suspend fun getFavorites(): List<Product> {
        return api.getFavorites()
    }

    suspend fun addFavorite(productId: String) {
        api.addFavorite(productId)
    }

    suspend fun removeFavorite(productId: String) {
        api.removeFavorite(productId)
    }
}