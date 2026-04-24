package com.andeshub.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andeshub.data.model.Product
import com.andeshub.data.model.TrendingCategory
import com.andeshub.data.remote.RetrofitClient
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

class HomeViewModel : ViewModel() {

    private val api = RetrofitClient.apiService

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch(Dispatchers.Main) { // ← corrutina en Main (UI)
            if (_uiState.value !is HomeUiState.Success) {
                _uiState.value = HomeUiState.Loading
            }

            // Dos corrutinas anidadas en IO, corriendo en paralelo (async/await)
            val productsDeferred = async(Dispatchers.IO) {
                api.getProducts()
            }
            val trendingDeferred = async(Dispatchers.IO) {
                try { api.getTrendingCategories() } catch (e: Exception) { emptyList<TrendingCategory>() }
            }

            try {
                val products = productsDeferred.await()
                val trending = trendingDeferred.await()

                // De vuelta en Main para actualizar la UI
                withContext(Dispatchers.Main) {
                    _uiState.value = HomeUiState.Success(
                        products = products.items ?: emptyList(),
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
