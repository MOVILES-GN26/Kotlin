package com.andeshub.ui.store

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andeshub.data.local.StoreLogger
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

class StoreViewModel(private val context: Context) : ViewModel() {

    private val repository = StoreRepository(context)

    private val _uiState = MutableStateFlow<StoreUiState>(StoreUiState.Idle)
    val uiState: StateFlow<StoreUiState> = _uiState

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun createStore(
        name: String,
        description: String,
        category: String,
        logoUri: Uri?
    ) {
        if (!isNetworkAvailable()) {
            _uiState.value = StoreUiState.Error("No internet connection. Please try again later.")
            return
        }
        viewModelScope.launch {
            _uiState.value = StoreUiState.Loading
            try {
                val store = repository.createStore(name, description, category, logoUri)
                StoreLogger.logCreatedStore(context, store)
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

    fun saveDraft(name: String, description: String, category: String) {
        StoreLogger.saveDraft(context, name, description, category)
    }

    fun loadDraft(): Triple<String, String, String>? {
        return StoreLogger.loadDraft(context)
    }

    fun clearDraft() {
        StoreLogger.clearDraft(context)
    }
}