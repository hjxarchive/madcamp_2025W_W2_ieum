package com.ieum.presentation.feature.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ieum.data.api.CoupleService
import com.ieum.data.api.UserService
import com.ieum.data.dto.CoupleUpdateRequest
import com.ieum.data.dto.UserUpdateRequest
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
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val scheduleRepository: ScheduleRepository,
    private val userService: UserService,
    private val coupleService: CoupleService
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

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
            withContext(NonCancellable + Dispatchers.IO) {
                try {
                    val state = _uiState.value

                    // 1. 서버에 사용자 정보 업데이트 (API 호출)
                    try {
                        val updateRequest = UserUpdateRequest(
                            nickname = state.nickname,
                            birthday = state.birthday?.format(dateFormatter)
                        )
                        val updatedUser = userService.updateMe(updateRequest)
                        Log.d("Onboarding", "서버 사용자 정보 업데이트 성공: ${updatedUser.nickname}")
                    } catch (e: Exception) {
                        Log.e("Onboarding", "서버 사용자 정보 업데이트 실패", e)
                        // 서버 실패해도 로컬 저장은 계속 진행
                    }

                    // 2. 로컬 저장소에도 저장 (오프라인 지원)
                    val user = User(
                        id = 0L,
                        name = "",
                        nickname = state.nickname,
                        mbti = "",
                        profileImageUrl = null,
                        birthday = state.birthday,
                        anniversaryDate = state.anniversaryDate
                    )
                    userRepository.saveUserProfile(user)

                    // 3. 생일을 Schedule로 추가 (로컬)
                    state.birthday?.let { birthday ->
                        addBirthdaySchedule(state.nickname, birthday)
                    }

                    // 4. 기념일 저장 (서버 + 로컬)
                    state.anniversaryDate?.let { anniversaryDate ->
                        // 서버에 기념일 업데이트 시도
                        try {
                            val coupleUpdateRequest = CoupleUpdateRequest(
                                anniversary = anniversaryDate.format(dateFormatter)
                            )
                            coupleService.updateCoupleInfo(coupleUpdateRequest)
                            Log.d("Onboarding", "서버 기념일 업데이트 성공")
                        } catch (e: Exception) {
                            Log.e("Onboarding", "서버 기념일 업데이트 실패 (커플 미연결 가능)", e)
                        }

                        // 로컬에 기념일 스케줄 추가
                        addAnniversarySchedules(anniversaryDate)
                    }

                    Log.d("Onboarding", "온보딩 데이터 저장 완료")
                } catch (e: Exception) {
                    Log.e("Onboarding", "온보딩 데이터 저장 실패", e)
                }
            }
        }
    }

    private suspend fun addBirthdaySchedule(nickname: String, birthday: LocalDate) {
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
                title = "${nickname}의 생일",
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
                title = "${nickname} 생일",
                emoji = "🎂",
                dDay = "",
                date = birthDateThisYear
            )
        )
    }

    private suspend fun addAnniversarySchedules(anniversaryDate: LocalDate) {
        val today = LocalDate.now()

        // 100일 단위 기념일
        for (days in 100..3000 step 100) {
            val milestoneDate = anniversaryDate.plusDays(days.toLong() - 1)
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

        // 년 단위 기념일
        for (years in 1..10) {
            val yearlyDate = try {
                anniversaryDate.plusYears(years.toLong())
            } catch (e: Exception) {
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
}
