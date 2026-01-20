package com.ieum.data.api

import com.ieum.data.dto.*
import retrofit2.http.*

interface UserService {
    @GET("api/users/me")
    suspend fun getMe(): UserDto

    @PATCH("api/users/me")
    suspend fun updateMe(@Body request: UserUpdateRequest): UserDto

    // E2EE 공개키 관리
    @PUT("api/users/me/public-key")
    suspend fun uploadPublicKey(@Body request: PublicKeyRequest)

    @GET("api/users/me/public-key")
    suspend fun getMyPublicKey(): PublicKeyResponse

    @GET("api/users/partner/public-key")
    suspend fun getPartnerPublicKey(): PublicKeyResponse
}
