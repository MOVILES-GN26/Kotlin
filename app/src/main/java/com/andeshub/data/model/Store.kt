package com.andeshub.data.model

import com.google.gson.annotations.SerializedName

data class StoreOwner(
    val id: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val major: String,
    val avatar_url: String?
)

data class Store(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val logo_url: String?,
    val owner_id: String,
    val owner: StoreOwner? = null,
    val products: List<Product>? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)
