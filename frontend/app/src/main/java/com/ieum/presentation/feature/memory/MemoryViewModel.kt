package com.ieum.presentation.feature.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ieum.domain.model.Memory
import com.ieum.domain.repository.MemoryRepository
import com.ieum.domain.usecase.memory.GetMemoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val getMemoriesUseCase: GetMemoriesUseCase,
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()

    init {
        refreshData()
        loadMemories()
    }

    private fun refreshData() {
        viewModelScope.launch {
            memoryRepository.refresh()
        }
    }

    private fun loadMemories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getMemoriesUseCase()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { memories ->
                    _uiState.value = _uiState.value.copy(
                        memories = memories,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    fun selectMemory(memory: Memory?) {
        _uiState.value = _uiState.value.copy(selectedMemory = memory)
    }
}
