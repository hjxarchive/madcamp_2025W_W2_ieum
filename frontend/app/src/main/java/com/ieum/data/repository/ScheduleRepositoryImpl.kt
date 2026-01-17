package com.ieum.data.repository

import com.ieum.domain.model.Anniversary
import com.ieum.domain.model.Schedule
import com.ieum.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepositoryImpl @Inject constructor() : ScheduleRepository {

    private val schedules = MutableStateFlow(
        listOf(
            Schedule(
                id = 1L,
                title = "영화 데이트",
                date = LocalDate.now().plusDays(2),
                time = "14:00",
                colorHex = "#FF8FAB",
                isShared = true,
                description = "듄2 보기"
            ),
            Schedule(
                id = 2L,
                title = "카페 투어",
                date = LocalDate.now().plusDays(5),
                time = "15:00",
                colorHex = "#4ECDC4",
                isShared = true
            ),
            Schedule(
                id = 3L,
                title = "점심 약속",
                date = LocalDate.now(),
                time = "12:00",
                colorHex = "#FF6B6B",
                isShared = false
            )
        )
    )

    private val anniversaries = MutableStateFlow(
        listOf(
            Anniversary(
                id = 1L,
                title = "1000일",
                emoji = "💕",
                dDay = "D-89",
                date = LocalDate.now().plusDays(89)
            ),
            Anniversary(
                id = 2L,
                title = "수현 생일",
                emoji = "🎂",
                dDay = "D-45",
                date = LocalDate.now().plusDays(45)
            ),
            Anniversary(
                id = 3L,
                title = "처음 만난 날",
                emoji = "✨",
                dDay = "D+365",
                date = LocalDate.now().minusDays(365)
            )
        )
    )

    override fun getSchedulesForMonth(yearMonth: YearMonth): Flow<List<Schedule>> =
        schedules.map { list ->
            list.filter {
                YearMonth.from(it.date) == yearMonth
            }
        }

    override fun getSchedulesForDate(date: LocalDate): Flow<List<Schedule>> =
        schedules.map { list ->
            list.filter { it.date == date }
        }

    override fun getAnniversaries(): Flow<List<Anniversary>> = anniversaries

    override suspend fun addSchedule(schedule: Schedule) {
        schedules.value = schedules.value + schedule
    }

    override suspend fun updateSchedule(schedule: Schedule) {
        schedules.value = schedules.value.map {
            if (it.id == schedule.id) schedule else it
        }
    }

    override suspend fun deleteSchedule(scheduleId: Long) {
        schedules.value = schedules.value.filter { it.id != scheduleId }
    }
}
