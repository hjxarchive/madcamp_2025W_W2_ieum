package com.ieum.presentation.feature.test

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import android.util.Log
import androidx.hilt.navigation.compose.hiltViewModel
import com.ieum.R
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll

// 질문 데이터 모델
data class Question(
    val category: String,
    val text: String,
    val leftType: Char,
    val rightType: Char
)

// 질문 리스트
val allQuestions = listOf(
    // 1. 데이트 계획 (Planner vs Flow) -> 오른쪽(O)이면 P, 왼쪽(X)이면 F
    Question("데이트 계획", "우리는 데이트 전에 계획이 잡혀 있어야 마음이 편하다.", 'F', 'P'),
    Question("데이트 계획", "데이트 당일, 즉흥적으로 결정해도 괜찮다.", 'P', 'F'),
    Question("데이트 계획", "그날 기분에 따라 데이트 코스를 유동적으로 변경한다.", 'P', 'F'),
    Question("데이트 계획", "데이트 전에 동선과 시간을 정해두는 편이다.", 'F', 'P'),
    Question("데이트 계획", "계획 없는 데이트도 충분히 즐길 수 있다.", 'P', 'F'),
    Question("데이트 계획", "데이트 장소는 미리 정해두는 게 좋다.", 'F', 'P'),
    Question("데이트 계획", "걷다가 마음에 드는 곳에 들어가는 게 좋다.", 'P', 'F'),
    Question("데이트 계획", "당일에 갑자기 바뀌는 일정도 재미있다.", 'P', 'F'),
    Question("데이트 계획", "계획한 대로 진행되는 데이트가 만족스럽다.", 'F', 'P'),

    // 2. 소비 성향 (Measured vs Indulgent) -> 오른쪽(O)이면 M, 왼쪽(X)이면 I
    Question("소비 성향", "데이트에서는 돈보다 경험이 더 중요하다.", 'M', 'I'),
    Question("소비 성향", "데이트 비용은 합리적인 선에서 쓰고 싶다.", 'I', 'M'),
    Question("소비 성향", "특별한 날이라면 비용이 커도 괜찮다.", 'M', 'I'),
    Question("소비 성향", "추억이 남는다면 지출이 아깝지 않다.", 'M', 'I'),
    Question("소비 성향", "데이트 비용의 상한을 미리 정해두는 게 좋다.", 'I', 'M'),
    Question("소비 성향", "계획보다 지출이 커지면 신경 쓰인다.", 'I', 'M'),
    Question("소비 성향", "좋은 경험을 위해 예산을 넘길 수 있다.", 'M', 'I'),
    Question("소비 성향", "데이트 비용이 부담되면 즐기기 어렵다.", 'I', 'M'),
    Question("소비 성향", "데이트 비용을 기록하는 게 필요하다.", 'I', 'M'),

    // 3. 갈등 성향 (Direct vs Thoughtful) -> 오른쪽(O)이면 D, 왼쪽(X)이면 T
    Question("갈등 성향", "감정이 가라앉을 시간이 필요하다.", 'D', 'T'),
    Question("갈등 성향", "문제가 생기면 그날 해결하고 싶다.", 'T', 'D'),
    Question("갈등 성향", "혼자 생각한 뒤 얘기하는 게 편하다.", 'D', 'T'),
    Question("갈등 성향", "감정 정리가 되지 않으면 말하기 어렵다.", 'D', 'T'),
    Question("갈등 성향", "서로의 생각을 바로 확인하고 싶다.", 'T', 'D'),
    Question("갈등 성향", "혼자 정리할 시간을 존중받고 싶다.", 'D', 'T'),
    Question("갈등 성향", "문제를 미루는 게 불안하다.", 'T', 'D'),
    Question("갈등 성향", "감정이 정리된 후 대화가 더 잘 된다.", 'D', 'T'),
    Question("갈등 성향", "싸운 채로 하루를 넘기기 싫다.", 'T', 'D'),

    // 4. 도전 성향 (Explorer vs Comfort) -> 오른쪽(O)이면 E, 왼쪽(X)이면 C
    Question("도전 성향", "새로운 장소를 가는 데 설렘을 느낀다.", 'C', 'E'),
    Question("도전 성향", "익숙한 장소가 가장 편하다.", 'E', 'C'),
    Question("도전 성향", "단골 데이트가 안정적이다.", 'E', 'C'),
    Question("도전 성향", "신상 맛집이나 공간에 관심이 많다.", 'C', 'E'),
    Question("도전 성향", "익숙한 루틴이 더 좋다.", 'E', 'C'),
    Question("도전 성향", "처음 해보는 활동도 도전해보고 싶다.", 'C', 'E'),
    Question("도전 성향", "여행지에서도 새로운 곳을 찾는 편이다.", 'C', 'E'),
    Question("도전 성향", "새로운 시도가 재미있다.", 'C', 'E'),
    Question("도전 성향", "실패하더라도 새로운 걸 해보고 싶다.", 'C', 'E')
)

