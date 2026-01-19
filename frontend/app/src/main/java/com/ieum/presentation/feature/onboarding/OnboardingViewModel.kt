package com.ieum.presentation.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ieum.domain.model.User
import com.ieum.domain.repository.UserRepository
import com.ieum.domain.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.Dispatchers
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

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

    fun setNickname(nickname: String) {
        _uiState.value = _uiState.value.copy(nickname = nickname)
    }

    fun setBirthday(date: LocalDate) {
        _uiState.value = _uiState.value.copy(birthday = date)
    }

    fun setAnniversaryDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(anniversaryDate = date)
    }

    fun saveOnboardingData() {
        viewModelScope.launch {
            // NonCancellable을 사용하여 화면 전환 중에도 저장이 완료되도록 보장
            withContext(NonCancellable + Dispatchers.IO) {
                try {
                    val state = _uiState.value
                    
                    val user = User(
                        id = 0L, // TODO: 실제 사용자 ID (로그인 후 받아야 함)
                        name = "", // TODO: 실제 이름
                        nickname = state.nickname,
                        mbti = "", // TODO: TestScreen에서 받은 MBTI
                        profileImageUrl = null,
                        birthday = state.birthday,
                        anniversaryDate = state.anniversaryDate
                    )
                    userRepository.saveUserProfile(user)
                    
                    // 생일을 Schedule로 추가
                    state.birthday?.let { birthday ->
                        val today = LocalDate.now()
                        var birthDateThisYear = try {
                            birthday.withYear(today.year)
                        } catch (e: Exception) {
                            if (birthday.monthValue == 2 && birthday.dayOfMonth == 29) {
                                LocalDate.of(today.year, 2, 28)
                            } else {
                                birthday
                            }
                        }
                        
                        // 이미 지난 생일이면 내년으로 설정
                        if (birthDateThisYear.isBefore(today)) {
                            birthDateThisYear = try {
                                birthday.withYear(today.year + 1)
                            } catch (e: Exception) {
                                if (birthday.monthValue == 2 && birthday.dayOfMonth == 29) {
                                    LocalDate.of(today.year + 1, 2, 28)
                                } else {
                                    birthday
                                }
                            }
                        }
                        
                        scheduleRepository.addSchedule(
                            com.ieum.domain.model.Schedule(
                                id = 0,
                                title = "${state.nickname}의 생일",
                                date = birthDateThisYear,
                                time = "",
                                colorHex = "#FFB6C1",
                                isShared = true,
                                description = "생일을 축하합니다!"
                            )
                        )
                        
                        scheduleRepository.addAnniversary(
                            com.ieum.domain.model.Anniversary(
                                id = 0L,
                                title = "${state.nickname} 생일",
                                emoji = "🎂",
                                dDay = "",
                                date = birthDateThisYear
                            )
                        )
                    }
                    
                    // 기념일 자동 생성
                    state.anniversaryDate?.let { anniversaryDate ->
                        val today = LocalDate.now()
                        
                        // 1. 백일 단위 기념일 (100일 ~ 3000일)
                        for (days in 100..3000 step 100) {
                            val milestoneDate = anniversaryDate.plusDays(days.toLong() - 1) // 당일 포함
                            if (!milestoneDate.isBefore(today)) {
                                scheduleRepository.addSchedule(
                                    com.ieum.domain.model.Schedule(
                                        id = 0,
                                        title = "${days}일 기념일",
                                        date = milestoneDate,
                                        time = "",
                                        colorHex = "#FFD700",
                                        isShared = true,
                                        description = "우리 함께한 지 ${days}일!"
                                    )
                                )
                                
                                scheduleRepository.addAnniversary(
                                    com.ieum.domain.model.Anniversary(
                                        id = 0L,
                                        title = "${days}일",
                                        emoji = "💕",
                                        dDay = "",
                                        date = milestoneDate
                                    )
                                )
                            }
                        }

                        // 2. 년 단위 기념일 (1주년 ~ 10주년)
                        for (years in 1..10) {
                            val yearlyDate = try {
                                anniversaryDate.plusYears(years.toLong())
                            } catch (e: Exception) {
                                // 2월 29일인 경우 처리
                                anniversaryDate.plusYears(years.toLong()).minusDays(1)
                            }
                            
                            if (!yearlyDate.isBefore(today)) {
                                scheduleRepository.addSchedule(
                                    com.ieum.domain.model.Schedule(
                                        id = 0,
                                        title = "${years}주년 기념일",
                                        date = yearlyDate,
                                        time = "",
                                        colorHex = "#FF6B6B",
                                        isShared = true,
                                        description = "우리 벌써 ${years}년!"
                                    )
                                )
                                
                                scheduleRepository.addAnniversary(
                                    com.ieum.domain.model.Anniversary(
                                        id = 0L,
                                        title = "${years}주년",
                                        emoji = "✨",
                                        dDay = "",
                                        date = yearlyDate
                                    )
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
