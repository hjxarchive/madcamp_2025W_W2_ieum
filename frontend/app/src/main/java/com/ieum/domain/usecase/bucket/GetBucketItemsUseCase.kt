package com.ieum.domain.usecase.bucket

import com.ieum.domain.model.BucketItem
import com.ieum.domain.repository.BucketRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBucketItemsUseCase @Inject constructor(
    private val bucketRepository: BucketRepository
) {
    operator fun invoke(): Flow<List<BucketItem>> = bucketRepository.getBucketItems()
}
