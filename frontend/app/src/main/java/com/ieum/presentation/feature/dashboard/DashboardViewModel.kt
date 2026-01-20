package com.ieum.presentation.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ieum.domain.repository.FinanceRepository
import com.ieum.domain.usecase.schedule.GetAnniversariesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val financeRepository: FinanceRepository,
    private val getAnniversariesUseCase: GetAnniversariesUseCase,
    private val userRepository: com.ieum.domain.repository.UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refreshData()
        observeUserData()
        loadEvents()
        observeFinanceData()
    }

    private fun refreshData() {
        viewModelScope.launch {
            financeRepository.refresh()
        }
    }

    private fun observeUserData() {
        viewModelScope.launch {
            userRepository.getCoupleInfo().collect { coupleInfo ->
                val today = LocalDate.now()
                val anniversaryDate = coupleInfo.startDate?.let {
                    try { LocalDate.parse(it) } catch (e: Exception) { null }
                } ?: LocalDate.now()
                
                val days = ChronoUnit.DAYS.between(anniversaryDate, today)
                
                _uiState.update { it.copy(
                    daysTogether = days,
                    partnerNames = "${coupleInfo.user.nickname} & ${coupleInfo.partner.nickname}"
                ) }
            }
        }
    }

    private fun calculateDays() {
        // Obsolete, handled in observeUserData
    }

    private fun loadEvents() {
        viewModelScope.launch {
            getAnniversariesUseCase().collect { domainAnniversaries ->
                val today = LocalDate.now()
                val formatter = DateTimeFormatter.ofPattern("M월 d일")

                val uiEvents = domainAnniversaries
                    .filter { !it.date.isBefore(today) } // 지난 기념일 제외
                    .sortedBy { it.date }
                    .take(3) // 상위 3개만 표시
                    .map { domain ->
                        val daysLeft = ChronoUnit.DAYS.between(today, domain.date).toInt()
                        Anniversary(
                            name = domain.title,
                            date = domain.date.format(formatter),
                            daysLeft = daysLeft
                        )
                    }
                _uiState.update { it.copy(upcomingEvents = uiEvents) }
            }
        }
    }

    private fun observeFinanceData() {
        viewModelScope.launch {
            // Repository에서 전체 지출 내역 Flow를 가져옴
            financeRepository.getExpenses().collect { expenses ->
                val currentMonth = LocalDate.now().monthValue
                val currentYear = LocalDate.now().year

                // 이번 달 지출만 필터링하여 합계 계산
                val monthlySpent = expenses.filter { expense ->
                    val dateParts = expense.date.split(".")
                    if (dateParts.size == 3) {
                        dateParts[0].toInt() == currentYear && dateParts[1].toInt() == currentMonth
                    } else false
                }.sumOf { it.amount }

                _uiState.update { it.copy(spentAmount = monthlySpent) }
            }
        }
        
        // Budget 관찰 추가
        viewModelScope.launch {
            financeRepository.getBudget().collect { budget ->
                _uiState.update { it.copy(totalBudget = budget.monthlyBudget) }
            }
        }
    }
}