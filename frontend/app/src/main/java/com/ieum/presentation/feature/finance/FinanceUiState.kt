package com.ieum.presentation.feature.finance

import com.ieum.domain.model.Budget
import com.ieum.domain.model.Expense
import com.ieum.domain.model.ExpenseCategory
import com.ieum.domain.model.MonthlySpending

data class FinanceUiState(
    val budget: Budget = Budget(50000, 0, 50000),
    val expenses: List<Expense> = emptyList(),
    val expensesByCategory: Map<ExpenseCategory, Int> = emptyMap(),
    val monthlySpending: List<MonthlySpending> = emptyList(),
    val selectedTab: Int = 0,
    val showBudgetDialog: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
