package com.ieum.presentation.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ieum.R

@Composable
fun NicknameInputScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onNext: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mainBrown = Color(0xFF5A3E2B)
    
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenHeight = maxHeight
        val screenWidth = maxWidth
        
        val titleFontSize = (screenWidth.value * 0.08f).sp
        val stepFontSize = (screenWidth.value * 0.04f).sp
        val subtitleFontSize = (screenWidth.value * 0.045f).sp
        
        Box(modifier = Modifier.fillMaxSize()) {
            // 배경
            Image(
                painter = painterResource(id = R.drawable.background2),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = screenWidth * 0.08f)
                    .padding(top = screenHeight * 0.06f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // "이음" 타이틀
                Text(
                    text = "이음",
                    fontSize = subtitleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = mainBrown
                )
                
                Spacer(modifier = Modifier.height(screenHeight * 0.07f))
                
                // 진행도 표시 (1.)
                Text(
                    text = "1.",
                    fontSize = stepFontSize,
                    fontWeight = FontWeight.Medium,
                    color = mainBrown.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(screenHeight * 0.03f))
                
                // 메인 문구
                Text(
                    text = "사용할 닉네임을 알려주세요.",
                    fontSize = subtitleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = mainBrown,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(screenHeight * 0.01f))
                
                Text(
                    text = "상대방이 불러주는 애칭도 좋아요.",
                    fontSize = (screenWidth.value * 0.035f).sp,
                    color = mainBrown.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(screenHeight * 0.15f))
                
                // 닉네임 입력란
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        BasicTextField(
                            value = uiState.nickname,
                            onValueChange = { viewModel.setNickname(it) },
                            textStyle = TextStyle(
                                fontSize = (screenWidth.value * 0.06f).sp,
                                fontWeight = FontWeight.Medium,
                                color = mainBrown,
                                textAlign = TextAlign.Center
                            ),
                            cursorBrush = SolidColor(mainBrown),
                            decorationBox = { innerTextField ->
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (uiState.nickname.isEmpty()) {
                                        Text(
                                            text = "닉네임 입력",
                                            fontSize = (screenWidth.value * 0.06f).sp,
                                            fontWeight = FontWeight.Medium,
                                            color = mainBrown.copy(alpha = 0.3f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 하단 라인
                        Box(
                            modifier = Modifier
                                .width(screenWidth * 0.4f)
                                .height(1.dp)
                                .background(mainBrown.copy(alpha = 0.3f))
                        )
                    }
                }
            }
            
            // 다음 버튼 (닉네임 활성화 시에만 표시)
            if (uiState.nickname.isNotBlank()) {
                IconButton(
                    onClick = onNext,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = screenWidth * 0.08f, bottom = screenHeight * 0.05f)
                        .size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "다음",
                        tint = mainBrown,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
