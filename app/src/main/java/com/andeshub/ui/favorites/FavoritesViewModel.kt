package com.andeshub.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andeshub.data.model.Product
import com.andeshub.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class FavoritesUiState {
    object Idle    : FavoritesUiState()
    object Loading : FavoritesUiState()
    data class Success(val favorites: List<Product>) : FavoritesUiState()
    data class Error(val message: String) : FavoritesUiState()
}

class FavoritesViewModel : ViewModel() {

    private val repository = FavoritesRepository()

    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Idle)
    val uiState: StateFlow<FavoritesUiState> = _uiState

    init {
        getFavorites()
    }

    fun getFavorites() {
        viewModelScope.launch {
            _uiState.value = FavoritesUiState.Loading
            try {
                val favorites = repository.getFavorites()
                _uiState.value = FavoritesUiState.Success(favorites)
            } catch (e: Exception) {
                _uiState.value = FavoritesUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun removeFavorite(productId: String) {
        viewModelScope.launch {
            try {
                repository.removeFavorite(productId)
                getFavorites() // refresca la lista
            } catch (e: Exception) {
                _uiState.value = FavoritesUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}