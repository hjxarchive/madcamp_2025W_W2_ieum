package com.ieum.presentation.feature.connection

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
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
    viewModel: ConnectionViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateToMain: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    Box(modifier = Modifier.fillMaxSize()) {
        // 배경 설정
        Image(
            painter = painterResource(id = R.drawable.background2),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. 메인 문구 (상단 고정)
            Spacer(modifier = Modifier.height(60.dp))
            Text(
                text = "서로의 코드를 입력하고\n이음을 시작하세요.",
                fontSize = 22.sp,
                lineHeight = 32.sp,
                textAlign = TextAlign.Center,
                color = uiState.mainTextColor,
                modifier = Modifier.padding(top = 20.dp)
            )

            Spacer(modifier = Modifier.height(10.dp)) // 문구와 강아지 사이 간격
            Image(
                painter = painterResource(id = R.drawable.dog),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp), // ⬅️ 강아지 크기를 여기서 조절하세요! (너무 크면 줄여보세요)
                colorFilter = ColorFilter.tint(Color(0xFFE6C8A0).copy(alpha = 0.6f)),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(10.dp))

            // 3. 나의 이음 코드 섹션 (하단 고정 영역 시작)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "나의 이음 코드",
                    fontSize = 19.sp,
                    color = uiState.mainTextColor.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.myCode,
                            fontSize = 29.sp,
                            letterSpacing = 4.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF5A3E2B),
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .width(160.dp)
                                .height(2.dp)
                                .background(Color(0xFF5A3E2B))
                        )
                    }

                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(uiState.myCode))
                            viewModel.setCopied(true)
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isCopied) Icons.Default.Done else Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color(0xFF5A3E2B),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(75.dp))

            // 4. 하단 코드 입력 섹션
            if (!uiState.showCodeInput) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "혹시,\n 이미 상대방의 이음 코드를 가지고 계신가요?",
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        color = uiState.mainTextColor.copy(alpha = 0.8f),
                    )
                    Text(
                        text = "코드 입력하기",
                        fontSize = 16.sp,
                        color = Color(0xFF5A3E2B),
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .padding(vertical = 12.dp)
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
                                fontSize = 20.sp,
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
                                            fontSize = 16.sp,
                                            color = Color(0xFF5A3E2B).copy(alpha = 0.3f)
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                            modifier = Modifier.width(240.dp)
                        )
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .width(200.dp)
                                .height(1.dp)
                                .background(Color(0xFF5A3E2B).copy(alpha = 0.2f))
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "초대장 보내기",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.partnerCode.length == 6) Color(0xFF5A3E2B) else Color(0xFF5A3E2B).copy(alpha = 0.4f),
                        modifier = Modifier
                            .clickable(enabled = uiState.partnerCode.length == 6) {
                                viewModel.sendInvitation()
                            }
                            .padding(8.dp)
                    )
                }
            }
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