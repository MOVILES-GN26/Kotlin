package com.andeshub.ui.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.andeshub.data.local.SessionManager
import com.andeshub.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val firstName: String = "",
    val lastName: String = "",
    val major: String = "",
    val phoneNumber: String = "",
    val avatarUrl: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class EditProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val userRepository = UserRepository(application)

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState

    init {
        loadCurrentProfile()
    }

    private fun loadCurrentProfile() {
        _uiState.value = _uiState.value.copy(
            firstName = sessionManager.getUserFirstName() ?: "",
            lastName  = sessionManager.getUserLastName()  ?: "",
            major     = sessionManager.getUserMajor()     ?: "",
            phoneNumber = sessionManager.getUserPhone()     ?: ""
        )
    }

    fun onPhoneNumberChange(value: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = value, errorMessage = null)
    }

    fun onFirstNameChange(value: String) {
        _uiState.value = _uiState.value.copy(firstName = value, errorMessage = null)
    }

    fun onLastNameChange(value: String) {
        _uiState.value = _uiState.value.copy(lastName = value, errorMessage = null)
    }

    fun onMajorChange(value: String) {
        _uiState.value = _uiState.value.copy(major = value, errorMessage = null)
    }

    fun saveProfile(password: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

            userRepository.updateProfile(
                firstName = _uiState.value.firstName.takeIf { it.isNotBlank() },
                lastName  = _uiState.value.lastName.takeIf { it.isNotBlank() },
                major     = _uiState.value.major.takeIf { it.isNotBlank() },
                phoneNumber = _uiState.value.phoneNumber.takeIf { it.isNotBlank() },
                password  = password?.takeIf { it.isNotBlank() }
            ).onSuccess {
                // Guardó en backend — actualizar cache local
                val cached = sessionManager.getCachedUser()
                if (cached != null) {
                    sessionManager.saveUser(
                        id = cached.id,
                        email = cached.email,
                        firstName = _uiState.value.firstName,
                        lastName = _uiState.value.lastName,
                        major = _uiState.value.major,
                        phoneNumber = _uiState.value.phoneNumber
                    )
                }
                sessionManager.clearPendingChanges()
                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            }.onFailure {
                // Sin red — guardar localmente como pendiente
                sessionManager.savePendingProfileUpdate(
                    firstName = _uiState.value.firstName,
                    lastName = _uiState.value.lastName,
                    major = _uiState.value.major,
                    phoneNumber = _uiState.value.phoneNumber
                )
                // Actualizar cache local igual
                val cached = sessionManager.getCachedUser()
                if (cached != null) {
                    sessionManager.saveUser(
                        id = cached.id,
                        email = cached.email,
                        firstName = _uiState.value.firstName,
                        lastName = _uiState.value.lastName,
                        major = _uiState.value.major,
                        phoneNumber = _uiState.value.phoneNumber
                    )
                }
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true,
                    errorMessage = "Guardado localmente. Se sincronizará cuando haya conexión."
                )
            }
        }
    }

    fun updateAvatar(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

            userRepository.updateAvatar(uri)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        avatarUrl = response.avatarUrl
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = "Error al subir imagen: ${error.message}"
                    )
                }
        }
    }
}