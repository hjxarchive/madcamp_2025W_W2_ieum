package com.ieum.data.repository

import android.util.Log
import com.ieum.data.api.MemoryService
import com.ieum.data.dto.MemoryRequest
import com.ieum.domain.model.Memory
import com.ieum.domain.repository.MemoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryRepositoryImpl @Inject constructor(
    private val memoryService: MemoryService
) : MemoryRepository {

    private val memories = MutableStateFlow<List<Memory>>(emptyList())
    private val memoryIdMap = mutableMapOf<Long, String>() // local id -> server id
    private var localIdCounter = 100L
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // Note: refreshMemories() is called when user navigates to memory screen
    // Not in init to avoid calling API before login

    private fun refreshMemories() {
        coroutineScope.launch {
            try {
                val response = memoryService.getMemories(page = 0, size = 100)
                val memoryList = response.memories.map { dto ->
                    val localId = dto.id.hashCode().toLong()
                    memoryIdMap[localId] = dto.id

                    Memory(
                        id = localId,
                        placeName = dto.title,
                        address = dto.location ?: "",
                        comment = dto.content ?: "",
                        date = dto.date,
                        latitude = dto.latitude ?: 37.5665,
                        longitude = dto.longitude ?: 126.9780,
                        colorHex = getColorForIndex(localId.toInt()),
                        imageUrl = dto.images?.firstOrNull()
                    )
                }
                memories.value = memoryList
                Log.d("MemoryRepository", "Loaded ${memoryList.size} memories from API")
            } catch (e: Exception) {
                Log.e("MemoryRepository", "Failed to load memories", e)
            }
        }
    }

    private fun getColorForIndex(index: Int): String {
        val colors = listOf("#FF8FAB", "#4ECDC4", "#9B59B6", "#FF6B6B", "#45B7D1")
        return colors[kotlin.math.abs(index) % colors.size]
    }

    override fun getMemories(): Flow<List<Memory>> = memories

    override fun getMemoriesByMonth(yearMonth: String): Flow<List<Memory>> =
        memories.map { list ->
            list.filter { it.date.startsWith(yearMonth) }
        }

    override suspend fun addMemory(memory: Memory) {
        try {
            val request = MemoryRequest(
                title = memory.placeName,
                content = memory.comment,
                date = memory.date,
                location = memory.address,
                latitude = memory.latitude,
                longitude = memory.longitude,
                images = memory.imageUrl?.let { listOf(it) }
            )
            val response = memoryService.createMemory(request)
            Log.d("MemoryRepository", "Created memory: ${response.id}")

            // Refresh memories
            refreshMemories()
        } catch (e: Exception) {
            Log.e("MemoryRepository", "Failed to add memory", e)
            // Fallback to local
            val newMemory = memory.copy(id = ++localIdCounter)
            memories.value = memories.value + newMemory
        }
    }

    override suspend fun updateMemory(memory: Memory) {
        try {
            val serverId = memoryIdMap[memory.id]
            if (serverId != null) {
                val request = MemoryRequest(
                    title = memory.placeName,
                    content = memory.comment,
                    date = memory.date,
                    location = memory.address,
                    latitude = memory.latitude,
                    longitude = memory.longitude,
                    images = memory.imageUrl?.let { listOf(it) }
                )
                memoryService.updateMemory(serverId, request)
                Log.d("MemoryRepository", "Updated memory: $serverId")

                refreshMemories()
            } else {
                // Local update
                memories.value = memories.value.map {
                    if (it.id == memory.id) memory else it
                }
            }
        } catch (e: Exception) {
            Log.e("MemoryRepository", "Failed to update memory", e)
            memories.value = memories.value.map {
                if (it.id == memory.id) memory else it
            }
        }
    }

    override suspend fun deleteMemory(memoryId: Long) {
        try {
            val serverId = memoryIdMap[memoryId]
            if (serverId != null) {
                memoryService.deleteMemory(serverId)
                Log.d("MemoryRepository", "Deleted memory: $serverId")
                memoryIdMap.remove(memoryId)

                refreshMemories()
            } else {
                memories.value = memories.value.filter { it.id != memoryId }
            }
        } catch (e: Exception) {
            Log.e("MemoryRepository", "Failed to delete memory", e)
            memories.value = memories.value.filter { it.id != memoryId }
        }
    }
}
