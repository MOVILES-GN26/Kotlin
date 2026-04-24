package com.andeshub.data.repository

import android.content.Context
import com.andeshub.data.local.AppDatabase
import com.andeshub.data.local.toEntity
import com.andeshub.data.local.toProduct
import com.andeshub.data.model.Product
import com.andeshub.data.remote.RetrofitClient

class FavoritesRepository(context: Context) {

    private val api = RetrofitClient.apiService
    private val dao = AppDatabase.getInstance(context).favoritesDao()

    suspend fun getLocalFavorites(): List<Product> {
        return dao.getAllFavorites().map { it.toProduct() }
    }

    suspend fun syncFavorites(): List<Product> {
        val remoteFavorites = api.getFavorites()
        dao.replaceFavorites(remoteFavorites.map { it.toEntity() })
        return remoteFavorites
    }

    suspend fun addFavorite(product: Product) {
        dao.insertFavorite(product.toEntity())

        try {
            api.addFavorite(product.id)
        } catch (_: Exception) {
            // queda guardado localmente aunque falle internet
        }
    }

    suspend fun removeFavorite(productId: String) {
        dao.deleteFavorite(productId)

        try {
            api.removeFavorite(productId)
        } catch (_: Exception) {
            // se elimina localmente aunque falle internet
        }
    }
}