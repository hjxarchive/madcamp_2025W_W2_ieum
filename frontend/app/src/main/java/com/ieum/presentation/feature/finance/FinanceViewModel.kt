package com.ieum.presentation.feature.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ieum.domain.usecase.finance.GetBudgetUseCase
import com.ieum.domain.usecase.finance.GetExpensesByCategoryUseCase
import com.ieum.domain.usecase.finance.GetExpensesUseCase
import com.ieum.domain.usecase.finance.SetBudgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val getBudgetUseCase: GetBudgetUseCase,
    private val getExpensesUseCase: GetExpensesUseCase,
    private val getExpensesByCategoryUseCase: GetExpensesByCategoryUseCase,
    private val setBudgetUseCase: SetBudgetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    init {
        loadFinanceData()
    }

    private fun loadFinanceData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            launch {
                getBudgetUseCase()
                    .catch { /* ignore */ }
                    .collect { budget ->
                        _uiState.value = _uiState.value.copy(budget = budget)
                    }
            }

            launch {
                getExpensesUseCase()
                    .catch { /* ignore */ }
                    .collect { expenses ->
                        _uiState.value = _uiState.value.copy(
                            expenses = expenses,
                            isLoading = false
                        )
                    }
            }

            launch {
                getExpensesByCategoryUseCase()
                    .catch { /* ignore */ }
                    .collect { categoryMap ->
                        _uiState.value = _uiState.value.copy(expensesByCategory = categoryMap)
                    }
            }
        }
    }

    fun selectTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun showBudgetDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showBudgetDialog = show)
    }

    fun setBudget(amount: Int) {
        viewModelScope.launch {
            setBudgetUseCase(amount)
            _uiState.value = _uiState.value.copy(showBudgetDialog = false)
        }
    }
}
