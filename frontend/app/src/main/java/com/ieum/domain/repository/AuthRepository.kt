package com.ieum.domain.repository

import com.ieum.data.dto.AuthResponse
import com.ieum.data.dto.UserDto

interface AuthRepository {
    suspend fun googleLogin(idToken: String): Result<AuthResponse>
    suspend fun logout(): Result<Unit>
    suspend fun getMe(): Result<UserDto>
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    suspend fun clearToken()
    suspend fun saveUserId(userId: String)
    suspend fun getUserId(): String?
    suspend fun isLoggedIn(): Boolean
}
