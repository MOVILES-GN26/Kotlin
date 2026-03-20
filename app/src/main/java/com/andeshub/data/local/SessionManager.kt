package com.andeshub.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit

class SessionManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = try {
        EncryptedSharedPreferences.create(
            context.applicationContext,
            "andeshub_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        context.getSharedPreferences("andeshub_prefs", Context.MODE_PRIVATE)
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
        }
    }

    fun getAccessToken(): String? = prefs.getString("access_token", null)

    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)

    fun clearSession() {
        prefs.edit { clear() }
    }

    fun isLoggedIn(): Boolean = getAccessToken() != null

    fun setOnboardingCompleted() {
        prefs.edit { putBoolean("onboarding_completed", true) }
    }

    fun isOnboardingCompleted(): Boolean = prefs.getBoolean("onboarding_completed", false)

    fun saveUser(id: String, email: String, firstName: String, lastName: String, major: String) {
        prefs.edit {
            putString("user_id", id)
            putString("user_email", email)
            putString("user_first_name", firstName)
            putString("user_last_name", lastName)
            putString("user_major", major)
        }
    }

    fun getUserEmail(): String? = prefs.getString("user_email", null)
    fun getUserFirstName(): String? = prefs.getString("user_first_name", null)
    fun getUserLastName(): String? = prefs.getString("user_last_name", null)
    fun getUserMajor(): String? = prefs.getString("user_major", null)
    fun getUserId(): String? = prefs.getString("user_id", null)
}
