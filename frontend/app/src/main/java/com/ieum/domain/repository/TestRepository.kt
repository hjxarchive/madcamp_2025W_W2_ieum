package com.ieum.domain.repository

import kotlinx.coroutines.flow.Flow

interface TestRepository {
    val mbtiResult: Flow<String?>
    val partnerMbtiResult: Flow<String?>

    suspend fun saveMbtiResult(mbti: String)
    suspend fun savePartnerMbtiResult(mbti: String)

    /**
     * Submit MBTI answers to server and get result
     * @param answers Map of question ID (1-36) to answer ("A" for left/X, "B" for right/O)
     * @return Result containing MBTI type string or error
     */
    suspend fun submitMbtiAnswers(answers: Map<String, String>): Result<String>
}
