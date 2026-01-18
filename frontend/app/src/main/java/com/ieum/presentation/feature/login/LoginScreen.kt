package com.ieum.presentation.feature.login

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.ieum.R

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Google Sign-In 설정
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // ActivityResultLauncher로 Google 로그인 결과 처리
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            android.util.Log.d("LoginScreen", "Google 로그인 성공: ${account.email}")

            // Google 계정 정보로 로그인 처리
            val email = account.email
            if (email != null) {
                viewModel.loginWithGoogleAccount(email, account.displayName, onLoginSuccess)
            } else {
                viewModel.setError("Google 로그인 실패: 이메일을 받지 못했습니다")
            }
        } catch (e: ApiException) {
            // 에러 코드 참고: https://developers.google.com/android/reference/com/google/android/gms/common/api/CommonStatusCodes
            val errorMsg = when (e.statusCode) {
                7 -> "네트워크 오류 (NETWORK_ERROR)"
                10 -> "개발자 오류 - SHA-1 또는 패키지명 확인 필요 (DEVELOPER_ERROR)"
                12501 -> "사용자가 로그인을 취소함 (SIGN_IN_CANCELLED)"
                12502 -> "로그인 진행 중 (SIGN_IN_CURRENTLY_IN_PROGRESS)"
                else -> "오류 코드: ${e.statusCode}"
            }
            android.util.Log.e("LoginScreen", "Google 로그인 실패: $errorMsg", e)
            viewModel.setError("Google 로그인 실패: $errorMsg")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 배경 이미지
        Image(
            painter = painterResource(id = R.drawable.background2),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 로그인 UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "이음",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF5A3E2B)
            )

            Spacer(modifier = Modifier.height(50.dp))

            Button(
                onClick = {
                    // 기존 로그인 세션 로그아웃 후 새로 로그인
                    googleSignInClient.signOut().addOnCompleteListener {
                        launcher.launch(googleSignInClient.signInIntent)
                    }
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .width(260.dp)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE6C8A0),
                    contentColor = Color(0xFF5A3E2B),
                    disabledContainerColor = Color(0xFFE6C8A0).copy(alpha = 0.6f),
                    disabledContentColor = Color(0xFF5A3E2B)
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp),
                        color = Color(0xFF5A3E2B)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("로그인 중…")
                } else {
                    Text(
                        text = "Google로 시작하기",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 테스트용 스킵 버튼 (개발 중에만 사용)
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = {
                    viewModel.skipLogin(onLoginSuccess)
                }
            ) {
                Text(
                    text = "[DEV] 로그인 스킵",
                    color = Color(0xFF5A3E2B).copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // 에러 메시지 표시
            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1.2f))
        }
    }
}