package com.ieum.data.api

import com.ieum.data.dto.*
import retrofit2.http.*

interface RecommendationService {
    @GET("api/recommendations")
    suspend fun getRecommendations(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): RecommendationsResponse

    @GET("api/recommendations/{recommendationId}")
    suspend fun getRecommendation(
        @Path("recommendationId") recommendationId: String
    ): RecommendationDto

    @POST("api/recommendations")
    suspend fun createRecommendation(@Body request: RecommendationRequest): RecommendationDto

    @POST("api/recommendations/{recommendationId}/feedback")
    suspend fun submitFeedback(
        @Path("recommendationId") recommendationId: String,
        @Body request: FeedbackRequest
    ): RecommendationDto
}
