package com.ieum.data.api

import com.ieum.data.dto.*
import retrofit2.http.*

interface AuthService {
    @POST("api/auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): AuthResponse

    @POST("api/auth/logout")
    suspend fun logout(): MessageResponse

    @GET("api/auth/me")
    suspend fun getMe(): UserDto
}
