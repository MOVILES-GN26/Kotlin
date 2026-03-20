package com.andeshub.data.model

import java.io.Serializable

data class UserProfile(
    val id: String,
    val name: String,
    val email: String? = null,
    val major: String? = null,
    val avatar_url: String? = null
) : Serializable
