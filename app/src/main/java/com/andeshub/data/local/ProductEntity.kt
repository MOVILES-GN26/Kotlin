package com.andeshub.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val category: String,
    val condition: String,
    val location: String,
    val imageUrl: String?,
    val sellerId: String?,
    val storeId: String?,
    val createdAt: String?,
    val isFavorite: Boolean = false,
    val lastViewedAt: Long? = null
)