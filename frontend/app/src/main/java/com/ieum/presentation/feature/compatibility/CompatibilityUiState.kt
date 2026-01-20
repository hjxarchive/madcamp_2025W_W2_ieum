package com.ieum.presentation.feature.compatibility

data class CompatibilityUiState(
    val name1: String = "",
    val name2: String = "",
    val isLoading: Boolean = false,
    val result: CompatibilityResult? = null,
    val error: String? = null
)

data class CompatibilityResult(
    val score: Int,
    val summary: String,
    val details: String
)
