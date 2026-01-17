package com.ieum.data.repository

import com.ieum.domain.model.BucketCategory
import com.ieum.domain.model.BucketItem
import com.ieum.domain.repository.BucketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BucketRepositoryImpl @Inject constructor() : BucketRepository {

    private var idCounter = 100L

    private val bucketItems = MutableStateFlow(
        listOf(
            BucketItem(
                id = 1L,
                title = "제주도 여행",
                category = BucketCategory.TRAVEL,
                isCompleted = true,
                createdAt = "2025-01-01",
                completedAt = "2025-06-15"
            ),
            BucketItem(
                id = 2L,
                title = "스카이다이빙",
                category = BucketCategory.ACTIVITY,
                isCompleted = false,
                createdAt = "2025-01-05"
            ),
            BucketItem(
                id = 3L,
                title = "미슐랭 레스토랑 가기",
                category = BucketCategory.FOOD,
                isCompleted = false,
                createdAt = "2025-01-10"
            ),
            BucketItem(
                id = 4L,
                title = "뮤지컬 보기",
                category = BucketCategory.CULTURE,
                isCompleted = true,
                createdAt = "2025-02-01",
                completedAt = "2025-03-20"
            ),
            BucketItem(
                id = 5L,
                title = "100일 기념 여행",
                category = BucketCategory.SPECIAL,
                isCompleted = true,
                createdAt = "2025-01-17",
                completedAt = "2025-04-27"
            ),
            BucketItem(
                id = 6L,
                title = "일본 오사카 여행",
                category = BucketCategory.TRAVEL,
                isCompleted = false,
                createdAt = "2025-02-15"
            ),
            BucketItem(
                id = 7L,
                title = "커플 요리 클래스",
                category = BucketCategory.ACTIVITY,
                isCompleted = false,
                createdAt = "2025-03-01"
            )
        )
    )

    override fun getBucketItems(): Flow<List<BucketItem>> = bucketItems

    override fun getCompletedCount(): Flow<Int> =
        bucketItems.map { list -> list.count { it.isCompleted } }

    override suspend fun addBucketItem(title: String, category: BucketCategory) {
        val newItem = BucketItem(
            id = ++idCounter,
            title = title,
            category = category,
            isCompleted = false,
            createdAt = java.time.LocalDate.now().toString()
        )
        bucketItems.value = bucketItems.value + newItem
    }

    override suspend fun toggleComplete(itemId: Long) {
        bucketItems.value = bucketItems.value.map { item ->
            if (item.id == itemId) {
                item.copy(
                    isCompleted = !item.isCompleted,
                    completedAt = if (!item.isCompleted) java.time.LocalDate.now().toString() else null
                )
            } else {
                item
            }
        }
    }

    override suspend fun deleteBucketItem(itemId: Long) {
        bucketItems.value = bucketItems.value.filter { it.id != itemId }
    }
}
