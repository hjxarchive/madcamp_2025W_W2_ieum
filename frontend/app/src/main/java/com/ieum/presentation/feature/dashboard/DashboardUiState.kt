package com.ieum.presentation.feature.dashboard

import androidx.compose.ui.graphics.Color

data class Anniversary(
    val name: String,
    val date: String,
    val daysLeft: Int
)

data class DashboardUiState(
    val daysTogether: Long = 0,
    val partnerNames: String = "우리",
    val spentAmount: Int = 350000,
    val totalBudget: Int = 500000,
    val upcomingEvents: List<Anniversary> = emptyList(),
    val mainTextColor: Color = Color(0xFF5A3E2B),
    val containerColor: Color = Color(0xFFECD4CD)
)
