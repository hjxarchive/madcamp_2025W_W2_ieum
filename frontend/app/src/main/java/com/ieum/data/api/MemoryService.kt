package com.ieum.data.api

import com.ieum.data.dto.*
import retrofit2.http.*

interface MemoryService {
    @GET("api/memories")
    suspend fun getMemories(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): MemoriesResponse

    @GET("api/memories/{memoryId}")
    suspend fun getMemory(@Path("memoryId") memoryId: String): MemoryDto

    @POST("api/memories")
    suspend fun createMemory(@Body request: MemoryRequest): MemoryDto

    @PATCH("api/memories/{memoryId}")
    suspend fun updateMemory(
        @Path("memoryId") memoryId: String,
        @Body request: MemoryRequest
    ): MemoryDto

    @DELETE("api/memories/{memoryId}")
    suspend fun deleteMemory(@Path("memoryId") memoryId: String)
}
