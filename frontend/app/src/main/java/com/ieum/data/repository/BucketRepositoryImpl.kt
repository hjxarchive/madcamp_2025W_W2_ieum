package com.ieum.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ieum.domain.model.BucketCategory
import com.ieum.domain.model.BucketItem
import com.ieum.domain.repository.BucketRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

private val Context.bucketDataStore by preferencesDataStore(name = "bucket_prefs")

@Singleton
class BucketRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BucketRepository {

    private val gson = Gson()
    private val BUCKET_ITEMS_KEY = stringPreferencesKey("bucket_items")

    private val _bucketItems = MutableStateFlow<List<BucketItem>>(emptyList())

    init {
        // 앱 시작 시 데이터 로드
        loadData()
    }

    private fun loadData() {
        // Flow를 통해 지속적으로 동기화하거나, 단순 초기 로드 후 memory update 하는 방식
        // 여기서는 memory flow와 DataStore를 연동하여 reactive하게 처리
        context.bucketDataStore.data
            .map { prefs ->
                val json = prefs[BUCKET_ITEMS_KEY]
                if (json != null) {
                    val type = object : TypeToken<List<BucketItem>>() {}.type
                    gson.fromJson<List<BucketItem>>(json, type) ?: emptyList()
                } else {
                    emptyList()
                }
            }
            .onEach { items ->
                _bucketItems.value = items
            }
            .launchIn(kotlinx.coroutines.GlobalScope) // 간단하게 처리 (Hilt Singleton이므로 수명 주기 일치)
    }

    override fun getBucketItems(): Flow<List<BucketItem>> = _bucketItems

    override fun getCompletedCount(): Flow<Int> =
        _bucketItems.map { list -> list.count { it.isCompleted } }

    override suspend fun addBucketItem(title: String, category: BucketCategory) {
        val currentList = _bucketItems.value
        val maxId = currentList.maxOfOrNull { it.id } ?: 0L
        val newItem = BucketItem(
            id = maxId + 1,
            title = title,
            category = category,
            isCompleted = false,
            createdAt = java.time.LocalDate.now().toString()
        )
        saveList(currentList + newItem)
    }

    override suspend fun toggleComplete(itemId: Long) {
        val newList = _bucketItems.value.map { item ->
            if (item.id == itemId) {
                item.copy(
                    isCompleted = !item.isCompleted,
                    completedAt = if (!item.isCompleted) java.time.LocalDate.now().toString() else null
                )
            } else {
                item
            }
        }
        saveList(newList)
    }

    override suspend fun deleteBucketItem(itemId: Long) {
        val newList = _bucketItems.value.filter { it.id != itemId }
        saveList(newList)
    }

    private suspend fun saveList(list: List<BucketItem>) {
        val json = gson.toJson(list)
        context.bucketDataStore.edit { prefs ->
            prefs[BUCKET_ITEMS_KEY] = json
        }
        // _bucketItems는 loadData()의 Flow에 의해 자동 갱신됨
    }
}
