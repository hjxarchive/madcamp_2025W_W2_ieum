package com.ieum.presentation.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ieum.domain.repository.ChatConnectionState
import com.ieum.domain.repository.ChatRepository
import com.ieum.domain.usecase.chat.GetChatMessagesUseCase
import com.ieum.domain.usecase.chat.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getChatMessagesUseCase: GetChatMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val chatRepository: ChatRepository,
    private val getSchedulesForMonthUseCase: com.ieum.domain.usecase.schedule.GetSchedulesForMonthUseCase,
    private val bucketRepository: com.ieum.domain.repository.BucketRepository,
    private val financeRepository: com.ieum.domain.repository.FinanceRepository,
    private val userRepository: com.ieum.domain.repository.UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var typingJob: Job? = null

    init {
        connectWebSocket()
        loadMessages()
        loadSharingSchedules()
        loadPartnerName()
        observeConnectionState()
        observeTypingIndicator()
    }

    /**
     * WebSocket 연결
     */
    private fun connectWebSocket() {
        viewModelScope.launch {
            chatRepository.connectWebSocket()
        }
    }

    /**
     * WebSocket 연결 상태 관찰
     */
    private fun observeConnectionState() {
        viewModelScope.launch {
            chatRepository.connectionState.collect { state ->
                _uiState.value = _uiState.value.copy(
                    connectionState = state,
                    isConnected = state == ChatConnectionState.CONNECTED
                )
            }
        }
    }

    /**
     * 타이핑 인디케이터 관찰
     */
    private fun observeTypingIndicator() {
        viewModelScope.launch {
            chatRepository.isPartnerTyping.collect { isTyping ->
                _uiState.value = _uiState.value.copy(isPartnerTyping = isTyping)
            }
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Load history messages first
            chatRepository.loadHistoryMessages()

            getChatMessagesUseCase()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { messages ->
                    _uiState.value = _uiState.value.copy(
                        messages = messages,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    private fun loadPartnerName() {
        viewModelScope.launch {
            userRepository.getCoupleInfo().collect { coupleInfo ->
                _uiState.value = _uiState.value.copy(
                    partnerName = coupleInfo.partner.nickname
                )
            }
        }
    }

    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)

        // Send typing indicator
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            chatRepository.sendTypingIndicator(true)
            delay(2000) // Stop typing after 2 seconds of inactivity
            chatRepository.sendTypingIndicator(false)
        }
    }

    fun sendMessage(content: String = _uiState.value.inputText) {
        val text = content.trim()
        if (text.isNotEmpty()) {
            viewModelScope.launch {
                // Cancel typing indicator
                typingJob?.cancel()
                chatRepository.sendTypingIndicator(false)

                // Send message
                chatRepository.sendMessage(text)
                _uiState.value = _uiState.value.copy(inputText = "")
            }
        }
    }

    /**
     * E2EE 암호화 메시지 전송
     */
    fun sendE2EEMessage(content: String = _uiState.value.inputText) {
        val text = content.trim()
        if (text.isNotEmpty()) {
            viewModelScope.launch {
                typingJob?.cancel()
                chatRepository.sendTypingIndicator(false)

                chatRepository.sendE2EEMessage(text)
                _uiState.value = _uiState.value.copy(inputText = "")
            }
        }
    }

    // --- 일정 공유 관련 ---

    fun loadSharingSchedules() {
        viewModelScope.launch {
            getSchedulesForMonthUseCase(_uiState.value.sharingYearMonth)
                .catch { }
                .collect { schedules ->
                    _uiState.value = _uiState.value.copy(sharingSchedules = schedules)
                }
        }
    }

    fun navigateSharingMonth(offset: Int) {
        val newMonth = _uiState.value.sharingYearMonth.plusMonths(offset.toLong())
        _uiState.value = _uiState.value.copy(sharingYearMonth = newMonth)
        loadSharingSchedules()
    }

    fun shareSchedule(schedule: com.ieum.domain.model.Schedule) {
        viewModelScope.launch {
            chatRepository.shareSchedule(
                title = schedule.title,
                date = schedule.date.toString()
            )
        }
    }

    fun addBucketList(title: String) {
        viewModelScope.launch {
            bucketRepository.addBucketItem(title, com.ieum.domain.model.BucketCategory.SPECIAL)
        }
    }

    fun updateBudget(amount: Int) {
        viewModelScope.launch {
            financeRepository.setBudget(amount)
        }
    }

    /**
     * 메시지 읽음 처리
     */
    fun markMessagesAsRead(messageIds: List<String>) {
        viewModelScope.launch {
            chatRepository.sendReadReceipt(messageIds)
        }
    }

    /**
     * WebSocket 재연결
     */
    fun reconnect() {
        viewModelScope.launch {
            chatRepository.disconnectWebSocket()
            delay(1000)
            chatRepository.connectWebSocket()
        }
    }

    override fun onCleared() {
        super.onCleared()
        // WebSocket 연결은 DashboardViewModel에서 관리하므로 여기서 끊지 않음
        // 타이핑 인디케이터만 정리
        typingJob?.cancel()
    }
}
