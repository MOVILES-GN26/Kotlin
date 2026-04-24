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
import com.andeshub.data.remote.RetrofitClient

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

    fun login(email: String, password: String) {
        viewModelScope.launch { // Main — actualiza UI
            _uiState.value = AuthUiState.Loading
            try {
                val response = withContext(Dispatchers.IO) { // IO — llamada de red
                    repository.login(email, password)
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
                _uiState.value = AuthUiState.Success(response)
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val message = try {
                    val json = org.json.JSONObject(errorBody ?: "")
                    json.optString("message", "Incorrect email or password.")
                } catch (_: Exception) { "Incorrect email or password." }
                _uiState.value = AuthUiState.Error(message)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Connection error. Check your network.")
            }
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
        viewModelScope.launch { // Main — actualiza UI
            _uiState.value = AuthUiState.Loading
            try {
                val response = withContext(Dispatchers.IO) { // IO — llamada de red
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
                _uiState.value = AuthUiState.Success(response)
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val message = try {
                    val json = org.json.JSONObject(errorBody ?: "")
                    json.optString("message", "This email is already registered.")
                } catch (_: Exception) { "This email is already registered." }
                _uiState.value = AuthUiState.Error(message)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Connection error. Check your network.")
            }
        }
    }

    fun nfcLogin(userId: String) {
        viewModelScope.launch { // Main — actualiza UI
            _uiState.value = AuthUiState.Loading
            try {
                val response = withContext(Dispatchers.IO) { // IO — llamada de red
                    repository.nfcLogin(userId)
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
                _uiState.value = AuthUiState.Success(response)
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "NFC Error: ${e.message}")
                _uiState.value = AuthUiState.Error("NFC login failed: ${e.message}")
            }
        }
    }

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()
}