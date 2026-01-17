package com.ieum.presentation.feature.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ieum.R

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {

        // ✅ 1) 배경 이미지
        Image(
            painter = painterResource(id = R.drawable.background2), // ← 네 파일명
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // ✅ 2) (선택) 반투명 오버레이 – 글자 가독성

        // ✅ 3) 로그인 UI
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
                onClick = { viewModel.onClickGoogleLogin(onLoginSuccess) },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .width(260.dp)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE6C8A0), // ⬅️ 연갈색 배경
                    contentColor = Color(0xFF5A3E2B),   // ⬅️ 버튼 안 글씨 진갈색
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

            Spacer(modifier = Modifier.weight(1.2f))
        }
    }
}
