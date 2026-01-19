package com.ieum.presentation.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ieum.domain.model.Anniversary
import com.ieum.domain.usecase.schedule.GetAnniversariesUseCase
import com.ieum.domain.usecase.schedule.GetSchedulesForMonthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getSchedulesForMonthUseCase: GetSchedulesForMonthUseCase,
    private val getAnniversariesUseCase: GetAnniversariesUseCase,
    private val financeRepository: com.ieum.domain.repository.FinanceRepository,
    private val scheduleRepository: com.ieum.domain.repository.ScheduleRepository,
    private val bucketRepository: com.ieum.domain.repository.BucketRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadAllData()
        observeExpenses()
        observeSchedules()
        observeBucketList()
    }

    private fun loadAllData() {
        loadSchedules()
        loadAnniversaries()
    }

    private fun observeExpenses() {
        viewModelScope.launch {
            financeRepository.getExpenses().collect { list ->
                _uiState.update { it.copy(expenses = list) }
            }
        }
    }

    private fun observeSchedules() {
        viewModelScope.launch {
            scheduleRepository.getSchedules().collect { list ->
                _uiState.update { currentState -> // it 대신 currentState 사용
                    currentState.copy( // it.copy 대신 currentState.copy 사용
                        schedules = list,
                        selectedDateSchedules = list.filter { s -> s.date == currentState.selectedDate }
                    )
                }
            }
        }
    }

    fun deleteExpense(expenseId: String) { // Expense 모델의 id가 String이므로 String으로 받음
        viewModelScope.launch {
            // Repository에서 Long을 요구한다면 .toLong()으로 변환하여 호출
            financeRepository.deleteExpense(expenseId.toLong())
        }
    }

    private fun loadSchedules() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getSchedulesForMonthUseCase(_uiState.value.currentMonth)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { schedules ->
                    _uiState.update { it.copy(
                        schedules = schedules,
                        selectedDateSchedules = schedules.filter { it.date == _uiState.value.selectedDate },
                        isLoading = false
                    ) }
                }
        }
    }

    private fun loadAnniversaries() {
        viewModelScope.launch {
            getAnniversariesUseCase()
                .catch { }
                .collect { list -> _uiState.update { it.copy(anniversaries = list) } }
        }
    }

    // --- 조작 함수 (UI 테스트용 메모리 업데이트) ---

    fun addAnniversary(title: String, date: LocalDate) {
        viewModelScope.launch {
            // 에러 해결: 모델에 정의된 모든 파라미터(emoji, dDay 등)를 전달해야 합니다.
            val newAnniversary = Anniversary(
                id = System.currentTimeMillis(),
                title = title,
                emoji = "💕", // 기본 이모지 설정
                dDay = "",    // 계산은 UI에서 수행하므로 비워둠
                date = date
            )
            _uiState.update { it.copy(anniversaries = it.anniversaries + newAnniversary) }
        }
    }


    fun addSchedule(title: String, date: LocalDate, memo: String) {
        viewModelScope.launch {
            val newSchedule = com.ieum.domain.model.Schedule(
                id = 0, // 리포지토리에서 새로 할당
                title = title,
                date = date,
                time = "17:00",
                colorHex = "#ECD4CD",
                description = memo
            )
            // 3. 실제 리포지토리에 데이터 추가 (연동 완료)
            scheduleRepository.addSchedule(newSchedule)
        }
    }

    fun addExpense(title: String, date: LocalDate, memo: String, category: com.ieum.domain.model.ExpenseCategory? = null) {
        viewModelScope.launch {
            val amountValue = memo.toIntOrNull() ?: 0

            val newExpense = com.ieum.domain.model.Expense(
                id = "", // 리포지토리에서 새로 할당하므로 비워둠
                title = title,
                category = category ?: com.ieum.domain.model.ExpenseCategory.FOOD,
                amount = amountValue,
                date = date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            )

            // 3. 리포지토리에 실제 데이터 추가 (이것이 연동의 핵심!)
            financeRepository.addExpense(newExpense)
        }
    }

    fun deleteAnniversary(anniversary: Anniversary) {
        _uiState.update { it.copy(anniversaries = it.anniversaries.filter { it.id != anniversary.id }) }
    }

    private fun observeBucketList() {
        viewModelScope.launch {
            bucketRepository.getBucketItems().collect { list ->
                _uiState.update { it.copy(bucketList = list) }
            }
        }
    }

    // 4. 버킷리스트 추가 (실제 레포지토리 연동)
    fun addBucketList(title: String) {
        viewModelScope.launch {
            // 카테고리는 기본값으로 SPECIAL 등을 설정하거나 UI에서 선택받게 확장 가능
            bucketRepository.addBucketItem(title, com.ieum.domain.model.BucketCategory.SPECIAL)
        }
    }

    // 5. 버킷리스트 완료 토글 (필요 시 사용)
    fun toggleBucketComplete(id: Long) {
        viewModelScope.launch {
            bucketRepository.toggleComplete(id)
        }
    }

    // 6. 버킷리스트 삭제
    fun deleteBucketItem(id: Long) {
        viewModelScope.launch {
            bucketRepository.deleteBucketItem(id) //
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(
            selectedDate = date,
            selectedDateSchedules = it.schedules.filter { s -> s.date == date }
        ) }
    }

    fun navigateMonth(offset: Int) {
        val newMonth = _uiState.value.currentMonth.plusMonths(offset.toLong())
        _uiState.update { it.copy(currentMonth = newMonth) }
        loadSchedules()
    }


}

