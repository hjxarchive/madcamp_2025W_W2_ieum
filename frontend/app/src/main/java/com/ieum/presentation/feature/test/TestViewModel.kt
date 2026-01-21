package com.ieum.presentation.feature.test

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ieum.domain.repository.TestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestViewModel @Inject constructor(
    private val testRepository: TestRepository
) : ViewModel() {
    private val _currentScreen = MutableStateFlow<TestScreenState>(TestScreenState.Intro)
    val currentScreen = _currentScreen.asStateFlow()

    private val _answers = MutableStateFlow<List<Answer>>(emptyList())
    val currentQuestionIndex = MutableStateFlow(0)

    private val _shuffledQuestions = MutableStateFlow<List<Question>>(emptyList())
    val shuffledQuestions = _shuffledQuestions.asStateFlow()

    fun startTest() {
        _answers.value = emptyList()
        currentQuestionIndex.value = 0
        _shuffledQuestions.value = allQuestions.shuffled()
        _currentScreen.value = TestScreenState.Testing
    }

    fun submitAnswer(type: Char) {
        val currentQuestion = _shuffledQuestions.value.getOrNull(currentQuestionIndex.value)
        if (currentQuestion == null) {
            Log.e("TestViewModel", "현재 질문을 찾을 수 없습니다")
            return
        }

        // 원래 질문의 ID 찾기 (allQuestions에서의 인덱스 + 1)
        val originalQuestionId = allQuestions.indexOf(currentQuestion) + 1

        // 선택 결정: 왼쪽(X) 선택 = "A", 오른쪽(O) 선택 = "B"
        val selection = if (type == currentQuestion.leftType) "A" else "B"

        Log.d("TestViewModel", "Q${currentQuestionIndex.value + 1}: 선택된 타입 = $type, 원래 질문 ID = $originalQuestionId, selection = $selection")

        // 현재 질문 인덱스에 대한 기존 답변이 있는지 확인
        val existingAnswerIndex = _answers.value.indexOfFirst { it.questionIndex == currentQuestionIndex.value }

        val newAnswer = Answer(
            questionIndex = currentQuestionIndex.value,
            answerType = type,
            originalQuestionId = originalQuestionId,
            selection = selection
        )

        val newList = if (existingAnswerIndex != -1) {
            // 기존 답변 수정
            _answers.value.toMutableList().apply {
                set(existingAnswerIndex, newAnswer)
            }
        } else {
            // 새 답변 추가
            _answers.value + newAnswer
        }
        _answers.value = newList

        if (currentQuestionIndex.value < 35) { // 총 36개 질문
            currentQuestionIndex.value += 1
        } else {
            calculateFinalResult(newList)
        }
    }
    
    fun goToPreviousQuestion() {
        if (currentQuestionIndex.value > 0) {
            currentQuestionIndex.value -= 1
        }
    }

    // TestViewModel.kt 내부
    fun onCompleteNavigation(onNavigate: () -> Unit) {
        onNavigate()
    }

    private fun calculateFinalResult(finalAnswers: List<Answer>) {
        // 답변 개수 검증
        if (finalAnswers.size != 36) {
            Log.e("TestViewModel", "답변이 36개가 아닙니다: ${finalAnswers.size}")
            return
        }

        // 로컬 카운트 (UI용)
        val counts = finalAnswers.groupBy { it.answerType }.mapValues { it.value.size }
        Log.d("TestViewModel", "최종 카운트: $counts")

        val localMbti = StringBuilder().apply {
            append(if ((counts['M'] ?: 0) >= (counts['I'] ?: 0)) "M" else "I")
            append(if ((counts['D'] ?: 0) >= (counts['T'] ?: 0)) "D" else "T")
            append(if ((counts['E'] ?: 0) >= (counts['C'] ?: 0)) "E" else "C")
            append(if ((counts['P'] ?: 0) >= (counts['F'] ?: 0)) "P" else "F")
        }.toString()

        val scoreMap = mapOf(
            'M' to (counts['M'] ?: 0), 'I' to (counts['I'] ?: 0),
            'D' to (counts['D'] ?: 0), 'T' to (counts['T'] ?: 0),
            'E' to (counts['E'] ?: 0), 'C' to (counts['C'] ?: 0),
            'P' to (counts['P'] ?: 0), 'F' to (counts['F'] ?: 0)
        )
        Log.d("TestViewModel", "로컬 MBTI: $localMbti, scoreMap: $scoreMap")

        // 백엔드 API 호출을 위한 답변 변환: Map<String, String>
        val answersMap = finalAnswers.associate {
            it.originalQuestionId.toString() to it.selection
        }

        Log.d("TestViewModel", "=== 제출 전 검증 ===")
        Log.d("TestViewModel", "답변 개수: ${answersMap.size}")
        Log.d("TestViewModel", "답변 형식: $answersMap")

        viewModelScope.launch {
            try {
                Log.d("TestViewModel", "백엔드 API 호출 시작...")
                val result = testRepository.submitMbtiAnswers(answersMap)

                result.onSuccess { serverMbti ->
                    Log.d("TestViewModel", "백엔드 제출 성공 - MBTI: $serverMbti")
                    _currentScreen.value = TestScreenState.Result(serverMbti, scoreMap)
                }.onFailure { e ->
                    Log.e("TestViewModel", "백엔드 제출 실패: ${e.message}")
                    e.printStackTrace()
                    // 백엔드 실패 시 로컬 결과 사용
                    Log.d("TestViewModel", "로컬 결과로 대체: $localMbti")
                    testRepository.saveMbtiResult(localMbti)
                    _currentScreen.value = TestScreenState.Result(localMbti, scoreMap)
                }
            } catch (e: Exception) {
                Log.e("TestViewModel", "예외 발생: ${e.message}")
                e.printStackTrace()
                // 예외 발생 시 로컬 결과 사용
                testRepository.saveMbtiResult(localMbti)
                _currentScreen.value = TestScreenState.Result(localMbti, scoreMap)
            }
        }
    }

    fun restart() {
        _currentScreen.value = TestScreenState.Intro
    }
}

