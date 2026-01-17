package com.ieum.domain.repository

import com.ieum.domain.model.CoupleInfo
import com.ieum.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<User>
    fun getPartner(): Flow<User>
    fun getCoupleInfo(): Flow<CoupleInfo>
    suspend fun updateUserProfile(user: User)
    suspend fun updatePartnerNickname(nickname: String)
}
