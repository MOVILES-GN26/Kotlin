package com.andeshub.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andeshub.data.local.AppDatabase
import com.andeshub.data.local.HomeLruCache
import com.andeshub.data.local.RecentlyViewedLruCache
import com.andeshub.data.local.SearchPreferences
import com.andeshub.data.local.TrendingCategoriesPreferences
import com.andeshub.data.model.Product
import com.andeshub.data.model.TrendingCategory
import com.andeshub.data.remote.RetrofitClient
import com.andeshub.data.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class HomeUiState {
    object Idle    : HomeUiState()
    object Loading : HomeUiState()
    data class Success(
        val products: List<Product>,
        val trendingCategories: List<TrendingCategory> = emptyList()
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.apiService
    private val repository = ProductRepository(application)
    private val searchPreferences = SearchPreferences(application)

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory

    private val _viewedTimestamps = MutableStateFlow<Map<String, Long>>(emptyMap())
    val viewedTimestamps: StateFlow<Map<String, Long>> = _viewedTimestamps

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory
    private val trendingPreferences = TrendingCategoriesPreferences(application)

    init {
        viewModelScope.launch {
            searchPreferences.searchHistory.collect { history ->
                _searchHistory.value = history
            }
        }
        loadData()
        loadViewedTimestamps()
    }

    fun loadViewedTimestamps() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val timestamps = AppDatabase.getInstance(getApplication())
                    .productDao().getAllViewedTimestamps()
                    .associate { it.id to it.lastViewedAt }
                _viewedTimestamps.value = timestamps
            } catch (e: Exception) {
                _viewedTimestamps.value = emptyMap()
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {  // ← sin Dispatchers.Main
            if (_uiState.value !is HomeUiState.Success) {
                withContext(Dispatchers.Main) {
                    _uiState.value = HomeUiState.Loading
                }
            }

            val productsDeferred = async(Dispatchers.IO) {
                try {
                    val products = repository.getProducts()
                    products.forEach {
                        try { repository.saveProductLocally(it) } catch (e: Exception) { }
                    }
                    products
                } catch (e: Exception) {
                    android.util.Log.d("HomeViewModel", "Sin internet, cargando desde Room")
                    repository.getAllLocalProducts()
                }
            }

            val trendingDeferred = async(Dispatchers.IO) {
                try {
                    val trending = api.getTrendingCategories()
                    trendingPreferences.save(trending)
                    trending
                } catch (e: Exception) {
                    trendingPreferences.load()
                }
            }

            try {
                val products = productsDeferred.await()
                val trending = trendingDeferred.await()
                withContext(Dispatchers.Main) {
                    _uiState.value = HomeUiState.Success(
                        products = products,
                        trendingCategories = trending
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = HomeUiState.Error(e.message ?: "Error desconocido")
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun search() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val currentTrending =
                if (currentState is HomeUiState.Success) currentState.trendingCategories else emptyList()

            val query = _searchQuery.value

            // Guarda en historial DataStore
            if (query.isNotBlank()) {
                searchPreferences.saveSearch(query)
            }

            // Revisa si ya está en el LRU
            val cached = HomeLruCache.get(query)
            if (cached != null) {
                android.util.Log.d("HomeLruCache", "Cargado desde caché: '$query'")
                _uiState.value = HomeUiState.Success(
                    products = cached,
                    trendingCategories = currentTrending
                )
                return@launch
            }

            // Si no está en caché llama a la API
            _uiState.value = HomeUiState.Loading
            try {
                val products = repository.getProducts(search = query)

                // Guarda en LRU para la próxima vez
                HomeLruCache.put(query, products)

                _uiState.value = HomeUiState.Success(
                    products = products,
                    trendingCategories = currentTrending
                )
            } catch (e: Exception) {
                // Fallback a búsqueda local si falla la red
                val localProducts = repository.getAllLocalProducts().filter {
                    it.title.contains(query, ignoreCase = true)
                }
                _uiState.value = HomeUiState.Success(
                    products = localProducts,
                    trendingCategories = currentTrending
                )
            }
        }
    }

    fun selectHistoryItem(query: String) {
        _searchQuery.value = query
        search()
    }

    private val _recentlyViewed = MutableStateFlow<List<Product>>(emptyList())
    val recentlyViewed: StateFlow<List<Product>> = _recentlyViewed

    private val _showRecentlyViewed = MutableStateFlow(false)
    val showRecentlyViewed: StateFlow<Boolean> = _showRecentlyViewed

    fun toggleFeedMode() {
        _showRecentlyViewed.value = !_showRecentlyViewed.value
        if (_showRecentlyViewed.value) {
            loadRecentlyViewed()
        }
    }

    fun loadRecentlyViewed() {
        // Primero revisa el LRU cache
        val cached = RecentlyViewedLruCache.getAll()
        if (cached.isNotEmpty()) {
            _recentlyViewed.value = cached
            android.util.Log.d("RecentlyViewed", "Cargado desde caché: ${cached.size} productos")
            return
        }

        // Si no hay en caché, carga desde Room usando la query correcta
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance(getApplication())
                val entities = db.productDao().getRecentlyViewedByUser()
                android.util.Log.d("RecentlyViewed", "Entities found: ${entities.size}")
                entities.forEach {
                    android.util.Log.d("RecentlyViewed", "Product: ${it.title} - lastViewedAt: ${it.lastViewedAt}")
                }

                val products = entities.map { entity ->
                    Product(
                        id = entity.id,
                        title = entity.title,
                        description = entity.description,
                        price = entity.price,
                        category = entity.category,
                        condition = entity.condition,
                        building_location = entity.location,
                        image_urls = entity.imageUrl?.let { listOf(it) } ?: emptyList(),
                        seller_id = entity.sellerId,
                        store_id = entity.storeId,
                        created_at = entity.createdAt
                    )
                }

                // Guarda en LRU
                products.forEach { RecentlyViewedLruCache.put(it) }

                withContext(Dispatchers.Main) {
                    _recentlyViewed.value = products
                }
            } catch (e: Exception) {
                android.util.Log.e("RecentlyViewed", "Error: ${e.message}")
            }
        }
    }

    fun addToRecentlyViewed(product: Product) {
        RecentlyViewedLruCache.put(product)
        if (_showRecentlyViewed.value) {
            viewModelScope.launch(Dispatchers.Main) {
                _recentlyViewed.value = RecentlyViewedLruCache.getAll()
            }
        }
    }
}