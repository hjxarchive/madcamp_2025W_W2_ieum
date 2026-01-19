package com.ieum.data.api

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ieum.data.repository.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val context: Context
) : Interceptor {

    private val TOKEN_KEY = stringPreferencesKey("access_token")
    private val USER_ID_KEY = stringPreferencesKey("user_id")

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 인증이 필요 없는 엔드포인트 체크
        val noAuthEndpoints = listOf(
            "/api/auth/google",
            "/api/mbti/questions"
        )

        val isNoAuthEndpoint = noAuthEndpoints.any {
            originalRequest.url.encodedPath.contains(it)
        }

        if (isNoAuthEndpoint) {
            return chain.proceed(originalRequest)
        }

        // 토큰과 사용자 ID 가져오기
        val (token, userId) = runBlocking {
            context.dataStore.data.map { prefs ->
                Pair(prefs[TOKEN_KEY], prefs[USER_ID_KEY])
            }.first()
        }

        // 토큰과 사용자 ID가 있으면 헤더에 추가
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .apply {
                    if (userId != null) {
                        addHeader("X-User-Id", userId)
                    }
                }
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}
