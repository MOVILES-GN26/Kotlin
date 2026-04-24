package com.andeshub.data.model

data class CachedUser(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val major: String,
    val phoneNumber: String? = null
)