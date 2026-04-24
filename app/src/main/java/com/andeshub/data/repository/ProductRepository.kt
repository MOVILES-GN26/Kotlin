package com.andeshub.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.andeshub.data.local.AppDatabase
import com.andeshub.data.local.ProductEntity
import com.andeshub.data.model.Product
import com.andeshub.data.remote.RetrofitClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ProductRepository(private val context: Context) {

    private val api = RetrofitClient.apiService
    private val productDao = AppDatabase.getInstance(context).productDao()

    suspend fun getProducts(
        search: String? = null,
        category: String? = null,
        condition: String? = null,
        priceSort: String? = null
    ): List<Product> {
        // No capturamos el error aquí para que el ViewModel pueda manejar el fallback filtrado
        val response = api.getProducts(search, category, condition, priceSort)
        return response.items ?: emptyList()
    }

    suspend fun getProductOffline(productId: String): Product? {
        val entity = productDao.getProductById(productId)
        return entity?.let { mapEntityToProduct(it) }
    }

    suspend fun getAllLocalProducts(): List<Product> {
        return productDao.getAllProducts().map { mapEntityToProduct(it) }
    }

    private fun mapEntityToProduct(it: ProductEntity): Product {
        return Product(
            id = it.id,
            title = it.title,
            description = it.description,
            price = it.price,
            category = it.category,
            condition = it.condition,
            building_location = it.location,
            // PRIORIDAD: Imagen local si existe, sino URL remota
            image_urls = if (!it.localImagePath.isNullOrEmpty() && File(it.localImagePath).exists()) 
                            listOf(it.localImagePath) 
                         else listOfNotNull(it.imageUrl),
            seller_id = it.sellerId,
            store_id = it.storeId,
            created_at = it.createdAt
        )
    }

    fun saveImageToInternalStorage(uri: Uri?, bitmap: Bitmap?): String? {
        val fileName = "prod_img_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        return try {
            val outputStream = FileOutputStream(file)
            if (uri != null) {
                context.contentResolver.openInputStream(uri)?.use { it.copyTo(outputStream) }
            } else if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveProductLocally(product: Product, localPath: String? = null) {
        try {
            val existing = productDao.getProductById(product.id)
            val entity = ProductEntity(
                id = product.id,
                title = product.title,
                description = product.description,
                price = product.price,
                category = product.category,
                condition = product.condition,
                location = product.building_location,
                imageUrl = product.image_urls.firstOrNull(),
                localImagePath = localPath ?: existing?.localImagePath,
                sellerId = product.seller_id,
                storeId = product.store_id,
                createdAt = product.created_at,
                isFavorite = existing?.isFavorite ?: false,
                lastViewedAt = existing?.lastViewedAt ?: System.currentTimeMillis()
            )
            productDao.insertProduct(entity)
        } catch (e: Exception) {
            android.util.Log.e("ProductRepository", "Error saving product locally: ${e.message}")
        }
    }

    suspend fun markProductAsViewed(productId: String) {
        try {
            productDao.updateLastViewed(productId, System.currentTimeMillis())
        } catch (e: Exception) {
            android.util.Log.e("ProductRepository", "Error marking product as viewed: ${e.message}")
        }
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

    suspend fun getProductsByUser(userId: String): Result<List<Product>> {
        return try {
            val response = api.getProductsByUser(userId)
            Result.success(response.items ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            api.deleteProduct(productId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
