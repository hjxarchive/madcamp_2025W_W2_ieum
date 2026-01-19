package com.ieum.data.api

import com.ieum.data.dto.*
import retrofit2.http.*

interface BudgetService {
    @GET("api/budgets/{yearMonth}")
    suspend fun getBudget(@Path("yearMonth") yearMonth: String): BudgetResponse

    @PUT("api/budgets/{yearMonth}")
    suspend fun setBudget(
        @Path("yearMonth") yearMonth: String,
        @Body request: BudgetRequest
    ): BudgetResponse
}
