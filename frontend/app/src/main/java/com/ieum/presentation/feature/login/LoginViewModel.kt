package com.ieum.presentation.feature.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ieum.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState

    init {
        checkLoginStatus()
    }

    /**
     * 앱 시작 시 저장된 토큰으로 로그인 상태 확인
     */
    private fun checkLoginStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val token = authRepository.getToken()
                if (token != null) {
                    // 토큰이 있으면 서버에서 사용자 정보 확인
                    val result = authRepository.getMe()
                    result.fold(
                        onSuccess = { user ->
                            Log.d("AutoLogin", "자동 로그인 성공: ${user.email}")
                            Log.d("AutoLogin", "  - coupleId: ${user.coupleId}")
                            Log.d("AutoLogin", "  - mbtiType: ${user.mbtiType}")
                            Log.d("AutoLogin", "  - nickname: ${user.nickname}")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isLoggedIn = true,
                                    isNewUser = user.nickname == null,
                                    hasCoupleId = user.coupleId != null,
                                    hasMbti = user.mbtiType != null,
                                    hasNickname = user.nickname != null
                                )
                            }
                        },
                        onFailure = { error ->
                            Log.e("AutoLogin", "토큰 만료 또는 무효: ${error.message}")
                            // 토큰이 유효하지 않으면 삭제
                            authRepository.clearToken()
                            _uiState.update {
                                it.copy(isLoading = false, isLoggedIn = false)
                            }
                        }
                    )
                } else {
                    Log.d("AutoLogin", "저장된 토큰 없음")
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = false) }
                }
            } catch (e: Exception) {
                Log.e("AutoLogin", "로그인 상태 확인 실패", e)
                _uiState.update { it.copy(isLoading = false, isLoggedIn = false) }
            }
        }
    }

    /**
     * Google ID Token으로 백엔드 서버에 로그인
     */
    fun loginWithGoogleIdToken(idToken: String, onSuccess: () -> Unit) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                Log.d("GoogleLogin", "1. ID Token 받음: ${idToken.take(50)}...")
                Log.d("GoogleLogin", "2. 서버 요청 시작...")

                val result = authRepository.googleLogin(idToken)

                result.fold(
                    onSuccess = { authResponse ->
                        Log.d("GoogleLogin", "3. 서버 응답 성공!")
                        Log.d("GoogleLogin", "   - accessToken: ${authResponse.accessToken.take(30)}...")
                        Log.d("GoogleLogin", "   - user email: ${authResponse.user.email}")
                        Log.d("GoogleLogin", "   - user id: ${authResponse.user.id}")
                        Log.d("GoogleLogin", "   - coupleId: ${authResponse.user.coupleId}")
                        Log.d("GoogleLogin", "   - mbtiType: ${authResponse.user.mbtiType}")
                        Log.d("GoogleLogin", "   - nickname: ${authResponse.user.nickname}")

                        // 토큰 및 사용자 ID 저장
                        authRepository.saveToken(authResponse.accessToken)
                        authRepository.saveUserId(authResponse.user.id)
                        Log.d("GoogleLogin", "4. 토큰 및 사용자 ID 저장 완료")

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isNewUser = authResponse.user.nickname == null,
                                hasCoupleId = authResponse.user.coupleId != null,
                                hasMbti = authResponse.user.mbtiType != null,
                                hasNickname = authResponse.user.nickname != null
                            )
                        }
                        onSuccess()
                    },
                    onFailure = { error ->
                        Log.e("GoogleLogin", "3. 서버 응답 실패!", error)
                        Log.e("GoogleLogin", "   - 에러 타입: ${error.javaClass.simpleName}")
                        Log.e("GoogleLogin", "   - 에러 메시지: ${error.message}")
                        error.cause?.let {
                            Log.e("GoogleLogin", "   - 원인: ${it.message}")
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "서버 로그인 실패: ${error.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("GoogleLogin", "예외 발생!", e)
                Log.e("GoogleLogin", "   - 예외 타입: ${e.javaClass.simpleName}")
                Log.e("GoogleLogin", "   - 예외 메시지: ${e.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "예외 발생: ${e.message}"
                    )
                }
            }
        }
    }

    // 개발용 스킵 로그인
    fun skipLogin(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            // 개발 중에는 토큰 없이 진행
            _uiState.update { it.copy(isLoading = false) }
            onSuccess()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun setError(message: String) {
        _uiState.update { it.copy(errorMessage = message, isLoading = false) }
    }
}
