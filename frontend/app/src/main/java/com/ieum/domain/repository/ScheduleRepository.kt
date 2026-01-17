package com.ieum.domain.repository

import com.ieum.domain.model.Anniversary
import com.ieum.domain.model.Schedule
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

interface ScheduleRepository {
    fun getSchedulesForMonth(yearMonth: YearMonth): Flow<List<Schedule>>
    fun getSchedulesForDate(date: LocalDate): Flow<List<Schedule>>
    fun getAnniversaries(): Flow<List<Anniversary>>
    suspend fun addSchedule(schedule: Schedule)
    suspend fun updateSchedule(schedule: Schedule)
    suspend fun deleteSchedule(scheduleId: Long)
}
