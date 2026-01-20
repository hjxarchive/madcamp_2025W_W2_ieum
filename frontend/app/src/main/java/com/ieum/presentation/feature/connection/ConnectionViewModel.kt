package com.ieum.presentation.feature.connection

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ieum.data.api.CoupleService
import com.ieum.data.dto.CoupleJoinRequest
import com.ieum.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val coupleService: CoupleService,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectionUiState())
    val uiState: StateFlow<ConnectionUiState> = _uiState.asStateFlow()

    private var pollingJob: kotlinx.coroutines.Job? = null

    init {
        generateInviteCode()
        startPollingCoupleStatus()
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }

    /**
     * 주기적으로 커플 연결 상태 확인 (코드 생성자용)
     */
    private fun startPollingCoupleStatus() {
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(3000) // 3초마다 확인
                if (_uiState.value.isConnected) {
                    break // 이미 연결됨
                }
                try {
                    val coupleInfo = coupleService.getCoupleInfo()
                    // partner가 있으면 연결 완료
                    if (coupleInfo.partner != null) {
                        Log.d("CoupleConnection", "폴링: 커플 연결 감지! 파트너: ${coupleInfo.partner.nickname}")
                        // 파트너 닉네임 저장
                        coupleInfo.partner.nickname?.let { nickname ->
                            userRepository.updatePartnerNickname(nickname)
                        }
                        _uiState.update {
                            it.copy(
                                isConnected = true,
                                partnerNickname = coupleInfo.partner.nickname,
                                showSuccessModal = true
                            )
                        }
                        break
                    }
                } catch (e: Exception) {
                    // 404 등의 에러는 아직 연결 안됨 - 무시
                    Log.d("CoupleConnection", "폴링: 아직 연결 안됨")
                }
            }
        }
    }

    /**
     * 서버에서 초대 코드 생성
     */
    fun generateInviteCode() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMyCode = true, errorMessage = null) }

            try {
                val response = coupleService.createInviteCode()
                Log.d("CoupleConnection", "초대 코드 생성 성공: ${response.inviteCode}")

                _uiState.update {
                    it.copy(
                        myCode = response.inviteCode,
                        myCodeExpiresAt = response.expiresAt,
                        isLoadingMyCode = false
                    )
                }
            } catch (e: Exception) {
                Log.e("CoupleConnection", "초대 코드 생성 실패: ${e.message}", e)

                // 409 에러 (이미 커플이 있음) - 기존 커플 삭제 후 재시도
                if (e.message?.contains("409") == true) {
                    Log.d("CoupleConnection", "기존 커플이 존재함. 삭제 후 재시도...")
                    try {
                        coupleService.disconnectCouple()
                        Log.d("CoupleConnection", "기존 커플 삭제 완료. 1초 후 재시도...")
                        delay(1000) // 서버 처리 대기
                        val response = coupleService.createInviteCode()
                        Log.d("CoupleConnection", "재시도 성공: ${response.inviteCode}")
                        _uiState.update {
                            it.copy(
                                myCode = response.inviteCode,
                                myCodeExpiresAt = response.expiresAt,
                                isLoadingMyCode = false
                            )
                        }
                        return@launch
                    } catch (retryError: Exception) {
                        Log.e("CoupleConnection", "재시도 실패: ${retryError.message}", retryError)
                    }
                }

                _uiState.update {
                    it.copy(
                        isLoadingMyCode = false,
                        errorMessage = "초대 코드 생성에 실패했습니다"
                    )
                }
            }
        }
    }

    /**
     * 상대방 코드 입력 변경
     */
    fun onPartnerCodeChange(newCode: String) {
        if (newCode.length <= 6) {
            _uiState.update {
                it.copy(
                    partnerCode = newCode.uppercase(),
                    errorMessage = null
                )
            }
        }
    }

    /**
     * 코드 입력 UI 토글
     */
    fun toggleCodeInput(show: Boolean) {
        _uiState.update { it.copy(showCodeInput = show, errorMessage = null) }
    }

    /**
     * 복사 상태 설정
     */
    fun setCopied(copied: Boolean) {
        _uiState.update { it.copy(isCopied = copied) }
    }

    /**
     * 커플 연결 요청 (초대 코드로 연결)
     */
    fun joinCouple() {
        val partnerCode = _uiState.value.partnerCode
        if (partnerCode.length != 6) {
            _uiState.update { it.copy(errorMessage = "6자리 코드를 입력해주세요") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isConnecting = true, errorMessage = null) }

            try {
                val response = coupleService.joinCouple(CoupleJoinRequest(partnerCode))
                Log.d("CoupleConnection", "커플 연결 성공: ${response.partner?.nickname}")

                // 파트너 닉네임 저장
                response.partner?.nickname?.let { nickname ->
                    userRepository.updatePartnerNickname(nickname)
                }

                _uiState.update {
                    it.copy(
                        isConnecting = false,
                        isConnected = true,
                        partnerNickname = response.partner?.nickname,
                        showSuccessModal = true
                    )
                }
            } catch (e: Exception) {
                Log.e("CoupleConnection", "커플 연결 실패: ${e.message}", e)

                // 409 에러 - 기존 커플 삭제 후 재시도
                if (e.message?.contains("409") == true) {
                    Log.d("CoupleConnection", "기존 커플 삭제 후 연결 재시도...")
                    try {
                        coupleService.disconnectCouple()
                        delay(500)
                        val response = coupleService.joinCouple(CoupleJoinRequest(partnerCode))
                        Log.d("CoupleConnection", "재연결 성공: ${response.partner?.nickname}")
                        // 파트너 닉네임 저장
                        response.partner?.nickname?.let { nickname ->
                            userRepository.updatePartnerNickname(nickname)
                        }
                        _uiState.update {
                            it.copy(
                                isConnecting = false,
                                isConnected = true,
                                partnerNickname = response.partner?.nickname,
                                showSuccessModal = true
                            )
                        }
                        return@launch
                    } catch (retryError: Exception) {
                        Log.e("CoupleConnection", "재연결 실패: ${retryError.message}", retryError)
                    }
                }

                val errorMessage = when {
                    e.message?.contains("404") == true -> "유효하지 않은 코드입니다"
                    e.message?.contains("409") == true -> "이미 연결된 커플이 있습니다"
                    e.message?.contains("410") == true -> "만료된 코드입니다"
                    else -> "연결에 실패했습니다"
                }

                _uiState.update {
                    it.copy(
                        isConnecting = false,
                        errorMessage = errorMessage
                    )
                }
            }
        }
    }

    /**
     * 초대장 모달 표시 (데모용)
     */
    fun sendInvitation() {
        // 코드 연결 시도
        joinCouple()
    }

    /**
     * 초대 응답 처리 (데모용)
     */
    fun handleInvitation(accept: Boolean) {
        _uiState.update { it.copy(showInvitationModal = false) }
        if (accept) {
            _uiState.update { it.copy(isConnected = true) }
        }
    }

    /**
     * 성공 모달 닫기
     */
    fun dismissSuccessModal() {
        _uiState.update { it.copy(showSuccessModal = false) }
    }

    /**
     * 에러 메시지 초기화
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 코드 새로고침
     */
    fun refreshCode() {
        generateInviteCode()
    }
}
