package com.andeshub.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.andeshub.data.local.SessionManager
import com.andeshub.data.remote.RetrofitClient
import com.andeshub.data.model.UpdateProfileRequest
import com.andeshub.data.model.UserResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class UserRepository(private val context: Context) {

    private val api = RetrofitClient.apiService
    private val sessionManager = SessionManager(context)

    suspend fun updateProfile(
        firstName: String? = null,
        lastName: String? = null,
        major: String? = null,
        phoneNumber: String? = null,
        password: String? = null
    ): Result<UserResponse> {
        return try {
            val response = api.updateProfile(
                UpdateProfileRequest(
                    firstName = firstName,
                    lastName  = lastName,
                    major      = major,
                    phoneNumber = phoneNumber,
                    password   = if (password.isNullOrBlank()) null else password
                )
            )
            sessionManager.saveUser(
                id        = response.id,
                email     = response.email,
                firstName = response.firstName,
                lastName  = response.lastName,
                major     = response.major,
                phoneNumber = response.phoneNumber
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMe(): Result<UserResponse> {
        return try {
            Result.success(api.getMe())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAvatar(imageUri: Uri): Result<UserResponse> {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return Result.failure(Exception("No se pudo leer la imagen"))

            val originalBitmap = BitmapFactory.decodeStream(inputStream)
                ?: return Result.failure(Exception("No se pudo decodificar la imagen"))

            val outputStream = ByteArrayOutputStream()
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val bytes = outputStream.toByteArray()

            val requestBody = bytes.toRequestBody("image/jpeg".toMediaType())
            val part = MultipartBody.Part.createFormData("avatar", "avatar.jpg", requestBody)

            val response = api.updateAvatar(part)

            sessionManager.saveUser(
                id        = response.id,
                email     = response.email,
                firstName = response.firstName,
                lastName  = response.lastName,
                major     = response.major,
                phoneNumber = response.phoneNumber
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}