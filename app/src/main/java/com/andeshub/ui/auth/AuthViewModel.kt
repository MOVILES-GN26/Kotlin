package com.andeshub.ui.auth

import androidx.lifecycle.viewModelScope
import com.andeshub.data.model.AuthResponse
import com.andeshub.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.andeshub.data.local.SessionManager

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val response: AuthResponse) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = repository.login(email, password)
                sessionManager.saveTokens(response.accessToken, response.refreshToken)
                sessionManager.saveUser(
                    id = response.user.id,
                    email = response.user.email,
                    firstName = response.user.firstName,
                    lastName = response.user.lastName,
                    major = response.user.major
                )
                android.util.Log.d("AuthViewModel", "Success: ${response.user.email}")
                android.util.Log.d("AuthViewModel", "Saved user: ${response.user.email}")
                _uiState.value = AuthUiState.Success(response)
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error: ${e.message}")
                _uiState.value = AuthUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun register(
        email: String,
        firstName: String,
        lastName: String,
        major: String,
        password: String
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = repository.register(email, firstName, lastName, major, password)
                sessionManager.saveTokens(response.accessToken, response.refreshToken)
                sessionManager.saveUser(
                    id = response.user.id,
                    email = response.user.email,
                    firstName = response.user.firstName,
                    lastName = response.user.lastName,
                    major = response.user.major
                )
                android.util.Log.d("AuthViewModel", "Success: ${response.user.email}")
                _uiState.value = AuthUiState.Success(response)
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error: ${e.message}")
                _uiState.value = AuthUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()
}