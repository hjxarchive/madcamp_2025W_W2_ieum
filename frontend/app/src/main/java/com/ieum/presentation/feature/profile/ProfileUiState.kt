package com.ieum.presentation.feature.profile

import com.ieum.domain.model.CoupleInfo

data class ProfileUiState(
    val coupleInfo: CoupleInfo? = null,
    val mbtiCompatibility: Int = 85,
    val isLoading: Boolean = false,
    val error: String? = null
)
