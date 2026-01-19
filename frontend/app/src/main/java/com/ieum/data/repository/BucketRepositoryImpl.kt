package com.ieum.data.repository

import android.util.Log
import com.ieum.data.api.BucketService
import com.ieum.data.dto.BucketRequest
import com.ieum.data.dto.BucketUpdateRequest
import com.ieum.domain.model.BucketCategory
import com.ieum.domain.model.BucketItem
import com.ieum.domain.repository.BucketRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BucketRepositoryImpl @Inject constructor(
    private val bucketService: BucketService
) : BucketRepository {

    private val bucketItems = MutableStateFlow<List<BucketItem>>(emptyList())
    private val bucketIdMap = mutableMapOf<Long, String>() // local id -> server id
    private var localIdCounter = 100L
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // Note: refreshBuckets() is called when user navigates to bucket screen
    // Not in init to avoid calling API before login

    private fun refreshBuckets() {
        coroutineScope.launch {
            try {
                val response = bucketService.getBuckets()
                val items = response.buckets.map { dto ->
                    val localId = dto.id.hashCode().toLong()
                    bucketIdMap[localId] = dto.id

                    BucketItem(
                        id = localId,
                        title = dto.title,
                        category = mapCategoryFromServer(dto.category),
                        isCompleted = dto.isCompleted,
                        createdAt = dto.createdAt.substring(0, 10),
                        completedAt = dto.completedAt?.substring(0, 10)
                    )
                }
                bucketItems.value = items
                Log.d("BucketRepository", "Loaded ${items.size} bucket items from API")
            } catch (e: Exception) {
                Log.e("BucketRepository", "Failed to load buckets", e)
            }
        }
    }

    private fun mapCategoryFromServer(category: String?): BucketCategory {
        return when (category?.lowercase()) {
            "여행", "travel" -> BucketCategory.TRAVEL
            "맛집", "food" -> BucketCategory.FOOD
            "문화", "culture" -> BucketCategory.CULTURE
            "액티비티", "activity" -> BucketCategory.ACTIVITY
            "특별한날", "special" -> BucketCategory.SPECIAL
            else -> BucketCategory.SPECIAL
        }
    }

    private fun mapCategoryToServer(category: BucketCategory): String {
        return category.label
    }

    override fun getBucketItems(): Flow<List<BucketItem>> = bucketItems

    override fun getCompletedCount(): Flow<Int> =
        bucketItems.map { list -> list.count { it.isCompleted } }

    override suspend fun addBucketItem(title: String, category: BucketCategory) {
        try {
            val request = BucketRequest(
                title = title,
                description = null,
                category = mapCategoryToServer(category)
            )
            val response = bucketService.createBucket(request)
            Log.d("BucketRepository", "Created bucket: ${response.id}")

            refreshBuckets()
        } catch (e: Exception) {
            Log.e("BucketRepository", "Failed to add bucket", e)
            // Fallback to local
            val newItem = BucketItem(
                id = ++localIdCounter,
                title = title,
                category = category,
                isCompleted = false,
                createdAt = java.time.LocalDate.now().toString()
            )
            bucketItems.value = bucketItems.value + newItem
        }
    }

    override suspend fun toggleComplete(itemId: Long) {
        try {
            val serverId = bucketIdMap[itemId]
            val currentItem = bucketItems.value.find { it.id == itemId }

            if (serverId != null && currentItem != null) {
                val request = BucketUpdateRequest(
                    isCompleted = !currentItem.isCompleted,
                    completedImage = null
                )
                bucketService.updateBucket(serverId, request)
                Log.d("BucketRepository", "Toggled bucket: $serverId")

                refreshBuckets()
            } else {
                // Local toggle
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
        } catch (e: Exception) {
            Log.e("BucketRepository", "Failed to toggle bucket", e)
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
    }

    override suspend fun deleteBucketItem(itemId: Long) {
        try {
            val serverId = bucketIdMap[itemId]
            if (serverId != null) {
                bucketService.deleteBucket(serverId)
                Log.d("BucketRepository", "Deleted bucket: $serverId")
                bucketIdMap.remove(itemId)

                refreshBuckets()
            } else {
                bucketItems.value = bucketItems.value.filter { it.id != itemId }
            }
        } catch (e: Exception) {
            Log.e("BucketRepository", "Failed to delete bucket", e)
            bucketItems.value = bucketItems.value.filter { it.id != itemId }
        }
    }
}
