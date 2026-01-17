package com.ieum.presentation.feature.bucket

import com.ieum.domain.model.BucketCategory
import com.ieum.domain.model.BucketItem

data class BucketUiState(
    val bucketItems: List<BucketItem> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val selectedCategory: BucketCategory? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val filteredItems: List<BucketItem>
        get() = if (selectedCategory == null) {
            bucketItems
        } else {
            bucketItems.filter { it.category == selectedCategory }
        }
}
