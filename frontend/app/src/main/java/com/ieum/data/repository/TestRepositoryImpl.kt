package com.ieum.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ieum.data.api.MbtiService
import com.ieum.data.api.UserService
import com.ieum.data.dto.MbtiSubmitRequest
import com.ieum.data.dto.UserUpdateRequest
import com.ieum.domain.repository.TestRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private val Context.testDataStore by preferencesDataStore(name = "test_prefs")

@Singleton
class TestRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mbtiService: MbtiService,
    private val userService: UserService
) : TestRepository {

    private val MBTI_KEY = stringPreferencesKey("my_mbti")
    private val PARTNER_MBTI_KEY = stringPreferencesKey("partner_mbti")

    private val _mbtiResult = MutableStateFlow<String?>(null)
    private val _partnerMbtiResult = MutableStateFlow<String?>(null)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        // Load from local storage first
        loadFromDataStore()
        // fetchMbtiFromServer() is called when user navigates to test/profile screen
        // Not in init to avoid calling API before login
    }

    private fun loadFromDataStore() {
        coroutineScope.launch {
            context.testDataStore.data.collect { prefs ->
                _mbtiResult.value = prefs[MBTI_KEY]
                _partnerMbtiResult.value = prefs[PARTNER_MBTI_KEY]
            }
        }
    }

    private fun fetchMbtiFromServer() {
        coroutineScope.launch {
            try {
                val response = mbtiService.getCoupleResult()
                response.myMbti?.let {
                    _mbtiResult.value = it
                    saveMbtiToDataStore(it)
                }
                response.partnerMbti?.let {
                    _partnerMbtiResult.value = it
                    savePartnerMbtiToDataStore(it)
                }
                Log.d("TestRepository", "Loaded MBTI from server: my=${response.myMbti}, partner=${response.partnerMbti}")
            } catch (e: Exception) {
                Log.e("TestRepository", "Failed to load MBTI from server", e)
            }
        }
    }

    override val mbtiResult: Flow<String?> = _mbtiResult

    override val partnerMbtiResult: Flow<String?> = _partnerMbtiResult

    override suspend fun saveMbtiResult(mbti: String) {
        _mbtiResult.value = mbti
        saveMbtiToDataStore(mbti)
        // Also send to server
        try {
            val request = UserUpdateRequest(mbtiType = mbti)
            userService.updateMe(request)
            Log.d("TestRepository", "MBTI saved to server: $mbti")
        } catch (e: Exception) {
            Log.e("TestRepository", "Failed to save MBTI to server", e)
        }
    }

    override suspend fun savePartnerMbtiResult(mbti: String) {
        _partnerMbtiResult.value = mbti
        savePartnerMbtiToDataStore(mbti)
    }

    private suspend fun saveMbtiToDataStore(mbti: String) {
        context.testDataStore.edit { preferences ->
            preferences[MBTI_KEY] = mbti
        }
    }

    private suspend fun savePartnerMbtiToDataStore(mbti: String) {
        context.testDataStore.edit { preferences ->
            preferences[PARTNER_MBTI_KEY] = mbti
        }
    }

    /**
     * Submit MBTI answers to server and get result
     */
    suspend fun submitMbtiAnswers(answers: Map<String, String>): Result<String> {
        return try {
            val request = MbtiSubmitRequest(answers = answers)
            val response = mbtiService.submitAnswers(request)
            saveMbtiResult(response.mbtiType)
            Log.d("TestRepository", "MBTI result: ${response.mbtiType}")
            Result.success(response.mbtiType)
        } catch (e: Exception) {
            Log.e("TestRepository", "Failed to submit MBTI answers", e)
            Result.failure(e)
        }
    }

    /**
     * Get MBTI questions from server
     */
    suspend fun getMbtiQuestions() = try {
        val response = mbtiService.getQuestions()
        Result.success(response.questions)
    } catch (e: Exception) {
        Log.e("TestRepository", "Failed to get MBTI questions", e)
        Result.failure(e)
    }
}
