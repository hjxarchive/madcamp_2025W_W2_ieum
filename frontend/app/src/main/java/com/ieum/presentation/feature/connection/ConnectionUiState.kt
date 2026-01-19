package com.ieum.presentation.feature.connection

import androidx.compose.ui.graphics.Color

data class ConnectionUiState(
    // 내 초대 코드
    val myCode: String = "",
    val myCodeExpiresAt: String? = null,
    val isLoadingMyCode: Boolean = false,

    // 상대방 코드 입력
    val partnerCode: String = "",
    val showCodeInput: Boolean = false,

    // 연결 상태
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val partnerNickname: String? = null,

    // UI 상태
    val isCopied: Boolean = false,
    val showInvitationModal: Boolean = false,
    val showSuccessModal: Boolean = false,

    // 에러 처리
    val errorMessage: String? = null,

    // 스타일
    val mainTextColor: Color = Color(0xFF5A3E2B),
    val accentColor: Color = Color(0xFFFF6B9D)
)
