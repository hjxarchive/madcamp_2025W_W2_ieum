package com.ieum.data.api

import com.ieum.data.dto.*
import retrofit2.http.*

interface ExpenseService {
    @GET("api/expenses")
    suspend fun getExpenses(
        @Query("yearMonth") yearMonth: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): ExpensesResponse

    @POST("api/expenses")
    suspend fun createExpense(@Body request: ExpenseRequest): ExpenseDto

    @PATCH("api/expenses/{expenseId}")
    suspend fun updateExpense(
        @Path("expenseId") expenseId: String,
        @Body request: ExpenseRequest
    ): ExpenseDto

    @DELETE("api/expenses/{expenseId}")
    suspend fun deleteExpense(@Path("expenseId") expenseId: String)
}
