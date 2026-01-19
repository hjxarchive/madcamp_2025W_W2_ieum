package com.ieum.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ieum.domain.model.Budget
import com.ieum.domain.model.Expense
import com.ieum.domain.model.ExpenseCategory
import com.ieum.domain.model.MonthlySpending
import com.ieum.domain.repository.FinanceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FinanceRepository {

    private val Context.financeDataStore: DataStore<Preferences> by preferencesDataStore(name = "finance_prefs")
    private val EXPENSES_KEY = stringPreferencesKey("expenses_json")
    private val gson = Gson()

    private var idCounter = 100L
    private val budgetAmount = MutableStateFlow(50000)

    private val expenses = MutableStateFlow<List<Expense>>(emptyList())

    init {
        // 앱 시작 시 저장된 데이터 불러오기
        CoroutineScope(Dispatchers.IO).launch {
            loadExpenses()
        }
    }

    private suspend fun loadExpenses() {
        val preferences = context.financeDataStore.data.first()
        val json = preferences[EXPENSES_KEY]
        if (json != null) {
            val type = object : TypeToken<List<Expense>>() {}.type
            val loadedList: List<Expense> = gson.fromJson(json, type)
            expenses.value = loadedList
            
            // ID 카운터 복구 (가장 큰 ID + 1)
            idCounter = loadedList.maxOfOrNull { it.id.toLongOrNull() ?: 100L } ?: 100L
        } else {
             // 없으면 빈 리스트로 시작
             expenses.value = emptyList()
             saveExpenses(emptyList())
        }
    }

    private suspend fun saveExpenses(list: List<Expense>) {
        val json = gson.toJson(list)
        context.financeDataStore.edit { prefs ->
            prefs[EXPENSES_KEY] = json
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

    override fun getMonthlySpending(): Flow<List<MonthlySpending>> = MutableStateFlow(
        listOf(
            MonthlySpending("1월", 0),
            MonthlySpending("2월", 0),
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
        val newExpense = expense.copy(id = (++idCounter).toString())
        val newList = expenses.value + newExpense
        expenses.value = newList
        saveExpenses(newList)
    }

    override suspend fun deleteExpense(expenseId: Long) {
        val newList = expenses.value.filter { it.id != expenseId.toString() }
        expenses.value = newList
        saveExpenses(newList)
    }
}
