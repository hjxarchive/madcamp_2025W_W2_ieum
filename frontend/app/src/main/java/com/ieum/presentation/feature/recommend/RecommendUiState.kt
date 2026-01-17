package com.ieum.presentation.feature.recommend

import com.ieum.domain.model.DateCategory
import com.ieum.domain.model.DateCourse

data class RecommendUiState(
    val todayRecommendation: DateCourse? = null,
    val popularCourses: List<DateCourse> = emptyList(),
    val selectedCategory: DateCategory? = null,
    val selectedCourse: DateCourse? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
