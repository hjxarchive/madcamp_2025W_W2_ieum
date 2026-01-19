package com.ieum.domain.model

import java.time.LocalDate

data class User(
    val id: Long,
    val name: String,
    val nickname: String,
    val mbti: String,
    val profileImageUrl: String? = null,
    val birthday: LocalDate? = null,
    val anniversaryDate: LocalDate? = null
)

data class CoupleInfo(
    val user: User,
    val partner: User,
    val dDay: Int,
    val startDate: String
)
