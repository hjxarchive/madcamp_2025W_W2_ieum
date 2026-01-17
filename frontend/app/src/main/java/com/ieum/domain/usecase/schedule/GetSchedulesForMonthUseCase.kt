package com.ieum.domain.usecase.schedule

import com.ieum.domain.model.Schedule
import com.ieum.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject

class GetSchedulesForMonthUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    operator fun invoke(yearMonth: YearMonth): Flow<List<Schedule>> =
        scheduleRepository.getSchedulesForMonth(yearMonth)
}
