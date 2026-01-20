package com.ieum.data.repository

import android.util.Log
import com.ieum.data.api.ChatService
import com.ieum.data.api.CoupleService
import com.ieum.data.crypto.CryptoManager
import com.ieum.data.crypto.EncryptedData
import com.ieum.data.crypto.KeyStorageManager
import com.ieum.data.websocket.ChatEventListener
import com.ieum.data.websocket.ChatWebSocketClient
import com.ieum.data.websocket.WebSocketE2EEMessageResponse
import com.ieum.data.websocket.WebSocketMessageResponse
import com.ieum.domain.model.ChatMessage
import com.ieum.domain.model.MessageType
import com.ieum.domain.repository.AuthRepository
import com.ieum.domain.repository.ChatConnectionState
import com.ieum.domain.repository.ChatRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val webSocketClient: ChatWebSocketClient,
    private val authRepository: AuthRepository,
    private val coupleService: CoupleService,
    private val chatService: ChatService,
    private val cryptoManager: CryptoManager,
    private val keyStorageManager: KeyStorageManager
) : ChatRepository {

    companion object {
        private const val TAG = "ChatRepositoryImpl"
    }

    private val gson = Gson()
    private var messageIdCounter = 100L
    private var currentUserId: String? = null
    private var currentCoupleId: String? = null

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    override val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _connectionState = MutableStateFlow(ChatConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ChatConnectionState> = _connectionState.asStateFlow()

    private val _isPartnerTyping = MutableStateFlow(false)
    override val isPartnerTyping: StateFlow<Boolean> = _isPartnerTyping.asStateFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val chatEventListener = object : ChatEventListener {
        override fun onConnected() {
            Log.d(TAG, "WebSocket connected")
            _connectionState.value = ChatConnectionState.CONNECTED
        }

        override fun onDisconnected() {
            Log.d(TAG, "WebSocket disconnected")
            _connectionState.value = ChatConnectionState.DISCONNECTED
        }

        override fun onMessageReceived(message: WebSocketMessageResponse) {
            Log.d(TAG, "Message received: ${message.id}")
            val chatMessage = mapToChatMessage(message)
            addMessage(chatMessage)
        }

        override fun onE2EEMessageReceived(message: WebSocketE2EEMessageResponse) {
            Log.d(TAG, "E2EE message received: ${message.id}")
            val decryptedContent = webSocketClient.decryptE2EEMessage(
                message.encryptedContent,
                message.iv
            ) ?: "[암호화된 메시지를 복호화할 수 없습니다]"

            val chatMessage = ChatMessage(
                id = message.id.hashCode().toLong(),
                content = decryptedContent,
                isMe = message.senderId == currentUserId,
                timestamp = parseDateTime(message.createdAt),
                type = MessageType.TEXT
            )
            addMessage(chatMessage)
        }

        override fun onReadReceipt(messageIds: List<String>, readAt: String) {
            Log.d(TAG, "Read receipt received for ${messageIds.size} messages")
            updateReadStatus(messageIds)
        }

        override fun onTypingIndicator(userId: String, isTyping: Boolean) {
            Log.d(TAG, "Typing indicator: $userId - $isTyping")
            if (userId != currentUserId) {
                _isPartnerTyping.value = isTyping
            }
        }

        override fun onError(error: Throwable) {
            Log.e(TAG, "WebSocket error", error)
            _connectionState.value = ChatConnectionState.ERROR
        }

        override fun onMessageSent(tempId: String?) {
            Log.d(TAG, "Message sent: $tempId")
        }
    }

    override fun getMessages(): Flow<List<ChatMessage>> = messages

    override suspend fun connectWebSocket() {
        _connectionState.value = ChatConnectionState.CONNECTING

        try {
            val token = authRepository.getToken()
            if (token == null) {
                Log.e(TAG, "Cannot connect: no auth token")
                _connectionState.value = ChatConnectionState.ERROR
                return
            }

            // Get current user ID
            val userResult = authRepository.getMe()
            userResult.onSuccess { user ->
                currentUserId = user.id
            }

            // Get couple info to get coupleId
            val coupleInfo = coupleService.getCoupleInfo()
            currentCoupleId = coupleInfo.id

            Log.d(TAG, "Connecting WebSocket with coupleId: $currentCoupleId")
            webSocketClient.connect(token, currentCoupleId!!, chatEventListener)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect WebSocket", e)
            _connectionState.value = ChatConnectionState.ERROR
        }
    }

    override suspend fun disconnectWebSocket() {
        webSocketClient.disconnect()
        _connectionState.value = ChatConnectionState.DISCONNECTED
    }

    override suspend fun sendMessage(content: String) {
        if (!webSocketClient.isConnected()) {
            // Fallback: add message locally when offline
            val newMessage = ChatMessage(
                id = ++messageIdCounter,
                content = content,
                isMe = true,
                timestamp = LocalDateTime.now()
            )
            addMessage(newMessage)
            return
        }

        // Send via WebSocket - server will broadcast back to us
        webSocketClient.sendMessage(content)
        // No optimistic update - wait for server broadcast to avoid duplicates
    }

    override suspend fun sendE2EEMessage(content: String) {
        if (!webSocketClient.isConnected()) {
            Log.e(TAG, "Cannot send E2EE message: not connected")
            return
        }

        if (!keyStorageManager.hasSharedKey()) {
            Log.e(TAG, "Cannot send E2EE message: no shared key")
            return
        }

        // Send via WebSocket - server will broadcast back to us
        webSocketClient.sendE2EEMessage(content)
        // No optimistic update - wait for server broadcast to avoid duplicates
    }

    override suspend fun sendReadReceipt(messageIds: List<String>) {
        if (webSocketClient.isConnected()) {
            webSocketClient.sendReadReceipt(messageIds)
        }
    }

    override suspend fun sendTypingIndicator(isTyping: Boolean) {
        if (webSocketClient.isConnected()) {
            webSocketClient.sendTypingIndicator(isTyping)
        }
    }

    override suspend fun shareSchedule(title: String, date: String) {
        if (webSocketClient.isConnected()) {
            // Send via WebSocket - server will broadcast back
            webSocketClient.sendMessage(
                content = """{"type":"SHARED_SCHEDULE","title":"$title","date":"$date"}""",
                type = "SHARED_SCHEDULE"
            )
        } else {
            // Offline fallback
            val newMessage = ChatMessage(
                id = ++messageIdCounter,
                content = "일정 공유",
                isMe = true,
                timestamp = LocalDateTime.now(),
                type = MessageType.SHARED_SCHEDULE,
                sharedData = mapOf("title" to title, "date" to date)
            )
            addMessage(newMessage)
        }
    }

    override suspend fun sharePlace(name: String, address: String) {
        if (webSocketClient.isConnected()) {
            webSocketClient.sendMessage(
                content = """{"type":"SHARED_PLACE","name":"$name","address":"$address"}""",
                type = "SHARED_PLACE"
            )
        } else {
            val newMessage = ChatMessage(
                id = ++messageIdCounter,
                content = "장소 공유",
                isMe = true,
                timestamp = LocalDateTime.now(),
                type = MessageType.SHARED_PLACE,
                sharedData = mapOf("name" to name, "address" to address)
            )
            addMessage(newMessage)
        }
    }

    override suspend fun shareBucket(title: String) {
        if (webSocketClient.isConnected()) {
            webSocketClient.sendMessage(
                content = """{"type":"SHARED_BUCKET","title":"$title"}""",
                type = "SHARED_BUCKET"
            )
        } else {
            val newMessage = ChatMessage(
                id = ++messageIdCounter,
                content = "버킷리스트 공유",
                isMe = true,
                timestamp = LocalDateTime.now(),
                type = MessageType.SHARED_BUCKET,
                sharedData = mapOf("title" to title)
            )
            addMessage(newMessage)
        }
    }

    override suspend fun loadHistoryMessages() {
        val coupleId = currentCoupleId
        if (coupleId == null) {
            Log.w(TAG, "Cannot load history: coupleId is null")
            return
        }

        try {
            Log.d(TAG, "Loading message history for couple: $coupleId")
            val response = chatService.getMessages(coupleId)

            val historyMessages = response.messages.map { dto ->
                val messageType = when (dto.type) {
                    "TEXT" -> MessageType.TEXT
                    "IMAGE" -> MessageType.IMAGE
                    "SHARED_SCHEDULE" -> MessageType.SHARED_SCHEDULE
                    "SHARED_PLACE" -> MessageType.SHARED_PLACE
                    "SHARED_BUCKET" -> MessageType.SHARED_BUCKET
                    else -> MessageType.TEXT
                }

                val sharedData: Map<String, String>? = when (messageType) {
                    MessageType.SHARED_SCHEDULE,
                    MessageType.SHARED_PLACE,
                    MessageType.SHARED_BUCKET -> parseSharedData(dto.content)
                    else -> null
                }

                ChatMessage(
                    id = dto.id.hashCode().toLong(),
                    content = dto.content ?: "",
                    isMe = dto.senderId == currentUserId,
                    timestamp = parseDateTime(dto.createdAt),
                    type = messageType,
                    sharedData = sharedData,
                    isRead = dto.isRead
                )
            }

            // 기존 메시지와 병합 (중복 제거)
            val existingIds = _messages.value.map { it.id }.toSet()
            val newMessages = historyMessages.filter { it.id !in existingIds }
            _messages.value = (newMessages + _messages.value).sortedBy { it.timestamp }

            Log.d(TAG, "Loaded ${historyMessages.size} messages from history")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load message history", e)
        }
    }

    private fun addMessage(message: ChatMessage) {
        // Prevent duplicate messages
        val existingIds = _messages.value.map { it.id }.toSet()
        if (message.id !in existingIds) {
            _messages.value = _messages.value + message
        }
    }

    /**
     * 읽음 상태 업데이트
     */
    private fun updateReadStatus(messageIds: List<String>) {
        val messageIdLongs = messageIds.mapNotNull { it.hashCode().toLong() }.toSet()
        _messages.value = _messages.value.map { message ->
            if (message.id in messageIdLongs && !message.isRead) {
                message.copy(isRead = true)
            } else {
                message
            }
        }
    }

    private fun mapToChatMessage(response: WebSocketMessageResponse): ChatMessage {
        val messageType = when (response.type) {
            "TEXT" -> MessageType.TEXT
            "IMAGE" -> MessageType.IMAGE
            "SHARED_SCHEDULE" -> MessageType.SHARED_SCHEDULE
            "SHARED_PLACE" -> MessageType.SHARED_PLACE
            "SHARED_BUCKET" -> MessageType.SHARED_BUCKET
            else -> MessageType.TEXT
        }

        // 공유 컨텐츠의 경우 content가 JSON 형식이므로 파싱
        val sharedData: Map<String, String>? = when (messageType) {
            MessageType.SHARED_SCHEDULE,
            MessageType.SHARED_PLACE,
            MessageType.SHARED_BUCKET -> {
                parseSharedData(response.content)
            }
            else -> null
        }

        return ChatMessage(
            id = response.id.hashCode().toLong(),
            content = response.content ?: "",
            isMe = response.senderId == currentUserId,
            timestamp = parseDateTime(response.createdAt),
            type = messageType,
            sharedData = sharedData,
            isRead = response.isRead
        )
    }

    /**
     * 공유 컨텐츠 JSON 파싱
     * 예: {"type":"SHARED_SCHEDULE","title":"데이트","date":"2025-01-20"}
     */
    private fun parseSharedData(content: String?): Map<String, String>? {
        if (content.isNullOrBlank()) return null

        return try {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val map: Map<String, Any> = gson.fromJson(content, type)
            // String으로 변환 (type 필드 제외)
            map.filterKeys { it != "type" }
                .mapValues { it.value.toString() }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse shared data: $content", e)
            null
        }
    }

    private fun parseDateTime(dateString: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            try {
                LocalDateTime.parse(dateString.substring(0, 19))
            } catch (e2: Exception) {
                LocalDateTime.now()
            }
        }
    }

    private fun getSampleMessages(): List<ChatMessage> {
        return listOf(
            ChatMessage(
                id = 1L,
                content = "오늘 뭐해? \uD83E\uDD70",
                isMe = false,
                timestamp = LocalDateTime.now().minusHours(2)
            ),
            ChatMessage(
                id = 2L,
                content = "집에서 쉬고 있어~ 너는?",
                isMe = true,
                timestamp = LocalDateTime.now().minusHours(1).minusMinutes(55)
            ),
            ChatMessage(
                id = 3L,
                content = "나도! 저녁에 만날까?",
                isMe = false,
                timestamp = LocalDateTime.now().minusHours(1).minusMinutes(50)
            ),
            ChatMessage(
                id = 4L,
                content = "일정 공유",
                isMe = false,
                timestamp = LocalDateTime.now().minusHours(1).minusMinutes(48),
                type = MessageType.SHARED_SCHEDULE,
                sharedData = mapOf(
                    "title" to "저녁 데이트",
                    "date" to "오늘 18:00",
                    "emoji" to "\uD83C\uDF7D\uFE0F"
                )
            ),
            ChatMessage(
                id = 5L,
                content = "좋아! 거기서 보자 \uD83D\uDC95",
                isMe = true,
                timestamp = LocalDateTime.now().minusHours(1).minusMinutes(45)
            )
        )
    }
}
