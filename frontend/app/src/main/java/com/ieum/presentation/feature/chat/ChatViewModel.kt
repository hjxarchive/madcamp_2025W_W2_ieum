package com.ieum.presentation.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ieum.domain.usecase.chat.GetChatMessagesUseCase
import com.ieum.domain.usecase.chat.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val chatRepository: com.ieum.domain.repository.ChatRepository,
    private val getSchedulesForMonthUseCase: com.ieum.domain.usecase.schedule.GetSchedulesForMonthUseCase,
    private val bucketRepository: com.ieum.domain.repository.BucketRepository,
    private val financeRepository: com.ieum.domain.repository.FinanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadMessages()
        loadSharingSchedules() // 초기 로딩
    }

    private fun loadMessages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
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

    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun sendMessage(content: String = _uiState.value.inputText) {
        val text = content.trim()
        if (text.isNotEmpty()) {
            viewModelScope.launch {
                sendMessageUseCase(text)
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
}
