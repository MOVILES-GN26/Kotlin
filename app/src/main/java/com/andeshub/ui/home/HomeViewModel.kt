package com.andeshub.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andeshub.data.local.AppDatabase
import com.andeshub.data.local.HomeLruCache
import com.andeshub.data.local.SearchPreferences
import com.andeshub.data.model.Product
import com.andeshub.data.model.TrendingCategory
import com.andeshub.data.remote.RetrofitClient
import com.andeshub.data.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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
        viewModelScope.launch(Dispatchers.Main) {
            if (_uiState.value !is HomeUiState.Success) {
                _uiState.value = HomeUiState.Loading
            }

            val productsDeferred = async(Dispatchers.IO) {
                try {
                    // Intenta traer de la API
                    val products = repository.getProducts()
                    // Guarda en Room para uso offline
                    products.forEach {
                        try { repository.saveProductLocally(it) } catch (e: Exception) { }
                    }
                    products
                } catch (e: Exception) {
                    // Sin internet: devuelve los productos guardados en Room
                    android.util.Log.d("HomeViewModel", "Sin internet, cargando desde Room")
                    repository.getAllLocalProducts()
                }
            }

            val trendingDeferred = async(Dispatchers.IO) {
                try { api.getTrendingCategories() } catch (e: Exception) { emptyList<TrendingCategory>() }
            }

            try {
                val products = productsDeferred.await()
                val trending = trendingDeferred.await()

                _uiState.value = HomeUiState.Success(
                    products = products,
                    trendingCategories = trending
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Error desconocido")
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
            val currentTrending = if (currentState is HomeUiState.Success) currentState.trendingCategories else emptyList()

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
}