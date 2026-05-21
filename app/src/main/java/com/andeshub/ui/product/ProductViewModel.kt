package com.andeshub.ui.product

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andeshub.data.local.SessionManager
import com.andeshub.data.local.UserPreferencesManager
import com.andeshub.data.local.DraftManager
import com.andeshub.data.local.ProductCache
import com.andeshub.data.model.*
import com.andeshub.data.repository.ProductRepository
import com.andeshub.data.repository.StoreRepository
import com.andeshub.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.andeshub.data.local.FavoritesEvent
import com.andeshub.data.remote.ApiService
import kotlinx.coroutines.supervisorScope

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

class ProductViewModel(private val context: Context) : ViewModel() {

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

    private val _isGridView = MutableStateFlow(userPrefs.isGridViewEnabled())
    val isGridView: StateFlow<Boolean> = _isGridView

    private val _toggleFavoriteError = MutableStateFlow<String?>(null)
    val toggleFavoriteError: StateFlow<String?> = _toggleFavoriteError

    private val _visitStats = MutableStateFlow<ProductVisitStats?>(null)
    val visitStats: StateFlow<ProductVisitStats?> = _visitStats

    private val prefs = context.getSharedPreferences("favorites_count", Context.MODE_PRIVATE)

    fun clearToggleFavoriteError() {
        _toggleFavoriteError.value = null
    }

    // ── Favorites count local ──
    fun saveFavoritesCount(productId: String, count: Int) {
        prefs.edit().putInt("count_$productId", count).apply()
    }

    fun getLocalFavoritesCount(productId: String): Int {
        return prefs.getInt("count_$productId", 0)
    }

