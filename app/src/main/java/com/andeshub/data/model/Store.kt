package com.andeshub.data.model

data class Store(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val logo_url: String?,
    val owner_id: String
)