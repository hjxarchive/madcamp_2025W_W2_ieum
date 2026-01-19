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
        Log.d("TestViewModel", "Q${currentQuestionIndex.value + 1}: 선택된 타입 = $type")
        val newList = _answers.value + Answer(currentQuestionIndex.value, type)
        _answers.value = newList

        if (currentQuestionIndex.value < 35) { // 총 36개 질문
            currentQuestionIndex.value += 1
        } else {
            calculateFinalResult(newList)
        }
    }

    // TestViewModel.kt 내부
    fun onCompleteNavigation(onNavigate: () -> Unit) {
        onNavigate()
    }

    private fun calculateFinalResult(finalAnswers: List<Answer>) {
        val counts = finalAnswers.groupBy { it.answerType }.mapValues { it.value.size }
        Log.d("TestViewModel", "최종 카운트: $counts")

        val mbti = StringBuilder().apply {
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
        Log.d("TestViewModel", "MBTI: $mbti, scoreMap: $scoreMap")
        
        viewModelScope.launch {
            testRepository.saveMbtiResult(mbti)
        }

        _currentScreen.value = TestScreenState.Result(mbti, scoreMap)
    }

    fun restart() {
        _currentScreen.value = TestScreenState.Intro
    }
}

