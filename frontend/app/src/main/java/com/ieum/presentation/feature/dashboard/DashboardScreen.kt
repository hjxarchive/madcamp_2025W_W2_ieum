package com.ieum.presentation.feature.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PunchClock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ieum.R
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToCalendar: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToBudgetPlanning: () -> Unit = {},
    onNavigateToClicker: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardContent(
        uiState = uiState,
        onNavigateToCalendar = onNavigateToCalendar,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToBudgetPlanning = onNavigateToBudgetPlanning,
        onNavigateToClicker = onNavigateToClicker
    )
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onNavigateToCalendar: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToBudgetPlanning: () -> Unit = {},
    onNavigateToClicker: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 배경 이미지 설정
//        Image(
//            // painter = painterResource(id = R.drawable.background2),
//            contentDescription = null,
//            modifier = Modifier.fillMaxSize(),
//            contentScale = ContentScale.FillBounds
//        )

        Column(modifier = Modifier.fillMaxSize()) {
            // 상단 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 48.dp, 24.dp, 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "이음",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5A3E2B)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateToProfile,
                        modifier = Modifier.size(45.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = uiState.mainTextColor,
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. 함께한 시간 컨테이너
                item {
                    Card(
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = uiState.containerColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("우리가 함께한 시간", color = uiState.mainTextColor)
                            Text(
                                "${uiState.daysTogether}일 째",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = uiState.mainTextColor,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // 겹쳐진 원 아이콘
                                Box {
                                    // 첫 번째 원 (왼쪽)
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .border(width = 2.dp, color = Color(0xFFC8B7A5), shape = CircleShape) // 테두리 추가
                                            .clip(CircleShape)
                                    )
                                    // 두 번째 원 (오른쪽, 살짝 겹침)
                                    Box(
                                        modifier = Modifier
                                            .padding(start = 25.dp) // 겹치는 정도 조절
                                            .size(40.dp)
                                            .border(width = 2.dp, color = Color(0xFFEC8B7A5), shape = CircleShape) // 테두리 추가
                                            .clip(CircleShape)
                                    )
                                }

                                Spacer(Modifier.width(35.dp))
                                Text(
                                    text = uiState.partnerNames,
                                    fontWeight = FontWeight.Medium,
                                    color = uiState.mainTextColor
                                )
                            }
                        }
                    }
                }

                // 2. 3개 버튼 행
                item {
                    Spacer(Modifier.height(30.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MenuButton("궁합", Icons.Default.Favorite, uiState, onClick = {
                            /* 클릭 시 동작: 예) println("궁합 클릭") */
                        })
                        MenuButton("클리커", Icons.Default.SportsEsports, uiState, onClick = {
                            onNavigateToClicker()
                        })
                        MenuButton("추억", Icons.Default.Image, uiState, onClick = {
                            /* 클릭 시 동작 */
                        })
                    }
                }

                // 3. 다가오는 기념일
                item {
                    Spacer(Modifier.height(30.dp))
                    Column {
                        Text("다가오는 기념일", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = uiState.mainTextColor)
                        Spacer(Modifier.height(12.dp))
                        uiState.upcomingEvents.forEach { event ->
                            AnniversaryItem(event, uiState)
                            Spacer(Modifier.height(8.dp))
                        }
                        TextButton(
                            onClick = onNavigateToCalendar,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("+ 더보기", color = uiState.mainTextColor)
                        }
                    }
                }

                // 4. 예산 관리 카드
                item {
                    Card(
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = uiState.containerColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToBudgetPlanning() }
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = uiState.mainTextColor)
                                Spacer(Modifier.width(8.dp))
                                Text("이번 달 데이트 비용", fontWeight = FontWeight.Bold, color = uiState.mainTextColor)
                            }

                            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(vertical = 12.dp)) {
                                Text(
                                    String.format("%,d", uiState.spentAmount),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = uiState.mainTextColor
                                )
                                Text("원", fontSize = 18.sp, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp), color = uiState.mainTextColor)
                            }

                            Text("총 한도 ${String.format("%,d", uiState.totalBudget)} 중", fontSize = 12.sp, color = uiState.mainTextColor)

                            val progress = uiState.spentAmount.toFloat() / uiState.totalBudget.toFloat()
                            LinearProgressIndicator(
                                progress = progress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .height(12.dp)
                                    .clip(CircleShape),
                                color = Color(0xFFF48FB1), // 게이지 바 색상
                                trackColor = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, uiState: DashboardUiState, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(uiState.containerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = uiState.mainTextColor)
        }
        Spacer(Modifier.height(8.dp))
        Text(label, fontSize = 14.sp, color = uiState.mainTextColor)
    }
}

@Composable
fun AnniversaryItem(event: Anniversary, uiState: DashboardUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.7f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(event.name, fontWeight = FontWeight.Bold, color = uiState.mainTextColor)
            Text(event.date, fontSize = 12.sp, color = Color.Gray)
        }
        Text("D-${event.daysLeft}", fontWeight = FontWeight.Bold, color = Color(0xFF5A3E2B))
    }
}