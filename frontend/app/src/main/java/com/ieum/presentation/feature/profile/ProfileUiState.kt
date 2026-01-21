package com.ieum.presentation.feature.profile

import com.ieum.domain.model.CoupleInfo

data class ProfileUiState(
    val coupleInfo: CoupleInfo? = null,
    val myMbti: String? = null,
    val partnerMbti: String? = null,
    val mbtiCompatibility: Int = 85,
    val isLoading: Boolean = false,
    val error: String? = null,
    // Gemini 추천/주의 카드 데이터
    val recommendations: List<ChemistryCard> = emptyList(),
    val cautions: List<ChemistryCard> = emptyList(),
    val isLoadingChemistry: Boolean = false,
    val chemistryError: String? = null
)

data class ChemistryCard(
    val category: String,  // "데이트", "소비", "갈등", "도전"
    val title: String,
    val description: String,
    val emoji: String
)
