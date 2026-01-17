package com.ieum.presentation.feature.recommend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ieum.domain.model.DateCategory
import com.ieum.domain.model.DateCourse
import com.ieum.domain.usecase.recommendation.GetPopularCoursesUseCase
import com.ieum.domain.usecase.recommendation.GetTodayRecommendationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendViewModel @Inject constructor(
    private val getTodayRecommendationUseCase: GetTodayRecommendationUseCase,
    private val getPopularCoursesUseCase: GetPopularCoursesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecommendUiState())
    val uiState: StateFlow<RecommendUiState> = _uiState.asStateFlow()

    init {
        loadRecommendations()
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            launch {
                getTodayRecommendationUseCase()
                    .catch { /* ignore */ }
                    .collect { course ->
                        _uiState.value = _uiState.value.copy(todayRecommendation = course)
                    }
            }

            launch {
                getPopularCoursesUseCase()
                    .catch { /* ignore */ }
                    .collect { courses ->
                        _uiState.value = _uiState.value.copy(
                            popularCourses = courses,
                            isLoading = false
                        )
                    }
            }
        }
    }

    fun selectCategory(category: DateCategory?) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = if (_uiState.value.selectedCategory == category) null else category
        )
    }

    fun selectCourse(course: DateCourse?) {
        _uiState.value = _uiState.value.copy(selectedCourse = course)
    }
}
