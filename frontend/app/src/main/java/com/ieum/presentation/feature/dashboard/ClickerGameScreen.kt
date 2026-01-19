package com.ieum.presentation.feature.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ieum.R

@Composable
fun ClickerGameScreen(
    onBack: () -> Unit
) {
    var partnerClicks by remember { mutableStateOf(0) }
    var userClicks by remember { mutableStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }
    var winner by remember { mutableStateOf("") }

    val mainBrown = Color(0xFF5A3E2B)
    val maxClicks = 30

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF9F6))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 상단 (상대방)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable(
                        enabled = !gameOver,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (partnerClicks < maxClicks) {
                            partnerClicks++
                            if (partnerClicks >= maxClicks) {
                                gameOver = true
                                winner = "상대방"
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "상대방 풍선",
                        color = mainBrown,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.rotate(180f)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Image(
                        painter = painterResource(
                            id = if (partnerClicks >= maxClicks) R.drawable.boom else R.drawable.balloon
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .size((50 + partnerClicks * 8).dp)
                            .rotate(180f),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // 구분선
            Divider(
                color = mainBrown.copy(alpha = 0.3f),
                thickness = 2.dp,
                modifier = Modifier.fillMaxWidth()
            )

            // 하단 (나)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable(
                        enabled = !gameOver,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (userClicks < maxClicks) {
                            userClicks++
                            if (userClicks >= maxClicks) {
                                gameOver = true
                                winner = "나"
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(
                            id = if (userClicks >= maxClicks) R.drawable.boom else R.drawable.balloon
                        ),
                        contentDescription = null,
                        modifier = Modifier.size((50 + userClicks * 8).dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "나의 풍선",
                        color = mainBrown,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 뒤로가기 버튼
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(top = 40.dp, start = 16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "뒤로가기",
                tint = mainBrown
            )
        }

        // 결과 다이얼로그
        if (gameOver) {
            AlertDialog(
                onDismissRequest = { 
                    gameOver = false
                    partnerClicks = 0
                    userClicks = 0
                },
                title = { Text(text = "게임 종료!", color = mainBrown) },
                text = { Text(text = "${winner}의 승리입니다!", color = mainBrown) },
                confirmButton = {
                    TextButton(onClick = {
                        gameOver = false
                        partnerClicks = 0
                        userClicks = 0
                    }) {
                        Text("다시 하기", color = mainBrown)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onBack) {
                        Text("나가기", color = mainBrown)
                    }
                },
                containerColor = Color.White
            )
        }
    }
}
