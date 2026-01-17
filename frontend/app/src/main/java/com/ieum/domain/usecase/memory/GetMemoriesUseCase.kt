package com.ieum.domain.usecase.memory

import com.ieum.domain.model.Memory
import com.ieum.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMemoriesUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    operator fun invoke(): Flow<List<Memory>> = memoryRepository.getMemories()
}
