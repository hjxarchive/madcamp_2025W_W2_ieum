package com.ieum.presentation.feature.onboarding

data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.WELCOME,
    val currentQuestion: Int = 0,
    val totalQuestions: Int = 5,
    val answers: List<Int> = emptyList(),
    val resultType: String? = null,
    val compatibility: Int = 0,
    val isLoading: Boolean = false
)

enum class OnboardingStep {
    WELCOME,
    TEST,
    RESULT
}
