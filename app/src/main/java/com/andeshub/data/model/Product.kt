package com.andeshub.data.model

data class Product(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val building_location: String,
    val price: Double,
    val condition: String,
    val image_urls: List<String>,
    val seller_id: String,
    val seller: UserProfile? = null,
    val store_id: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)