    // ── Pending favorites sync ──
    fun addPendingFavorite(productId: String) {
        val pending = prefs.getStringSet("pending_add", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        pending.add(productId)
        prefs.edit().putStringSet("pending_add", pending).apply()
    }

    fun removePendingFavorite(productId: String) {
        val pending = prefs.getStringSet("pending_add", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        pending.remove(productId)
        prefs.edit().putStringSet("pending_add", pending).apply()
    }

    fun getPendingFavorites(): Set<String> {
        return prefs.getStringSet("pending_add", emptySet()) ?: emptySet()
    }

    fun addPendingRemove(productId: String) {
        val pending = prefs.getStringSet("pending_remove", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        pending.add(productId)
        prefs.edit().putStringSet("pending_remove", pending).apply()
    }

    fun removePendingRemove(productId: String) {
        val pending = prefs.getStringSet("pending_remove", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        pending.remove(productId)
        prefs.edit().putStringSet("pending_remove", pending).apply()
    }

    fun getPendingRemoves(): Set<String> {
        return prefs.getStringSet("pending_remove", emptySet()) ?: emptySet()
    }

    fun syncPendingFavorites() {
        if (!isNetworkAvailable()) return
        viewModelScope.launch(Dispatchers.IO) {
            getPendingFavorites().forEach { productId ->
                try {
                    api.addFavorite(productId)
                    removePendingFavorite(productId)
                    Log.d("ProductViewModel", "Synced pending add: $productId")
                } catch (e: Exception) {
                    Log.e("ProductViewModel", "Error syncing add: ${e.message}")
                }
            }
            getPendingRemoves().forEach { productId ->
                try {
                    api.removeFavorite(productId)
                    removePendingRemove(productId)
                    Log.d("ProductViewModel", "Synced pending remove: $productId")
                } catch (e: Exception) {
                    Log.e("ProductViewModel", "Error syncing remove: ${e.message}")
                }
            }
        }
    }

    init {
        loadUserStores()
        loadViewedTimestamps()
        syncPendingFavorites()
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun getProducts(
        search: String? = null,
        category: String? = null,
        condition: String? = null,
        priceSort: String? = null
    ) {
        viewModelScope.launch {
            val localProducts = try {
                withContext(Dispatchers.IO) { repository.getAllLocalProducts() }
            } catch (e: Exception) {
                emptyList<Product>()
            }

            var filteredLocal = localProducts.filter { product ->
                val matchesSearch = search == null || product.title.contains(search, ignoreCase = true)
                val matchesCategory = category == null || product.category == category
                val matchesCondition = condition == null || product.condition == condition
                matchesSearch && matchesCategory && matchesCondition
            }

            // Aplicar ordenamiento localmente por precio
            if (priceSort != null) {
                filteredLocal = if (priceSort == "asc") {
                    filteredLocal.sortedBy { it.price }
                } else {
                    filteredLocal.sortedByDescending { it.price }
                }
            }

            _uiState.value = ProductUiState.Success(filteredLocal)

            if (!isNetworkAvailable()) {
                _isOfflineMode.value = true
                return@launch
            }

            try {
                supervisorScope {
                    val productsDeferred = async(Dispatchers.IO) {
                        repository.getProducts(search, category, condition, priceSort)
                    }
                    val trendingDeferred = async(Dispatchers.IO) {
                        try { api.getTrendingCategories() } catch (e: Exception) { emptyList<TrendingCategory>() }
                    }

                    try {
                        val remoteProducts = productsDeferred.await()
                        val trending = trendingDeferred.await()

                        _uiState.value = ProductUiState.Success(remoteProducts, trending)
                        _isOfflineMode.value = false

                        launch(Dispatchers.IO) {
                            remoteProducts.forEach {
                                try { repository.saveProductLocally(it) } catch (e: Exception) {}
                            }
                        }
                    } catch (netEx: Exception) {
                        _isOfflineMode.value = true
                    }
                }
            } catch (e: Exception) {
                _isOfflineMode.value = true
            }
        }
    }

    fun toggleViewMode() {
        val newMode = !_isGridView.value
        userPrefs.setGridViewEnabled(newMode)
        _isGridView.value = newMode
    }

    fun saveDraft(title: String, description: String) {
        draftManager.saveDraft("$title|$description")
    }

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
            try {
                val timestamps = db.productDao().getAllViewedTimestamps()
                    .associate { it.id to it.lastViewedAt }
                _viewedTimestamps.value = timestamps
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading viewed timestamps", e)
            }
        }
    }

    private fun loadUserStores() {
        if (!isNetworkAvailable()) return
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
            try {
                currentProduct?.let { 
                    _uiState.value = ProductUiState.Success(listOf(it))
                    _isOfflineMode.value = !isNetworkAvailable()
                    launch(Dispatchers.IO) {
                        try {
                            repository.saveProductLocally(it)
                        } catch (e: Exception) {}
                    }
                }
            } catch (e: Exception) {
                val localProduct = try {
                    withContext(Dispatchers.IO) { repository.getProductOffline(productId) }
                } catch (dbEx: Exception) { null }
                
                if (localProduct != null) {
                    _uiState.value = ProductUiState.Success(listOf(localProduct))
                    _isOfflineMode.value = true
                } else {
                    _uiState.value = ProductUiState.Error("Product not available offline", isOffline = true)
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
            if (!isNetworkAvailable()) {
                val product = db.productDao().getProductById(productId)
                withContext(Dispatchers.Main) {
                    _isFavorited.value = product?.isFavorite == true
                }
                return@launch
            }
            try {
                val favorites = api.getFavorites()
                // Primero desmarca todos los productos
                db.productDao().unmarkAllFavorites()
                // Luego marca solo los que son favoritos del usuario
                favorites.forEach { product ->
                    db.productDao().markAsFavorite(product.id)
                }
                withContext(Dispatchers.Main) {
                    _isFavorited.value = favorites.any { it.id == productId }
                }
            } catch (e: Exception) {
                val product = db.productDao().getProductById(productId)
                withContext(Dispatchers.Main) {
                    _isFavorited.value = product?.isFavorite == true
                }
            }
        }
    }


    fun toggleFavorite(productId: String, product: Product? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (_isFavorited.value) {
                    // Actualiza Room inmediatamente
                    db.productDao().unmarkAsFavorite(productId)
                    withContext(Dispatchers.Main) {
                        _isFavorited.value = false
                        val newCount = (_favoritesCount.value - 1).coerceAtLeast(0)
                        _favoritesCount.value = newCount
                        saveFavoritesCount(productId, newCount)
                        FavoritesEvent.notifyChanged()
                    }
                    // Intenta sincronizar con el backend
                    if (isNetworkAvailable()) {
                        api.removeFavorite(productId)
                    } else {
                        addPendingRemove(productId)
                    }
                } else {
                    // Guarda en Room inmediatamente
                    if (product != null) {
                        db.productDao().insertProduct(
                            com.andeshub.data.local.ProductEntity(
                                id = product.id,
                                title = product.title,
                                description = product.description,
                                price = product.price,
                                category = product.category,
                                condition = product.condition,
                                location = product.building_location,
                                imageUrl = product.image_urls.firstOrNull(),
                                sellerId = product.seller_id,
                                storeId = product.store_id,
                                createdAt = product.created_at,
                                isFavorite = true
                            )
                        )
                    } else {
                        db.productDao().markAsFavorite(productId)
                    }
                    withContext(Dispatchers.Main) {
                        _isFavorited.value = true
                        val newCount = _favoritesCount.value + 1
                        _favoritesCount.value = newCount
                        saveFavoritesCount(productId, newCount)
                        FavoritesEvent.notifyChanged()
                    }
                    // Intenta sincronizar con el backend
                    if (isNetworkAvailable()) {
                        api.addFavorite(productId)
                    } else {
                        addPendingFavorite(productId)
                    }
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error toggling favorite: ${e.message}")
            }
        }
    }



    fun recordProductView(product: Product, source: String? = null) {
        viewModelScope.launch(Dispatchers.Main) { 
            launch(Dispatchers.IO) {
                try {
                    repository.saveProductLocally(product)
                    repository.markProductAsViewed(product.id)
                    if (isNetworkAvailable() && source != null) {
                        api.recordProductVisit(ProductVisitRequest(product.id, source))
                    }
                } catch (e: Exception) {}
            }

            try {
                loadProductStats(product.id)
                if (isNetworkAvailable() && !isOwner(product)) {
                    api.recordInteraction(RecordInteractionRequest(product.id, product.seller_id))
                }
            } catch (e: Exception) {}
        }
    }

    fun loadProductStats(productId: String) {
        val cached = ProductCache.getStats(productId)
        if (cached != null) {
            _productStats.value = cached
            return
        }

        if (!isNetworkAvailable()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val stats = api.getProductStats(productId)
                ProductCache.saveStats(productId, stats)
                withContext(Dispatchers.Main) {
                    _productStats.value = stats
                }
            } catch (e: Exception) {
                _productStats.value = ProductStats(0, null, null)
            }
        }
    }

    fun loadVisitStats(productId: String? = null) {
        if (!isNetworkAvailable()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val stats = api.getProductVisitStats(productId)
                withContext(Dispatchers.Main) {
                    _visitStats.value = stats
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading visit stats", e)
            }
        }
    }

    fun loadFavoritesCount(productId: String) {
        // Carga el count local inmediatamente
        _favoritesCount.value = getLocalFavoritesCount(productId)

        if (!isNetworkAvailable()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = api.getFavoritesCount(productId)
                // Guarda en local para uso offline
                saveFavoritesCount(productId, result.count)
                withContext(Dispatchers.Main) {
                    _favoritesCount.value = result.count
                }
            } catch (e: Exception) {
                _favoritesCount.value = getLocalFavoritesCount(productId)
            }
        }
    }

    fun getWhatsAppContactUrl(productId: String, onUrlReady: (String) -> Unit, onError: (String) -> Unit) {
        if (!isNetworkAvailable()) {
            onError("No internet connection")
            return
        }
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
        if (!isNetworkAvailable()) {
            _uiState.value = ProductUiState.Error("Internet connection is required to post.")
            return
        }
        
        // Robust token retrieval: Prefs -> Memory -> Error
        val token = sessionManager.getAccessToken() ?: RetrofitClient.getToken() ?: ""
        
        if (token.isEmpty()) {
            _uiState.value = ProductUiState.Error("No active session. Please login again.")
            return
        }
        
        // Sync Retrofit just in case it was null but prefs had it
        if (RetrofitClient.getToken() == null && token.isNotEmpty()) {
            RetrofitClient.setToken(token)
        }

        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            try {
                val product = withContext(Dispatchers.IO) {
                    repository.createProduct(token, title, description, category, location, price, condition, storeId, imageUri, imageBitmap)
                }
                withContext(Dispatchers.IO) {
                    try { repository.saveProductLocally(product) } catch (e: Exception) {}
                }
                _uiState.value = ProductUiState.Created(product)
                clearDraft()
            } catch (e: Exception) {
                _uiState.value = ProductUiState.Error(e.message ?: "Error creating product")
            }
        }
    }

    fun recordPurchaseFromFavorite(productId: String, wasFavorited: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                api.recordPurchaseFromFavorite(
                    PurchaseFromFavoriteRequest(productId, wasFavorited)
                )
                Log.d("ProductViewModel", "Purchase recorded — wasFavorited: $wasFavorited")
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error recording purchase: ${e.message}")
            }
        }
    }
    private val _topCategories = MutableStateFlow<List<ApiService.TopCategoryResponse>>(emptyList())
    val topCategories: StateFlow<List<ApiService.TopCategoryResponse>> = _topCategories

    fun loadTopCategoriesThisWeek() {
        if (!isNetworkAvailable()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = api.getTopCategoriesThisWeek()
                Log.d("TopCategories", "Result: $result — size: ${result.size}")
                withContext(Dispatchers.Main) {
                    _topCategories.value = result
                }
            } catch (e: Exception) {
                Log.e("TopCategories", "Error: ${e.message}")
            }
        }
    }

}
