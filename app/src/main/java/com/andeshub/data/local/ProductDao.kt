package com.andeshub.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProductDao {

    @Query("SELECT * FROM products WHERE isFavorite = 1")
    suspend fun getFavoriteProducts(): List<ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Query("UPDATE products SET isFavorite = 1 WHERE id = :productId")
    suspend fun markAsFavorite(productId: String)

    @Query("UPDATE products SET isFavorite = 0 WHERE id = :productId")
    suspend fun unmarkAsFavorite(productId: String)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProduct(productId: String)

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: String): ProductEntity?

    @Query("SELECT * FROM products ORDER BY id DESC")
    suspend fun getAllProducts(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE lastViewedAt IS NOT NULL ORDER BY lastViewedAt DESC LIMIT 10")
    suspend fun getRecentlyViewedProducts(): List<ProductEntity>

    @Query("SELECT id, lastViewedAt FROM products WHERE lastViewedAt IS NOT NULL")
    suspend fun getAllViewedTimestamps(): List<ViewedTimestamp>

    @Query("UPDATE products SET lastViewedAt = :timestamp WHERE id = :productId")
    suspend fun updateLastViewed(productId: String, timestamp: Long)
    @Query("SELECT * FROM products WHERE sellerId = :sellerId")
    suspend fun getProductsBySeller(sellerId: String): List<ProductEntity>
}

data class ViewedTimestamp(
    val id: String,
    val lastViewedAt: Long
)
