package com.ieum.data.api

import com.ieum.data.dto.*
import retrofit2.http.*

interface ChatService {
    @GET("api/chat/room")
    suspend fun getChatRoom(): ChatRoomResponse

    @GET("api/chat/rooms/{roomId}/messages")
    suspend fun getMessages(
        @Path("roomId") roomId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): ChatMessagesResponse

    @POST("api/chat/rooms/{roomId}/messages")
    suspend fun sendMessage(
        @Path("roomId") roomId: String,
        @Body request: ChatMessageRequest
    ): ChatMessageDto
}
