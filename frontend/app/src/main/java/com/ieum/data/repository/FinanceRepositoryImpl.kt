package com.ieum.data.repository

import com.ieum.domain.model.Budget
import com.ieum.domain.model.Expense
import com.ieum.domain.model.ExpenseCategory
import com.ieum.domain.model.MonthlySpending
import com.ieum.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepositoryImpl @Inject constructor() : FinanceRepository {

    private var idCounter = 100L
    private val budgetAmount = MutableStateFlow(50000)

    private val expenses = MutableStateFlow(
        listOf(
            Expense(1L, "점심 식사", ExpenseCategory.FOOD, 12000, "2026.01.15"),
            Expense(2L, "카페 라떼", ExpenseCategory.CAFE, 5500, "2026.01.14"),
            Expense(3L, "맥주", ExpenseCategory.DRINK, 8000, "2026.01.13"),
            Expense(4L, "영화 관람", ExpenseCategory.CULTURE, 15000, "2026.01.12")
        )
    )

    override fun getBudget(): Flow<Budget> = combine(budgetAmount, expenses) { budget, expenseList ->
        val totalSpent = expenseList.sumOf { it.amount }
        Budget(
            monthlyBudget = budget,
            totalSpent = totalSpent,
            remainingBudget = budget - totalSpent
        )
    }

    override fun getExpenses(): Flow<List<Expense>> = expenses

    override fun getExpensesByCategory(): Flow<Map<ExpenseCategory, Int>> =
        expenses.map { list ->
            list.groupBy { it.category }
                .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
        }

    override fun getMonthlySpending(): Flow<List<MonthlySpending>> = MutableStateFlow(
        listOf(
            MonthlySpending("1월", 45000),
            MonthlySpending("2월", 32000),
            MonthlySpending("3월", 0),
            MonthlySpending("4월", 0),
            MonthlySpending("5월", 0),
            MonthlySpending("6월", 0)
        )
    )

    override suspend fun setBudget(amount: Int) {
        budgetAmount.value = amount
    }

    override suspend fun addExpense(expense: Expense) {
        val newExpense = expense.copy(id = ++idCounter)
        expenses.value = expenses.value + newExpense
    }

    override suspend fun deleteExpense(expenseId: Long) {
        expenses.value = expenses.value.filter { it.id != expenseId }
    }
}
