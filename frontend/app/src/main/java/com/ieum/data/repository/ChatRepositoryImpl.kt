package com.ieum.data.repository

import com.ieum.domain.model.ChatMessage
import com.ieum.domain.model.MessageType
import com.ieum.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor() : ChatRepository {

    private var messageIdCounter = 100L

    private val messages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = 1L,
                content = "오늘 뭐해? 🥰",
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
                    "emoji" to "🍽️"
                )
            ),
            ChatMessage(
                id = 5L,
                content = "좋아! 거기서 보자 💕",
                isMe = true,
                timestamp = LocalDateTime.now().minusHours(1).minusMinutes(45)
            ),
            ChatMessage(
                id = 6L,
                content = "이 장소 어때?",
                isMe = false,
                timestamp = LocalDateTime.now().minusMinutes(30),
                type = MessageType.SHARED_PLACE,
                sharedData = mapOf(
                    "name" to "성수동 파스타",
                    "address" to "서울 성동구 성수이로 88",
                    "category" to "이탈리안"
                )
            )
        )
    )

    override fun getMessages(): Flow<List<ChatMessage>> = messages

    override suspend fun sendMessage(content: String) {
        val newMessage = ChatMessage(
            id = ++messageIdCounter,
            content = content,
            isMe = true,
            timestamp = LocalDateTime.now()
        )
        messages.value = messages.value + newMessage
    }

    override suspend fun shareSchedule(title: String, date: String) {
        val newMessage = ChatMessage(
            id = ++messageIdCounter,
            content = "일정 공유",
            isMe = true,
            timestamp = LocalDateTime.now(),
            type = MessageType.SHARED_SCHEDULE,
            sharedData = mapOf("title" to title, "date" to date)
        )
        messages.value = messages.value + newMessage
    }

    override suspend fun sharePlace(name: String, address: String) {
        val newMessage = ChatMessage(
            id = ++messageIdCounter,
            content = "장소 공유",
            isMe = true,
            timestamp = LocalDateTime.now(),
            type = MessageType.SHARED_PLACE,
            sharedData = mapOf("name" to name, "address" to address)
        )
        messages.value = messages.value + newMessage
    }

    override suspend fun shareBucket(title: String) {
        val newMessage = ChatMessage(
            id = ++messageIdCounter,
            content = "버킷리스트 공유",
            isMe = true,
            timestamp = LocalDateTime.now(),
            type = MessageType.SHARED_BUCKET,
            sharedData = mapOf("title" to title)
        )
        messages.value = messages.value + newMessage
    }
}
