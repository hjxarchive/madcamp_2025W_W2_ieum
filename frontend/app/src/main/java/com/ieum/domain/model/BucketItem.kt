package com.ieum.domain.model

data class BucketItem(
    val id: Long,
    val title: String,
    val category: BucketCategory,
    val isCompleted: Boolean,
    val createdAt: String,
    val completedAt: String? = null
)

enum class BucketCategory(val label: String, val colorHex: String) {
    TRAVEL("여행", "#4ECDC4"),
    FOOD("맛집", "#FF6B6B"),
    CULTURE("문화", "#9B59B6"),
    ACTIVITY("액티비티", "#45B7D1"),
    SPECIAL("특별한날", "#FF8FAB")
}
