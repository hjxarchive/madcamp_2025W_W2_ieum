package com.ieum.presentation.feature.chat

import com.ieum.domain.model.ChatMessage

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val partnerName: String = "수현",
    val isLoading: Boolean = false,
    val error: String? = null
)
