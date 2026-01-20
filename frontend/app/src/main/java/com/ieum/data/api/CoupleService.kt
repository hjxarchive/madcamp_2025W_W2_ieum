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

    // E2EE 공유 대칭키 관리
    @POST("api/couples/me/shared-key")
    suspend fun setMySharedKey(@Body request: SharedKeyRequest)

    @GET("api/couples/me/shared-key")
    suspend fun getMySharedKey(): SharedKeyResponse

    @POST("api/couples/partner/shared-key")
    suspend fun setPartnerSharedKey(@Body request: SharedKeyRequest)
}
