package com.ieum.domain.usecase.user

import com.ieum.domain.model.CoupleInfo
import com.ieum.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCoupleInfoUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<CoupleInfo> = userRepository.getCoupleInfo()
}
