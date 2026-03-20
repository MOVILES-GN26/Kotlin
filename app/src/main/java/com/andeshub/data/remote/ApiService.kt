package com.andeshub.data.remote

import com.andeshub.data.model.Product
import com.andeshub.data.model.Store
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

data class ProductsResponse(
    val items: List<Product>? = emptyList()
)

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
