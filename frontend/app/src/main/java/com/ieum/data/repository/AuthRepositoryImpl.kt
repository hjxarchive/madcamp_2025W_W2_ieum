package com.ieum.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ieum.data.api.AuthService
import com.ieum.data.api.GoogleLoginRequest
import com.ieum.data.api.AuthResponse
import com.ieum.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authService: AuthService
) : AuthRepository {

    private val TOKEN_KEY = stringPreferencesKey("access_token")

    override suspend fun googleLogin(idToken: String): Result<AuthResponse> = runCatching {
        authService.googleLogin(GoogleLoginRequest(idToken))
    }

    override suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    override suspend fun getToken(): String? {
        return context.dataStore.data.map { prefs ->
            prefs[TOKEN_KEY]
        }.first()
    }
}
