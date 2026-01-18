package com.ieum.presentation.feature.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ieum.domain.model.Expense
import com.ieum.domain.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val financeRepository: FinanceRepository
) : ViewModel() {

    // Month Navigation State
    private val _currentMonth = MutableStateFlow(java.time.YearMonth.now())
    val currentMonth: StateFlow<java.time.YearMonth> = _currentMonth.asStateFlow()

    // Consumption Screen State
    // Using a safe timeout/sharing strategy
    val expenses: StateFlow<List<Expense>> = financeRepository.getExpenses()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val filteredExpenses: StateFlow<List<Expense>> = combine(expenses, _currentMonth) { list, month ->
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd")
        list.filter {
            try {
                val date = java.time.LocalDate.parse(it.date, formatter)
                java.time.YearMonth.from(date) == month
            } catch (e: Exception) { false }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val thisMonthTotal: StateFlow<Int> = filteredExpenses.map { list ->
        list.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    // Budget Planning State
    private val _budget = MutableStateFlow(500000) // Default 500,000
    val budget: StateFlow<Int> = _budget

    private val _isAiSuggesting = MutableStateFlow(false)
    val isAiSuggesting: StateFlow<Boolean> = _isAiSuggesting

    private val _suggestedCategories = MutableStateFlow<Map<String, Int>>(emptyMap())
    val suggestedCategories: StateFlow<Map<String, Int>> = _suggestedCategories

    init {
        // Load initial budget if desired, or just start with default
        // If we had a getBudget() in repo, we'd call it here.
    }

    fun setBudget(amount: Int) {
        _budget.value = amount
    }

    fun confirmBudgetAndSuggest() {
        viewModelScope.launch {
            _isAiSuggesting.value = true
            // Simulate AI delay
            kotlinx.coroutines.delay(1500)
            
            val total = _budget.value
            // Mock Suggestion Logic
            val suggestion = mapOf(
                "식비" to (total * 0.4).toInt(),
                "카페" to (total * 0.15).toInt(),
                "문화생활" to (total * 0.1).toInt(),
                "술" to (total * 0.15).toInt(),
                "여행" to (total * 0.15).toInt(),
                "사진" to (total * 0.05).toInt()
            )
            _suggestedCategories.value = suggestion
            _isAiSuggesting.value = false
        }
    }
}
