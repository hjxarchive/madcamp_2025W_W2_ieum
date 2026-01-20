package com.ieum.data.repository

import android.util.Log
import com.ieum.data.api.EventService
import com.ieum.data.dto.EventRequest
import com.ieum.domain.model.Anniversary
import com.ieum.domain.model.Schedule
import com.ieum.domain.repository.ScheduleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    private val eventService: EventService
) : ScheduleRepository {

    private val schedules = MutableStateFlow<List<Schedule>>(emptyList())
    private val anniversaries = MutableStateFlow<List<Anniversary>>(emptyList())
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // Note: refresh() is called when user navigates to schedule screen
    // Not in init to avoid calling API before login

    private suspend fun refreshSchedules() {
        try {
            val now = LocalDateTime.now()
            val startDate = now.minusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val endDate = now.plusMonths(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

            val response = eventService.getEvents(startDate, endDate)
            val scheduleList = response.events.map { dto ->
                Schedule(
                    id = dto.id.hashCode(),
                    title = dto.title,
                    date = LocalDate.parse(dto.startDate.substring(0, 10)),
                    time = dto.startDate.substring(11, 16),
                    colorHex = "#FF6B9D",
                    isShared = true,
                    description = dto.description
                )
            }
            schedules.value = scheduleList
            Log.d("ScheduleRepository", "Loaded ${scheduleList.size} schedules from API")

            // Load D-days for anniversaries
            try {
                val dDaysResponse = eventService.getDDays()
                val anniversaryList = dDaysResponse.ddays.mapIndexed { index, dto ->
                    Anniversary(
                        id = index.toLong(),
                        title = dto.title,
                        emoji = if (dto.type == "anniversary") "💕" else "📅",
                        dDay = if (dto.dday >= 0) "D-${dto.dday}" else "D+${-dto.dday}",
                        date = LocalDate.parse(dto.date)
                    )
                }
                anniversaries.value = anniversaryList
            } catch (e: Exception) {
                Log.e("ScheduleRepository", "Failed to load D-days", e)
            }
        } catch (e: Exception) {
            Log.e("ScheduleRepository", "Failed to load schedules", e)
        }
    }

    override fun getSchedules(): Flow<List<Schedule>> = schedules

    override fun getSchedulesForMonth(yearMonth: YearMonth): Flow<List<Schedule>> {
        // Refresh if needed
        coroutineScope.launch {
            try {
                val startDate = yearMonth.atDay(1).atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                val response = eventService.getEvents(startDate, endDate)
                val newSchedules = response.events.map { dto ->
                    Schedule(
                        id = dto.id.hashCode(),
                        title = dto.title,
                        date = LocalDate.parse(dto.startDate.substring(0, 10)),
                        time = dto.startDate.substring(11, 16),
                        colorHex = "#FF6B9D",
                        isShared = true,
                        description = dto.description
                    )
                }
                // Merge with existing schedules
                val existingIds = newSchedules.map { it.id }.toSet()
                schedules.value = schedules.value.filter { it.id !in existingIds } + newSchedules
            } catch (e: Exception) {
                Log.e("ScheduleRepository", "Failed to load schedules for month", e)
            }
        }

        return schedules.map { list ->
            list.filter { YearMonth.from(it.date) == yearMonth }
        }
    }

    override fun getSchedulesForDate(date: LocalDate): Flow<List<Schedule>> =
        schedules.map { list ->
            list.filter { it.date == date }
        }

    override fun getAnniversaries(): Flow<List<Anniversary>> = anniversaries

    override suspend fun addSchedule(schedule: Schedule) {
        try {
            val timeStr = if (schedule.time.isNullOrEmpty()) "00:00" else schedule.time
            val request = EventRequest(
                title = schedule.title,
                description = schedule.description,
                startDate = "${schedule.date}T${timeStr}:00",
                endDate = "${schedule.date}T${timeStr}:00",
                isAllDay = schedule.time.isNullOrEmpty(),
                location = null,
                reminderMinutes = null,
                repeat = "NONE"
            )
            val response = eventService.createEvent(request)
            Log.d("ScheduleRepository", "Created event: ${response.id}")

            // Refresh schedules
            refreshSchedules()
        } catch (e: Exception) {
            Log.e("ScheduleRepository", "Failed to add schedule", e)
            // Fallback to local
            schedules.value = schedules.value + schedule
        }
    }

    override suspend fun addAnniversary(anniversary: Anniversary) {
        anniversaries.value = anniversaries.value + anniversary
    }

    override suspend fun updateSchedule(schedule: Schedule) {
        try {
            val timeStr = if (schedule.time.isNullOrEmpty()) "00:00" else schedule.time
            val request = EventRequest(
                title = schedule.title,
                description = schedule.description,
                startDate = "${schedule.date}T${timeStr}:00",
                endDate = "${schedule.date}T${timeStr}:00",
                isAllDay = schedule.time.isNullOrEmpty(),
                location = null,
                reminderMinutes = null,
                repeat = "NONE"
            )
            // Find original ID (we need to store mapping)
            eventService.updateEvent(schedule.id.toString(), request)
            Log.d("ScheduleRepository", "Updated event: ${schedule.id}")

            refreshSchedules()
        } catch (e: Exception) {
            Log.e("ScheduleRepository", "Failed to update schedule", e)
            schedules.value = schedules.value.map {
                if (it.id == schedule.id) schedule else it
            }
        }
    }

    override suspend fun deleteSchedule(scheduleId: Int) {
        try {
            eventService.deleteEvent(scheduleId.toString())
            Log.d("ScheduleRepository", "Deleted event: $scheduleId")

            refreshSchedules()
        } catch (e: Exception) {
            Log.e("ScheduleRepository", "Failed to delete schedule", e)
            schedules.value = schedules.value.filter { it.id != scheduleId }
        }
    }

    override suspend fun refresh() {
        refreshSchedules()
    }
}
