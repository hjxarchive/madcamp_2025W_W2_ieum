package com.ieum.data.api

import com.ieum.data.dto.*
import retrofit2.http.*

interface FileService {
    @POST("api/files/presign")
    suspend fun getPresignedUrl(@Body request: FilePresignRequest): FilePresignResponse

    @GET("api/files/{fileId}")
    suspend fun getFileInfo(@Path("fileId") fileId: String): FileInfoResponse
}
