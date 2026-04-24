package com.andeshub.data.local

import androidx.room.*

@Dao
interface FavoritesDao {

    @Query("SELECT * FROM favorites")
    suspend fun getAllFavorites(): List<ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorites(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(product: ProductEntity)

    @Query("DELETE FROM favorites WHERE id = :productId")
    suspend fun deleteFavorite(productId: String)

    @Query("DELETE FROM favorites")
    suspend fun clearFavorites()

    @Transaction
    suspend fun replaceFavorites(products: List<ProductEntity>) {
        clearFavorites()
        insertFavorites(products)
    }
}