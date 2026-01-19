package com.ieum.data.api

import com.ieum.data.dto.*
import retrofit2.http.*

interface UserService {
    @GET("api/users/me")
    suspend fun getMe(): UserDto

    @PATCH("api/users/me")
    suspend fun updateMe(@Body request: UserUpdateRequest): UserDto
}
