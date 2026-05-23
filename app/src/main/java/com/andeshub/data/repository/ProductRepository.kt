package com.andeshub.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.andeshub.data.local.*
import com.andeshub.data.model.Product
import com.andeshub.data.model.UserProfile
import com.andeshub.data.remote.RetrofitClient
import com.andeshub.data.remote.ApiService
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
     * ESTRATEGIA [A]: Multi-threading / Concurrency
     * Procesa y guarda un borrador usando paralelismo para no bloquear la UI.
     */
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
        // [A] Ejecutamos el procesamiento de imagen en paralelo (Hilo de CPU)
        val imageJob = async(Dispatchers.Default) {
            val fileName = "draft_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            
            try {
                FileOutputStream(file).use { out ->
                    if (imageUri != null) {
                        context.contentResolver.openInputStream(imageUri)?.use { it.copyTo(out) }
                    } else if (imageBitmap != null) {
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                    }
                }
                
                // [C] REQUERIMIENTO: Caching - Miniatura en memoria para respuesta instantánea
                val original = BitmapFactory.decodeFile(file.absolutePath)
                if (original != null) {
                    val thumb = Bitmap.createScaledBitmap(original, 150, 150, true)
                    DraftImageCache.put(fileName, thumb)
                }
                
                file.absolutePath
            } catch (e: Exception) {
                null
            }
        }

        // [A] Esperamos a que el hilo paralelo de la imagen termine
        val localPath = imageJob.await()

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

        // [A] Guardamos en la BD en un hilo de I/O para no trabar la interfaz
        withContext(Dispatchers.IO) {
            // [B] ESTRATEGIA: Local Storage Management (Límite de 10 borradores)
            draftDao.insertWithLimit(draft, 10)
        }
    }

    suspend fun syncPendingDrafts() = withContext(Dispatchers.IO) {
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
                try { api.recordDraftPublished() } catch (e: Exception) {}
            } catch (e: Exception) {
                android.util.Log.e("ProductRepository", "Error syncing: ${e.message}")
            }
        }
    }

    suspend fun saveProductLocally(product: Product, localPath: String? = null) = withContext(Dispatchers.IO) {
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
            android.util.Log.e("ProductRepository", "Error marking product: ${e.message}")
        }
    }

    suspend fun markProductAsViewedByUser(productId: String) {
        try {
            productDao.markAsViewedByUser(productId)
        } catch (e: Exception) {
            android.util.Log.e("ProductRepository", "Error marking product by user: ${e.message}")
        }
    }

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
                val bytes = context.contentResolver.openInputStream(imageUri)?.readBytes()
                bytes?.let { 
                    val requestBody = it.toRequestBody("image/jpeg".toMediaType())
                    MultipartBody.Part.createFormData("images", "product.jpg", requestBody)
                }
            }
            imageBitmap != null -> {
                val stream = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                val requestBody = stream.toByteArray().toRequestBody("image/jpeg".toMediaType())
                MultipartBody.Part.createFormData("images", "product.jpg", requestBody)
            }
            else -> null
        }

        return api.createProduct(
            titlePart, descriptionPart, categoryPart, locationPart, pricePart, conditionPart, storeIdPart, imagePart
        )
    }

    suspend fun getProductsByUser(userId: String): Result<List<Product>> {
        return try {
            val response = api.getProductsByUser(userId)
            Result.success(response.items ?: emptyList())
        } catch (e: Exception) {
            val cached = productDao.getProductsBySeller(userId).map { mapEntityToProduct(it) }
            if (cached.isNotEmpty()) Result.success(cached) else Result.failure(e)
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

    suspend fun updateProduct(productId: String, request: ApiService.UpdateProductRequest): Product {
        return api.updateProduct(productId, request)
    }
}
