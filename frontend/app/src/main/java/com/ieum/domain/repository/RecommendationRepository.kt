package com.ieum.domain.repository

import com.ieum.domain.model.DateCategory
import com.ieum.domain.model.DateCourse
import kotlinx.coroutines.flow.Flow

interface RecommendationRepository {
    fun getTodayRecommendation(): Flow<DateCourse>
    fun getPopularCourses(): Flow<List<DateCourse>>
    fun getCoursesByCategory(category: DateCategory): Flow<List<DateCourse>>
    suspend fun addToSchedule(courseId: Long)
}
