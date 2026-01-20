package com.ieum.domain.repository

import com.ieum.domain.model.BucketCategory
import com.ieum.domain.model.BucketItem
import kotlinx.coroutines.flow.Flow

interface BucketRepository {
    fun getBucketItems(): Flow<List<BucketItem>>
    fun getCompletedCount(): Flow<Int>
    suspend fun addBucketItem(title: String, category: BucketCategory)
    suspend fun toggleComplete(itemId: Long)
    suspend fun deleteBucketItem(itemId: Long)
    suspend fun refresh()
}
