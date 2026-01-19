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

    private val schedules = MutableStateFlow<List<Schedule>>(emptyList())
    private val anniversaries = MutableStateFlow<List<Anniversary>>(emptyList())

    override fun getSchedules(): Flow<List<Schedule>> = schedules
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

    override suspend fun addAnniversary(anniversary: Anniversary) {
        anniversaries.value = anniversaries.value + anniversary
    }

    override suspend fun updateSchedule(schedule: Schedule) {
        schedules.value = schedules.value.map {
            if (it.id == schedule.id) schedule else it
        }
    }

    override suspend fun deleteSchedule(scheduleId: Int) {
        schedules.value = schedules.value.filter { it.id != scheduleId }
    }
}
