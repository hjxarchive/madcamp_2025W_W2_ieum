package com.ieum.presentation.feature.chat

import com.ieum.domain.model.ChatMessage
import com.ieum.domain.repository.ChatConnectionState

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val partnerName: String = "상대방",
    val isLoading: Boolean = false,
    val error: String? = null,
    val sharingYearMonth: java.time.YearMonth = java.time.YearMonth.now(),
    val sharingSchedules: List<com.ieum.domain.model.Schedule> = emptyList(),
    // WebSocket 관련 상태
    val connectionState: ChatConnectionState = ChatConnectionState.DISCONNECTED,
    val isConnected: Boolean = false,
    val isPartnerTyping: Boolean = false
)
