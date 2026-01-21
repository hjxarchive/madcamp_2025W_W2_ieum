package com.ieum.presentation.feature.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.ieum.BuildConfig
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

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

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

    /**
     * MBTI 기반 케미 추천/주의 사항 로드 (Gemini API)
     */
    fun loadChemistryRecommendations() {
        val myMbti = _uiState.value.myMbti?.uppercase()
        val partnerMbti = _uiState.value.partnerMbti?.uppercase()

        if (myMbti.isNullOrEmpty() || partnerMbti.isNullOrEmpty()) {
            Log.d("ProfileViewModel", "MBTI 정보 부족: myMbti=$myMbti, partnerMbti=$partnerMbti")
            return
        }

        // 이미 로딩 중이거나 데이터가 있으면 스킵
        if (_uiState.value.isLoadingChemistry || _uiState.value.recommendations.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingChemistry = true, chemistryError = null)

            try {
                val prompt = buildChemistryPrompt(myMbti, partnerMbti)
                Log.d("ProfileViewModel", "Gemini 프롬프트: $prompt")

                val response = generativeModel.generateContent(prompt)
                val responseText = response.text ?: throw Exception("응답을 받지 못했습니다.")

                Log.d("ProfileViewModel", "Gemini 응답: $responseText")

                val (recommendations, cautions) = parseChemistryResponse(responseText)

                _uiState.value = _uiState.value.copy(
                    recommendations = recommendations,
                    cautions = cautions,
                    isLoadingChemistry = false
                )
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "케미 추천 로드 실패", e)
                _uiState.value = _uiState.value.copy(
                    isLoadingChemistry = false,
                    chemistryError = e.message
                )
            }
        }
    }

    private fun buildChemistryPrompt(myMbti: String, partnerMbti: String): String {
        // MBTI 차원 해석
        // M/I: 소비 성향 (Measured 절제형 / Indulgent 향유형)
        // D/T: 갈등 성향 (Direct 직접형 / Thoughtful 숙고형)
        // E/C: 도전 성향 (Explorer 탐험형 / Comfort 안정형)
        // P/F: 데이트 성향 (Planner 계획형 / Flow 즉흥형)

        val myTraits = parseMbtiTraits(myMbti)
        val partnerTraits = parseMbtiTraits(partnerMbti)

        return """
            당신은 커플 관계 전문가입니다. 두 사람의 연애 MBTI를 분석하여 추천과 주의사항을 알려주세요.

            나의 MBTI: $myMbti
            - 소비 성향: ${myTraits.spending}
            - 갈등 성향: ${myTraits.conflict}
            - 도전 성향: ${myTraits.adventure}
            - 데이트 성향: ${myTraits.dating}

            상대방 MBTI: $partnerMbti
            - 소비 성향: ${partnerTraits.spending}
            - 갈등 성향: ${partnerTraits.conflict}
            - 도전 성향: ${partnerTraits.adventure}
            - 데이트 성향: ${partnerTraits.dating}

            다음 형식으로 정확히 응답해주세요:

            [추천]
            데이트: (제목) | (설명 한 문장)
            소비: (제목) | (설명 한 문장)
            갈등: (제목) | (설명 한 문장)
            도전: (제목) | (설명 한 문장)

            [주의]
            데이트: (제목) | (설명 한 문장)
            소비: (제목) | (설명 한 문장)
            갈등: (제목) | (설명 한 문장)
            도전: (제목) | (설명 한 문장)

            각 항목은 두 사람의 성향 조합을 고려하여 구체적이고 실용적인 조언을 해주세요.
            제목은 10자 이내, 설명은 30자 이내로 간결하게 작성해주세요.
        """.trimIndent()
    }

    private data class MbtiTraits(
        val spending: String,
        val conflict: String,
        val adventure: String,
        val dating: String
    )

    private fun parseMbtiTraits(mbti: String): MbtiTraits {
        val upper = mbti.uppercase()
        return MbtiTraits(
            spending = if (upper.contains("M")) "절제형 (Measured)" else "향유형 (Indulgent)",
            conflict = if (upper.contains("D")) "직접형 (Direct)" else "숙고형 (Thoughtful)",
            adventure = if (upper.contains("E")) "탐험형 (Explorer)" else "안정형 (Comfort)",
            dating = if (upper.contains("P")) "계획형 (Planner)" else "즉흥형 (Flow)"
        )
    }

    private fun parseChemistryResponse(response: String): Pair<List<ChemistryCard>, List<ChemistryCard>> {
        val recommendations = mutableListOf<ChemistryCard>()
        val cautions = mutableListOf<ChemistryCard>()

        val emojiMap = mapOf(
            "데이트" to "💕",
            "소비" to "💰",
            "갈등" to "💬",
            "도전" to "🚀"
        )

        var isRecommendation = true

        for (line in response.lines()) {
            val trimmed = line.trim()

            if (trimmed.contains("[추천]")) {
                isRecommendation = true
                continue
            }
            if (trimmed.contains("[주의]")) {
                isRecommendation = false
                continue
            }

            for (category in listOf("데이트", "소비", "갈등", "도전")) {
                if (trimmed.startsWith("$category:")) {
                    val content = trimmed.removePrefix("$category:").trim()
                    val parts = content.split("|").map { it.trim() }
                    val title = parts.getOrNull(0) ?: continue
                    val description = parts.getOrNull(1) ?: ""

                    val card = ChemistryCard(
                        category = category,
                        title = title,
                        description = description,
                        emoji = emojiMap[category] ?: "✨"
                    )

                    if (isRecommendation) {
                        recommendations.add(card)
                    } else {
                        cautions.add(card)
                    }
                }
            }
        }

        return Pair(recommendations, cautions)
    }
}
