package com.andeshub.ui.auth

import androidx.lifecycle.viewModelScope
import com.andeshub.data.model.AuthResponse
import com.andeshub.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.andeshub.data.local.SessionManager
import com.andeshub.data.model.CachedUser
import com.andeshub.data.remote.RetrofitClient
import com.andeshub.data.model.UserResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val response: AuthResponse) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val repository = AuthRepository(sessionManager)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(email: String, password: String, isNfc: Boolean = false) {
        viewModelScope.launch { 
            _uiState.value = AuthUiState.Loading
            try {
                val response = withContext(Dispatchers.IO) { 
                    coroutineScope {
                        val loginDeferred = async { repository.login(email, password, isNfc) }
                        val cacheDeferred = async { sessionManager.getCachedUser() }
                        cacheDeferred.await()
                        loginDeferred.await()
                    }
                }
                sessionManager.saveTokens(response.accessToken, response.refreshToken)
                RetrofitClient.setToken(response.accessToken)
                sessionManager.saveUser(
                    id = response.user.id,
                    email = response.user.email,
                    firstName = response.user.firstName,
                    lastName = response.user.lastName,
                    major = response.user.major,
                    phoneNumber = response.user.phoneNumber
                )

                repository.cacheUser(
                    CachedUser(
                        id = response.user.id,
                        email = response.user.email,
                        firstName = response.user.firstName,
                        lastName = response.user.lastName,
                        major = response.user.major,
                        phoneNumber = response.user.phoneNumber
                    )
                )

                _uiState.value = AuthUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Login failed. Please check your credentials.")
            }
        }
    }

    fun enableBiometric(enabled: Boolean) {
        sessionManager.setBiometricEnabled(enabled)
    }

    fun loginWithBiometric() {
        val cachedUser = sessionManager.getCachedUser()
        
        // Si tenemos un usuario guardado (cacheado), permitimos entrar con huella
        if (cachedUser != null) {
            val response = AuthResponse(
                accessToken = sessionManager.getAccessToken() ?: "biometric-session", 
                refreshToken = sessionManager.getRefreshToken() ?: "",
                user = UserResponse(
                    id = cachedUser.id,
                    email = cachedUser.email,
                    firstName = cachedUser.firstName,
                    lastName = cachedUser.lastName,
                    major = cachedUser.major,
                    phoneNumber = cachedUser.phoneNumber
                )
            )
            // Restauramos el token en Retrofit para las llamadas de red
            RetrofitClient.setToken(response.accessToken)
            _uiState.value = AuthUiState.Success(response)
        } else {
            _uiState.value = AuthUiState.Error("No biometric record found. Login with password once.")
            sessionManager.setBiometricEnabled(false)
        }
    }

    fun register(
        email: String,
        firstName: String,
        lastName: String,
        major: String,
        password: String,
        phoneNumber: String
    ) {
        viewModelScope.launch { 
            _uiState.value = AuthUiState.Loading
            try {
                val response = withContext(Dispatchers.IO) { 
                    repository.register(email, firstName, lastName, major, password, phoneNumber)
                }
                sessionManager.saveTokens(response.accessToken, response.refreshToken)
                RetrofitClient.setToken(response.accessToken)
                sessionManager.saveUser(
                    id = response.user.id,
                    email = response.user.email,
                    firstName = response.user.firstName,
                    lastName = response.user.lastName,
                    major = response.user.major,
                    phoneNumber = response.user.phoneNumber
                )
                repository.cacheUser(
                    CachedUser(
                        id = response.user.id,
                        email = response.user.email,
                        firstName = response.user.firstName,
                        lastName = response.user.lastName,
                        major = response.user.major,
                        phoneNumber = response.user.phoneNumber
                    )
                )
                _uiState.value = AuthUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Registration failed.")
            }
        }
    }

    fun nfcLogin(userId: String) {
        viewModelScope.launch { 
            _uiState.value = AuthUiState.Loading
            try {
                val response = withContext(Dispatchers.IO) { 
                    coroutineScope {
                        val loginDeferred = async { repository.nfcLogin(userId) }
                        val cacheDeferred = async { sessionManager.getCachedUser() }
                        cacheDeferred.await()
                        loginDeferred.await()
                    }
                }
                sessionManager.saveTokens(response.accessToken, response.refreshToken)
                RetrofitClient.setToken(response.accessToken)
                sessionManager.saveUser(
                    id = response.user.id,
                    email = response.user.email,
                    firstName = response.user.firstName,
                    lastName = response.user.lastName,
                    major = response.user.major,
                    phoneNumber = response.user.phoneNumber
                )
                repository.cacheUser(
                    CachedUser(
                        id = response.user.id,
                        email = response.user.email,
                        firstName = response.user.firstName,
                        lastName = response.user.lastName,
                        major = response.user.major,
                        phoneNumber = response.user.phoneNumber
                    )
                )
                _uiState.value = AuthUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("NFC login failed.")
            }
        }
    }

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()
    fun isBiometricEnabled(): Boolean = sessionManager.isBiometricEnabled()
}
