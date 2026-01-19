package com.ieum.presentation.feature.onboarding

import java.time.LocalDate

data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.WELCOME,
    val currentQuestion: Int = 0,
    val totalQuestions: Int = 5,
    val answers: List<Int> = emptyList(),
    val resultType: String? = null,
    val compatibility: Int = 0,
    val isLoading: Boolean = false,
    
    // 신규 추가 필드
    val nickname: String = "",
    val birthday: LocalDate? = null,
    val anniversaryDate: LocalDate? = null
)

enum class OnboardingStep {
    WELCOME,
    TEST,
    RESULT
}
