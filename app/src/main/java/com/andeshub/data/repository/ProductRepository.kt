package com.andeshub.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.andeshub.data.local.AppDatabase
import com.andeshub.data.local.ProductEntity
import com.andeshub.data.local.ProductDraftEntity
import com.andeshub.data.model.Product
import com.andeshub.data.model.UserProfile
import com.andeshub.data.remote.RetrofitClient
import com.andeshub.data.remote.ApiService
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
            image_urls = if (!it.localImagePath.isNullOrEmpty() && File(it.localImagePath).exists()) 
                            listOf(it.localImagePath) 
                         else listOfNotNull(it.imageUrl),
            seller_id = it.sellerId,
            seller = if (it.sellerName != null) UserProfile(id = it.sellerId ?: "", name = it.sellerName, major = it.sellerMajor) else null,
            store_id = it.storeId,
            created_at = it.createdAt
        )
    }

    /**
     * MICRO-OPTIMIZACIÓN: Compresión y redimensionamiento de imagen inteligente.
     * Utiliza inSampleSize para cargar solo los píxeles necesarios en RAM.
     */
    private fun compressImageToBytes(uri: Uri, maxDimension: Int, quality: Int): ByteArray? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }

            var inSampleSize = 1
            if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= maxDimension && halfWidth / inSampleSize >= maxDimension) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
            val bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            }

            val outputStream = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val result = outputStream.toByteArray()
            bitmap?.recycle() // Liberar memoria nativa inmediatamente
            result
        } catch (e: Exception) {
            null
        }
    }

    fun saveImageToInternalStorage(uri: Uri?, bitmap: Bitmap?): String? {
        val fileName = "prod_img_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        return try {
            val outputStream = FileOutputStream(file)
            if (uri != null) {
                // También optimizamos el almacenamiento local de borradores
                val compressed = compressImageToBytes(uri, 1024, 80)
                if (compressed != null) outputStream.write(compressed)
            } else if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
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
                sellerId = product.seller_id ?: product.seller?.id,
                sellerName = product.seller?.name ?: existing?.sellerName,
                sellerMajor = product.seller?.major ?: existing?.sellerMajor,
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

    /**
     * CÓDIGO OPTIMIZADO: Ahora usa la función de compresión para evitar el error 413.
     */
    suspend fun createProduct(
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
                // MICRO-OPTIMIZACIÓN: Redimensionamos a max 1200px y comprimimos al 80%
                val bytes = compressImageToBytes(imageUri, 1200, 80)
                if (bytes != null) {
                    val requestBody = bytes.toRequestBody("image/jpeg".toMediaType())
                    MultipartBody.Part.createFormData("images", "product.jpg", requestBody)
                } else null
            }
            imageBitmap != null -> {
                val stream = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                val bytes = stream.toByteArray()
                val requestBody = bytes.toRequestBody("image/jpeg".toMediaType())
                MultipartBody.Part.createFormData("images", "product.jpg", requestBody)
            }
            else -> null
        }

        return api.createProduct(
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
            val products = response.items ?: emptyList()
            Result.success(products)
        } catch (e: Exception) {
            val cached = productDao.getProductsBySeller(userId).map { mapEntityToProduct(it) }
            if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
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

    suspend fun saveDraftAdvanced(
        title: String,
        description: String,
        category: String,
        location: String,
        price: String,
        condition: String,
        imageUri: Uri?,
        imageBitmap: Bitmap? = null,
        storeId: String?,
        isReadyToSync: Boolean = false
    ) {
        val localPath = saveImageToInternalStorage(imageUri, imageBitmap)
        val draft = ProductDraftEntity(
            title = title,
            description = description,
            price = price,
            category = category,
            condition = condition,
            location = location,
            imageUri = imageUri?.toString(),
            localImagePath = localPath,
            storeId = storeId,
            isReadyToSync = isReadyToSync
        )
        val draftDao = AppDatabase.getInstance(context).productDraftDao()
        draftDao.insertWithLimit(draft)
    }

    suspend fun syncPendingDrafts() {
        val draftDao = AppDatabase.getInstance(context).productDraftDao()
        val pendingDrafts = draftDao.getDraftsReadyToSync()

        pendingDrafts.forEach { draft ->
            try {
                val imageFile = draft.localImagePath?.let { File(it) }
                val imageUri = if (imageFile?.exists() == true) Uri.fromFile(imageFile) else null

                createProduct(
                    title = draft.title,
                    description = draft.description,
                    category = draft.category,
                    location = draft.location,
                    price = draft.price.toDoubleOrNull() ?: 0.0,
                    condition = draft.condition,
                    storeId = draft.storeId,
                    imageUri = imageUri,
                    imageBitmap = null
                )
                draftDao.deleteDraft(draft)
            } catch (e: Exception) {
                android.util.Log.e("ProductRepository", "Error syncing draft ${draft.id}: ${e.message}")
            }
        }
    }

    suspend fun updateProduct(productId: String, request: ApiService.UpdateProductRequest): Product {
        return api.updateProduct(productId, request)
    }

    suspend fun markProductAsViewedByUser(productId: String) {
        try {
            productDao.markAsViewedByUser(productId)
        } catch (e: Exception) {
            android.util.Log.e("ProductRepository", "Error: ${e.message}")
        }
    }

}
