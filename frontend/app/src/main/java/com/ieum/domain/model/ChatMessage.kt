package com.ieum.domain.model

import java.time.LocalDateTime

data class ChatMessage(
    val id: Long,
    val content: String,
    val isMe: Boolean,
    val timestamp: LocalDateTime,
    val type: MessageType = MessageType.TEXT,
    val sharedData: Map<String, String>? = null,
    val isRead: Boolean = false
)

enum class MessageType {
    TEXT,
    SHARED_SCHEDULE,
    SHARED_PLACE,
    SHARED_BUCKET,
    IMAGE
}
