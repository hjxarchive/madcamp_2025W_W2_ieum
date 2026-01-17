package com.ieum.presentation.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ieum.domain.usecase.schedule.GetAnniversariesUseCase
import com.ieum.domain.usecase.schedule.GetSchedulesForMonthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getSchedulesForMonthUseCase: GetSchedulesForMonthUseCase,
    private val getAnniversariesUseCase: GetAnniversariesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadSchedules()
        loadAnniversaries()
    }

    private fun loadSchedules() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getSchedulesForMonthUseCase(_uiState.value.currentMonth)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { schedules ->
                    _uiState.value = _uiState.value.copy(
                        schedules = schedules,
                        selectedDateSchedules = schedules.filter { it.date == _uiState.value.selectedDate },
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    private fun loadAnniversaries() {
        viewModelScope.launch {
            getAnniversariesUseCase()
                .catch { /* ignore */ }
                .collect { anniversaries ->
                    _uiState.value = _uiState.value.copy(anniversaries = anniversaries)
                }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
            selectedDateSchedules = _uiState.value.schedules.filter { it.date == date }
        )
    }

    fun navigateMonth(offset: Int) {
        val newMonth = _uiState.value.currentMonth.plusMonths(offset.toLong())
        changeMonth(newMonth)
    }

    fun changeMonth(yearMonth: YearMonth) {
        _uiState.value = _uiState.value.copy(currentMonth = yearMonth)
        loadSchedules()
    }
}
