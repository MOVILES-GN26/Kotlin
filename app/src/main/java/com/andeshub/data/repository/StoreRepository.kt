package com.andeshub.data.repository

import android.net.Uri
import android.content.Context
import com.andeshub.data.model.Store
import com.andeshub.data.remote.RetrofitClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class StoreRepository(private val context: Context) {

    private val api = RetrofitClient.apiService

    suspend fun createStore(
        name: String,
        description: String,
        category: String,
        logoUri: Uri?
    ): Store {
        val namePart = name.toRequestBody("text/plain; charset=utf-8".toMediaType())
        val descriptionPart = description.toRequestBody("text/plain; charset=utf-8".toMediaType())
        val categoryPart = category.toRequestBody("text/plain; charset=utf-8".toMediaType())

        val logoPart = logoUri?.let {
            val bytes = context.contentResolver.openInputStream(it)?.readBytes() ?: return@let null
            val requestBody = bytes.toRequestBody("image/*".toMediaType())
            MultipartBody.Part.createFormData("logo", "logo.jpg", requestBody)
        }
        android.util.Log.d("StoreRepository", "name: $name, description: $description, category: $category, logoUri: $logoUri")

        return api.createStore(namePart, descriptionPart, categoryPart, logoPart)
    }
    suspend fun getStore(id: String): Store {
        return api.getStore(id)
    }
}