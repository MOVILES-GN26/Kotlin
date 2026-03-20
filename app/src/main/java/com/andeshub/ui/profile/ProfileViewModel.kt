package com.andeshub.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.andeshub.data.local.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ProfileUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val major: String = ""
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadProfile()
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
}