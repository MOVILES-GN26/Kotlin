package com.andeshub.ui.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andeshub.data.model.Product
import com.andeshub.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class FavoritesUiState {
    object Idle : FavoritesUiState()
    object Loading : FavoritesUiState()
    data class Success(val favorites: List<Product>) : FavoritesUiState()
    data class Error(val message: String) : FavoritesUiState()
}

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FavoritesRepository(application)

    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Idle)
    val uiState: StateFlow<FavoritesUiState> = _uiState

    init {
        getFavorites()
    }

    fun getFavorites() {
        viewModelScope.launch {
            _uiState.value = FavoritesUiState.Loading

            val localFavorites = repository.getLocalFavorites()

            if (localFavorites.isNotEmpty()) {
                _uiState.value = FavoritesUiState.Success(localFavorites)
            }

            try {
                val remoteFavorites = repository.syncFavorites()
                _uiState.value = FavoritesUiState.Success(remoteFavorites)
            } catch (e: Exception) {
                if (localFavorites.isEmpty()) {
                    _uiState.value = FavoritesUiState.Error(
                        e.message ?: "No se pudieron cargar los favoritos"
                    )
                }
            }
        }
    }

    fun removeFavorite(productId: String) {
        viewModelScope.launch {
            repository.removeFavorite(productId)
            getFavorites()
        }
    }
}