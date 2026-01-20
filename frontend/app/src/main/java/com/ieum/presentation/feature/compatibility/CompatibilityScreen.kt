package com.ieum.presentation.feature.compatibility

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ieum.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompatibilityScreen(
    viewModel: CompatibilityViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val containerColor = Color(0xFFECD4CD)
    val mainTextColor = Color(0xFF5A3E2B)

    Box(modifier = Modifier.fillMaxSize()) {
        // 배경 이미지
        Image(
            painter = painterResource(id = R.drawable.background2),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // 상단 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 36.dp, 16.dp, 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로 가기",
                        tint = mainTextColor
                    )
                }
                Text(
                    text = "이름 궁합",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = mainTextColor
                )
                // 균형을 위한 빈 공간
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 메인 콘텐츠
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 하트 아이콘
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFE57373),
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 이름 입력 카드
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "두 사람의 이름을 입력해주세요",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = mainTextColor
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // 첫 번째 이름 입력
                        OutlinedTextField(
                            value = uiState.name1,
                            onValueChange = { viewModel.updateName1(it) },
                            label = { Text("첫 번째 이름") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE57373),
                                focusedLabelColor = Color(0xFFE57373),
                                cursorColor = Color(0xFFE57373),
                                unfocusedBorderColor = mainTextColor.copy(alpha = 0.5f),
                                unfocusedLabelColor = mainTextColor.copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !uiState.isLoading
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 하트 아이콘 (사이)
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFE57373),
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 두 번째 이름 입력
                        OutlinedTextField(
                            value = uiState.name2,
                            onValueChange = { viewModel.updateName2(it) },
                            label = { Text("두 번째 이름") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE57373),
                                focusedLabelColor = Color(0xFFE57373),
                                cursorColor = Color(0xFFE57373),
                                unfocusedBorderColor = mainTextColor.copy(alpha = 0.5f),
                                unfocusedLabelColor = mainTextColor.copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !uiState.isLoading
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // 분석하기 버튼
                        Button(
                            onClick = { viewModel.analyzeCompatibility() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE57373)
                            ),
                            enabled = !uiState.isLoading && uiState.name1.isNotBlank() && uiState.name2.isNotBlank()
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "궁합 분석하기",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 에러 메시지
                AnimatedVisibility(
                    visible = uiState.error != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        )
                    ) {
                        Text(
                            text = uiState.error ?: "",
                            color = Color(0xFFC62828),
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 결과 표시
                AnimatedVisibility(
                    visible = uiState.result != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    uiState.result?.let { result ->
                        Spacer(modifier = Modifier.height(24.dp))

                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = containerColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "궁합 결과",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = mainTextColor
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // 점수 표시
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.7f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${result.score}",
                                            fontSize = 40.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE57373)
                                        )
                                        Text(
                                            text = "점",
                                            fontSize = 16.sp,
                                            color = mainTextColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // 이름 표시
                                Text(
                                    text = "${uiState.name1} & ${uiState.name2}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = mainTextColor
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // 요약
                                Text(
                                    text = result.summary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFE57373),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // 구분선
                                Divider(
                                    color = mainTextColor.copy(alpha = 0.2f),
                                    thickness = 1.dp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // 상세 분석
                                Text(
                                    text = result.details,
                                    fontSize = 14.sp,
                                    color = mainTextColor,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 22.sp
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // 다시하기 버튼
                                OutlinedButton(
                                    onClick = { viewModel.resetResult() },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFFE57373)
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("다시 분석하기")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
