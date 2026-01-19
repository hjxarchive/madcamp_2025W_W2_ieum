package com.ieum.data.repository

import android.util.Log
import com.ieum.data.api.BudgetService
import com.ieum.data.api.ExpenseService
import com.ieum.data.dto.BudgetRequest
import com.ieum.data.dto.ExpenseRequest
import com.ieum.domain.model.Budget
import com.ieum.domain.model.Expense
import com.ieum.domain.model.ExpenseCategory
import com.ieum.domain.model.MonthlySpending
import com.ieum.domain.repository.FinanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepositoryImpl @Inject constructor(
    private val expenseService: ExpenseService,
    private val budgetService: BudgetService
) : FinanceRepository {

    private val expenses = MutableStateFlow<List<Expense>>(emptyList())
    private val budgetAmount = MutableStateFlow(500000)
    private val expenseIdMap = mutableMapOf<Long, String>() // local id -> server id
    private var localIdCounter = 100L
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // Note: refresh functions are called when user navigates to finance screen
    // Not in init to avoid calling API before login

    private fun refreshExpenses() {
        coroutineScope.launch {
            try {
                val currentMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
                val response = expenseService.getExpenses(yearMonth = currentMonth, page = 0, size = 100)

                val expenseList = response.expenses.map { dto ->
                    val localId = dto.id.hashCode().toLong()
                    expenseIdMap[localId] = dto.id

                    Expense(
                        id = localId.toString(),
                        title = dto.description ?: "",
                        category = mapCategoryFromServer(dto.category),
                        amount = dto.amount.toInt(),
                        date = dto.date.replace("-", ".")
                    )
                }
                expenses.value = expenseList
                Log.d("FinanceRepository", "Loaded ${expenseList.size} expenses from API")
            } catch (e: Exception) {
                Log.e("FinanceRepository", "Failed to load expenses", e)
            }
        }
    }

    private fun refreshBudget() {
        coroutineScope.launch {
            try {
                val currentMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
                val response = budgetService.getBudget(currentMonth)
                budgetAmount.value = response.totalBudget.toInt()
                Log.d("FinanceRepository", "Loaded budget: ${response.totalBudget}")
            } catch (e: Exception) {
                Log.e("FinanceRepository", "Failed to load budget (using default)", e)
            }
        }
    }

    private fun mapCategoryFromServer(category: String): ExpenseCategory {
        return when (category.uppercase()) {
            "FOOD" -> ExpenseCategory.FOOD
            "CAFE" -> ExpenseCategory.CAFE
            "DRINK" -> ExpenseCategory.DRINK
            "TRANSPORT" -> ExpenseCategory.OTHER
            "ENTERTAINMENT" -> ExpenseCategory.CULTURE
            "SHOPPING" -> ExpenseCategory.OTHER
            "CULTURE" -> ExpenseCategory.CULTURE
            "TRAVEL" -> ExpenseCategory.TRAVEL
            "PHOTO" -> ExpenseCategory.PHOTO
            else -> ExpenseCategory.OTHER
        }
    }

    private fun mapCategoryToServer(category: ExpenseCategory): String {
        return when (category) {
            ExpenseCategory.FOOD -> "FOOD"
            ExpenseCategory.CAFE -> "FOOD"
            ExpenseCategory.DRINK -> "FOOD"
            ExpenseCategory.CULTURE -> "ENTERTAINMENT"
            ExpenseCategory.TRAVEL -> "SHOPPING"
            ExpenseCategory.PHOTO -> "ETC"
            ExpenseCategory.OTHER -> "ETC"
        }
    }

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

    override fun getMonthlySpending(): Flow<List<MonthlySpending>> = expenses.map { list ->
        val months = listOf("1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월")
        months.map { month ->
            val monthNum = month.replace("월", "").toInt()
            val totalForMonth = list.filter { expense ->
                try {
                    val date = LocalDate.parse(expense.date, DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                    date.monthValue == monthNum
                } catch (e: Exception) { false }
            }.sumOf { it.amount }
            MonthlySpending(month, totalForMonth)
        }
    }

    override suspend fun setBudget(amount: Int) {
        try {
            val currentMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
            val request = BudgetRequest(
                totalBudget = amount.toDouble(),
                categoryBudgets = mapOf(
                    "FOOD" to (amount * 0.4),
                    "TRANSPORT" to (amount * 0.1),
                    "ENTERTAINMENT" to (amount * 0.2),
                    "SHOPPING" to (amount * 0.2),
                    "ETC" to (amount * 0.1)
                )
            )
            budgetService.setBudget(currentMonth, request)
            Log.d("FinanceRepository", "Set budget: $amount")

            budgetAmount.value = amount
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Failed to set budget", e)
            budgetAmount.value = amount
        }
    }

    override suspend fun addExpense(expense: Expense) {
        try {
            val request = ExpenseRequest(
                amount = expense.amount.toDouble(),
                category = mapCategoryToServer(expense.category),
                description = expense.title,
                date = expense.date.replace(".", "-"),
                paidBy = "ME"
            )
            val response = expenseService.createExpense(request)
            Log.d("FinanceRepository", "Created expense: ${response.id}")

            refreshExpenses()
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Failed to add expense", e)
            // Fallback to local
            val newExpense = expense.copy(id = (++localIdCounter).toString())
            expenses.value = expenses.value + newExpense
        }
    }

    override suspend fun deleteExpense(expenseId: Long) {
        try {
            val serverId = expenseIdMap[expenseId]
            if (serverId != null) {
                expenseService.deleteExpense(serverId)
                Log.d("FinanceRepository", "Deleted expense: $serverId")
                expenseIdMap.remove(expenseId)

                refreshExpenses()
            } else {
                expenses.value = expenses.value.filter { it.id != expenseId.toString() }
            }
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Failed to delete expense", e)
            expenses.value = expenses.value.filter { it.id != expenseId.toString() }
        }
    }
}
