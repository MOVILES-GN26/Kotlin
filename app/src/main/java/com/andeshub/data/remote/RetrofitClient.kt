package com.andeshub.data.remote

import com.andeshub.data.remote.ApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://172.24.48.1:3000/"

    fun getBaseUrl(): String = BASE_URL

    private var token: String? = null

    fun setToken(accessToken: String) {
        token = accessToken
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder().apply {
                token?.let { addHeader("Authorization", "Bearer $it") }
            }.build()
            chain.proceed(request)
        }
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
