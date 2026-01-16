package com.ieum.presentation.feature.calendar

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ieum.presentation.theme.IeumColors
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

/**
 * 캘린더 화면
 * PDF 기반: 2026.02 형식, 일정 표시
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(YearMonth.of(2026, 2)) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IeumColors.Background)
    ) {
        // 상단 헤더
        CalendarHeader(
            currentMonth = currentMonth,
            onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
            onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
        )
        
        // 캘린더 본체
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            // 요일 헤더
            WeekDayHeader()
            
            // 날짜 그리드
            CalendarGrid(
                yearMonth = currentMonth,
                selectedDate = selectedDate,
                schedules = sampleSchedules,
                onDateSelected = { selectedDate = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 디데이 섹션
            DDaySection()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 선택된 날짜의 일정
            selectedDate?.let { date ->
                DayScheduleSection(
                    date = date,
                    schedules = sampleSchedules.filter { it.date == date }
                )
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
        
        // FAB
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { /* 일정 추가 */ },
                containerColor = IeumColors.Primary,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "일정 추가"
                )
            }
        }
    }
}

@Composable
private fun CalendarHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = IeumColors.Background,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "이전 달",
                    tint = IeumColors.TextPrimary
                )
            }
            
            Text(
                text = "${currentMonth.year}.${String.format("%02d", currentMonth.monthValue)}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = IeumColors.TextPrimary
            )
            
            IconButton(onClick = onNextMonth) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "다음 달",
                    tint = IeumColors.TextPrimary
                )
            }
        }
    }
}

@Composable
private fun WeekDayHeader() {
    val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
    val dayColors = listOf(
        IeumColors.Error, // 일요일
        IeumColors.TextPrimary,
        IeumColors.TextPrimary,
        IeumColors.TextPrimary,
        IeumColors.TextPrimary,
        IeumColors.TextPrimary,
        Color(0xFF2196F3) // 토요일
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        daysOfWeek.forEachIndexed { index, day ->
            Text(
                text = day,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = dayColors[index],
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    schedules: List<Schedule>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 일요일 = 0
    val daysInMonth = yearMonth.lengthOfMonth()
    
    val totalCells = firstDayOfWeek + daysInMonth
    val rows = (totalCells + 6) / 7
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        repeat(rows) { row ->
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(7) { col ->
                    val dayIndex = row * 7 + col - firstDayOfWeek + 1
                    
                    if (dayIndex in 1..daysInMonth) {
                        val date = yearMonth.atDay(dayIndex)
                        val hasSchedule = schedules.any { it.date == date }
                        val isSelected = date == selectedDate
                        val isToday = date == LocalDate.now()
                        
                        CalendarDay(
                            day = dayIndex,
                            isSelected = isSelected,
                            isToday = isToday,
                            hasSchedule = hasSchedule,
                            isSunday = col == 0,
                            isSaturday = col == 6,
                            scheduleColor = schedules.find { it.date == date }?.color,
                            onClick = { onDateSelected(date) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasSchedule: Boolean,
    isSunday: Boolean,
    isSaturday: Boolean,
    scheduleColor: Color?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = when {
        isSelected -> Color.White
        isSunday -> IeumColors.Error
        isSaturday -> Color(0xFF2196F3)
        else -> IeumColors.TextPrimary
    }
    
    val backgroundColor = when {
        isSelected -> IeumColors.Primary
        isToday -> IeumColors.Primary.copy(alpha = 0.1f)
        else -> Color.Transparent
    }
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = textColor
            )
            
            if (hasSchedule && !isSelected) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(scheduleColor ?: IeumColors.Primary)
                )
            }
        }
    }
}

/**
 * 디데이 섹션
 */
@Composable
private fun DDaySection() {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "기념일",
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
            DDayCard(
                emoji = "💕",
                title = "우리가 만난 날",
                dDay = "D+365",
                color = IeumColors.Primary,
                modifier = Modifier.weight(1f)
            )
            
            DDayCard(
                emoji = "🎂",
                title = "지민이 생일",
                dDay = "D-42",
                color = IeumColors.Secondary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DDayCard(
    emoji: String,
    title: String,
    dDay: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                fontSize = 28.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = IeumColors.TextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = dDay,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
        }
    }
}

/**
 * 선택된 날짜의 일정 섹션
 */
@Composable
private fun DayScheduleSection(
    date: LocalDate,
    schedules: List<Schedule>
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "${date.monthValue}월 ${date.dayOfMonth}일 일정",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = IeumColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (schedules.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "일정이 없습니다",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IeumColors.TextSecondary
                    )
                }
            }
        } else {
            schedules.forEach { schedule ->
                ScheduleItem(schedule = schedule)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ScheduleItem(schedule: Schedule) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            // 색상 인디케이터
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(schedule.color)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = schedule.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = IeumColors.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = schedule.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = IeumColors.TextSecondary
                )
            }
            
            // 공유 아이콘 (커플 일정인 경우)
            if (schedule.isShared) {
                Icon(
                    imageVector = Icons.Outlined.People,
                    contentDescription = "공유 일정",
                    tint = IeumColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// 샘플 데이터
data class Schedule(
    val id: Int,
    val title: String,
    val date: LocalDate,
    val time: String,
    val color: Color,
    val isShared: Boolean = false
)

private val sampleSchedules = listOf(
    Schedule(1, "데이트", LocalDate.of(2026, 2, 14), "18:00", IeumColors.Primary, true),
    Schedule(2, "영화 관람", LocalDate.of(2026, 2, 14), "20:00", IeumColors.Secondary, true),
    Schedule(3, "기념일 저녁", LocalDate.of(2026, 2, 20), "19:00", IeumColors.Accent, true),
    Schedule(4, "미용실", LocalDate.of(2026, 2, 8), "14:00", Color(0xFF9E9E9E), false),
    Schedule(5, "운동", LocalDate.of(2026, 2, 10), "07:00", Color(0xFF4CAF50), false)
)
