package com.ieum.presentation.feature.connection

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.BasicTextField
import com.ieum.R

@Composable
fun CodeConnectionScreen(
    viewModel: ConnectionViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onNavigateToMain: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    // 연결 성공 시 자동 이동
    LaunchedEffect(uiState.isConnected) {
        if (uiState.isConnected && !uiState.showSuccessModal) {
            onNavigateToMain()
        }
    }

    // 화면 크기 가져오기
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    // 반응형 크기 계산
    val isSmallScreen = screenHeight < 700.dp
    val isMediumScreen = screenHeight in 700.dp..850.dp

    val topPadding = when {
        isSmallScreen -> 24.dp
        isMediumScreen -> 40.dp
        else -> 60.dp
    }

    val titleFontSize = when {
        isSmallScreen -> 18.sp
        isMediumScreen -> 20.sp
        else -> 22.sp
    }

    val dogImageHeight = when {
        isSmallScreen -> (screenHeight * 0.25f)
        isMediumScreen -> (screenHeight * 0.30f)
        else -> (screenHeight * 0.35f)
    }

    val codeFontSize = when {
        isSmallScreen -> 24.sp
        else -> 29.sp
    }

    val sectionSpacing = when {
        isSmallScreen -> 30.dp
        isMediumScreen -> 50.dp
        else -> 75.dp
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 배경 설정
//        Image(
//            // painter = painterResource(id = R.drawable.background2),
//            contentDescription = null,
//            modifier = Modifier.fillMaxSize(),
//            contentScale = ContentScale.FillBounds
//        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars) // 시스템 바 패딩 적용 (반응형 필수, 내 수정사항 유지)
                .verticalScroll(scrollState) // 스크롤 적용 (팀원 수정사항 유지)
                .padding(horizontal = 24.dp)
                .padding(top = topPadding, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. 메인 문구
            Spacer(modifier = Modifier.height(if (isSmallScreen) 20.dp else 40.dp))
            Text(
                text = "서로의 코드를 입력하고\n이음을 시작하세요.",
                fontSize = titleFontSize,
                lineHeight = titleFontSize * 1.4f,
                textAlign = TextAlign.Center,
                color = uiState.mainTextColor,
                modifier = Modifier.padding(top = if (isSmallScreen) 8.dp else 20.dp)
            )

            // 2. 강아지 이미지 (반응형 높이)
            Spacer(modifier = Modifier.height(if (isSmallScreen) 8.dp else 10.dp))
            Image(
                painter = painterResource(id = R.drawable.dog),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dogImageHeight),
                colorFilter = ColorFilter.tint(Color(0xFFE6C8A0).copy(alpha = 0.6f)),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(if (isSmallScreen) 8.dp else 10.dp))

            // 3. 나의 이음 코드 섹션
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "나의 이음 코드",
                    fontSize = if (isSmallScreen) 16.sp else 19.sp,
                    color = uiState.mainTextColor.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = if (isSmallScreen) 8.dp else 12.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (uiState.isLoadingMyCode) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFF5A3E2B),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = uiState.myCode.ifEmpty { "------" },
                                fontSize = codeFontSize,
                                letterSpacing = 4.sp,
                                textAlign = TextAlign.Center,
                                color = Color(0xFF5A3E2B),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(if (isSmallScreen) 140.dp else 160.dp)
                                .height(2.dp)
                                .background(Color(0xFF5A3E2B))
                        )
                    }

                    IconButton(
                        onClick = {
                            if (uiState.myCode.isNotEmpty()) {
                                clipboardManager.setText(AnnotatedString(uiState.myCode))
                                viewModel.setCopied(true)
                            }
                        },
                        enabled = uiState.myCode.isNotEmpty() && !uiState.isLoadingMyCode,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isCopied) Icons.Default.Done else Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = if (uiState.myCode.isNotEmpty()) Color(0xFF5A3E2B) else Color(0xFF5A3E2B).copy(alpha = 0.3f),
                            modifier = Modifier.size(if (isSmallScreen) 20.dp else 24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(sectionSpacing))

            // 4. 하단 코드 입력 섹션
            if (!uiState.showCodeInput) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "혹시,\n이미 상대방의 이음 코드를 가지고 계신가요?",
                        fontSize = if (isSmallScreen) 13.sp else 15.sp,
                        textAlign = TextAlign.Center,
                        color = uiState.mainTextColor.copy(alpha = 0.8f),
                    )
                    Text(
                        text = "코드 입력하기",
                        fontSize = if (isSmallScreen) 14.sp else 16.sp,
                        color = Color(0xFF5A3E2B),
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .padding(vertical = if (isSmallScreen) 8.dp else 12.dp)
                            .clickable { viewModel.toggleCodeInput(true) }
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        BasicTextField(
                            value = uiState.partnerCode,
                            onValueChange = { viewModel.onPartnerCodeChange(it) },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = if (isSmallScreen) 18.sp else 20.sp,
                                textAlign = TextAlign.Center,
                                letterSpacing = 4.sp,
                                color = Color(0xFF5A3E2B)
                            ),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF5A3E2B)),
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.Center) {
                                    if (uiState.partnerCode.isEmpty()) {
                                        Text(
                                            text = "상대방의 코드를 입력하세요",
                                            fontSize = if (isSmallScreen) 14.sp else 16.sp,
                                            color = Color(0xFF5A3E2B).copy(alpha = 0.3f)
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                            modifier = Modifier.width(if (isSmallScreen) 200.dp else 240.dp)
                        )
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .width(if (isSmallScreen) 170.dp else 200.dp)
                                .height(1.dp)
                                .background(Color(0xFF5A3E2B).copy(alpha = 0.2f))
                        )
                    }

                    Spacer(Modifier.height(if (isSmallScreen) 16.dp else 24.dp))

                    // 에러 메시지 표시
                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            fontSize = 12.sp,
                            color = Color(0xFFE57373),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (uiState.isConnecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF5A3E2B),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "연결하기",
                            fontSize = if (isSmallScreen) 16.sp else 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.partnerCode.length == 6) Color(0xFF5A3E2B) else Color(0xFF5A3E2B).copy(alpha = 0.4f),
                            modifier = Modifier
                                .clickable(enabled = uiState.partnerCode.length == 6 && !uiState.isConnecting) {
                                    viewModel.joinCouple()
                                }
                                .padding(8.dp)
                        )
                    }
                }
            }

            // 하단 여백
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // 5. 초대장 모달 (Dialog)
    if (uiState.showInvitationModal) {
        InvitationModal(
            onAccept = {
                viewModel.handleInvitation(true)
                onNavigateToMain()
            },
            onReject = { viewModel.handleInvitation(false) }
        )
    }

    // 6. 연결 성공 모달
    if (uiState.showSuccessModal) {
        SuccessConnectionModal(
            partnerNickname = uiState.partnerNickname,
            onDismiss = {
                viewModel.dismissSuccessModal()
                onNavigateToMain()
            }
        )
    }
}

@Composable
fun InvitationModal(onAccept: () -> Unit, onReject: () -> Unit) {
    Dialog(onDismissRequest = onReject) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.letter),
                    contentDescription = null,
                    modifier = Modifier.size(150.dp),
                    tint = Color(0xFFE6C8A0).copy(0.6f)
                )
                Spacer(Modifier.height(16.dp))
                Text("초대장이 날아왔어요", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5A3E2B))
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE6C8A0))
                    ) {
                        Text("거절", color = Color.Gray)
                    }
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE6C8A0).copy(0.6f))
                    ) {
                        Text("수락", color = Color(0xFF5A3E2B))
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessConnectionModal(
    partnerNickname: String?,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💕",
                    fontSize = 64.sp
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "연결 성공!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5A3E2B)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (partnerNickname != null) {
                        "${partnerNickname}님과 연결되었습니다"
                    } else {
                        "파트너와 연결되었습니다"
                    },
                    fontSize = 16.sp,
                    color = Color(0xFF5A3E2B).copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE6C8A0))
                ) {
                    Text("시작하기", color = Color(0xFF5A3E2B), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}