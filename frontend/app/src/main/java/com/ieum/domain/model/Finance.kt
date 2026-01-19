package com.ieum.domain.model

data class Expense(
    val id: String,
    val title: String,
    val category: ExpenseCategory,
    val amount: Int,
    val date: String
)

enum class ExpenseCategory(val label: String, val colorHex: String) {
    FOOD("식비", "#E1C3B3"),
    CAFE("카페", "#BBA288"),
    DRINK("술", "#ECDDCD"),
    CULTURE("문화생활", "#ECD4CD"),
    TRAVEL("여행", "#C8B7A5"),
    PHOTO("사진", "#E1BBB3"),
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
