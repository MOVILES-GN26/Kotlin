package com.andeshub.data.remote

import com.andeshub.data.model.FavoritesCount
import com.andeshub.data.model.Product
import com.andeshub.data.model.Store
import com.andeshub.data.model.TrendingCategory
import com.andeshub.data.model.RecordInteractionRequest
import com.andeshub.data.model.ProductStats
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.DELETE
import retrofit2.http.Query
import com.andeshub.data.model.UpdateProfileRequest
import com.andeshub.data.model.UserResponse
import com.andeshub.data.model.ProductVisitRequest
import com.andeshub.data.model.ProductVisitStats
import com.andeshub.data.model.PurchaseFromFavoriteRequest

import retrofit2.Response
import retrofit2.http.*

data class ProductsResponse(
    val items: List<Product>? = emptyList()
)

data class WhatsAppContactResponse(
    val url: String
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

    @GET("users/me/favorites")
    suspend fun getFavorites(): List<Product>

    @POST("users/me/favorites/{productId}")
    suspend fun addFavorite(@Path("productId") productId: String): Response<Unit>

    @DELETE("users/me/favorites/{productId}")
    suspend fun removeFavorite(@Path("productId") productId: String): Response<Unit>

    @GET("products")
    suspend fun getProducts(
        @Query("search") search: String? = null,
        @Query("category") category: String? = null,
        @Query("condition") condition: String? = null,
        @Query("price_sort") priceSort: String? = null
    ): ProductsResponse

    @GET("stores/my-stores")
    suspend fun getMyStores(): List<Store>

    @GET("trending/categories")
    suspend fun getTrendingCategories(): List<TrendingCategory>

    @GET("users/{userId}/products")
    suspend fun getProductsByUser(
        @Path("userId") userId: String
    ): ProductsResponse

    @PATCH("users/me")
    suspend fun updateProfile(
        @Body body: UpdateProfileRequest
    ): UserResponse

    @Multipart
    @PATCH("users/me/avatar")
    suspend fun updateAvatar(
        @Part avatar: MultipartBody.Part
    ): UserResponse

    @DELETE("posts/{id}")
    suspend fun deleteProduct(@Path("id") productId: String)


    @POST("interactions/view")
    suspend fun recordInteraction(@Body request: RecordInteractionRequest): Response<Unit>

    @GET("interactions/product/{id}/stats")
    suspend fun getProductStats(@Path("id") productId: String): ProductStats

    @GET("products/{id}/favorites/count")
    suspend fun getFavoritesCount(@Path("id") productId: String): FavoritesCount

    @GET("products/{id}/contact-whatsapp")
    suspend fun getWhatsAppContactUrl(
        @Path("id") productId: String,
        @Query("redirect") redirect: String = "false"
    ): WhatsAppContactResponse

    @POST("analytics/product-visit")
    suspend fun recordProductVisit(@Body request: ProductVisitRequest): Response<Unit>

    @GET("analytics/product-visit/stats")
    suspend fun getProductVisitStats(@Query("productId") productId: String? = null): ProductVisitStats

    @POST("interactions/purchase")
    suspend fun recordPurchaseFromFavorite(
        @Body request: PurchaseFromFavoriteRequest
    ): Response<Unit>

}
