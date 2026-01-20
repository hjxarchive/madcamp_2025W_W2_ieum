package com.ieum.presentation.feature.compatibility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.ieum.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompatibilityViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CompatibilityUiState())
    val uiState: StateFlow<CompatibilityUiState> = _uiState.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    fun updateName1(name: String) {
        _uiState.update { it.copy(name1 = name) }
    }

    fun updateName2(name: String) {
        _uiState.update { it.copy(name2 = name) }
    }

    fun analyzeCompatibility() {
        val name1 = _uiState.value.name1.trim()
        val name2 = _uiState.value.name2.trim()

        if (name1.isEmpty() || name2.isEmpty()) {
            _uiState.update { it.copy(error = "두 이름을 모두 입력해주세요.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, result = null) }

            try {
                val prompt = """
                    당신은 재미있는 이름 궁합 분석가입니다.
                    다음 두 사람의 이름으로 궁합을 분석해주세요.

                    첫 번째 사람: $name1
                    두 번째 사람: $name2

                    다음 형식으로 응답해주세요 (반드시 이 형식을 지켜주세요):

                    점수: [1-100 사이의 숫자만]
                    요약: [한 줄로 궁합 요약]
                    상세분석: [2-3문장으로 재미있게 궁합 분석. 이름의 글자, 획수, 느낌 등을 고려하여 재미있게 분석해주세요.]

                    긍정적이고 재미있는 톤으로 작성해주세요.
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val responseText = response.text ?: throw Exception("응답을 받지 못했습니다.")

                val result = parseResponse(responseText)
                _uiState.update { it.copy(isLoading = false, result = result) }

            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("API key", ignoreCase = true) == true ->
                        "API 키를 확인해주세요."
                    e.message?.contains("not found", ignoreCase = true) == true ->
                        "모델을 찾을 수 없습니다. 앱을 업데이트해주세요."
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "네트워크 연결을 확인해주세요."
                    else -> e.message ?: "궁합 분석 중 오류가 발생했습니다."
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            }
        }
    }

    private fun parseResponse(response: String): CompatibilityResult {
        val lines = response.lines()
        var score = 75
        var summary = "좋은 궁합이에요!"
        var details = response

        for (line in lines) {
            when {
                line.startsWith("점수:") -> {
                    val scoreText = line.removePrefix("점수:").trim()
                    score = scoreText.filter { it.isDigit() }.toIntOrNull() ?: 75
                    score = score.coerceIn(1, 100)
                }
                line.startsWith("요약:") -> {
                    summary = line.removePrefix("요약:").trim()
                }
                line.startsWith("상세분석:") -> {
                    details = line.removePrefix("상세분석:").trim()
                }
            }
        }

        // 상세분석이 여러 줄에 걸쳐있을 수 있으므로 처리
        val detailsIndex = response.indexOf("상세분석:")
        if (detailsIndex != -1) {
            details = response.substring(detailsIndex + "상세분석:".length).trim()
        }

        return CompatibilityResult(
            score = score,
            summary = summary,
            details = details
        )
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetResult() {
        _uiState.update { it.copy(result = null, name1 = "", name2 = "") }
    }
}
