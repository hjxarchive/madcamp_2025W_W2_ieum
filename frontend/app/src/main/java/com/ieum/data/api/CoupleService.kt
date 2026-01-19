package com.ieum.data.api

import com.ieum.data.dto.*
import retrofit2.http.*

interface CoupleService {
    @POST("api/couples/invite")
    suspend fun createInviteCode(): InviteCodeResponse

    @POST("api/couples/join")
    suspend fun joinCouple(@Body request: CoupleJoinRequest): CoupleResponse

    @GET("api/couples/me")
    suspend fun getCoupleInfo(): CoupleResponse

    @PATCH("api/couples/me")
    suspend fun updateCoupleInfo(@Body request: CoupleUpdateRequest): CoupleResponse

    @DELETE("api/couples/me")
    suspend fun disconnectCouple()
}
