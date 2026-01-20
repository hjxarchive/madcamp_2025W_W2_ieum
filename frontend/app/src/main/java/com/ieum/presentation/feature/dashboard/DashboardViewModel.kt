package com.ieum.presentation.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ieum.domain.repository.ChatRepository
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
    private val scheduleRepository: com.ieum.domain.repository.ScheduleRepository,
    private val getAnniversariesUseCase: GetAnniversariesUseCase,
    private val userRepository: com.ieum.domain.repository.UserRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        connectWebSocket()
        refreshData()
        observeUserData()
        loadEvents()
        observeFinanceData()
    }

    /**
     * WebSocket 연결 (앱 전체 실시간 동기화용)
     */
    private fun connectWebSocket() {
        viewModelScope.launch {
            chatRepository.connectWebSocket()
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            financeRepository.refresh()
            scheduleRepository.refresh()
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
            // 두 Flow를 combine하여 동시에 업데이트 (race condition 방지)
            kotlinx.coroutines.flow.combine(
                financeRepository.getExpenses(),
                financeRepository.getBudget()
            ) { expenses, budget ->
                val currentMonth = LocalDate.now().monthValue
                val currentYear = LocalDate.now().year

                // 이번 달 지출만 필터링하여 합계 계산
                val monthlySpent = expenses.filter { expense ->
                    val dateParts = expense.date.split(".")
                    if (dateParts.size == 3) {
                        dateParts[0].toInt() == currentYear && dateParts[1].toInt() == currentMonth
                    } else false
                }.sumOf { it.amount }

                Pair(monthlySpent, budget.monthlyBudget)
            }.collect { (monthlySpent, monthlyBudget) ->
                _uiState.update { it.copy(
                    spentAmount = monthlySpent,
                    totalBudget = monthlyBudget
                ) }
            }
        }
    }
}