@Composable
fun TestMainScreen(
    viewModel: TestViewModel = hiltViewModel(),
    onTestFinished: () -> Unit = {}
) {
    val screenState by viewModel.currentScreen.collectAsState()
    val mainBrown = Color(0xFF5A3E2B)
    val buttonBeige = Color(0xFFE6C8A0)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background2),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        when (val state = screenState) {
            is TestScreenState.Intro -> IntroContent(mainBrown, buttonBeige) { viewModel.startTest() }
            is TestScreenState.Testing -> TestingContent(viewModel, mainBrown, buttonBeige)
            is TestScreenState.Result -> ResultContent(state.mbti, state.scores, mainBrown, buttonBeige) {
                onTestFinished()
            }
        }
    }
}

@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color,
    fontSize: TextUnit,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign = TextAlign.Center,
    lineHeight: TextUnit = TextUnit.Unspecified,
    delayMillis: Long = 80L,
    onFinished: () -> Unit = {}
) {
    var visibleText by remember { mutableStateOf("") }

    LaunchedEffect(text) {
        visibleText = ""
        text.forEach { char ->
            visibleText += char
            delay(delayMillis)
        }
        onFinished()
    }

    Text(
        text = visibleText,
        modifier = modifier,
        color = textColor,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
        lineHeight = lineHeight
    )
}

