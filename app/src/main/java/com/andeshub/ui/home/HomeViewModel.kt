package com.andeshub.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andeshub.data.local.AppDatabase
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
        // Carga el historial de búsquedas desde DataStore
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

            // ESTRATEGIA DE MULTITHREADING PARALELO (RÚBRICA)
            val productsDeferred = async(Dispatchers.IO) {
                try { 
                    repository.getProducts() 
                } catch (e: Exception) { 
                    // Fallback automático a local si falla la red al cargar el inicio
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

            if (_searchQuery.value.isNotBlank()) {
                searchPreferences.saveSearch(_searchQuery.value)
            }

            _uiState.value = HomeUiState.Loading
            try {
                val products = repository.getProducts(search = _searchQuery.value)
                _uiState.value = HomeUiState.Success(
                    products = products,
                    trendingCategories = currentTrending
                )
            } catch (e: Exception) {
                // Fallback a búsqueda local si falla la red
                val localProducts = repository.getAllLocalProducts().filter { 
                    it.title.contains(_searchQuery.value, ignoreCase = true) 
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
