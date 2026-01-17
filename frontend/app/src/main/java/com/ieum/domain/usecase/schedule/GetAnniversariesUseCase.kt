package com.ieum.domain.usecase.schedule

import com.ieum.domain.model.Anniversary
import com.ieum.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAnniversariesUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    operator fun invoke(): Flow<List<Anniversary>> = scheduleRepository.getAnniversaries()
}
