package com.andeshub.data.repository

import com.andeshub.data.model.AuthResponse
import com.andeshub.data.model.CachedUser
import com.andeshub.data.model.LoginRequest
import com.andeshub.data.model.RegisterRequest
import com.andeshub.data.remote.RetrofitClient
import com.andeshub.data.model.NfcLoginRequest
import com.andeshub.data.local.SessionManager
import android.util.LruCache

class AuthRepository(private val sessionManager: SessionManager) {
    private val authService = RetrofitClient.authService
    private val userCache = LruCache<String, CachedUser>(1)

    fun getCachedUser(): CachedUser? {
        // Buscar en LRU Cache (memoria)
        val cached = userCache.get("current_user")
        if (cached != null) return cached

        return sessionManager.getCachedUser()
    }
    fun cacheUser(user: CachedUser) {
        userCache.put("current_user", user)
    }

    fun clearCache() {
        userCache.evictAll()
    }

    suspend fun login(email: String, password: String, isNfc: Boolean = false): AuthResponse {
        return authService.login(LoginRequest(email, password, loginType = if (isNfc) "NFC" else "email_password"))
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