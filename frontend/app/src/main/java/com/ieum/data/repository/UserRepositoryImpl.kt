package com.ieum.data.repository

import com.ieum.domain.model.CoupleInfo
import com.ieum.domain.model.User
import com.ieum.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor() : UserRepository {

    private val currentUser = User(
        id = 1L,
        name = "지민",
        nickname = "귀요미",
        mbti = "ENFP"
    )

    private val partner = User(
        id = 2L,
        name = "수현",
        nickname = "멋쟁이",
        mbti = "INTJ"
    )

    override fun getCurrentUser(): Flow<User> = flowOf(currentUser)

    override fun getPartner(): Flow<User> = flowOf(partner)

    override fun getCoupleInfo(): Flow<CoupleInfo> = flowOf(
        CoupleInfo(
            user = currentUser,
            partner = partner,
            dDay = 365,
            startDate = "2025-01-17"
        )
    )

    override suspend fun updateUserProfile(user: User) {
        // TODO: Implement when backend is ready
    }

    override suspend fun saveUserProfile(user: User) {
        // TODO: Implement actual saving (e.g., DataStore or Backend)
    }

    override suspend fun updatePartnerNickname(nickname: String) {
        // TODO: Implement when backend is ready
    }
}
