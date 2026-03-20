package com.andeshub.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.andeshub.data.local.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import com.andeshub.data.model.Store
import com.andeshub.data.repository.StoreRepository
import kotlinx.coroutines.launch
import com.andeshub.data.model.Product
import com.andeshub.data.repository.ProductRepository

data class ProfileUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val major: String = "",
    val stores: List<Store> = emptyList(),
    val listings: List<Product> = emptyList(),
    val isLoadingListings: Boolean = false,
    val listingsError: String? = null
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val storeRepository = StoreRepository(application)
    private val productRepository = ProductRepository(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadProfile()
        loadStores()
        loadListings()
    }

    private fun loadProfile() {
        val firstName = sessionManager.getUserFirstName()
        val lastName = sessionManager.getUserLastName()
        val email = sessionManager.getUserEmail()
        val major = sessionManager.getUserMajor()

        android.util.Log.d("ProfileViewModel", "firstName: $firstName, email: $email")

        _uiState.value = ProfileUiState(
            firstName = firstName ?: "",
            lastName = lastName ?: "",
            email = email ?: "",
            major = major ?: ""
        )
    }

    private fun loadStores() {
        viewModelScope.launch {
            try {
                val stores = storeRepository.getMyStores()
                _uiState.value = _uiState.value.copy(stores = stores)
            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "Error loading stores: ${e.message}")
            }
        }
    }

    private fun loadListings() {
        val sellerId = sessionManager.getUserId()  ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingListings = true, listingsError = null)

            productRepository.getProductsByUser( sellerId)
                .onSuccess { products ->
                    _uiState.value = _uiState.value.copy(
                        listings = products,
                        isLoadingListings = false
                    )
                }
                .onFailure { error ->
                    android.util.Log.e("ProfileViewModel", "Error loading listings: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        listingsError = "No se pudieron cargar los listings",
                        isLoadingListings = false
                    )
                }
        }
    }

    fun refresh() {
        loadStores()
        loadListings()
    }
}