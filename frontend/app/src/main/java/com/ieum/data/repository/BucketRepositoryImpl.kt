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

    // Note: refresh() is called when user navigates to bucket screen
    // Not in init to avoid calling API before login

    private suspend fun refreshBuckets() {
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
        // 낙관적 업데이트: 즉시 UI에 표시
        val tempId = ++localIdCounter
        val tempItem = BucketItem(
            id = tempId,
            title = title,
            category = category,
            isCompleted = false,
            createdAt = java.time.LocalDate.now().toString()
        )
        bucketItems.value = bucketItems.value + tempItem
        Log.d("BucketRepository", "Added bucket optimistically: $title")

        try {
            val request = BucketRequest(
                title = title,
                description = null,
                category = mapCategoryToServer(category)
            )
            val response = bucketService.createBucket(request)
            Log.d("BucketRepository", "Created bucket on server: ${response.id}")

            // 서버 ID로 매핑 저장
            val serverId = response.id
            val serverHashId = serverId.hashCode().toLong()
            bucketIdMap[serverHashId] = serverId

            // 임시 ID를 서버 ID로 업데이트
            bucketItems.value = bucketItems.value.map {
                if (it.id == tempId) it.copy(id = serverHashId) else it
            }
        } catch (e: Exception) {
            Log.e("BucketRepository", "Failed to add bucket to server", e)
            // 에러 발생 시 낙관적 업데이트는 유지
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

    override suspend fun refresh() {
        refreshBuckets()
    }

    /**
     * WebSocket을 통한 버킷 동기화 이벤트 처리
     * 백엔드에서 id는 UUID(String)로 전송되므로 hashCode()로 Long 변환
     */
    override fun handleBucketSync(message: com.ieum.data.websocket.BucketSyncMessage) {
        Log.d("BucketRepository", "📨 Handling bucket sync: ${message.eventType} - ${message.bucket.title}")
        Log.d("BucketRepository", "Bucket ID (UUID): ${message.bucket.id}")

        // UUID String을 Long으로 변환 (기존 ID 체계와 호환)
        val bucketId = message.bucket.id.hashCode().toLong()

        when (message.eventType) {
            com.ieum.data.websocket.BucketEventType.ADDED -> {
                val newItem = BucketItem(
                    id = bucketId,
                    title = message.bucket.title,
                    category = mapCategoryFromServer(message.bucket.category),
                    isCompleted = message.bucket.isCompleted,
                    createdAt = message.bucket.createdAt.substringBefore("T"),
                    completedAt = message.bucket.completedAt?.substringBefore("T")
                )

                // 서버 ID 매핑 저장
                bucketIdMap[bucketId] = message.bucket.id

                val existingIds = bucketItems.value.map { it.id }.toSet()
                if (newItem.id !in existingIds) {
                    bucketItems.value = bucketItems.value + newItem
                    Log.d("BucketRepository", "✅ Added bucket via WebSocket: ${newItem.title}")
                } else {
                    Log.d("BucketRepository", "⚠️ Bucket already exists (duplicate): ${newItem.title}")
                }
            }

            com.ieum.data.websocket.BucketEventType.COMPLETED -> {
                bucketItems.value = bucketItems.value.map {
                    if (it.id == bucketId) {
                        it.copy(
                            isCompleted = message.bucket.isCompleted,
                            completedAt = message.bucket.completedAt?.substringBefore("T")
                        )
                    } else it
                }
                Log.d("BucketRepository", "✅ Completed bucket via WebSocket: ${message.bucket.title}")
            }

            com.ieum.data.websocket.BucketEventType.UPDATED -> {
                // 버킷 일반 수정 (제목, 카테고리 등)
                bucketItems.value = bucketItems.value.map {
                    if (it.id == bucketId) {
                        it.copy(
                            title = message.bucket.title,
                            category = mapCategoryFromServer(message.bucket.category),
                            isCompleted = message.bucket.isCompleted,
                            completedAt = message.bucket.completedAt?.substringBefore("T")
                        )
                    } else it
                }
                Log.d("BucketRepository", "✅ Updated bucket via WebSocket: ${message.bucket.title}")
            }

            com.ieum.data.websocket.BucketEventType.DELETED -> {
                bucketItems.value = bucketItems.value.filter { it.id != bucketId }
                bucketIdMap.remove(bucketId)
                Log.d("BucketRepository", "✅ Deleted bucket via WebSocket: ${message.bucket.title}")
            }
        }
    }
}
