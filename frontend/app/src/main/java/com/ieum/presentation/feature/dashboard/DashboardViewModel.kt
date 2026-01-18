package com.ieum.presentation.feature.dashboard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class DashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        calculateDays()
        loadEvents()
    }

    private fun calculateDays() {
        val startDate = LocalDate.of(2024, 3, 1) // 예시 시작일
        val today = LocalDate.now()
        val days = ChronoUnit.DAYS.between(startDate, today)
        _uiState.value = _uiState.value.copy(daysTogether = days)
    }

    private fun loadEvents() {
        // 예시 데이터 로드
        val events = listOf(
            Anniversary("100일 기념일", "1월 20일", 3),
            Anniversary("철수 생일", "1월 29일", 12),
            Anniversary("첫 만남 기념일", "2월 11일", 25)
        )
        _uiState.value = _uiState.value.copy(upcomingEvents = events)
    }
}