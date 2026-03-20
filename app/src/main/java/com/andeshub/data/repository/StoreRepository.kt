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
        val namePart        = name.toRequestBody("text/plain".toMediaType())
        val descriptionPart = description.toRequestBody("text/plain".toMediaType())
        val categoryPart    = category.toRequestBody("text/plain".toMediaType())

        val logoPart = logoUri?.let {
            val bytes = context.contentResolver.openInputStream(it)?.readBytes() ?: return@let null
            val requestBody = bytes.toRequestBody("image/*".toMediaType())
            MultipartBody.Part.createFormData("logo", "logo.jpg", requestBody)
        }

        return api.createStore(namePart, descriptionPart, categoryPart, logoPart)
    }
    suspend fun getStore(id: String): Store {
        return api.getStore(id)
    }
    suspend fun getMyStores(): List<Store> {
        return api.getMyStores()
    }
}