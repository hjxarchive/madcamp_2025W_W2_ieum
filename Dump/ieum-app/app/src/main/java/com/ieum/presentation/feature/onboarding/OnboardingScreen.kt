package com.ieum.presentation.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ieum.presentation.theme.IeumColors

/**
 * 온보딩 화면 - MBTI/취향 테스트 시작
 * PDF 디자인 기반: "우리 이야기 시작하기" 화면
 */
@Composable
fun OnboardingScreen(
    onStartClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 배경 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(IeumColors.Background)
    ) {
        // 배경 그라데이션 원형 블러 효과
        BackgroundBubbles(animatedOffset)
        
        // 메인 콘텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.3f))
            
            // 로고 영역
            InfinityLogo()
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // 앱 이름
            Text(
                text = "이음",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = IeumColors.Primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 설명 텍스트
            Text(
                text = "서로의 일상이 이어진 이음 속에서,\n우리는 어떤 방식으로 사랑하고\n있을까요?",
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 28.sp
                ),
                color = IeumColors.TextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "몇 가지 질문으로,\n우리에게 잘 맞는 연애의 모습을 찾아볼게요.",
                style = MaterialTheme.typography.bodyMedium,
                color = IeumColors.TextSecondary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.weight(0.4f))
            
            // 시작 버튼
            StartButton(onClick = onStartClick)
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun BackgroundBubbles(animatedOffset: Float) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 핑크 버블
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(
                    x = (-50 + animatedOffset * 30).dp,
                    y = (100 + animatedOffset * 50).dp
                )
                .blur(80.dp)
                .clip(CircleShape)
                .background(IeumColors.Primary.copy(alpha = 0.3f))
        )
        
        // 퍼플 버블
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.TopEnd)
                .offset(
                    x = (50 - animatedOffset * 20).dp,
                    y = (200 - animatedOffset * 30).dp
                )
                .blur(70.dp)
                .clip(CircleShape)
                .background(IeumColors.Secondary.copy(alpha = 0.25f))
        )
        
        // 민트 버블
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomStart)
                .offset(
                    x = (30 + animatedOffset * 40).dp,
                    y = (-100 + animatedOffset * 20).dp
                )
                .blur(60.dp)
                .clip(CircleShape)
                .background(IeumColors.Accent.copy(alpha = 0.2f))
        )
    }
}

@Composable
private fun InfinityLogo() {
    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Canvas(
        modifier = Modifier.size(120.dp)
    ) {
        val gradient = Brush.linearGradient(
            colors = listOf(
                IeumColors.Primary,
                IeumColors.Secondary,
                IeumColors.Accent,
                IeumColors.Primary
            )
        )
        
        // 무한대 심볼 (∞) 그리기
        drawCircle(
            brush = gradient,
            radius = size.minDimension / 4,
            center = Offset(size.width * 0.3f, size.height / 2)
        )
        drawCircle(
            brush = gradient,
            radius = size.minDimension / 4,
            center = Offset(size.width * 0.7f, size.height / 2)
        )
        drawCircle(
            color = IeumColors.Background,
            radius = size.minDimension / 6,
            center = Offset(size.width * 0.3f, size.height / 2)
        )
        drawCircle(
            color = IeumColors.Background,
            radius = size.minDimension / 6,
            center = Offset(size.width * 0.7f, size.height / 2)
        )
    }
}

@Composable
private fun StartButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = IeumColors.Primary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        Text(
            text = "우리 이야기 시작하기",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = Color.White
        )
    }
}

/**
 * MBTI 테스트 화면
 */
@Composable
fun MBTITestScreen(
    currentQuestion: Int,
    totalQuestions: Int,
    question: String,
    options: List<String>,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IeumColors.Background)
            .padding(24.dp)
    ) {
        // 진행률 표시
        LinearProgressIndicator(
            progress = { currentQuestion.toFloat() / totalQuestions },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = IeumColors.Primary,
            trackColor = IeumColors.Primary.copy(alpha = 0.2f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "$currentQuestion / $totalQuestions",
            style = MaterialTheme.typography.labelMedium,
            color = IeumColors.TextSecondary
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // 질문
        Text(
            text = question,
            style = MaterialTheme.typography.headlineMedium,
            color = IeumColors.TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // 선택지
        options.forEachIndexed { index, option ->
            OptionCard(
                text = option,
                onClick = { onOptionSelected(index) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OptionCard(
    text: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = IeumColors.TextPrimary,
            modifier = Modifier.padding(20.dp),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 테스트 결과 화면
 */
@Composable
fun TestResultScreen(
    coupleType: String,
    description: String,
    compatibility: Int,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        IeumColors.Primary.copy(alpha = 0.1f),
                        IeumColors.Background
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "우리의 연애 스타일은",
            style = MaterialTheme.typography.titleMedium,
            color = IeumColors.TextSecondary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 결과 타입
        Text(
            text = coupleType,
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = IeumColors.Primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 궁합도
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "궁합도",
                    style = MaterialTheme.typography.titleSmall,
                    color = IeumColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "$compatibility%",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = IeumColors.Primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LinearProgressIndicator(
                    progress = { compatibility / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = IeumColors.Primary,
                    trackColor = IeumColors.Primary.copy(alpha = 0.2f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 설명
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = IeumColors.TextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 확인 버튼
        Button(
            onClick = onConfirmClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = IeumColors.Primary
            )
        ) {
            Text(
                text = "결과 확인하기",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
