package com.ieum.domain.repository

import com.ieum.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessages(): Flow<List<ChatMessage>>
    suspend fun sendMessage(content: String)
    suspend fun shareSchedule(title: String, date: String)
    suspend fun sharePlace(name: String, address: String)
    suspend fun shareBucket(title: String)
}
