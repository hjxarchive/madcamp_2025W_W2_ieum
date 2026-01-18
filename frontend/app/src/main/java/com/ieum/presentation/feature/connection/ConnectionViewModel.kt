package com.ieum.presentation.feature.connection

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class ConnectionViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ConnectionUiState())
    val uiState: StateFlow<ConnectionUiState> = _uiState.asStateFlow()

    init {
        generateMyCode()
    }

    private fun generateMyCode() {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val code = (1..6)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
        _uiState.update { it.copy(myCode = code) }
    }

    fun onPartnerCodeChange(newCode: String) {
        if (newCode.length <= 6) {
            _uiState.update { it.copy(partnerCode = newCode.uppercase()) }
        }
    }

    fun toggleCodeInput(show: Boolean) {
        _uiState.update { it.copy(showCodeInput = show) }
    }

    fun setCopied(copied: Boolean) {
        _uiState.update { it.copy(isCopied = copied) }
    }

    fun sendInvitation() {
        // 실제 서버 통신 대신 데모용 모달 띄우기
        _uiState.update { it.copy(showInvitationModal = true) }
    }

    fun handleInvitation(accept: Boolean) {
        _uiState.update { it.copy(showInvitationModal = false) }
        if (accept) {
            // 연결 성공 후 다음 화면 이동 로직 필요
        }
    }
}