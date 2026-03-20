package com.andeshub.data.model

data class Store(
    val id: String,
    val name: String,
    val description: String? = null,
    val category: String? = null,
    val ownerId: String,
    val logoUrl: String? = null
)
