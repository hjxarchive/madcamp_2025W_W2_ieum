package com.ieum.presentation.feature.calendar

import com.ieum.domain.model.Anniversary
import com.ieum.domain.model.Schedule
import java.time.LocalDate
import java.time.YearMonth
import com.ieum.domain.model.BucketItem

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val schedules: List<Schedule> = emptyList(),
    val selectedDateSchedules: List<Schedule> = emptyList(),
    val anniversaries: List<Anniversary> = emptyList(),
    // 추가된 필드들
    val bucketList: List<BucketItem> = emptyList(),
    val expenses: List<com.ieum.domain.model.Expense> = emptyList(),  // 지출 데이터
    // 이번 달 재무 정보 (실시간 동기화)
    val monthlySpent: Int = 0,
    val totalBudget: Int = 500000,
    val isLoading: Boolean = false,
    val error: String? = null
)

// 버킷리스트와 지출을 위한 간단한 데이터 모델 예시
data class BucketItem(
    val id: Long,
    val content: String,
    val isCompleted: Boolean = false
)
data class ExpenseItem(val date: String, val category: String, val amount: Long)
