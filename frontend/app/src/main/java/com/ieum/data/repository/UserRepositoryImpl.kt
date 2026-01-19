package com.ieum.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ieum.domain.model.CoupleInfo
import com.ieum.domain.model.User
import com.ieum.domain.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userDataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class UserRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserRepository {

    private val ID = longPreferencesKey("user_id")
    private val NAME = stringPreferencesKey("name")
    private val NICKNAME = stringPreferencesKey("nickname")
    private val MBTI = stringPreferencesKey("mbti")
    private val PROFILE_IMAGE_URL = stringPreferencesKey("profile_image_url")
    private val BIRTHDAY = longPreferencesKey("birthday")
    private val ANNIVERSARY_DATE = longPreferencesKey("anniversary_date")

    private val PARTNER_NICKNAME = stringPreferencesKey("partner_nickname")

    override fun getCurrentUser(): Flow<User> = context.userDataStore.data.map { prefs ->
        User(
            id = prefs[ID] ?: 0L,
            name = prefs[NAME] ?: "",
            nickname = prefs[NICKNAME] ?: "사용자",
            mbti = prefs[MBTI] ?: "",
            profileImageUrl = prefs[PROFILE_IMAGE_URL],
            birthday = prefs[BIRTHDAY]?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            },
            anniversaryDate = prefs[ANNIVERSARY_DATE]?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            }
        )
    }

    override fun getPartner(): Flow<User> = context.userDataStore.data.map { prefs ->
        User(
            id = 0L,
            name = "",
            nickname = prefs[PARTNER_NICKNAME] ?: "상대방",
            mbti = ""
        )
    }

    override fun getCoupleInfo(): Flow<CoupleInfo> {
        return combine(getCurrentUser(), getPartner(), context.userDataStore.data) { user, partner, prefs ->
            val anniversaryLong = prefs[ANNIVERSARY_DATE] ?: 0L
            val anniversaryDate = if (anniversaryLong != 0L) {
                Instant.ofEpochMilli(anniversaryLong).atZone(ZoneId.systemDefault()).toLocalDate()
            } else {
                LocalDate.now()
            }
            val dDay = ChronoUnit.DAYS.between(anniversaryDate, LocalDate.now()).toInt()
            
            CoupleInfo(
                user = user,
                partner = partner,
                dDay = dDay,
                startDate = anniversaryDate.toString()
            )
        }
    }

    override suspend fun updateUserProfile(user: User) {
        saveUserProfile(user)
    }

    override suspend fun saveUserProfile(user: User) {
        context.userDataStore.edit { prefs ->
            prefs[ID] = user.id
            prefs[NAME] = user.name
            prefs[NICKNAME] = user.nickname
            prefs[MBTI] = user.mbti
            user.profileImageUrl?.let { prefs[PROFILE_IMAGE_URL] = it }
            user.birthday?.let {
                prefs[BIRTHDAY] = it.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
            user.anniversaryDate?.let {
                prefs[ANNIVERSARY_DATE] = it.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
        }
    }

    override suspend fun updatePartnerNickname(nickname: String) {
        context.userDataStore.edit { prefs ->
            prefs[PARTNER_NICKNAME] = nickname
        }
    }
}
