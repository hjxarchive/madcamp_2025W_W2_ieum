package com.ieum.data.repository

import com.ieum.domain.model.Memory
import com.ieum.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryRepositoryImpl @Inject constructor() : MemoryRepository {

    private var idCounter = 100L

    private val memories = MutableStateFlow(
        listOf(
            Memory(
                id = 1L,
                placeName = "성수동 카페",
                address = "서울 성동구 성수이로 88",
                comment = "분위기 너무 좋았던 곳 💕",
                date = "2026-01-10",
                latitude = 37.5445,
                longitude = 127.0558,
                colorHex = "#FF8FAB"
            ),
            Memory(
                id = 2L,
                placeName = "한강공원",
                address = "서울 영등포구 여의동로 330",
                comment = "피크닉 최고!",
                date = "2026-01-05",
                latitude = 37.5283,
                longitude = 126.9328,
                colorHex = "#4ECDC4"
            ),
            Memory(
                id = 3L,
                placeName = "남산타워",
                address = "서울 용산구 남산공원길 105",
                comment = "야경이 예뻤어",
                date = "2025-12-25",
                latitude = 37.5512,
                longitude = 126.9882,
                colorHex = "#9B59B6"
            ),
            Memory(
                id = 4L,
                placeName = "홍대 맛집",
                address = "서울 마포구 어울마당로 42",
                comment = "파스타 맛있었다",
                date = "2025-12-20",
                latitude = 37.5563,
                longitude = 126.9236,
                colorHex = "#FF6B6B"
            ),
            Memory(
                id = 5L,
                placeName = "경복궁",
                address = "서울 종로구 사직로 161",
                comment = "한복 데이트 성공!",
                date = "2025-12-15",
                latitude = 37.5796,
                longitude = 126.9770,
                colorHex = "#45B7D1"
            )
        )
    )

    override fun getMemories(): Flow<List<Memory>> = memories

    override fun getMemoriesByMonth(yearMonth: String): Flow<List<Memory>> =
        memories.map { list ->
            list.filter { it.date.startsWith(yearMonth) }
        }

    override suspend fun addMemory(memory: Memory) {
        val newMemory = memory.copy(id = ++idCounter)
        memories.value = memories.value + newMemory
    }

    override suspend fun updateMemory(memory: Memory) {
        memories.value = memories.value.map {
            if (it.id == memory.id) memory else it
        }
    }

    override suspend fun deleteMemory(memoryId: Long) {
        memories.value = memories.value.filter { it.id != memoryId }
    }
}
