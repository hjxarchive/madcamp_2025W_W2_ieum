package com.ieum.domain.usecase.chat

import com.ieum.domain.model.ChatMessage
import com.ieum.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChatMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(): Flow<List<ChatMessage>> = chatRepository.getMessages()
}
