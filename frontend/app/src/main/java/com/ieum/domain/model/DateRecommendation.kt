package com.ieum.domain.model

data class DateCourse(
    val id: Long,
    val title: String,
    val description: String,
    val category: DateCategory,
    val duration: String,
    val estimatedCost: Int,
    val places: List<CoursePlace>
)

data class CoursePlace(
    val id: Long,
    val name: String,
    val category: String,
    val address: String,
    val duration: String,
    val estimatedCost: Int
)

enum class DateCategory(val label: String, val emoji: String, val colorHex: String) {
    FOOD("맛집", "🍽️", "#FF6B6B"),
    CAFE("카페", "☕", "#8B4513"),
    DRINK("술", "🍻", "#DAA520"),
    CULTURE("문화생활", "🎭", "#9B59B6"),
    TRAVEL("여행", "✈️", "#4ECDC4"),
    GAME("게임", "🎮", "#2ECC71")
}
