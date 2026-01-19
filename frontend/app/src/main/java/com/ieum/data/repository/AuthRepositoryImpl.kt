package com.ieum.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ieum.data.api.AuthService
import com.ieum.data.dto.AuthResponse
import com.ieum.data.dto.GoogleLoginRequest
import com.ieum.data.dto.UserDto
import com.ieum.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// DataStore extension - 다른 클래스에서도 사용 가능하도록 public
val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authService: AuthService
) : AuthRepository {

    private val TOKEN_KEY = stringPreferencesKey("access_token")
    private val USER_ID_KEY = stringPreferencesKey("user_id")

    override suspend fun googleLogin(idToken: String): Result<AuthResponse> = runCatching {
        authService.googleLogin(GoogleLoginRequest(idToken))
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        authService.logout()
        clearToken()
    }

    override suspend fun getMe(): Result<UserDto> = runCatching {
        authService.getMe()
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

    override suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(USER_ID_KEY)
        }
    }

    override suspend fun saveUserId(userId: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = userId
        }
    }

    override suspend fun getUserId(): String? {
        return context.dataStore.data.map { prefs ->
            prefs[USER_ID_KEY]
        }.first()
    }

    override suspend fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}
