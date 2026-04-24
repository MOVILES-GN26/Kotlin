package com.andeshub.ui.product

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andeshub.data.local.SessionManager
import com.andeshub.data.local.UserPreferencesManager
import com.andeshub.data.local.DraftManager
import com.andeshub.data.local.ProductCache
import com.andeshub.data.model.Product
import com.andeshub.data.model.TrendingCategory
import com.andeshub.data.model.ProductStats
import com.andeshub.data.model.RecordInteractionRequest
import com.andeshub.data.model.Store
import com.andeshub.data.repository.ProductRepository
import com.andeshub.data.repository.StoreRepository
import com.andeshub.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ProductUiState {
    object Idle : ProductUiState()
    object Loading : ProductUiState()
    data class Success(
        val products: List<Product>,
        val trendingCategories: List<TrendingCategory> = emptyList()
    ) : ProductUiState()
    data class Created(val product: Product) : ProductUiState()
    data class Error(val message: String, val isOffline: Boolean = false) : ProductUiState()
}

class ProductViewModel(context: Context) : ViewModel() {

    private val repository = ProductRepository(context)
    private val storeRepository = StoreRepository(context)
    private val sessionManager = SessionManager(context)
    private val userPrefs = UserPreferencesManager(context)
    private val draftManager = DraftManager(context)
    private val api = RetrofitClient.apiService
    private val db = com.andeshub.data.local.AppDatabase.getInstance(context)

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Idle)
    val uiState: StateFlow<ProductUiState> = _uiState

    private val _productStats = MutableStateFlow<ProductStats?>(null)
    val productStats: StateFlow<ProductStats?> = _productStats

    private val _userStores = MutableStateFlow<List<Store>>(emptyList())
    val userStores: StateFlow<List<Store>> = _userStores

    private val _favoritesCount = MutableStateFlow(0)
    val favoritesCount: StateFlow<Int> = _favoritesCount

    private val _isFavorited = MutableStateFlow(false)
    val isFavorited: StateFlow<Boolean> = _isFavorited

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode

    private val _viewedTimestamps = MutableStateFlow<Map<String, Long>>(emptyMap())
    val viewedTimestamps: StateFlow<Map<String, Long>> = _viewedTimestamps

    init {
        loadUserStores()
        loadViewedTimestamps()
    }

    /**
     * ESTRATEGIA: ARCHIVOS LOCALES
     * Guarda el texto del borrador en un archivo físico.
     */
    fun saveDraft(title: String, description: String) {
        draftManager.saveDraft("$title|$description")
    }

    /**
     * ESTRATEGIA: ARCHIVOS LOCALES
     * Carga el contenido del archivo físico.
     */
    fun loadDraft(): Pair<String, String>? {
        val raw = draftManager.getDraft() ?: return null
        val parts = raw.split("|")
        return if (parts.size >= 2) {
            Pair(parts[0], parts[1])
        } else {
            null
        }
    }

    fun clearDraft() {
        draftManager.clearDraft()
    }

    fun loadViewedTimestamps() {
        viewModelScope.launch(Dispatchers.IO) {
            val timestamps = db.productDao().getAllViewedTimestamps()
                .associate { it.id to it.lastViewedAt }
            _viewedTimestamps.value = timestamps
        }
    }

    private fun loadUserStores() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val stores = storeRepository.getMyStores()
                withContext(Dispatchers.Main) {
                    _userStores.value = stores
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading user stores", e)
            }
        }
    }

    fun loadLocalProducts() {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            try {
                val localProducts = withContext(Dispatchers.IO) {
                    repository.getAllLocalProducts()
                }
                _uiState.value = ProductUiState.Success(localProducts)
                _isOfflineMode.value = true
            } catch (e: Exception) {
                _uiState.value = ProductUiState.Error("Error loading local products")
            }
        }
    }

    fun getProductDetail(productId: String, currentProduct: Product?) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            try {
                currentProduct?.let { 
                    _uiState.value = ProductUiState.Success(listOf(it))
                    _isOfflineMode.value = false
                    launch(Dispatchers.IO) {
                        repository.saveProductLocally(it)
                    }
                }
            } catch (e: Exception) {
                val localProduct = withContext(Dispatchers.IO) {
                    repository.getProductOffline(productId)
                }
                
                if (localProduct != null) {
                    _uiState.value = ProductUiState.Success(listOf(localProduct))
                    _isOfflineMode.value = true
                } else {
                    _uiState.value = ProductUiState.Error("Product not available offline", isOffline = true)
                }
            }
        }
    }

    fun getProducts(
        search: String? = null,
        category: String? = null,
        condition: String? = null,
        priceSort: String? = null
    ) {
        viewModelScope.launch {
            if (_uiState.value !is ProductUiState.Success) {
                _uiState.value = ProductUiState.Loading
            }
            try {
                // ESTRATEGIA DE MULTITHREADING ASÍNCRONO (RÚBRICA)
                val productsDeferred = async(Dispatchers.IO) {
                    repository.getProducts(search, category, condition, priceSort)
                }
                val trendingDeferred = async(Dispatchers.IO) {
                    try { api.getTrendingCategories() } catch (e: Exception) { emptyList() }
                }

                val products = productsDeferred.await()
                val trending = trendingDeferred.await()
                
                if (products.isEmpty() && search == null && category == null && condition == null && priceSort == null) {
                    val localProducts = withContext(Dispatchers.IO) { repository.getAllLocalProducts() }
                    if (localProducts.isNotEmpty()) {
                        _uiState.value = ProductUiState.Success(localProducts, trending)
                        _isOfflineMode.value = true
                        return@launch
                    }
                }

                _uiState.value = ProductUiState.Success(products, trending)
                _isOfflineMode.value = false
            } catch (e: Exception) {
                val localProducts = withContext(Dispatchers.IO) { repository.getAllLocalProducts() }
                if (localProducts.isNotEmpty()) {
                    _uiState.value = ProductUiState.Success(localProducts)
                    _isOfflineMode.value = true
                } else {
                    _uiState.value = ProductUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun isOwner(product: Product): Boolean {
        val currentUserId = sessionManager.getUserId()
        val firstName = sessionManager.getUserFirstName() ?: ""
        val lastName = sessionManager.getUserLastName() ?: ""
        val currentUserName = "$firstName $lastName".trim()
        val sellerName = product.seller?.name?.trim() ?: ""

        return (currentUserId != null && currentUserId == product.seller_id) || 
               (currentUserName.isNotEmpty() && currentUserName.equals(sellerName, ignoreCase = true))
    }

    fun checkIfFavorited(productId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val favorites = api.getFavorites()
                withContext(Dispatchers.Main) {
                    _isFavorited.value = favorites.any { it.id == productId }
                }
            } catch (e: Exception) {
                _isFavorited.value = false
            }
        }
    }

    fun toggleFavorite(productId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (_isFavorited.value) {
                    val response = api.removeFavorite(productId)
                    if (response.isSuccessful) {
                        withContext(Dispatchers.Main) {
                            _isFavorited.value = false
                            _favoritesCount.value = (_favoritesCount.value - 1).coerceAtLeast(0)
                        }
                    }
                } else {
                    val response = api.addFavorite(productId)
                    if (response.isSuccessful) {
                        withContext(Dispatchers.Main) {
                            _isFavorited.value = true
                            _favoritesCount.value = _favoritesCount.value + 1
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error toggling favorite: ${e.message}")
            }
        }
    }

    fun recordProductView(product: Product) {
        viewModelScope.launch(Dispatchers.Main) { 
            launch(Dispatchers.IO) {
                repository.saveProductLocally(product)
                repository.markProductAsViewed(product.id)
            }

            try {
                loadProductStats(product.id)
                if (!isOwner(product)) {
                    api.recordInteraction(RecordInteractionRequest(product.id, product.seller_id))
                }
            } catch (e: Exception) {
                Log.e("EvC", "Network failed, viewing cached stats if available")
            }
        }
    }

    /**
     * ESTRATEGIA: LRU CACHE (CACHING - RÚBRICA)
     * Implementación manual para evitar llamadas repetitivas a la API.
     */
    fun loadProductStats(productId: String) {
        // 1. INTENTO LEER DE MI CACHÉ MANUAL (Estrategia LRU)
        val cached = ProductCache.getStats(productId)
        if (cached != null) {
            _productStats.value = cached
            Log.d("ProductViewModel", "Stats loaded from ProductCache (LRU) for $productId")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val stats = api.getProductStats(productId)
                
                // 2. LO GUARDO EN MI CACHÉ PARA LA PRÓXIMA VEZ
                ProductCache.saveStats(productId, stats)
                
                withContext(Dispatchers.Main) {
                    _productStats.value = stats
                }
            } catch (e: Exception) {
                _productStats.value = ProductStats(0, null, null)
            }
        }
    }

    fun loadFavoritesCount(productId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = api.getFavoritesCount(productId)
                withContext(Dispatchers.Main) {
                    _favoritesCount.value = result.count
                }
            } catch (e: Exception) {
                _favoritesCount.value = 0
            }
        }
    }

    fun getWhatsAppContactUrl(productId: String, onUrlReady: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = api.getWhatsAppContactUrl(productId)
                withContext(Dispatchers.Main) {
                    onUrlReady(response.url)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorMessage = if (e is retrofit2.HttpException && e.code() == 404) {
                        "Seller phone number not available or product not found"
                    } else {
                        "Error connecting to server"
                    }
                    onError(errorMessage)
                }
            }
        }
    }

    fun createProduct(title: String, description: String, category: String, location: String, price: Double, condition: String, storeId: String?, imageUri: Uri?, imageBitmap: Bitmap? = null) {
        val token = sessionManager.getAccessToken() ?: ""
        if (token.isEmpty()) {
            _uiState.value = ProductUiState.Error("No active session. Please login again.")
            return
        }
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            try {
                val product = withContext(Dispatchers.IO) {
                    repository.createProduct(token, title, description, category, location, price, condition, storeId, imageUri, imageBitmap)
                }
                _uiState.value = ProductUiState.Created(product)
                clearDraft()
            } catch (e: Exception) {
                _uiState.value = ProductUiState.Error(e.message ?: "Error creating product")
            }
        }
    }
}
