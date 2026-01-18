package com.ieum.presentation.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ieum.domain.repository.TestRepository
import com.ieum.domain.usecase.user.GetCoupleInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCoupleInfoUseCase: GetCoupleInfoUseCase,
    private val testRepository: TestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Launch MBTI collection concurrently
            launch {
                testRepository.mbtiResult.collect { mbti ->
                    _uiState.value = _uiState.value.copy(myMbti = mbti)
                }
            }

            getCoupleInfoUseCase()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { coupleInfo ->
                    _uiState.value = _uiState.value.copy(
                        coupleInfo = coupleInfo,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }
}
