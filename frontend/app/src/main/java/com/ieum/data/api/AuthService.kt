package com.ieum.data.api

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): AuthResponse
}

data class GoogleLoginRequest(val idToken: String)

data class AuthResponse(
    val accessToken: String,
    val user: User
)

data class User(
    val id: String,
    val email: String,
    val name: String?,
    val nickname: String?,
    val profileImage: String?,
    val birthday: String?,
    val gender: String?,
    val coupleId: String?,
    val mbtiType: String?,
    val isActive: Boolean
)