package com.andeshub.data.repository

import com.andeshub.data.model.AuthResponse
import com.andeshub.data.model.CachedUser
import com.andeshub.data.model.LoginRequest
import com.andeshub.data.model.RegisterRequest
import com.andeshub.data.remote.RetrofitClient
import com.andeshub.data.model.NfcLoginRequest
import com.andeshub.data.local.SessionManager

class AuthRepository(private val sessionManager: SessionManager) {
    private val authService = RetrofitClient.authService

    fun getCachedUser(): CachedUser? = sessionManager.getCachedUser()

    suspend fun login(email: String, password: String): AuthResponse {
        return authService.login(LoginRequest(email, password))
    }

    suspend fun register(
        email: String,
        firstName: String,
        lastName: String,
        major: String,
        password: String,
        phoneNumber: String
    ): AuthResponse {
        return authService.register(
            RegisterRequest(
                email = email,
                firstName = firstName,
                lastName = lastName,
                major = major,
                password = password,
                phoneNumber = phoneNumber
            )
        )
    }
    suspend fun nfcLogin(userId: String): AuthResponse {
        return authService.nfcLogin(NfcLoginRequest(userId))
    }
}