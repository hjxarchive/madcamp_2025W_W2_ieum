package com.ieum.domain.repository

import com.ieum.domain.model.Memory
import kotlinx.coroutines.flow.Flow

interface MemoryRepository {
    fun getMemories(): Flow<List<Memory>>
    fun getMemoriesByMonth(yearMonth: String): Flow<List<Memory>>
    suspend fun addMemory(memory: Memory)
    suspend fun updateMemory(memory: Memory)
    suspend fun deleteMemory(memoryId: Long)
    suspend fun refresh()
}
