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

    private val _viewedTimestamps = MutableStateFlow<Map<String, Long>>(emptyList<Pair<String, Long>>().toMap())
    val viewedTimestamps: StateFlow<Map<String, Long>> = _viewedTimestamps

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    init {
        // Carga el historial de búsquedas desde DataStore
        viewModelScope.launch {
            searchPreferences.searchHistory.collect { history ->
                android.util.Log.d("DataStore", "Historial cargado: $history")
                _searchHistory.value = history
            }
        }
        loadData()
        loadViewedTimestamps()
    }

    fun loadViewedTimestamps() {
        viewModelScope.launch(Dispatchers.IO) {
            val timestamps = AppDatabase.getInstance(getApplication())
                .productDao().getAllViewedTimestamps()
                .associate { it.id to it.lastViewedAt }
            _viewedTimestamps.value = timestamps
        }
    }

    fun loadData() {
        viewModelScope.launch(Dispatchers.Main) {
            if (_uiState.value !is HomeUiState.Success) {
                _uiState.value = HomeUiState.Loading
            }

            val productsDeferred = async(Dispatchers.IO) {
                try { api.getProducts() } catch (e: Exception) { null }
            }
            val trendingDeferred = async(Dispatchers.IO) {
                try { api.getTrendingCategories() } catch (e: Exception) { emptyList<TrendingCategory>() }
            }

            try {
                val productsResponse = productsDeferred.await()
                val trending = trendingDeferred.await()

                _uiState.value = HomeUiState.Success(
                    products = productsResponse?.items ?: emptyList(),
                    trendingCategories = trending
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        // Ya no guarda mientras escribe
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
                android.util.Log.d("LruCache", "Cargado desde caché: '$query'")
                _uiState.value = HomeUiState.Success(
                    products = cached,
                    trendingCategories = currentTrending
                )
                return@launch
            }

            // Si no está en caché, llama a la API
            _uiState.value = HomeUiState.Loading
            try {
                val response = api.getProducts(search = query)
                val products = response.items ?: emptyList()

                // Guarda en LRU para la próxima vez
                HomeLruCache.put(query, products)

                _uiState.value = HomeUiState.Success(
                    products = products,
                    trendingCategories = currentTrending
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun selectHistoryItem(query: String) {
        _searchQuery.value = query
        search()
    }
}