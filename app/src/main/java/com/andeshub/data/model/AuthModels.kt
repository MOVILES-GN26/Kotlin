package com.andeshub.data.model

import com.google.gson.annotations.SerializedName

data class UserResponse(
    val id: String,
    val email: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val major: String,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null
)

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    val user: UserResponse
)

data class LoginRequest(
    val email: String,
    val password: String,
    @SerializedName("login_type") val loginType: String = "email_password"

)

data class RegisterRequest(
    val email: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val major: String,
    val password: String,
    @SerializedName("phone_number") val phoneNumber: String
)

data class UpdateProfileRequest(
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    val major: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    val password: String? = null
)

data class NfcLoginRequest(
    @SerializedName("userId") val userId: String
)