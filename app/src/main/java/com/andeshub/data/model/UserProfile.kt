package com.andeshub.data.model

data class UserProfile(
    val id: String,
    val name: String,
    val email: String? = null,
    val major: String? = null,
    val profilePictureUrl: String? = null
)
