package com.ieum.presentation.feature.connection

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ieum.data.api.CoupleService
import com.ieum.data.dto.CoupleJoinRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val coupleService: CoupleService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectionUiState())
    val uiState: StateFlow<ConnectionUiState> = _uiState.asStateFlow()

    init {
        generateInviteCode()
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
                Log.e("CoupleConnection", "초대 코드 생성 실패", e)
                _uiState.update {
                    it.copy(
                        isLoadingMyCode = false,
                        errorMessage = "초대 코드 생성에 실패했습니다: ${e.message}"
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

                _uiState.update {
                    it.copy(
                        isConnecting = false,
                        isConnected = true,
                        partnerNickname = response.partner?.nickname,
                        showSuccessModal = true
                    )
                }
            } catch (e: Exception) {
                Log.e("CoupleConnection", "커플 연결 실패", e)

                val errorMessage = when {
                    e.message?.contains("404") == true -> "유효하지 않은 코드입니다"
                    e.message?.contains("409") == true -> "이미 연결된 커플이 있습니다"
                    e.message?.contains("410") == true -> "만료된 코드입니다"
                    else -> "연결에 실패했습니다: ${e.message}"
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
