package com.andeshub.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.andeshub.data.local.*
import com.andeshub.data.model.Product
import com.andeshub.data.model.UserProfile
import com.andeshub.data.remote.RetrofitClient
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ProductRepository(private val context: Context) {

    private val api = RetrofitClient.apiService
    private val db = AppDatabase.getInstance(context)
    private val productDao = db.productDao()
    private val draftDao = db.productDraftDao()

    // REQUERIMIENTO [A]: Multi-threading / Concurrencia
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
    ) = coroutineScope {
        val imageProcessing = async(Dispatchers.Default) {
            val original = when {
                imageUri != null -> {
                    try {
                        val inputStream = context.contentResolver.openInputStream(imageUri)
                        BitmapFactory.decodeStream(inputStream)
                    } catch (e: Exception) { null }
                }
                imageBitmap != null -> imageBitmap
                else -> null
            }

            original?.let { bitmap ->
                try {
                    val thumb = Bitmap.createScaledBitmap(bitmap, 200, 200, true)
                    
                    val fileName = "draft_${System.currentTimeMillis()}.jpg"
                    val file = File(context.filesDir, fileName)
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
                    }
                    
                    DraftImageCache.put(fileName, thumb)
                    file.absolutePath
                } catch (e: Exception) {
                    null
                }
            }
        }

        val localPath = imageProcessing.await()
        
        val draft = ProductDraftEntity(
            title = title,
            description = description,
            category = category,
            location = location,
            price = price,
            condition = condition,
            imageUri = imageUri?.toString(),
            localImagePath = localPath,
            storeId = storeId,
            isReadyToSync = isReadyToSync
        )

        withContext(Dispatchers.IO) {
            draftDao.insertWithLimit(draft, limit = 10)
        }
    }

    suspend fun syncPendingDrafts() = withContext(Dispatchers.IO) {
        val pending = draftDao.getDraftsReadyToSync()
        pending.forEach { draft ->
            try {
                val imageFile = draft.localImagePath?.let { File(it) }
                val imagePart = if (imageFile != null && imageFile.exists()) {
                    val requestBody = imageFile.readBytes().toRequestBody("image/jpeg".toMediaType())
                    MultipartBody.Part.createFormData("images", imageFile.name, requestBody)
                } else null

                api.createProduct(
                    draft.title.toRequestBody("text/plain".toMediaType()),
                    draft.description.toRequestBody("text/plain".toMediaType()),
                    draft.category.toRequestBody("text/plain".toMediaType()),
                    draft.location.toRequestBody("text/plain".toMediaType()),
                    draft.price.toRequestBody("text/plain".toMediaType()),
                    draft.condition.toRequestBody("text/plain".toMediaType()),
                    draft.storeId?.toRequestBody("text/plain".toMediaType()),
                    imagePart
                )
                
                draftDao.deleteDraft(draft)
                try { api.recordDraftPublished() } catch (e: Exception) {}
            } catch (e: Exception) {
                android.util.Log.e("Sync", "Failed to sync draft ${draft.id}: ${e.message}")
            }
        }
    }

    suspend fun getProducts(search: String? = null, category: String? = null, condition: String? = null, priceSort: String? = null): List<Product> {
        val response = api.getProducts(search, category, condition, priceSort)
        return response.items ?: emptyList()
    }

    suspend fun getProductOffline(productId: String): Product? {
        val entity = productDao.getProductById(productId)
        return entity?.let { mapEntityToProduct(it) }
    }

    suspend fun markProductAsViewed(productId: String) {
        try {
            productDao.updateLastViewed(productId, System.currentTimeMillis())
        } catch (e: Exception) {
            android.util.Log.e("ProductRepository", "Error marking product: ${e.message}")
        }
    }

    suspend fun saveProductLocally(product: Product, localPath: String? = null) = withContext(Dispatchers.IO) {
        val existing = productDao.getProductById(product.id)
        val entity = ProductEntity(
            id = product.id, title = product.title, description = product.description, price = product.price,
            category = product.category, condition = product.condition, location = product.building_location,
            imageUrl = product.image_urls.firstOrNull(), localImagePath = localPath ?: existing?.localImagePath,
            sellerId = product.seller_id ?: product.seller?.id, sellerName = product.seller?.name ?: existing?.sellerName,
            sellerMajor = product.seller?.major ?: existing?.sellerMajor, storeId = product.store_id,
            createdAt = product.created_at, isFavorite = existing?.isFavorite ?: false, lastViewedAt = existing?.lastViewedAt ?: System.currentTimeMillis()
        )
        productDao.insertProduct(entity)
    }

    suspend fun createProduct(title: String, description: String, category: String, location: String, price: Double, condition: String, storeId: String?, imageUri: Uri?, imageBitmap: Bitmap? = null): Product {
        val titlePart = title.toRequestBody("text/plain".toMediaType())
        val descriptionPart = description.toRequestBody("text/plain".toMediaType())
        val categoryPart = category.toRequestBody("text/plain".toMediaType())
        val locationPart = location.toRequestBody("text/plain".toMediaType())
        val pricePart = price.toString().toRequestBody("text/plain".toMediaType())
        val conditionPart = condition.toRequestBody("text/plain".toMediaType())
        val storeIdPart = storeId?.toRequestBody("text/plain".toMediaType())

        val imagePart = when {
            imageUri != null -> {
                val bytes = context.contentResolver.openInputStream(imageUri)?.readBytes()
                bytes?.let { MultipartBody.Part.createFormData("images", "product.jpg", it.toRequestBody("image/*".toMediaType())) }
            }
            imageBitmap != null -> {
                val stream = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                val bytes = stream.toByteArray()
                MultipartBody.Part.createFormData("images", "product.jpg", bytes.toRequestBody("image/jpeg".toMediaType()))
            }
            else -> null
        }
        return api.createProduct(titlePart, descriptionPart, categoryPart, locationPart, pricePart, conditionPart, storeIdPart, imagePart)
    }

    suspend fun getProductsByUser(userId: String): kotlin.Result<List<Product>> {
        return try {
            val response = api.getProductsByUser(userId)
            val products = response.items ?: emptyList()
            kotlin.Result.success(products)
        } catch (e: Exception) {
            val cached = productDao.getProductsBySeller(userId).map { mapEntityToProduct(it) }
            if (cached.isNotEmpty()) kotlin.Result.success(cached) else kotlin.Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String): kotlin.Result<Unit> {
        return try {
            api.deleteProduct(productId)
            kotlin.Result.success(Unit)
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }

    suspend fun getAllLocalProducts(): List<Product> = productDao.getAllProducts().map { mapEntityToProduct(it) }

    private fun mapEntityToProduct(it: ProductEntity): Product {
        return Product(
            id = it.id,
            title = it.title,
            description = it.description,
            price = it.price,
            category = it.category,
            condition = it.condition,
            building_location = it.location,
            image_urls = if (!it.localImagePath.isNullOrEmpty() && File(it.localImagePath).exists()) listOf(it.localImagePath) else listOfNotNull(it.imageUrl),
            seller_id = it.sellerId,
            seller = if (it.sellerName != null) UserProfile(id = it.sellerId ?: "", name = it.sellerName, major = it.sellerMajor) else null,
            store_id = it.storeId,
            created_at = it.createdAt
        )
    }
}
