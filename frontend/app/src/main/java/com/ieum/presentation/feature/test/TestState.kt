package com.ieum.presentation.feature.test


sealed class TestScreenState {
    object Intro : TestScreenState()
    object Testing : TestScreenState()
    data class Result(val mbti: String, val scores: Map<Char, Int>) : TestScreenState()
}

data class Answer(
    val questionIndex: Int,
    val answerType: Char
)

// 결과 데이터 클래스 (Figma에서 제공한 텍스트 기반)
data class ResultData(
    val title: String,
    val description: String,
    val emoji: String = "✨"
)