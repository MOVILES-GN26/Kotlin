package com.andeshub.data.remote

import ProductsResponse
import com.andeshub.data.model.Product
import com.andeshub.data.model.Store
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.DELETE
import retrofit2.http.Query

import retrofit2.Response

interface ApiService {

    @Multipart
    @POST("stores")
    suspend fun createStore(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("category") category: RequestBody,
        @Part logo: MultipartBody.Part?
    ): Store

    @GET("stores/{id}")
    suspend fun getStore(@Path("id") id: String): Store

    @GET("users/me/favorites")
    suspend fun getFavorites(): List<Product>

    @POST("users/me/favorites/{productId}")
    suspend fun addFavorite(@Path("productId") productId: String)

    @DELETE("users/me/favorites/{productId}")
    suspend fun removeFavorite(@Path("productId") productId: String)

    @GET("products")
    suspend fun getProducts(
        @Query("search") search: String? = null,
        @Query("category") category: String? = null,
        @Query("condition") condition: String? = null,
        @Query("price_sort") priceSort: String? = null
    ): ProductsResponse

    @GET("stores/my-stores")
    suspend fun getMyStores(): List<Store>
}

