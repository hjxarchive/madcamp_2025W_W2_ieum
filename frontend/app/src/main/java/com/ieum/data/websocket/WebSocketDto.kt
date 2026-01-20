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

    // 실시간 동기화 이벤트
    fun onScheduleSync(message: ScheduleSyncMessage)
    fun onBucketSync(message: BucketSyncMessage)
    fun onFinanceSync(message: FinanceSyncMessage)

    // MBTI 업데이트 이벤트
    fun onMbtiUpdated(message: MbtiUpdateMessage)
}

// ==================== 일정 동기화 ====================

/**
 * 일정 이벤트 타입
 */
enum class ScheduleEventType {
    @SerializedName("ADDED")
    ADDED,

    @SerializedName("UPDATED")
    UPDATED,

    @SerializedName("DELETED")
    DELETED
}

/**
 * 일정 동기화 메시지
 */
data class ScheduleSyncMessage(
    val eventType: ScheduleEventType,
    val schedule: ScheduleDto,
    val userId: String,
    val timestamp: String
)

/**
 * 일정 DTO
 * 백엔드에서 id는 UUID(String) 형태로 전송됨
 */
data class ScheduleDto(
    val id: String,             // UUID 형식 (백엔드)
    val title: String,
    val date: String,           // ISO-8601 format (yyyy-MM-dd)
    val time: String?,          // nullable - 종일 일정의 경우 null
    val colorHex: String?,      // nullable
    val description: String?
)

// ==================== 버킷리스트 동기화 ====================

/**
 * 버킷 이벤트 타입
 */
enum class BucketEventType {
    @SerializedName("ADDED")
    ADDED,

    @SerializedName("COMPLETED")
    COMPLETED,

    @SerializedName("UPDATED")
    UPDATED,

    @SerializedName("DELETED")
    DELETED
}

/**
 * 버킷 동기화 메시지
 */
data class BucketSyncMessage(
    val eventType: BucketEventType,
    val bucket: BucketDto,
    val userId: String,
    val timestamp: String
)

/**
 * 버킷 DTO
 * 백엔드에서 id는 UUID(String) 형태로 전송됨
 */
data class BucketDto(
    val id: String,             // UUID 형식 (백엔드)
    val title: String,
    val category: String?,
    val isCompleted: Boolean,
    val createdAt: String,
    val completedAt: String?
)

// ==================== 재무 동기화 ====================

/**
 * 재무 이벤트 타입
 */
enum class FinanceEventType {
    @SerializedName("BUDGET_UPDATED")
    BUDGET_UPDATED,

    @SerializedName("EXPENSE_ADDED")
    EXPENSE_ADDED,

    @SerializedName("EXPENSE_UPDATED")
    EXPENSE_UPDATED,

    @SerializedName("EXPENSE_DELETED")
    EXPENSE_DELETED
}

/**
 * 재무 동기화 메시지
 */
data class FinanceSyncMessage(
    val eventType: FinanceEventType,
    val budget: BudgetDto?,
    val expense: ExpenseDto?,
    val userId: String,
    val timestamp: String
)

/**
 * 예산 DTO
 */
data class BudgetDto(
    val monthlyBudget: Int,
    val month: String  // "2026-01"
)

/**
 * 지출 DTO
 */
data class ExpenseDto(
    val id: String,
    val title: String,
    val category: String,
    val amount: Int,
    val date: String   // ISO-8601 format (yyyy-MM-dd)
)

// ==================== MBTI 동기화 ====================

/**
 * MBTI 업데이트 메시지
 * 파트너가 MBTI 테스트를 완료하면 전송됨
 */
data class MbtiUpdateMessage(
    val type: String,           // "MBTI_UPDATED"
    val userId: String,
    val userName: String?,
    val mbtiType: String,
    val timestamp: String
)
