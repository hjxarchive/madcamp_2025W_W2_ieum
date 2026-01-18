package com.ieum.domain.repository

import kotlinx.coroutines.flow.Flow

interface TestRepository {
    val mbtiResult: Flow<String?>
    val partnerMbtiResult: Flow<String?> // For now, we might just mock or store partner's too
    
    suspend fun saveMbtiResult(mbti: String)
    suspend fun savePartnerMbtiResult(mbti: String)
}
