package com.ieum.domain.repository

import com.ieum.data.api.AuthResponse

interface AuthRepository {
    suspend fun googleLogin(idToken: String): Result<AuthResponse>
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
}
