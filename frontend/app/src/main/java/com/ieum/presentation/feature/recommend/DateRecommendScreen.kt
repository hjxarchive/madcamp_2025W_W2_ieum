package com.ieum.presentation.feature.recommend

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ieum.presentation.theme.IeumColors
import java.text.NumberFormat
import java.util.*

/**
 * 데이트 코스 추천 화면
 * 실제 장소 데이터 기반 코스 추천 (예상 금액 포함)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRecommendScreen(
    modifier: Modifier = Modifier,
    viewModel: RecommendViewModel = hiltViewModel()
) {
    var selectedCategory by remember { mutableStateOf<DateCategory?>(null) }
    var selectedCourse by remember { mutableStateOf<DateCourse?>(null) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IeumColors.Background)
    ) {
        // 상단 헤더
        RecommendHeader()
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // 카테고리 섹션
            item {
                CategorySection(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
            }
            
            // 오늘의 추천 코스
            item {
                TodayRecommendSection(
                    onCourseClick = { selectedCourse = it }
                )
            }
            
            // 인기 데이트 코스
            item {
                PopularCoursesSection(
                    onCourseClick = { selectedCourse = it }
                )
            }
            
            // 분위기별 추천
            item {
                MoodSection()
            }
        }
    }
    
    // 코스 상세 바텀시트
    selectedCourse?.let { course ->
        CourseDetailSheet(
            course = course,
            onDismiss = { selectedCourse = null }
        )
    }
}

@Composable
private fun RecommendHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = IeumColors.Background,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "데이트 추천",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = IeumColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "오늘은 어떤 데이트를 할까요? 💕",
                style = MaterialTheme.typography.bodyMedium,
                color = IeumColors.TextSecondary
            )
        }
    }
}

/**
 * 카테고리 섹션
 */
@Composable
private fun CategorySection(
    selectedCategory: DateCategory?,
    onCategorySelected: (DateCategory?) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "카테고리",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = IeumColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(DateCategory.entries) { category ->
                CategoryCard(
                    category = category,
                    isSelected = selectedCategory == category,
                    onClick = {
                        onCategorySelected(
                            if (selectedCategory == category) null else category
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: DateCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) category.color else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = category.emoji,
                fontSize = 28.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = category.label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isSelected) Color.White else IeumColors.TextPrimary
            )
        }
    }
}

/**
 * 오늘의 추천 코스 섹션
 */
@Composable
private fun TodayRecommendSection(
    onCourseClick: (DateCourse) -> Unit
) {
    val todayCourse = sampleCourses.first()
    
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "오늘의 추천 코스",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = IeumColors.TextPrimary
            )
            
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = IeumColors.Primary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "✨ AI 추천",
                    style = MaterialTheme.typography.labelSmall,
                    color = IeumColors.Primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 메인 추천 카드
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCourseClick(todayCourse) },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column {
                // 썸네일 영역
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    todayCourse.category.color.copy(alpha = 0.6f),
                                    todayCourse.category.color
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = todayCourse.category.emoji,
                        fontSize = 64.sp
                    )
                    
                    // 공유 버튼
                    IconButton(
                        onClick = { /* 공유 */ },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "공유",
                            tint = Color.White
                        )
                    }
                }
                
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = todayCourse.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = IeumColors.TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = todayCourse.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = IeumColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 정보 행
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoChip(
                            icon = Icons.Outlined.AccessTime,
                            text = todayCourse.duration
                        )
                        InfoChip(
                            icon = Icons.Outlined.Place,
                            text = "${todayCourse.places.size}곳"
                        )
                        InfoChip(
                            icon = Icons.Outlined.AttachMoney,
                            text = "약 ${NumberFormat.getNumberInstance(Locale.KOREA).format(todayCourse.estimatedCost)}원",
                            highlight = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    text: String,
    highlight: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (highlight) IeumColors.Primary.copy(alpha = 0.1f)
                else Color.Transparent
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (highlight) IeumColors.Primary else IeumColors.TextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (highlight) IeumColors.Primary else IeumColors.TextSecondary
        )
    }
}

