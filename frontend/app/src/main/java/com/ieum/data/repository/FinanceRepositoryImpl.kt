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

    // Note: refresh() is called when user navigates to finance screen
    // Not in init to avoid calling API before login

    private suspend fun refreshExpenses() {
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

    private suspend fun refreshBudget() {
        try {
            val currentMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
            val response = budgetService.getBudget(currentMonth)
            budgetAmount.value = response.totalBudget.toInt()
            Log.d("FinanceRepository", "Loaded budget: ${response.totalBudget}")
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Failed to load budget (using default)", e)
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
        // 낙관적 업데이트: 즉시 UI에 표시
        budgetAmount.value = amount
        Log.d("FinanceRepository", "Set budget optimistically: $amount")

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
            Log.d("FinanceRepository", "Set budget on server: $amount")
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Failed to set budget on server", e)
            // 에러 발생 시 낙관적 업데이트는 유지
        }
    }

    override suspend fun addExpense(expense: Expense) {
        // 낙관적 업데이트: 즉시 UI에 표시
        val tempId = (++localIdCounter).toString()
        val tempExpense = expense.copy(id = tempId)
        expenses.value = expenses.value + tempExpense
        Log.d("FinanceRepository", "Added expense optimistically: ${expense.title}")

        try {
            val request = ExpenseRequest(
                amount = expense.amount.toDouble(),
                category = mapCategoryToServer(expense.category),
                description = expense.title,
                date = expense.date.replace(".", "-"),
                paidBy = "ME"
            )
            val response = expenseService.createExpense(request)
            Log.d("FinanceRepository", "Created expense on server: ${response.id}")

            // 임시 ID를 서버 ID로 업데이트
            expenses.value = expenses.value.map {
                if (it.id == tempId) it.copy(id = response.id) else it
            }
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Failed to add expense to server", e)
            // 에러 발생 시 낙관적 업데이트는 유지
        }
    }

    override suspend fun deleteExpense(expenseId: Long) {
        // 낙관적 업데이트: 즉시 UI에서 제거
        expenses.value = expenses.value.filter { it.id != expenseId.toString() }
        Log.d("FinanceRepository", "✅ Deleted expense optimistically: $expenseId")

        try {
            val serverId = expenseIdMap[expenseId]
            if (serverId != null) {
                expenseService.deleteExpense(serverId)
                Log.d("FinanceRepository", "✅ Deleted expense on server: $serverId")
                expenseIdMap.remove(expenseId)
            }
        } catch (e: Exception) {
            Log.e("FinanceRepository", "Failed to delete expense on server", e)
            // 에러 발생해도 낙관적 업데이트 유지 (이미 삭제됨)
        }
    }

    override suspend fun refresh() {
        refreshExpenses()
        refreshBudget()
    }

    /**
     * WebSocket을 통한 재무 동기화 이벤트 처리
     */
    override fun handleFinanceSync(message: com.ieum.data.websocket.FinanceSyncMessage) {
        Log.d("FinanceRepository", "📨 Handling finance sync: ${message.eventType}")

        when (message.eventType) {
            com.ieum.data.websocket.FinanceEventType.BUDGET_UPDATED -> {
                message.budget?.let { budgetDto ->
                    budgetAmount.value = budgetDto.monthlyBudget
                    Log.d("FinanceRepository", "✅ Updated budget via WebSocket: ${budgetDto.monthlyBudget}")
                }
            }

            com.ieum.data.websocket.FinanceEventType.EXPENSE_ADDED -> {
                message.expense?.let { expenseDto ->
                    val newExpense = Expense(
                        id = expenseDto.id,
                        title = expenseDto.title,
                        category = mapCategoryFromServer(expenseDto.category),
                        amount = expenseDto.amount,
                        date = expenseDto.date.replace("-", ".")
                    )

                    val existingIds = expenses.value.map { it.id }.toSet()
                    if (newExpense.id !in existingIds) {
                        expenses.value = expenses.value + newExpense
                        Log.d("FinanceRepository", "✅ Added expense via WebSocket: ${newExpense.title}")
                    } else {
                        Log.d("FinanceRepository", "⚠️ Expense already exists (duplicate): ${newExpense.title}")
                    }
                }
            }

            com.ieum.data.websocket.FinanceEventType.EXPENSE_UPDATED -> {
                message.expense?.let { expenseDto ->
                    val updatedExpense = Expense(
                        id = expenseDto.id,
                        title = expenseDto.title,
                        category = mapCategoryFromServer(expenseDto.category),
                        amount = expenseDto.amount,
                        date = expenseDto.date.replace("-", ".")
                    )

                    expenses.value = expenses.value.map { existing ->
                        if (existing.id == updatedExpense.id) updatedExpense else existing
                    }
                    Log.d("FinanceRepository", "✅ Updated expense via WebSocket: ${updatedExpense.title}")
                }
            }

            com.ieum.data.websocket.FinanceEventType.EXPENSE_DELETED -> {
                message.expense?.let { expenseDto ->
                    expenses.value = expenses.value.filter { it.id != expenseDto.id }
                    Log.d("FinanceRepository", "✅ Deleted expense via WebSocket: ${expenseDto.title}")
                }
            }
        }
    }
}
