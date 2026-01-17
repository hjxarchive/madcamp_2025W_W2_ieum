package com.ieum.domain.model

data class Memory(
    val id: Long,
    val placeName: String,
    val address: String,
    val comment: String,
    val date: String,
    val latitude: Double,
    val longitude: Double,
    val colorHex: String,
    val imageUrl: String? = null
)
