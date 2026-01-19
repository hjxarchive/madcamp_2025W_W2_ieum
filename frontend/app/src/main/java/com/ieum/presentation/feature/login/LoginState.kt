package com.ieum.presentation.feature.login


data class LoginState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isNewUser: Boolean = true,
    val isLoggedIn: Boolean = false
)
