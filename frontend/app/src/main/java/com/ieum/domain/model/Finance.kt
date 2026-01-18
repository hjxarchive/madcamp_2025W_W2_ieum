package com.ieum.domain.model

data class Expense(
    val id: String,
    val title: String,
    val category: ExpenseCategory,
    val amount: Int,
    val date: String
)

enum class ExpenseCategory(val label: String, val colorHex: String) {
    FOOD("식비", "#FF6B6B"),
    CAFE("카페", "#8B4513"),
    DRINK("술", "#DAA520"),
    CULTURE("문화생활", "#9B59B6"),
    TRAVEL("여행", "#4ECDC4"),
    OTHER("기타", "#9E9E9E")
}

data class Budget(
    val monthlyBudget: Int,
    val totalSpent: Int,
    val remainingBudget: Int
) {
    val spentPercentage: Float
        get() = if (monthlyBudget > 0) (totalSpent.toFloat() / monthlyBudget * 100).coerceIn(0f, 100f) else 0f
}

data class MonthlySpending(
    val month: String,
    val amount: Int
)
