package com.andeshub.data.repository

import com.andeshub.data.model.AuthResponse
import com.andeshub.data.model.LoginRequest
import com.andeshub.data.model.RegisterRequest
import com.andeshub.data.remote.RetrofitClient

class AuthRepository {
    private val authService = RetrofitClient.authService

    suspend fun login(email: String, password: String): AuthResponse {
        return authService.login(LoginRequest(email, password))
    }

    suspend fun register(
        email: String,
        firstName: String,
        lastName: String,
        major: String,
        password: String
    ): AuthResponse {
        return authService.register(
            RegisterRequest(
                email = email,
                firstName = firstName,
                lastName = lastName,
                major = major,
                password = password
            )
        )
    }
}