package com.ieum.presentation.feature.connection

data class ConnectionUiState(
    val myCode: String = "",
    val partnerCode: String = "",
    val showCodeInput: Boolean = false,
    val isCopied: Boolean = false,
    val showInvitationModal: Boolean = false,
    val mainTextColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(0xFF5A3E2B),
    val accentColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(0xFFFF6B9D)
)