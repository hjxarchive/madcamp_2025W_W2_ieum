package com.ieum.domain.usecase.recommendation

import com.ieum.domain.model.DateCourse
import com.ieum.domain.repository.RecommendationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTodayRecommendationUseCase @Inject constructor(
    private val recommendationRepository: RecommendationRepository
) {
    operator fun invoke(): Flow<DateCourse> = recommendationRepository.getTodayRecommendation()
}

class GetPopularCoursesUseCase @Inject constructor(
    private val recommendationRepository: RecommendationRepository
) {
    operator fun invoke(): Flow<List<DateCourse>> = recommendationRepository.getPopularCourses()
}