/**
 * 인기 코스 섹션
 */
@Composable
private fun PopularCoursesSection(
    onCourseClick: (DateCourse) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "인기 데이트 코스",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = IeumColors.TextPrimary
            )
            
            TextButton(onClick = { }) {
                Text(
                    text = "전체보기",
                    color = IeumColors.Primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        sampleCourses.drop(1).forEach { course ->
            CourseCard(
                course = course,
                onClick = { onCourseClick(course) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun CourseCard(
    course: DateCourse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 썸네일
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(course.category.color.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = course.category.emoji,
                    fontSize = 32.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = IeumColors.TextPrimary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // 카테고리 태그
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = course.category.color.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = course.category.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = course.category.color,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = course.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = IeumColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = IeumColors.TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = course.duration,
                        style = MaterialTheme.typography.labelSmall,
                        color = IeumColors.TextSecondary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = "💰 약 ${NumberFormat.getNumberInstance(Locale.KOREA).format(course.estimatedCost)}원",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = IeumColors.Primary
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = IeumColors.TextSecondary.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * 분위기별 추천 섹션
 */
@Composable
private fun MoodSection() {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "분위기별 추천",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = IeumColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MoodCard(
                emoji = "🌙",
                title = "로맨틱한 밤",
                color = IeumColors.Secondary,
                modifier = Modifier.weight(1f)
            )
            MoodCard(
                emoji = "☀️",
                title = "활기찬 낮",
                color = IeumColors.Warning,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MoodCard(
                emoji = "🏠",
                title = "집 데이트",
                color = IeumColors.Accent,
                modifier = Modifier.weight(1f)
            )
            MoodCard(
                emoji = "🎨",
                title = "문화생활",
                color = IeumColors.CategoryCulture,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MoodCard(
    emoji: String,
    title: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                fontSize = 24.sp
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = IeumColors.TextPrimary
            )
        }
    }
}

/**
 * 코스 상세 바텀시트
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseDetailSheet(
    course: DateCourse,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = IeumColors.TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = course.category.color.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${course.category.emoji} ${course.category.label}",
                                style = MaterialTheme.typography.labelMedium,
                                color = course.category.color,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = course.duration,
                            style = MaterialTheme.typography.labelMedium,
                            color = IeumColors.TextSecondary
                        )
                    }
                }
                
                // 예상 금액
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "예상 금액",
                        style = MaterialTheme.typography.labelSmall,
                        color = IeumColors.TextSecondary
                    )
                    Text(
                        text = "${numberFormat.format(course.estimatedCost)}원",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = IeumColors.Primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 코스 순서
            Text(
                text = "코스 순서",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = IeumColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            course.places.forEachIndexed { index, place ->
                PlaceItem(
                    index = index + 1,
                    place = place,
                    isLast = index == course.places.lastIndex
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* 공유 */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Share, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("친구에게 공유")
                }
                
                Button(
                    onClick = { /* 일정에 추가 */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IeumColors.Primary
                    )
                ) {
                    Icon(Icons.Outlined.CalendarMonth, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("일정 추가")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PlaceItem(
    index: Int,
    place: CoursePlace,
    isLast: Boolean
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
    
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 타임라인
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(IeumColors.Primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = index.toString(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
            
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(80.dp)
                        .background(IeumColors.Primary.copy(alpha = 0.3f))
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // 장소 정보
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF8F8F8)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = place.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = IeumColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = place.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = IeumColors.TextSecondary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Place,
                        contentDescription = null,
                        tint = IeumColors.TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = place.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = IeumColors.TextSecondary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⏱️ ${place.duration}",
                        style = MaterialTheme.typography.labelSmall,
                        color = IeumColors.TextSecondary
                    )
                    
                    Text(
                        text = "💰 약 ${numberFormat.format(place.estimatedCost)}원",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = IeumColors.Primary
                    )
                }
            }
        }
    }
}

// 카테고리
enum class DateCategory(
    val label: String,
    val emoji: String,
    val color: Color
) {
    FOOD("맛집", "🍽️", IeumColors.CategoryFood),
    CAFE("카페", "☕", IeumColors.CategoryCafe),
    DRINK("술", "🍻", IeumColors.CategoryDrink),
    CULTURE("문화생활", "🎭", IeumColors.CategoryCulture),
    TRAVEL("여행", "✈️", IeumColors.CategoryTravel),
    GAME("게임", "🎮", IeumColors.CategoryGame)
}

// 데이터 클래스 (UI용 - ViewModel 적용 후 domain 모델로 대체 예정)
data class DateCourse(
    val id: Int,
    val title: String,
    val description: String,
    val category: DateCategory,
    val duration: String,
    val estimatedCost: Int,
    val places: List<CoursePlace>
)

data class CoursePlace(
    val id: Int,
    val name: String,
    val category: String,
    val address: String,
    val duration: String,
    val estimatedCost: Int
)

// 샘플 데이터
private val sampleCourses = listOf(
    DateCourse(
        id = 1,
        title = "성수동 감성 카페 투어",
        description = "인스타 감성 넘치는 성수동 핫플레이스 코스! 사진 찍기 좋은 곳들로 구성했어요.",
        category = DateCategory.CAFE,
        duration = "약 4시간",
        estimatedCost = 45000,
        places = listOf(
            CoursePlace(1, "어니언 성수", "베이커리카페", "서울 성동구 아차산로9길 8", "1시간", 15000),
            CoursePlace(2, "대림창고", "복합문화공간", "서울 성동구 성수이로 78", "1시간 30분", 10000),
            CoursePlace(3, "카페 할아버지공장", "카페", "서울 성동구 연무장5길 7", "1시간 30분", 20000)
        )
    ),
    DateCourse(
        id = 2,
        title = "홍대 맛집 탐방",
        description = "홍대 핫한 맛집들을 돌아보는 미식 코스",
        category = DateCategory.FOOD,
        duration = "약 3시간",
        estimatedCost = 55000,
        places = listOf(
            CoursePlace(1, "연남동 파스타집", "이탈리안", "서울 마포구 연남로 23", "1시간 30분", 35000),
            CoursePlace(2, "밀크티 전문점", "디저트", "서울 마포구 와우산로 35", "1시간", 12000),
            CoursePlace(3, "수제 아이스크림", "디저트", "서울 마포구 어울마당로 42", "30분", 8000)
        )
    ),
    DateCourse(
        id = 3,
        title = "한강 피크닉 데이트",
        description = "여의도 한강공원에서 즐기는 로맨틱 피크닉",
        category = DateCategory.TRAVEL,
        duration = "약 5시간",
        estimatedCost = 30000,
        places = listOf(
            CoursePlace(1, "편의점 장보기", "마트", "여의도 한강공원 입구", "30분", 20000),
            CoursePlace(2, "한강공원 피크닉", "야외", "서울 영등포구 여의동로 330", "3시간", 0),
            CoursePlace(3, "치맥 배달", "치킨", "한강공원 내", "1시간 30분", 10000)
        )
    ),
    DateCourse(
        id = 4,
        title = "이태원 와인바 투어",
        description = "분위기 좋은 이태원 와인바에서 로맨틱한 밤을",
        category = DateCategory.DRINK,
        duration = "약 4시간",
        estimatedCost = 80000,
        places = listOf(
            CoursePlace(1, "와인앤모어", "와인바", "서울 용산구 이태원로 200", "2시간", 50000),
            CoursePlace(2, "루프탑바", "바", "서울 용산구 이태원로 210", "2시간", 30000)
        )
    )
)
