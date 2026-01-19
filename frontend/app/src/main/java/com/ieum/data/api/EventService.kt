package com.ieum.data.api

import com.ieum.data.dto.*
import retrofit2.http.*

interface EventService {
    @GET("api/events")
    suspend fun getEvents(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): EventsResponse

    @GET("api/events/{eventId}")
    suspend fun getEvent(@Path("eventId") eventId: String): EventDto

    @POST("api/events")
    suspend fun createEvent(@Body request: EventRequest): EventDto

    @PATCH("api/events/{eventId}")
    suspend fun updateEvent(
        @Path("eventId") eventId: String,
        @Body request: EventRequest
    ): EventDto

    @DELETE("api/events/{eventId}")
    suspend fun deleteEvent(@Path("eventId") eventId: String)

    @GET("api/ddays")
    suspend fun getDDays(): DDaysResponse
}
