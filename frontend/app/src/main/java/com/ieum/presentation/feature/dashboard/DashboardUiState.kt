package com.ieum.presentation.feature.dashboard

import com.ieum.domain.model.CoupleInfo

data class DashboardUiState(
    val coupleInfo: CoupleInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
