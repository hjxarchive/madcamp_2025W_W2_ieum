package com.ieum.presentation.feature.calendar

import com.ieum.domain.model.Anniversary
import com.ieum.domain.model.Schedule
import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val schedules: List<Schedule> = emptyList(),
    val selectedDateSchedules: List<Schedule> = emptyList(),
    val anniversaries: List<Anniversary> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
