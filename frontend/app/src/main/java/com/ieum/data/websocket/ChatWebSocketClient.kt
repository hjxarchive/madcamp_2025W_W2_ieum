package com.ieum.data.websocket

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.ieum.data.crypto.CryptoManager
import com.ieum.data.crypto.EncryptedData
import com.ieum.data.crypto.KeyStorageManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WebSocket 채팅 클라이언트 (STOMP 프로토콜)
 */
@Singleton
class ChatWebSocketClient @Inject constructor(
    private val cryptoManager: CryptoManager,
    private val keyStorageManager: KeyStorageManager
) {
    companion object {
        private const val TAG = "ChatWebSocketClient"
        // 순수 WebSocket 엔드포인트 (SockJS 제거)
        private const val WS_BASE_URL = "ws://54.66.195.91/ws/stomp"
    }

    private var stompClient: StompClient? = null
    private val disposables = CompositeDisposable()
    private val gson = Gson()

    private var coupleId: String? = null
    private var listener: ChatEventListener? = null
    private var isConnected = false

    // 재연결 관련
    private var currentJwtToken: String? = null
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5
    private val baseReconnectDelayMs = 1000L
    private val reconnectHandler = Handler(Looper.getMainLooper())
    private var isReconnecting = false

    /**
     * WebSocket 연결
     */
    fun connect(jwtToken: String, coupleId: String, listener: ChatEventListener) {
        this.coupleId = coupleId
        this.listener = listener
        this.currentJwtToken = jwtToken

        // 디버깅: 연결 정보 로그
        Log.d(TAG, "========== WebSocket Connection Attempt ==========")
        Log.d(TAG, "CoupleId: $coupleId")
        Log.d(TAG, "Token (first 20 chars): ${jwtToken.take(20)}...")
        Log.d(TAG, "Token length: ${jwtToken.length}")

        // 수동 재연결이 아닌 경우에만 시도 횟수 리셋
        if (!isReconnecting) {
            reconnectAttempts = 0
        }
        isReconnecting = false

        // 순수 WebSocket URL (SockJS 제거)
        // 형식: /ws/stomp?token=...
        val wsUrl = "$WS_BASE_URL?token=$jwtToken"
        Log.d(TAG, "Full WebSocket URL: $wsUrl")
        Log.d(TAG, "Attempt: ${reconnectAttempts + 1}/$maxReconnectAttempts")

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS) // No timeout for WebSocket
            .writeTimeout(30, TimeUnit.SECONDS)
            .pingInterval(25, TimeUnit.SECONDS) // Keep-alive
            .build()

        // STOMP over WebSocket 연결
        stompClient = Stomp.over(
            Stomp.ConnectionProvider.OKHTTP,
            wsUrl,
            null,
            okHttpClient
        )

        // 연결 생명주기 관리
        val lifecycleDisposable = stompClient!!.lifecycle()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { lifecycleEvent ->
                when (lifecycleEvent.type) {
                    LifecycleEvent.Type.OPENED -> {
                        Log.d(TAG, "✅ WebSocket connected successfully!")
                        isConnected = true
                        reconnectAttempts = 0 // 연결 성공 시 재연결 횟수 리셋
                        subscribeToTopics()
                        listener.onConnected()
                    }
                    LifecycleEvent.Type.ERROR -> {
                        Log.e(TAG, "❌ WebSocket error occurred")
                        Log.e(TAG, "Error message: ${lifecycleEvent.exception?.message}")
                        Log.e(TAG, "Error type: ${lifecycleEvent.exception?.javaClass?.simpleName}")
                        lifecycleEvent.exception?.printStackTrace()
                        isConnected = false
                        listener.onError(lifecycleEvent.exception ?: Exception("Unknown WebSocket error"))
                        scheduleReconnect()
                    }
                    LifecycleEvent.Type.CLOSED -> {
                        Log.d(TAG, "🔌 WebSocket closed")
                        isConnected = false
                        listener.onDisconnected()
                        scheduleReconnect()
                    }
                    LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> {
                        Log.w(TAG, "💔 Server heartbeat failed")
                        scheduleReconnect()
                    }
                    else -> {}
                }
            }

        disposables.add(lifecycleDisposable)
        stompClient?.connect()
    }

    /**
     * 토픽 구독
     */
    private fun subscribeToTopics() {
        val currentCoupleId = coupleId ?: return

        Log.d(TAG, "========== Starting Topic Subscriptions ==========")
        Log.d(TAG, "Couple ID: $currentCoupleId")

        try {
            // 채팅 메시지 구독
            Log.d(TAG, "Subscribing to messages...")
            subscribeToMessages(currentCoupleId)

            // E2EE 메시지 구독
            Log.d(TAG, "Subscribing to E2EE messages...")
            subscribeToE2EEMessages(currentCoupleId)

            // 읽음 확인 구독
            Log.d(TAG, "Subscribing to read receipts...")
            subscribeToReadReceipts(currentCoupleId)

            // 타이핑 인디케이터 구독
            Log.d(TAG, "Subscribing to typing indicator...")
            subscribeToTypingIndicator(currentCoupleId)

            // 실시간 동기화 구독
            Log.d(TAG, "Subscribing to schedule sync...")
            subscribeToScheduleSync(currentCoupleId)

            Log.d(TAG, "Subscribing to bucket sync...")
            subscribeToBucketSync(currentCoupleId)

            Log.d(TAG, "Subscribing to finance sync...")
            subscribeToFinanceSync(currentCoupleId)

            Log.d(TAG, "✅ All subscriptions initiated")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to subscribe to topics", e)
        }
    }

    /**
     * 일반 메시지 구독
     */
    private fun subscribeToMessages(coupleId: String) {
        val topic = "/topic/couple/$coupleId"
        Log.d(TAG, "Subscribing to topic: $topic")

        val disposable = stompClient?.topic(topic)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ stompMessage ->
                try {
                    val message = gson.fromJson(stompMessage.payload, WebSocketMessageResponse::class.java)
                    Log.d(TAG, "✅ Received message: ${message.id}")
                    listener?.onMessageReceived(message)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to parse message", e)
                }
            }, { error ->
                Log.e(TAG, "❌ Message subscription error for $topic", error)
                listener?.onError(error)
            }, {
                Log.d(TAG, "✅ Successfully subscribed to $topic")
            })

        disposable?.let {
            disposables.add(it)
            Log.d(TAG, "✅ Added message subscription disposable")
        }
    }

    /**
     * E2EE 메시지 구독 (Phase 2에서 구현 예정)
     */
    private fun subscribeToE2EEMessages(coupleId: String) {
        val topic = "/topic/couple/$coupleId/e2ee"
        Log.d(TAG, "Subscribing to topic: $topic")

        val disposable = stompClient?.topic(topic)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ stompMessage ->
                try {
                    val message = gson.fromJson(stompMessage.payload, WebSocketE2EEMessageResponse::class.java)
                    Log.d(TAG, "✅ Received E2EE message: ${message.id}")
                    listener?.onE2EEMessageReceived(message)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to parse E2EE message", e)
                }
            }, { error ->
                Log.e(TAG, "❌ E2EE message subscription error for $topic", error)
                listener?.onError(error)
            }, {
                Log.d(TAG, "✅ Successfully subscribed to $topic")
            })

        disposable?.let {
            disposables.add(it)
            Log.d(TAG, "✅ Added E2EE subscription disposable")
        }
    }

    /**
     * 읽음 확인 구독
     */
    private fun subscribeToReadReceipts(coupleId: String) {
        val topic = "/topic/couple/$coupleId/read"
        Log.d(TAG, "Subscribing to topic: $topic")

        val disposable = stompClient?.topic(topic)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ stompMessage ->
                try {
                    val receipt = gson.fromJson(stompMessage.payload, ReadReceiptMessage::class.java)
                    Log.d(TAG, "✅ Received read receipt: ${receipt.messageIds.size} messages")
                    listener?.onReadReceipt(receipt.messageIds, receipt.readAt)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to parse read receipt", e)
                }
            }, { error ->
                Log.e(TAG, "❌ Read receipt subscription error for $topic", error)
                listener?.onError(error)
            }, {
                Log.d(TAG, "✅ Successfully subscribed to $topic")
            })

        disposable?.let {
            disposables.add(it)
            Log.d(TAG, "✅ Added read receipt subscription disposable")
        }
    }

    /**
     * 타이핑 인디케이터 구독
     */
    private fun subscribeToTypingIndicator(coupleId: String) {
        val topic = "/topic/couple/$coupleId/typing"
        Log.d(TAG, "Subscribing to topic: $topic")

        val disposable = stompClient?.topic(topic)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ stompMessage ->
                try {
                    val indicator = gson.fromJson(stompMessage.payload, TypingIndicator::class.java)
                    Log.d(TAG, "✅ Typing indicator: ${indicator.userId} - ${indicator.isTyping}")
                    listener?.onTypingIndicator(indicator.userId, indicator.isTyping)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to parse typing indicator", e)
                }
            }, { error ->
                Log.e(TAG, "❌ Typing indicator subscription error for $topic", error)
                listener?.onError(error)
            }, {
                Log.d(TAG, "✅ Successfully subscribed to $topic")
            })

        disposable?.let {
            disposables.add(it)
            Log.d(TAG, "✅ Added typing indicator subscription disposable")
        }
    }

    /**
     * 일정 동기화 구독
     */
    private fun subscribeToScheduleSync(coupleId: String) {
        val topic = "/topic/couple/$coupleId/schedule"
        Log.d(TAG, "Subscribing to topic: $topic")

        val disposable = stompClient?.topic(topic)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ stompMessage ->
                try {
                    Log.d(TAG, "📨 Raw schedule message received: ${stompMessage.payload}")
                    val message = gson.fromJson(stompMessage.payload, ScheduleSyncMessage::class.java)
                    Log.d(TAG, "✅ Received schedule sync: ${message.eventType} - ${message.schedule.title}")
                    listener?.onScheduleSync(message)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to parse schedule sync: ${e.message}", e)
                    Log.e(TAG, "❌ Raw payload was: ${stompMessage.payload}")
                }
            }, { error ->
                Log.e(TAG, "❌ Schedule sync subscription error for $topic", error)
                listener?.onError(error)
            }, {
                Log.d(TAG, "✅ Successfully subscribed to $topic")
            })

        disposable?.let {
            disposables.add(it)
            Log.d(TAG, "✅ Added schedule sync subscription disposable")
        }
    }

    /**
     * 버킷리스트 동기화 구독
     */
    private fun subscribeToBucketSync(coupleId: String) {
        val topic = "/topic/couple/$coupleId/bucket"
        Log.d(TAG, "Subscribing to topic: $topic")

        val disposable = stompClient?.topic(topic)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ stompMessage ->
                try {
                    val message = gson.fromJson(stompMessage.payload, BucketSyncMessage::class.java)
                    Log.d(TAG, "✅ Received bucket sync: ${message.eventType} - ${message.bucket.title}")
                    listener?.onBucketSync(message)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to parse bucket sync", e)
                }
            }, { error ->
                Log.e(TAG, "❌ Bucket sync subscription error for $topic", error)
                listener?.onError(error)
            }, {
                Log.d(TAG, "✅ Successfully subscribed to $topic")
            })

        disposable?.let {
            disposables.add(it)
            Log.d(TAG, "✅ Added bucket sync subscription disposable")
        }
    }

    /**
     * 재무 동기화 구독
     */
    private fun subscribeToFinanceSync(coupleId: String) {
        val topic = "/topic/couple/$coupleId/finance"
        Log.d(TAG, "Subscribing to topic: $topic")

        val disposable = stompClient?.topic(topic)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ stompMessage ->
                try {
                    val message = gson.fromJson(stompMessage.payload, FinanceSyncMessage::class.java)
                    Log.d(TAG, "✅ Received finance sync: ${message.eventType}")
                    listener?.onFinanceSync(message)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to parse finance sync", e)
                }
            }, { error ->
                Log.e(TAG, "❌ Finance sync subscription error for $topic", error)
                listener?.onError(error)
            }, {
                Log.d(TAG, "✅ Successfully subscribed to $topic")
            })

        disposable?.let {
            disposables.add(it)
            Log.d(TAG, "✅ Added finance sync subscription disposable")
        }
    }

    /**
     * 일반 메시지 전송
     */
    fun sendMessage(content: String, type: String = "TEXT", imageUrl: String? = null) {
        val currentCoupleId = coupleId ?: run {
            Log.e(TAG, "Cannot send message: coupleId is null")
            return
        }

        val tempId = UUID.randomUUID().toString()
        val request = WebSocketMessageRequest(
            type = type,
            content = content,
            imageUrl = imageUrl,
            tempId = tempId
        )

        val payload = gson.toJson(request)
        Log.d(TAG, "Sending message to /app/chat/$currentCoupleId: $payload")

        val disposable = stompClient?.send("/app/chat/$currentCoupleId", payload)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                Log.d(TAG, "Message sent successfully: $tempId")
                listener?.onMessageSent(tempId)
            }, { error ->
                Log.e(TAG, "Failed to send message", error)
                listener?.onError(error)
            })

        disposable?.let { disposables.add(it) }
    }

    /**
     * E2EE 암호화 메시지 전송
     */
    fun sendE2EEMessage(content: String) {
        val currentCoupleId = coupleId ?: run {
            Log.e(TAG, "Cannot send E2EE message: coupleId is null")
            return
        }

        val sharedKey = keyStorageManager.getSharedKey()
        if (sharedKey == null) {
            Log.e(TAG, "Cannot send E2EE message: shared key not found")
            listener?.onError(IllegalStateException("E2EE 키가 설정되지 않았습니다"))
            return
        }

        try {
            val encrypted = cryptoManager.encryptMessage(content, sharedKey)
            val tempId = UUID.randomUUID().toString()

            val request = WebSocketE2EEMessageRequest(
                encryptedContent = encrypted.cipherText,
                iv = encrypted.iv,
                tempId = tempId
            )

            val payload = gson.toJson(request)
            Log.d(TAG, "Sending E2EE message to /app/chat/$currentCoupleId/e2ee")

            val disposable = stompClient?.send("/app/chat/$currentCoupleId/e2ee", payload)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({
                    Log.d(TAG, "E2EE message sent successfully: $tempId")
                    listener?.onMessageSent(tempId)
                }, { error ->
                    Log.e(TAG, "Failed to send E2EE message", error)
                    listener?.onError(error)
                })

            disposable?.let { disposables.add(it) }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to encrypt message", e)
            listener?.onError(e)
        }
    }

    /**
     * E2EE 메시지 복호화
     */
    fun decryptE2EEMessage(encryptedContent: String, iv: String): String? {
        val sharedKey = keyStorageManager.getSharedKey()
        if (sharedKey == null) {
            Log.e(TAG, "Cannot decrypt message: shared key not found")
            return null
        }

        return try {
            cryptoManager.decryptMessage(EncryptedData(encryptedContent, iv), sharedKey)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decrypt message", e)
            null
        }
    }

    /**
     * 읽음 확인 전송
     * 백엔드는 List<String> (messageIds만) 기대
     */
    fun sendReadReceipt(messageIds: List<String>) {
        val currentCoupleId = coupleId ?: return

        // 백엔드에서 List<String> 형식 기대
        val payload = gson.toJson(messageIds)

        val disposable = stompClient?.send("/app/chat/$currentCoupleId/read", payload)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                Log.d(TAG, "Read receipt sent")
            }, { error ->
                Log.e(TAG, "Failed to send read receipt", error)
            })

        disposable?.let { disposables.add(it) }
    }

    /**
     * 타이핑 인디케이터 전송
     */
    fun sendTypingIndicator(isTyping: Boolean) {
        val currentCoupleId = coupleId ?: return

        val indicator = mapOf("isTyping" to isTyping)
        val payload = gson.toJson(indicator)

        val disposable = stompClient?.send("/app/chat/$currentCoupleId/typing", payload)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                Log.d(TAG, "Typing indicator sent: $isTyping")
            }, { error ->
                Log.e(TAG, "Failed to send typing indicator", error)
            })

        disposable?.let { disposables.add(it) }
    }

    /**
     * 일정 동기화 이벤트 전송 (추가/수정/삭제)
     */
    fun sendScheduleSyncEvent(eventType: String, schedule: ScheduleDto, userId: String? = null) {
        if (!isConnected) {
            Log.e(TAG, "❌ Cannot send schedule sync: WebSocket not connected")
            return
        }

        val currentCoupleId = coupleId ?: run {
            Log.e(TAG, "❌ Cannot send schedule sync: coupleId is null")
            return
        }

        val message = mapOf(
            "eventType" to eventType,
            "schedule" to schedule,
            "userId" to (userId ?: "unknown"),
            "timestamp" to java.time.Instant.now().toString()
        )
        val payload = gson.toJson(message)
        Log.d(TAG, "📤 Sending schedule sync to /app/sync/$currentCoupleId/schedule: $payload")

        val disposable = stompClient?.send("/app/sync/$currentCoupleId/schedule", payload)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                Log.d(TAG, "✅ Schedule sync event sent: $eventType - ${schedule.title}")
            }, { error ->
                Log.e(TAG, "❌ Failed to send schedule sync event", error)
            })

        disposable?.let { disposables.add(it) }
    }

    /**
     * 연결 상태 확인
     */
    fun isConnected(): Boolean = isConnected

    /**
     * 지수 백오프를 적용한 재연결 스케줄링
     */
    private fun scheduleReconnect() {
        val token = currentJwtToken
        val currentCoupleId = coupleId
        val currentListener = listener

        if (token == null || currentCoupleId == null || currentListener == null) {
            Log.d(TAG, "Cannot reconnect: missing required data")
            return
        }

        if (reconnectAttempts >= maxReconnectAttempts) {
            Log.w(TAG, "Max reconnect attempts ($maxReconnectAttempts) reached")
            return
        }

        reconnectAttempts++
        isReconnecting = true

        // 지수 백오프: 1초, 2초, 4초, 8초, 16초...
        val delayMs = baseReconnectDelayMs * (1L shl (reconnectAttempts - 1))
        Log.d(TAG, "Scheduling reconnect in ${delayMs}ms (attempt $reconnectAttempts/$maxReconnectAttempts)")

        reconnectHandler.postDelayed({
            if (isReconnecting && !isConnected) {
                Log.d(TAG, "Attempting reconnect...")
                disposables.clear()
                stompClient?.disconnect()
                stompClient = null
                connect(token, currentCoupleId, currentListener)
            }
        }, delayMs)
    }

    /**
     * 재연결 취소
     */
    fun cancelReconnect() {
        reconnectHandler.removeCallbacksAndMessages(null)
        isReconnecting = false
        reconnectAttempts = 0
    }

    /**
     * 연결 해제
     */
    fun disconnect() {
        Log.d(TAG, "Disconnecting WebSocket")
        cancelReconnect() // 재연결 스케줄 취소
        disposables.clear()
        stompClient?.disconnect()
        stompClient = null
        isConnected = false
        coupleId = null
        listener = null
        currentJwtToken = null
    }

    /**
     * 재연결
     */
    fun reconnect(jwtToken: String) {
        val currentCoupleId = coupleId
        val currentListener = listener

        if (currentCoupleId != null && currentListener != null) {
            disconnect()
            connect(jwtToken, currentCoupleId, currentListener)
        }
    }
}
