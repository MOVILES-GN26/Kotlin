package com.andeshub.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.andeshub.data.model.Product

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
    val localImagePath: String? = null,
    val sellerId: String?,
    val storeId: String?,
    val createdAt: String?,
    val isFavorite: Boolean = false
) {
    fun toModel(): Product {
        return Product(
            id = id,
            title = title,
            description = description,
            price = price,
            category = category,
            condition = condition,
            building_location = location,
            image_urls = if (localImagePath != null) listOf(localImagePath) else if (imageUrl != null) listOf(imageUrl) else emptyList(),
            seller_id = sellerId,
            store_id = storeId,
            created_at = createdAt
        )
    }

    companion object {
        fun fromModel(product: Product, localPath: String? = null): ProductEntity {
            return ProductEntity(
                id = product.id,
                title = product.title,
                description = product.description,
                price = product.price,
                category = product.category,
                condition = product.condition,
                location = product.building_location,
                imageUrl = product.image_urls.firstOrNull(),
                localImagePath = localPath,
                sellerId = product.seller_id,
                storeId = product.store_id,
                createdAt = product.created_at
            )
        }
    }
}
