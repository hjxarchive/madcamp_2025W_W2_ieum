package com.ieum.presentation.feature.onboarding

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun startTest() {
        _uiState.value = _uiState.value.copy(currentStep = OnboardingStep.TEST)
    }

    fun answerQuestion(answer: Int) {
        val newAnswers = _uiState.value.answers + answer
        val nextQuestion = _uiState.value.currentQuestion + 1

        if (nextQuestion >= _uiState.value.totalQuestions) {
            _uiState.value = _uiState.value.copy(
                answers = newAnswers,
                currentStep = OnboardingStep.RESULT,
                resultType = "감성충만 커플",
                compatibility = 85
            )
        } else {
            _uiState.value = _uiState.value.copy(
                answers = newAnswers,
                currentQuestion = nextQuestion
            )
        }
    }

    fun completeOnboarding() {
        // Navigate to main screen
    }
}
