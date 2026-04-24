package com.andeshub.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _viewedTimestamps = MutableStateFlow<Map<String, Long>>(emptyList<Pair<String, Long>>().toMap())
    val viewedTimestamps: StateFlow<Map<String, Long>> = _viewedTimestamps

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    init {
        loadData()
        loadViewedTimestamps()
    }

    fun loadViewedTimestamps() {
        viewModelScope.launch(Dispatchers.IO) {
            val timestamps = com.andeshub.data.local.AppDatabase.getInstance(getApplication())
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
    }

    fun search() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val currentTrending = if (currentState is HomeUiState.Success) currentState.trendingCategories else emptyList()
            
            _uiState.value = HomeUiState.Loading
            try {
                val response = api.getProducts(search = _searchQuery.value)
                _uiState.value = HomeUiState.Success(
                    products = response.items ?: emptyList(),
                    trendingCategories = currentTrending
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun onCategorySelected(category: String) {
        // Si ya está seleccionada, la deselecciona (toggle)
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }
}
