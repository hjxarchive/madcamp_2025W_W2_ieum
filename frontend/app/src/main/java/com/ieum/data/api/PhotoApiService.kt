package com.ieum.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface PhotoApiService {
    @Multipart
    @POST("/api/photos/upload")
    suspend fun uploadPhoto(
        @Part image: MultipartBody.Part,
        @Part("lat") lat: RequestBody,
        @Part("lng") lng: RequestBody,
        @Part("coupleId") coupleId: RequestBody
    ): Response<Unit>
}
