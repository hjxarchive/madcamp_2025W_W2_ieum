package com.ieum.presentation.feature.bucket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ieum.domain.model.BucketCategory
import com.ieum.domain.repository.BucketRepository
import com.ieum.domain.usecase.bucket.AddBucketItemUseCase
import com.ieum.domain.usecase.bucket.GetBucketItemsUseCase
import com.ieum.domain.usecase.bucket.ToggleBucketCompleteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BucketViewModel @Inject constructor(
    private val getBucketItemsUseCase: GetBucketItemsUseCase,
    private val addBucketItemUseCase: AddBucketItemUseCase,
    private val toggleBucketCompleteUseCase: ToggleBucketCompleteUseCase,
    private val bucketRepository: BucketRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BucketUiState())
    val uiState: StateFlow<BucketUiState> = _uiState.asStateFlow()

    init {
        refreshData()
        loadBucketItems()
    }

    private fun refreshData() {
        viewModelScope.launch {
            bucketRepository.refresh()
        }
    }

    private fun loadBucketItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getBucketItemsUseCase()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { items ->
                    _uiState.value = _uiState.value.copy(
                        bucketItems = items,
                        completedCount = items.count { it.isCompleted },
                        totalCount = items.size,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    fun selectCategory(category: BucketCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun toggleComplete(itemId: Long) {
        viewModelScope.launch {
            toggleBucketCompleteUseCase(itemId)
        }
    }

    fun addItem(title: String, category: BucketCategory) {
        viewModelScope.launch {
            addBucketItemUseCase(title, category)
        }
    }
}
