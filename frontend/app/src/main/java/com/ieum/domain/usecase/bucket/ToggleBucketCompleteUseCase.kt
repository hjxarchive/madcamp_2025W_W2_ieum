package com.ieum.domain.usecase.bucket

import com.ieum.domain.repository.BucketRepository
import javax.inject.Inject

class ToggleBucketCompleteUseCase @Inject constructor(
    private val bucketRepository: BucketRepository
) {
    suspend operator fun invoke(itemId: Long) = bucketRepository.toggleComplete(itemId)
}
