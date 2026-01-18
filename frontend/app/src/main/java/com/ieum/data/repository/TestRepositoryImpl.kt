package com.ieum.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ieum.domain.repository.TestRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.testDataStore by preferencesDataStore(name = "test_prefs")

@Singleton
class TestRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TestRepository {

    private val MBTI_KEY = stringPreferencesKey("my_mbti")
    private val PARTNER_MBTI_KEY = stringPreferencesKey("partner_mbti")

    override val mbtiResult: Flow<String?> = context.testDataStore.data
        .map { preferences ->
            preferences[MBTI_KEY]
        }

    override val partnerMbtiResult: Flow<String?> = context.testDataStore.data
        .map { preferences ->
            preferences[PARTNER_MBTI_KEY]
        }

    override suspend fun saveMbtiResult(mbti: String) {
        context.testDataStore.edit { preferences ->
            preferences[MBTI_KEY] = mbti
        }
    }

    override suspend fun savePartnerMbtiResult(mbti: String) {
        context.testDataStore.edit { preferences ->
            preferences[PARTNER_MBTI_KEY] = mbti
        }
    }
}
