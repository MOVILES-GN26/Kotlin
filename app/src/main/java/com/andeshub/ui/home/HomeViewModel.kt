package com.andeshub.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andeshub.data.model.Product
import com.andeshub.data.model.TrendingCategory
import com.andeshub.data.remote.RetrofitClient
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

class HomeViewModel : ViewModel() {

    private val api = RetrofitClient.apiService

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val productsResponse = api.getProducts()
                val trendingResponse = try {
                    api.getTrendingCategories()
                } catch (e: Exception) {
                    emptyList<TrendingCategory>()
                }
                
                _uiState.value = HomeUiState.Success(
                    products = productsResponse.items ?: emptyList(),
                    trendingCategories = trendingResponse
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
}
