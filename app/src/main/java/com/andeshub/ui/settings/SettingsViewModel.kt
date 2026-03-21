package com.andeshub.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andeshub.data.local.SessionManager
import com.andeshub.data.model.Product
import com.andeshub.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val listings: List<Product> = emptyList(),
    val isLoadingListings: Boolean = false,
    val listingsError: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val productRepository = ProductRepository(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        loadProfile()
        loadListings()
    }

    private fun loadProfile() {
        _uiState.value = _uiState.value.copy(
            firstName = sessionManager.getUserFirstName() ?: "",
            lastName  = sessionManager.getUserLastName()  ?: "",
            email     = sessionManager.getUserEmail()     ?: ""
        )
    }

    private fun loadListings() {
        val sellerId = sessionManager.getUserId() ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingListings = true, listingsError = null)

            productRepository.getProductsByUser(sellerId)
                .onSuccess { products ->
                    _uiState.value = _uiState.value.copy(
                        listings = products,
                        isLoadingListings = false
                    )
                }
                .onFailure { error ->
                    android.util.Log.e("SettingsViewModel", "Error: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        listingsError = "No se pudieron cargar los listings",
                        isLoadingListings = false
                    )
                }
        }

    }
    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            productRepository.deleteProduct(productId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        listings = _uiState.value.listings.filter { it.id != productId }
                    )
                }
                .onFailure { error ->
                    android.util.Log.e("SettingsViewModel", "Error deleting: ${error.message}")
                }
        }
    }
    fun refresh() {
        loadProfile()
        loadListings()
    }
    fun logout() {
        sessionManager.clearSession()
    }
}