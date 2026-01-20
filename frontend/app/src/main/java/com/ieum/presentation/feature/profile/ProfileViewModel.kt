package com.ieum.presentation.feature.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ieum.data.api.CoupleService
import com.ieum.data.api.MbtiService
import com.ieum.data.api.UserService
import com.ieum.domain.model.CoupleInfo
import com.ieum.domain.model.User
import com.ieum.domain.repository.AuthRepository
import com.ieum.domain.repository.ChatRepository
import com.ieum.domain.repository.TestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val coupleService: CoupleService,
    private val userService: UserService,
    private val mbtiService: MbtiService,
    private val testRepository: TestRepository,
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _logoutEvent = MutableStateFlow(false)
    val logoutEvent: StateFlow<Boolean> = _logoutEvent.asStateFlow()

    init {
        loadProfile()
        loadMbtiData()
        observeMbtiUpdateEvent()
    }

    /**
     * 파트너 MBTI 업데이트 이벤트 관찰
     * WebSocket으로 파트너가 MBTI 테스트를 완료하면 알림 수신
     */
    private fun observeMbtiUpdateEvent() {
        viewModelScope.launch {
            chatRepository.mbtiUpdateEvent.collect { event ->
                Log.d("ProfileViewModel", "🎉 파트너 MBTI 업데이트 수신: ${event.userName} - ${event.mbtiType}")

                // 파트너 MBTI 즉시 업데이트
                _uiState.value = _uiState.value.copy(partnerMbti = event.mbtiType)

                // 로컬 저장소에도 저장
                testRepository.savePartnerMbtiResult(event.mbtiType)

                // 전체 커플 정보 새로고침 (추가 정보 동기화)
                loadProfile()
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Launch MBTI collection from TestRepository concurrently
            launch {
                testRepository.mbtiResult.collect { mbti ->
                    Log.d("ProfileViewModel", "TestRepository myMbti 업데이트: $mbti")
                    if (mbti != null) {
                        _uiState.value = _uiState.value.copy(myMbti = mbti)
                    }
                }
            }
            
            // Also collect partner MBTI from TestRepository
            launch {
                testRepository.partnerMbtiResult.collect { partnerMbti ->
                    Log.d("ProfileViewModel", "TestRepository partnerMbti 업데이트: $partnerMbti")
                    if (partnerMbti != null) {
                        _uiState.value = _uiState.value.copy(partnerMbti = partnerMbti)
                    }
                }
            }

            // Fetch user and couple info from API
            try {
                val userResponse = userService.getMe()
                val currentUser = User(
                    id = userResponse.id.hashCode().toLong(),
                    name = userResponse.name ?: "",
                    nickname = userResponse.nickname ?: "사용자",
                    mbti = userResponse.mbtiType ?: "",
                    profileImageUrl = userResponse.profileImage,
                    birthday = userResponse.birthday?.let { parseDate(it) }
                )

                // Update user MBTI from API
                if (!userResponse.mbtiType.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(myMbti = userResponse.mbtiType)
                }

                // Fetch couple info
                try {
                    val coupleResponse = coupleService.getCoupleInfo()
                    val partnerMbtiFromApi = coupleResponse.partner?.mbtiType
                    Log.d("ProfileViewModel", "커플 API 파트너 MBTI: $partnerMbtiFromApi")

                    val partner = coupleResponse.partner?.let { partnerDto ->
                        User(
                            id = partnerDto.id.hashCode().toLong(),
                            name = partnerDto.name ?: "",
                            nickname = partnerDto.nickname ?: "상대방",
                            mbti = partnerDto.mbtiType ?: "",
                            profileImageUrl = partnerDto.profileImage,
                            birthday = partnerDto.birthday?.let { parseDate(it) }
                        )
                    } ?: User(id = 0L, name = "", nickname = "상대방", mbti = "")

                    val anniversaryDate = coupleResponse.anniversary?.let { parseDate(it) } ?: LocalDate.now()
                    val dDay = ChronoUnit.DAYS.between(anniversaryDate, LocalDate.now()).toInt()

                    val coupleInfo = CoupleInfo(
                        user = currentUser,
                        partner = partner,
                        dDay = dDay,
                        startDate = coupleResponse.anniversary ?: LocalDate.now().toString()
                    )

                    // partnerMbti가 null이거나 빈 값이면 기존 값 유지 (덮어쓰기 방지)
                    val newPartnerMbti = coupleResponse.partner?.mbtiType?.takeIf { it.isNotEmpty() }
                        ?: _uiState.value.partnerMbti

                    _uiState.value = _uiState.value.copy(
                        coupleInfo = coupleInfo,
                        partnerMbti = newPartnerMbti,
                        isLoading = false,
                        error = null
                    )
                    
                    // Save partner MBTI to TestRepository for local persistence
                    coupleResponse.partner?.mbtiType?.let { partnerMbti ->
                        viewModelScope.launch {
                            testRepository.savePartnerMbtiResult(partnerMbti)
                            Log.d("ProfileViewModel", "Partner MBTI saved to local: $partnerMbti")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "커플 정보 로드 실패", e)
                    // 커플 정보가 없어도 사용자 정보는 표시
                    val coupleInfo = CoupleInfo(
                        user = currentUser,
                        partner = User(id = 0L, name = "", nickname = "상대방", mbti = ""),
                        dDay = 0,
                        startDate = LocalDate.now().toString()
                    )
                    _uiState.value = _uiState.value.copy(
                        coupleInfo = coupleInfo,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "프로필 로드 실패", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun loadMbtiData() {
        viewModelScope.launch {
            try {
                val mbtiResult = mbtiService.getCoupleResult()
                Log.d("ProfileViewModel", "MBTI API 응답: myMbti=${mbtiResult.myMbti}, partnerMbti=${mbtiResult.partnerMbti}")
                
                // Update state with API result, fallback to current values
                val newMyMbti = mbtiResult.myMbti ?: _uiState.value.myMbti
                val newPartnerMbti = mbtiResult.partnerMbti ?: _uiState.value.partnerMbti
                
                Log.d("ProfileViewModel", "업데이트할 MBTI 값: myMbti=$newMyMbti, partnerMbti=$newPartnerMbti")
                
                _uiState.value = _uiState.value.copy(
                    myMbti = newMyMbti,
                    partnerMbti = newPartnerMbti
                )
                
                Log.d("ProfileViewModel", "최종 상태: myMbti=${_uiState.value.myMbti}, partnerMbti=${_uiState.value.partnerMbti}")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "MBTI 데이터 로드 실패", e)
                Log.e("ProfileViewModel", "현재 상태 유지: myMbti=${_uiState.value.myMbti}, partnerMbti=${_uiState.value.partnerMbti}")
            }
        }
    }

    private fun parseDate(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString.substring(0, 10))
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 로그아웃 처리
     */
    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // 서버에 로그아웃 요청
                val result = authRepository.logout()
                result.fold(
                    onSuccess = {
                        Log.d("Logout", "서버 로그아웃 성공")
                    },
                    onFailure = { error ->
                        Log.e("Logout", "서버 로그아웃 실패 (토큰은 삭제됨)", error)
                    }
                )
            } catch (e: Exception) {
                Log.e("Logout", "로그아웃 중 예외 발생", e)
            } finally {
                // 서버 응답과 관계없이 로컬 토큰 삭제
                authRepository.clearToken()
                _uiState.value = _uiState.value.copy(isLoading = false)
                _logoutEvent.value = true
            }
        }
    }

    fun resetLogoutEvent() {
        _logoutEvent.value = false
    }
}
