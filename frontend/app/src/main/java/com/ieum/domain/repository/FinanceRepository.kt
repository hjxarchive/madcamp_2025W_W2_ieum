package com.ieum.domain.repository

import com.ieum.domain.model.Budget
import com.ieum.domain.model.Expense
import com.ieum.domain.model.ExpenseCategory
import com.ieum.domain.model.MonthlySpending
import kotlinx.coroutines.flow.Flow

interface FinanceRepository {
    fun getBudget(): Flow<Budget>
    fun getExpenses(): Flow<List<Expense>>
    fun getExpensesByCategory(): Flow<Map<ExpenseCategory, Int>>
    fun getMonthlySpending(): Flow<List<MonthlySpending>>
    suspend fun setBudget(amount: Int)
    suspend fun addExpense(expense: Expense)
    suspend fun deleteExpense(expenseId: Long)
}
