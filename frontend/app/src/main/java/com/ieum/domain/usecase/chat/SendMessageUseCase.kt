package com.ieum.domain.usecase.chat

import com.ieum.domain.repository.ChatRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(content: String) = chatRepository.sendMessage(content)
}
