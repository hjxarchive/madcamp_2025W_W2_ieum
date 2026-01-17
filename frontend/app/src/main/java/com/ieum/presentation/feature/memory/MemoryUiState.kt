package com.ieum.presentation.feature.memory

import com.ieum.domain.model.Memory

data class MemoryUiState(
    val memories: List<Memory> = emptyList(),
    val selectedMemory: Memory? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
