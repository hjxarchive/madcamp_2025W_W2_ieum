package com.ieum.domain.usecase.bucket

import com.ieum.domain.model.BucketCategory
import com.ieum.domain.repository.BucketRepository
import javax.inject.Inject

class AddBucketItemUseCase @Inject constructor(
    private val bucketRepository: BucketRepository
) {
    suspend operator fun invoke(title: String, category: BucketCategory) =
        bucketRepository.addBucketItem(title, category)
}
