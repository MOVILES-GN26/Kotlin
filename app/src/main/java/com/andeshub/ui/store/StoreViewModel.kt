package com.andeshub.ui.store

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andeshub.data.model.Store
import com.andeshub.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class StoreUiState {
    object Idle    : StoreUiState()
    object Loading : StoreUiState()
    data class Success(val store: Store) : StoreUiState()
    data class Error(val message: String) : StoreUiState()
}

class StoreViewModel(context: Context) : ViewModel() {

    private val repository = StoreRepository(context)

    private val _uiState = MutableStateFlow<StoreUiState>(StoreUiState.Idle)
    val uiState: StateFlow<StoreUiState> = _uiState

    fun createStore(
        name: String,
        description: String,
        category: String,
        logoUri: Uri?
    ) {
        viewModelScope.launch {
            _uiState.value = StoreUiState.Loading
            try {
                val store = repository.createStore(name, description, category, logoUri)
                _uiState.value = StoreUiState.Success(store)
            } catch (e: Exception) {
                _uiState.value = StoreUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun getStore(id: String) {
        viewModelScope.launch {
            _uiState.value = StoreUiState.Loading
            try {
                val store = repository.getStore(id)
                _uiState.value = StoreUiState.Success(store)
            } catch (e: Exception) {
                _uiState.value = StoreUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}