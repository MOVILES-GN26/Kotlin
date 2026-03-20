package com.andeshub.ui.product

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andeshub.data.local.SessionManager
import com.andeshub.data.model.Product
import com.andeshub.data.model.TrendingCategory
import com.andeshub.data.repository.ProductRepository
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
    private val sessionManager = SessionManager(context)
    private val api = RetrofitClient.apiService

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Idle)
    val uiState: StateFlow<ProductUiState> = _uiState

    fun getProducts(
        search: String? = null,
        category: String? = null,
        condition: String? = null,
        priceSort: String? = null
    ) {
        viewModelScope.launch {
            // No ponemos Loading aquí para no parpadear toda la pantalla si ya hay datos, 
            // pero si es la primera vez sí.
            if (_uiState.value !is ProductUiState.Success) {
                _uiState.value = ProductUiState.Loading
            }
            
            try {
                val products = repository.getProducts(search, category, condition, priceSort)
                
                // También cargamos las tendencias para ordenar el catálogo
                val trending = try {
                    api.getTrendingCategories()
                } catch (e: Exception) {
                    emptyList()
                }

                _uiState.value = ProductUiState.Success(products, trending)
            } catch (e: Exception) {
                _uiState.value = ProductUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun createProduct(
        title: String,
        description: String,
        category: String,
        location: String,
        price: Double,
        condition: String,
        storeId: String?,
        imageUri: Uri?,
        imageBitmap: Bitmap? = null
    ) {
        val token = sessionManager.getAccessToken() ?: ""
        if (token.isEmpty()) {
            _uiState.value = ProductUiState.Error("No active session. Please login again.")
            return
        }

        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            try {
                val product = repository.createProduct(
                    token, title, description, category, location, price, condition, storeId, imageUri, imageBitmap
                )
                _uiState.value = ProductUiState.Created(product)
            } catch (e: Exception) {
                _uiState.value = ProductUiState.Error(e.message ?: "Error creating product")
            }
        }
    }
}
