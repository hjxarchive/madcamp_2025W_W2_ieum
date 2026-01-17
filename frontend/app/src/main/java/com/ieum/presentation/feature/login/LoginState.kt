package com.ieum.presentation.feature.login


data class LoginState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
