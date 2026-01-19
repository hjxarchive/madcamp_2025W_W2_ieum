package com.ieum.presentation.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import java.time.LocalDate

@Composable
fun AnniversaryInputScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onNext: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mainBrown = Color(0xFF5A3E2B)
    
    var selectedYear by remember { mutableStateOf(2022) }
    var selectedMonth by remember { mutableStateOf(12) }
    var selectedDay by remember { mutableStateOf(1) }
    
    LaunchedEffect(selectedYear, selectedMonth, selectedDay) {
        try {
            viewModel.setAnniversaryDate(LocalDate.of(selectedYear, selectedMonth, selectedDay))
        } catch (e: Exception) {
            // Ignore invalid dates temporarily
        }
    }
    
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenHeight = maxHeight
        val screenWidth = maxWidth
        
        val titleFontSize = (screenWidth.value * 0.08f).sp
        val stepFontSize = (screenWidth.value * 0.04f).sp
        val subtitleFontSize = (screenWidth.value * 0.045f).sp
        val dateFontSize = (screenWidth.value * 0.055f).sp
        
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
                
                // 진행도 표시 (3.)
                Text(
                    text = "3.",
                    fontSize = stepFontSize,
                    fontWeight = FontWeight.Medium,
                    color = mainBrown.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(screenHeight * 0.03f))
                
                // 메인 문구
                Text(
                    text = "두 분의 기념일을 입력하세요",
                    fontSize = subtitleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = mainBrown,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(screenHeight * 0.01f))
                
                Text(
                    text = "기념일, 위젯 등에 표시돼요",
                    fontSize = (screenWidth.value * 0.035f).sp,
                    color = mainBrown.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(screenHeight * 0.06f))
                
                // 선택된 날짜 표시
                Text(
                    text = "${selectedYear}년 ${selectedMonth}월 ${selectedDay}일",
                    fontSize = dateFontSize,
                    fontWeight = FontWeight.Bold,
                    color = mainBrown
                )
                
                Spacer(modifier = Modifier.height(screenHeight * 0.04f))
                
                // Date Picker
                Row(
                    modifier = Modifier.height(screenHeight * 0.25f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val currentYear = LocalDate.now().year
                    
                    // 년
                    DatePickerWheel(
                        items = (2015..currentYear).toList(),
                        selectedItem = selectedYear,
                        onItemSelected = { selectedYear = it },
                        suffix = "년"
                    )
                    
                    // 월
                    DatePickerWheel(
                        items = (1..12).toList(),
                        selectedItem = selectedMonth,
                        onItemSelected = { selectedMonth = it },
                        suffix = "월"
                    )
                    
                    // 일 (연도/월에 따른 일수 계산)
                    val daysInMonth = java.time.YearMonth.of(selectedYear, selectedMonth).lengthOfMonth()
                    LaunchedEffect(daysInMonth) {
                        if (selectedDay > daysInMonth) {
                            selectedDay = daysInMonth
                        }
                    }
                    
                    DatePickerWheel(
                        items = (1..daysInMonth).toList(),
                        selectedItem = selectedDay,
                        onItemSelected = { selectedDay = it },
                        suffix = "일"
                    )
                }
            }
            
            // 다음 버튼
            IconButton(
                onClick = {
                    viewModel.saveOnboardingData()
                    onNext()
                },
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
