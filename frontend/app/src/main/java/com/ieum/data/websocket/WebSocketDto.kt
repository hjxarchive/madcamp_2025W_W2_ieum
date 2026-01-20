package com.ieum.data.websocket

import com.google.gson.annotations.SerializedName

/**
 * 클라이언트 → 서버 메시지 요청
 */
data class WebSocketMessageRequest(
    val type: String = "TEXT",
    val content: String?,
    val imageUrl: String? = null,
    val tempId: String? = null
)

/**
 * E2EE 암호화된 메시지 요청
 */
data class WebSocketE2EEMessageRequest(
    val encryptedContent: String,
    val iv: String,
    val tempId: String? = null
)

/**
 * 서버 → 클라이언트 메시지 응답
 */
data class WebSocketMessageResponse(
    val id: String,
    val senderId: String,
    val senderName: String?,
    val senderProfileImage: String?,
    val content: String?,
    val type: String,
    val imageUrl: String?,
    val isRead: Boolean,
    val readAt: String?,
    val createdAt: String,
    val tempId: String?
)

/**
 * E2EE 암호화된 메시지 응답
 */
data class WebSocketE2EEMessageResponse(
    val id: String,
    val senderId: String,
    val encryptedContent: String,
    val iv: String,
    val type: String,
    val isRead: Boolean,
    val createdAt: String,
    val tempId: String?
)

/**
 * 읽음 확인 메시지
 */
data class ReadReceiptMessage(
    val type: String = "READ_RECEIPT",
    val messageIds: List<String>,
    val readAt: String
)

/**
 * 시스템 메시지
 */
data class SystemMessage(
    val type: String,
    val event: String, // USER_CONNECTED, USER_DISCONNECTED, TYPING
    val userId: String,
    val timestamp: String
)

/**
 * 타이핑 인디케이터
 */
data class TypingIndicator(
    val userId: String,
    val isTyping: Boolean
)

/**
 * 에러 메시지
 */
data class WebSocketErrorMessage(
    val type: String = "ERROR",
    val code: String,
    val message: String,
    val tempId: String? = null
)

/**
 * WebSocket 연결 상태
 */
enum class WebSocketConnectionState {
    CONNECTING,
    CONNECTED,
    DISCONNECTED,
    ERROR
}

/**
 * 채팅 이벤트 리스너
 */
interface ChatEventListener {
    fun onConnected()
    fun onDisconnected()
    fun onMessageReceived(message: WebSocketMessageResponse)
    fun onE2EEMessageReceived(message: WebSocketE2EEMessageResponse)
    fun onReadReceipt(messageIds: List<String>, readAt: String)
    fun onTypingIndicator(userId: String, isTyping: Boolean)
    fun onError(error: Throwable)
    fun onMessageSent(tempId: String?)
}
