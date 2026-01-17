package com.ieum.domain.usecase.finance

import com.ieum.domain.model.Budget
import com.ieum.domain.model.Expense
import com.ieum.domain.model.ExpenseCategory
import com.ieum.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBudgetUseCase @Inject constructor(
    private val financeRepository: FinanceRepository
) {
    operator fun invoke(): Flow<Budget> = financeRepository.getBudget()
}

class GetExpensesUseCase @Inject constructor(
    private val financeRepository: FinanceRepository
) {
    operator fun invoke(): Flow<List<Expense>> = financeRepository.getExpenses()
}

class GetExpensesByCategoryUseCase @Inject constructor(
    private val financeRepository: FinanceRepository
) {
    operator fun invoke(): Flow<Map<ExpenseCategory, Int>> = financeRepository.getExpensesByCategory()
}

class SetBudgetUseCase @Inject constructor(
    private val financeRepository: FinanceRepository
) {
    suspend operator fun invoke(amount: Int) = financeRepository.setBudget(amount)
}
