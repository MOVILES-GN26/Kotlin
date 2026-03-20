package com.andeshub.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.andeshub.data.model.Product
import com.andeshub.data.remote.RetrofitClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class ProductRepository(private val context: Context) {

    private val api = RetrofitClient.apiService

    suspend fun getProducts(
        search: String? = null,
        category: String? = null,
        condition: String? = null,
        priceSort: String? = null
    ): List<Product> {
        val response = api.getProducts(search, category, condition, priceSort)
        return response.items ?: emptyList()
    }

    suspend fun createProduct(
        token: String,
        title: String,
        description: String,
        category: String,
        location: String,
        price: Double,
        condition: String,
        storeId: String?,
        imageUri: Uri?,
        imageBitmap: Bitmap? = null
    ): Product {
        val titlePart = title.toRequestBody("text/plain".toMediaType())
        val descriptionPart = description.toRequestBody("text/plain".toMediaType())
        val categoryPart = category.toRequestBody("text/plain".toMediaType())
        val locationPart = location.toRequestBody("text/plain".toMediaType())
        val pricePart = price.toString().toRequestBody("text/plain".toMediaType())
        val conditionPart = condition.toRequestBody("text/plain".toMediaType())
        val storeIdPart = storeId?.toRequestBody("text/plain".toMediaType())

        val imagePart = when {
            imageUri != null -> {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.use { it.readBytes() }
                if (bytes != null) {
                    val requestBody = bytes.toRequestBody("image/*".toMediaType())
                    MultipartBody.Part.createFormData("images", "product.jpg", requestBody)
                } else null
            }
            imageBitmap != null -> {
                val stream = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                val bytes = stream.toByteArray()
                val requestBody = bytes.toRequestBody("image/jpeg".toMediaType())
                MultipartBody.Part.createFormData("images", "product.jpg", requestBody)
            }
            else -> null
        }

        return api.createProduct(
            "Bearer $token",
            titlePart,
            descriptionPart,
            categoryPart,
            locationPart,
            pricePart,
            conditionPart,
            storeIdPart,
            imagePart
        )
    }
}
