package com.andeshub.data.remote

import ProductsResponse
import com.andeshub.data.model.Product
import com.andeshub.data.model.Store
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

data class ProductsResponse(
    val items: List<Product>? = emptyList()
)
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

    @GET("products")
    suspend fun getProducts(): ProductsResponse

    @Multipart
    @POST("posts")
    suspend fun createProduct(
        @Header("Authorization") token: String,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("category") category: RequestBody,
        @Part("building_location") location: RequestBody,
        @Part("price") price: RequestBody,
        @Part("condition") condition: RequestBody,
        @Part("store_id") storeId: RequestBody?,
        @Part images: MultipartBody.Part?
    ): Product
}
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
}
