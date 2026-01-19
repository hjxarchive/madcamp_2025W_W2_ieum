package com.ieum.data.api

import com.ieum.data.dto.*
import retrofit2.http.*

interface BucketService {
    @GET("api/buckets")
    suspend fun getBuckets(): BucketsResponse

    @POST("api/buckets")
    suspend fun createBucket(@Body request: BucketRequest): BucketDto

    @PATCH("api/buckets/{bucketId}")
    suspend fun updateBucket(
        @Path("bucketId") bucketId: String,
        @Body request: BucketUpdateRequest
    ): BucketDto

    @DELETE("api/buckets/{bucketId}")
    suspend fun deleteBucket(@Path("bucketId") bucketId: String)
}
