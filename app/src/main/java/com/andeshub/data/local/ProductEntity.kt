package com.andeshub.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.andeshub.data.model.Product

@Entity(tableName = "favorites")
data class ProductEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val building_location: String,
    val price: Double,
    val condition: String,
    val imageUrls: String,
    val seller_id: String?,
    val store_id: String?,
    val created_at: String?,
    val updated_at: String?
)

fun ProductEntity.toProduct(): Product = Product(
    id = id,
    title = title,
    description = description,
    category = category,
    building_location = building_location,
    price = price,
    condition = condition,
    image_urls = imageUrls.split("|||").filter { it.isNotBlank() },
    seller_id = seller_id,
    store_id = store_id,
    created_at = created_at,
    updated_at = updated_at
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    building_location = building_location,
    price = price,
    condition = condition,
    imageUrls = image_urls.joinToString("|||"),
    seller_id = seller_id,
    store_id = store_id,
    created_at = created_at,
    updated_at = updated_at
)