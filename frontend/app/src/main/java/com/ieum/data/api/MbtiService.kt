package com.ieum.data.api

import com.ieum.data.dto.*
import retrofit2.http.*

interface MbtiService {
    @GET("api/mbti/questions")
    suspend fun getQuestions(): MbtiQuestionsResponse

    @POST("api/mbti/submit")
    suspend fun submitAnswers(@Body request: MbtiSubmitRequest): MbtiResultResponse

    @GET("api/mbti/couple-result")
    suspend fun getCoupleResult(): MbtiCoupleResultResponse
}
