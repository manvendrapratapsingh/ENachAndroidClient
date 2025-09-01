package com.enach.client.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class AuthTokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs = createEncryptedPreferences()
    
    private fun createEncryptedPreferences(): android.content.SharedPreferences {
        return try {
            EncryptedSharedPreferences.create(
                context,
                "secure_auth_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            clearCorruptedData()
            EncryptedSharedPreferences.create(
                context,
                "secure_auth_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }
    
    private fun clearCorruptedData() {
        try {
            context.deleteSharedPreferences("secure_auth_prefs")
        } catch (e: Exception) {
            // Fallback: manually clear the preferences file
            val prefsFile = context.getSharedPreferences("secure_auth_prefs", Context.MODE_PRIVATE)
            prefsFile.edit().clear().apply()
        }
    }
    
    companion object {
        private const val TOKEN_KEY = "auth_token"
        private const val USERNAME_KEY = "username"
        private const val USER_EMAIL_KEY = "user_email"
        private val TOKEN_PREF_KEY = stringPreferencesKey("auth_token")
        private val USERNAME_PREF_KEY = stringPreferencesKey("username")
    }
    
    fun saveToken(token: String) {
        encryptedPrefs.edit().putString(TOKEN_KEY, token).apply()
    }
    
    fun getToken(): String? {
        return encryptedPrefs.getString(TOKEN_KEY, null)
    }
    
    fun getAuthHeader(): String? {
        return getToken()?.let { "Bearer $it" }
    }
    
    fun saveUserInfo(username: String, email: String) {
        encryptedPrefs.edit()
            .putString(USERNAME_KEY, username)
            .putString(USER_EMAIL_KEY, email)
            .apply()
    }
    
    fun getUsername(): String? {
        return encryptedPrefs.getString(USERNAME_KEY, null)
    }
    
    fun getUserEmail(): String? {
        return encryptedPrefs.getString(USER_EMAIL_KEY, null)
    }
    
    fun clearAuth() {
        encryptedPrefs.edit().clear().apply()
    }
    
    fun isAuthenticated(): Boolean {
        return getToken() != null
    }
    
    // DataStore methods for preferences
    val tokenFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[TOKEN_PREF_KEY]
        }
    
    suspend fun saveTokenToDataStore(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_PREF_KEY] = token
        }
    }
    
    val usernameFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USERNAME_PREF_KEY]
        }
    
    suspend fun saveUsernameToDataStore(username: String) {
        context.dataStore.edit { preferences ->
            preferences[USERNAME_PREF_KEY] = username
        }
    }
}
