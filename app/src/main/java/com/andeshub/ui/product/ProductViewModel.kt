package com.andeshub.ui.product

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andeshub.data.local.SessionManager
import com.andeshub.data.model.Product
import com.andeshub.data.model.TrendingCategory
import com.andeshub.data.model.ProductStats
import com.andeshub.data.model.RecordInteractionRequest
import com.andeshub.data.model.Store
import com.andeshub.data.repository.ProductRepository
import com.andeshub.data.repository.StoreRepository
import com.andeshub.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ProductUiState {
    object Idle : ProductUiState()
    object Loading : ProductUiState()
    data class Success(
        val products: List<Product>,
        val trendingCategories: List<TrendingCategory> = emptyList()
    ) : ProductUiState()
    data class Created(val product: Product) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
}

class ProductViewModel(context: Context) : ViewModel() {

    private val repository = ProductRepository(context)
    private val storeRepository = StoreRepository(context)
    private val sessionManager = SessionManager(context)
    private val api = RetrofitClient.apiService

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

    init {
        loadUserStores()
    }

    private fun loadUserStores() {
        viewModelScope.launch {
            try {
                val stores = storeRepository.getMyStores()
                _userStores.value = stores
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading user stores", e)
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
                val products = repository.getProducts(search, category, condition, priceSort)
                val trending = try { api.getTrendingCategories() } catch (e: Exception) { emptyList() }
                _uiState.value = ProductUiState.Success(products, trending)
            } catch (e: Exception) {
                _uiState.value = ProductUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun isOwner(product: Product): Boolean {
        val currentUserId = sessionManager.getUserId()
        val firstName = sessionManager.getUserFirstName() ?: ""
        val lastName = sessionManager.getUserLastName() ?: ""
        val currentUserName = "$firstName $lastName".trim()
        val sellerName = product.seller?.name?.trim() ?: ""

        val isByOwnerId = currentUserId != null && currentUserId == product.seller_id
        val isByOwnerName = currentUserName.isNotEmpty() && currentUserName.equals(sellerName, ignoreCase = true)

        Log.d("ProductViewModel", "isOwner Check - ID Match: $isByOwnerId, Name Match: $isByOwnerName")
        Log.d("ProductViewModel", "Details - CurrentID: $currentUserId, SellerID: ${product.seller_id}")
        Log.d("ProductViewModel", "Details - CurrentName: '$currentUserName', SellerName: '$sellerName'")

        return isByOwnerId || isByOwnerName
    }

    fun checkIfFavorited(productId: String) {
        viewModelScope.launch {
            try {
                val favorites = api.getFavorites()
                _isFavorited.value = favorites.any { it.id == productId }
            } catch (e: Exception) {
                _isFavorited.value = false
            }
        }
    }

    fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            try {
                if (_isFavorited.value) {
                    val response = api.removeFavorite(productId)
                    if (response.isSuccessful) {
                        _isFavorited.value = false
                        _favoritesCount.value = (_favoritesCount.value - 1).coerceAtLeast(0)
                    }
                } else {
                    val response = api.addFavorite(productId)
                    if (response.isSuccessful) {
                        _isFavorited.value = true
                        _favoritesCount.value = _favoritesCount.value + 1
                    }
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error toggling favorite: ${e.message}")
            }
        }
    }

    fun recordProductView(product: Product) {
        viewModelScope.launch {
            try {
                loadProductStats(product.id)
                
                // No contar la interacción si el usuario es el dueño
                if (!isOwner(product)) {
                    api.recordInteraction(RecordInteractionRequest(product.id, product.seller_id))
                } else {
                    Log.d("ProductViewModel", "Interaction not recorded: User is owner (by ID or Name)")
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error recording view", e)
            }
        }
    }

    fun loadProductStats(productId: String) {
        viewModelScope.launch {
            try {
                val stats = api.getProductStats(productId)
                _productStats.value = stats
            } catch (e: Exception) {
                _productStats.value = ProductStats(0, null, null)
            }
        }
    }

    fun loadFavoritesCount(productId: String) {
        viewModelScope.launch {
            try {
                val result = api.getFavoritesCount(productId)
                _favoritesCount.value = result.count
            } catch (e: Exception) {
                _favoritesCount.value = 0
            }
        }
    }

    fun getWhatsAppContactUrl(productId: String, onUrlReady: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.getWhatsAppContactUrl(productId)
                Log.d("ProductViewModel", "WhatsApp URL: ${response.url}")
                onUrlReady(response.url)
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error getting WhatsApp URL", e)
                val errorMessage = if (e is retrofit2.HttpException && e.code() == 404) {
                    "Seller phone number not available or product not found"
                } else {
                    "Error connecting to server"
                }
                onError(errorMessage)
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
                val product = repository.createProduct(token, title, description, category, location, price, condition, storeId, imageUri, imageBitmap)
                _uiState.value = ProductUiState.Created(product)
            } catch (e: Exception) {
                _uiState.value = ProductUiState.Error(e.message ?: "Error creating product")
            }
        }
    }
}