@Composable
fun IntroContent(textColor: Color, btnColor: Color, onStart: () -> Unit) {
    var startSecondText by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(160.dp))

        TypewriterText(
            text = "서로의 일상이 이어진 이음 속에서,\n우리는 어떤 방식으로 사랑하고\n있을까요?",
            textColor = textColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 28.sp,
            delayMillis = 70L,
            onFinished = { startSecondText = true }
        )

        Spacer(modifier = Modifier.height(160.dp))

        if (startSecondText) {
            TypewriterText(
                text = "몇 가지 질문으로,\n우리에게 잘 맞는 연애의 모습을 찾아볼게요.",
                textColor = textColor.copy(alpha = 0.8f),
                fontSize = 15.sp,
                lineHeight = 22.sp,
                delayMillis = 50L,
                onFinished = { showButton = true }
            )
        }

        Spacer(modifier = Modifier.height(200.dp))

        // ✅ 애니메이션 소괄호와 중괄호 위치 수정됨
        AnimatedVisibility(
            visible = showButton,
            enter = fadeIn(animationSpec = tween(1000))
        ) {
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = btnColor,
                    contentColor = textColor
                ),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.fillMaxWidth(0.8f).height(56.dp)
            ) {
                Text("우리 이야기 시작하기", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TestingContent(viewModel: TestViewModel, textColor: Color, btnColor: Color) {
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val shuffledQuestions by viewModel.shuffledQuestions.collectAsState()
    val question = if (shuffledQuestions.isNotEmpty()) shuffledQuestions[currentIndex] else null
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(targetValue = offsetX)

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // 1. 프로그레스 바
        LinearProgressIndicator(
            progress = (currentIndex + 1) / 36f,
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = btnColor,
            trackColor = Color.White.copy(alpha = 0.3f)
        )

        // 2. 카드 상단 여백
        Spacer(modifier = Modifier.height(70.dp))

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.TopCenter
        ) {
            question?.let { q ->
                // 3. 카드 전체 컨테이너
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp) // 높이 유지
                        .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                        .graphicsLayer { rotationZ = animatedOffsetX * 0.05f }
                        .pointerInput(currentIndex) {
                            detectDragGestures(
                                onDragEnd = {
                                    if (offsetX > 300) viewModel.submitAnswer(q.rightType)
                                    else if (offsetX < -300) viewModel.submitAnswer(q.leftType)
                                    offsetX = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    offsetX += dragAmount.x
                                }
                            )
                        }
                ) {
                    // 배경 이미지
                    Image(
                        painter = painterResource(id = R.drawable.background),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(32.dp)),
                        contentScale = ContentScale.FillBounds,
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize() // 부모(카드) 크기에 맞춤
                            .clip(RoundedCornerShape(32.dp)) // 카드 모서리 둥글게 깎기
                            .background(Color.Black.copy(alpha = 0.1f))
                    )

                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 질문 번호와 뒤로가기 버튼을 같은 라인에 배치
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 뒤로가기 버튼 (왼쪽)
                            if (currentIndex > 0) {
                                IconButton(
                                    onClick = { viewModel.goToPreviousQuestion() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "이전 질문",
                                        tint = textColor.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                // 첫 번째 질문일 때 공간 유지
                                Spacer(modifier = Modifier.size(24.dp))
                            }
                            
                            // 질문 번호 (중앙)
                            Text(
                                text = "${currentIndex + 1} / 36",
                                color = textColor.copy(alpha = 0.6f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            // 오른쪽 공간 유지 (대칭)
                            Spacer(modifier = Modifier.size(24.dp))
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 카테고리 태그 (데이트 계획 등)
                        Surface(color = btnColor, shape = RoundedCornerShape(20.dp)) {
                            Text(
                                text = q.category,
                                color = textColor,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontSize = 14.sp
                            )
                        }

                        // --- 질문 텍스트 영역 (중앙 배치) ---
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = q.text,
                                color = textColor,
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 34.sp
                            )
                        }

                        // --- 하단 X / O 가이드 (질문 아래 고정) ---
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // X 버튼 (왼쪽)
                            IconButton(
                                onClick = { 
                                    viewModel.submitAnswer(q.leftType)
                                    offsetX = 0f
                                },
                                modifier = Modifier.size(60.dp)
                            ) {
                                Text("X", color = textColor, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                            }

                            // O 버튼 (오른쪽)
                            IconButton(
                                onClick = { 
                                    viewModel.submitAnswer(q.rightType)
                                    offsetX = 0f
                                },
                                modifier = Modifier.size(60.dp)
                            ) {
                                Text("O", color = textColor, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }

            // 가장 하단 안내 텍스트
            Text(
                text = "좌우로 스와이프하여 선택하세요",
                color = Color(0xFF5A3E2B).copy(alpha = 0.8f),
                fontSize = 17.sp,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)
            )
        }
    }
}

@Composable
fun ResultContent(
    mbti: String,
    scores: Map<Char, Int>,
    textColor: Color,
    btnColor: Color,
    onNavigateMain: () -> Unit
) {
    // 1. MBTI별 결과 데이터 매칭 (이미지 리소스 포함)
    val resultData = when (mbti) {
        "MDEP" -> ResultInfo("똑똑한 꼬마 탐험가", "알뜰살뜰 모은 돈으로 새로운 곳을 정복하고, 서운함은 바로 풀고 다시 웃는 씩씩이들!", R.drawable.mdep_r)
        "MDEF" -> ResultInfo("실속 만점 동네 대장", "가성비 넘치는 새로운 맛집을 찾아 떠나며, 즉흥적인 데이트 속에서도 시원시원하게 소통해요.", R.drawable.mdef_r)
        "MDCP" -> ResultInfo("든든한 울타리 설계자", "우리만의 익숙한 아지트와 예산을 딱 지키며, 서로 오해 없게 바로바로 말해주는 듬직이들!", R.drawable.mdcp_r)
        "MDCF" -> ResultInfo("포근한 소풍 메이커", "\"어디든 좋아!\" 하며 익숙한 곳에서 힐링하고, 지갑도 마음도 편안하게 유지하는 커플이에요.", R.drawable.mdcf_r)
        "MTEP" -> ResultInfo("수줍은 꿈의 기획자", "새로운 도전을 꼼꼼히 준비하지만, 싸우면 잠시 숨을 고르고 예쁜 말만 골라 전해주는 섬세함!", R.drawable.mtep_r)
        "MTEF" -> ResultInfo("말랑말랑 여행 작가", "발길 닿는 대로 새로운 세상을 구경하되, 서로의 마음이 다치지 않게 천천히 다가가는 다정함.", R.drawable.mtef_r)
        "MTCP" -> ResultInfo("포근포근 안심 지킴이", "계획된 루틴과 익숙한 장소에서 평화를 느끼며, 갈등 앞에서도 차분하게 서로를 기다려줘요.", R.drawable.mtcp_r)
        "MTCF" -> ResultInfo("소박한 행복 수집가", "큰 지출 없이도 익숙한 골목길 데이트를 사랑하며, 속 깊은 대화로 서로를 따뜻하게 안아줘요.", R.drawable.mtcf_r)
        "IDEP" -> ResultInfo("눈부신 이벤트 왕자/공주", "아낌없는 지출과 완벽한 계획으로 매번 역대급 데이트를 만들고, 열정적으로 사랑을 표현해요!", R.drawable.idep_r)
        "IDEF" -> ResultInfo("통통 튀는 낭만 방랑자", "\"오늘 완전 즐겁게 놀자!\" 하며 새로운 것에 팍팍 투자하고, 솔직한 감정을 마구마구 공유해요.", R.drawable.idef_r)
        "IDCP" -> ResultInfo("스윗한 홈 데이트 장인", "우리가 좋아하는 익숙한 장소를 위해 정성을 쏟고 투자하며, 분명한 표현으로 신뢰를 쌓아요.", R.drawable.idcp_r)
        "IDCF" -> ResultInfo("꿀 떨어지는 자유 영혼", "기분 내고 싶은 날엔 화끈하게! 즉흥적인 데이트를 즐기며 서로의 기분을 바로바로 챙겨줘요.", R.drawable.idcf_r)
        "ITEP" -> ResultInfo("사색하는 낭만 탐험가", "특별한 경험을 위해 정성껏 투자하고 준비하지만, 싸우면 마음을 정리할 시간이 필요해요.", R.drawable.itep_r)
        "ITEF" -> ResultInfo("포실포실 감성 여행자", "과감하게 새로운 자극을 찾아 떠나는 모험가이면서도, 서로의 속마음은 아주 조심스럽게 살펴요.", R.drawable.itef_r)
        "ITCP" -> ResultInfo("단단한 설렘의 건축가", "우리 관계에 듬뿍 투자하며 안정적인 계획을 세우고, 늘 신중하고 성숙한 태도로 사랑을 지켜요.", R.drawable.itcp_r)
        "ITCF" -> ResultInfo("구름 위의 솜사탕 커플", "돈보다 소중한 추억을 위해 아낌없이 쓰며, 편안한 곳에서 서로를 배려하며 느긋하게 즐겨요.", R.drawable.itcf_r)
        else -> ResultInfo("신비로운 커플", "우리만의 독특한 사랑의 방식을 가진 커플입니다.", R.drawable.background2) // 기본 이미지
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // 2. 캐릭터 이미지 표시 (시안 상단 영역)
        Image(
            painter = painterResource(id = resultData.imageRes),
            contentDescription = null,
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = mbti,
            color = Color(0xFF5A3E2B),
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )

        // 3. 별명 (MBTI 아래 배치)
        Text(
            text = resultData.title,
            color = Color(0xFF5A3E2B),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. 캐릭터 상세 설명
        Text(
            text = resultData.description,
            color = Color(0xFF5A3E2B),
            fontSize = 16.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 10.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        // 5. 성향 수치 게이지 (시안 하단 영역)
        // 각 카테고리별로 9문제 중 해당 성향이 몇 개인지 수치화
        GaugeBar("소비 성향", scores['M'] ?: 0, scores['I'] ?: 0, "M", "I", btnColor)
        GaugeBar("갈등 해결", scores['D'] ?: 0, scores['T'] ?: 0, "D", "T", btnColor)
        GaugeBar("도전 성향", scores['E'] ?: 0, scores['C'] ?: 0, "E", "C", btnColor)
        GaugeBar("데이트 계획", scores['P'] ?: 0, scores['F'] ?: 0, "P", "F", btnColor)

        Spacer(modifier = Modifier.height(40.dp))

        // 메인 이동 버튼
        Button(
            onClick = onNavigateMain,
            colors = ButtonDefaults.buttonColors(containerColor = btnColor, contentColor = textColor),
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier.fillMaxWidth().height(40.dp)
        ) {
            Text("다음 화면", fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// 성향 수치화를 위한 게이지 바 컴포저블
@Composable
fun GaugeBar(label: String, s1: Int, s2: Int, t1: String, t2: String, color: Color) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$t1 $s1", color = Color(0xFF5A3E2B), fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(label, color = Color(0xFF5A3E2B).copy(alpha = 0.6f), fontSize = 15.sp)
            Text("$s2 $t2", color = Color(0xFF5A3E2B), fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 게이지 바 - 양쪽 성향 모두 표시
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
        ) {
            // 왼쪽 성향 (s1) 바
            Box(
                modifier = Modifier
                    .weight(s1.coerceAtLeast(1).toFloat())
                    .fillMaxHeight()
                    .background(Color(0xFFE6C8A0))
            )
            // 오른쪽 성향 (s2) 바
            Box(
                modifier = Modifier
                    .weight(s2.coerceAtLeast(1).toFloat())
                    .fillMaxHeight()
                    .background(Color(0xFFD4A574))
            )
        }
    }
}

// 결과 데이터 모델 클래스
data class ResultInfo(val title: String, val description: String, val imageRes: Int)