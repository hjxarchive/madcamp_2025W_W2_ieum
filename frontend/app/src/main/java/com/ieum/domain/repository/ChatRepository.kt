package com.ieum.domain.repository

import com.ieum.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChatRepository {
    val messages: StateFlow<List<ChatMessage>>
    val connectionState: StateFlow<ChatConnectionState>
    val isPartnerTyping: StateFlow<Boolean>

    fun getMessages(): Flow<List<ChatMessage>>
    suspend fun connectWebSocket()
    suspend fun disconnectWebSocket()
    suspend fun sendMessage(content: String)
    suspend fun sendE2EEMessage(content: String)
    suspend fun sendReadReceipt(messageIds: List<String>)
    suspend fun sendTypingIndicator(isTyping: Boolean)
    suspend fun shareSchedule(title: String, date: String)
    suspend fun sharePlace(name: String, address: String)
    suspend fun shareBucket(title: String)
    suspend fun loadHistoryMessages()
}

enum class ChatConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}
