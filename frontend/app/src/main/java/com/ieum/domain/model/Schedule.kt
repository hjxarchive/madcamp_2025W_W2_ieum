package com.ieum.domain.model

import java.time.LocalDate

data class Schedule(
    val id: Int,
    val title: String,
    val date: LocalDate,
    val time: String,
    val colorHex: String,
    val isShared: Boolean = false,
    val description: String? = null
)

data class Anniversary(
    val id: Long,
    val title: String,
    val emoji: String,
    val dDay: String,
    val date: LocalDate
)
