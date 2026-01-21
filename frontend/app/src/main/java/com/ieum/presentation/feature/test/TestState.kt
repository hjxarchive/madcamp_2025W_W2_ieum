package com.ieum.presentation.feature.test


sealed class TestScreenState {
    object Intro : TestScreenState()
    object Testing : TestScreenState()
    data class Result(val mbti: String, val scores: Map<Char, Int>) : TestScreenState()
}

data class Answer(
    val questionIndex: Int,      // 섞인 순서의 인덱스 (0-35)
    val answerType: Char,        // 선택된 타입 (M, I, D, T, E, C, P, F)
    val originalQuestionId: Int, // 원래 질문 ID (1-36) - 백엔드 API용
    val selection: String        // "A" (왼쪽/X) 또는 "B" (오른쪽/O) - 백엔드 API용
)

// 결과 데이터 클래스 (Figma에서 제공한 텍스트 기반)
data class ResultData(
    val title: String,
    val description: String,
    val emoji: String = "✨"
)