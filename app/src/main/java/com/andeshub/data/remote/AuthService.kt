package com.andeshub.data.remote

import com.andeshub.data.model.AuthResponse
import com.andeshub.data.model.LoginRequest
import com.andeshub.data.model.NfcLoginRequest
import com.andeshub.data.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/nfc-login")
    suspend fun nfcLogin(@Body body: NfcLoginRequest): AuthResponse
}