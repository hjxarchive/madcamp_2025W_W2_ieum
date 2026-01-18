package com.ieum.presentation.feature.calendar

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import com.ieum.domain.model.Expense
import com.ieum.presentation.theme.IeumColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import com.ieum.domain.model.Anniversary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBackClick: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showFabMenu by remember { mutableStateOf(false) }
    var activeSheetType by remember { mutableStateOf<String?>(null) }

    // 선택된 지출 상태 관리 (수정/삭제용)
    var selectedExpense by remember { mutableStateOf<Expense?>(null) }
    var itemToDelete by remember { mutableStateOf<com.ieum.domain.model.BucketItem?>(null) }
    var anniversaryToDelete by remember { mutableStateOf<Anniversary?>(null) }
    var selectedSchedule by remember { mutableStateOf<com.ieum.domain.model.Schedule?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = IeumColors.Background,
        topBar = {
            TopAppBar(
                title = { Text("캘린더", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IeumColors.Background)
            )
        },
        floatingActionButton = {
            MultiFabSection(
                isOpen = showFabMenu,
                onToggle = { showFabMenu = !showFabMenu },
                onOptionClick = { option ->
                    showFabMenu = false
                    selectedExpense = null // 추가 모드이므로 선택 데이터 비움
                    activeSheetType = option.replace(" 추가", "")
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(30.dp), // ✅ 전체 아이템 간 간격 30dp로 증가
            contentPadding = PaddingValues(bottom = 100.dp) // 하단 여백 추가
        ) {
            // 1. 달력 부분
            item {
                CalendarHeader(
                    currentMonth = uiState.currentMonth,
                    onPreviousMonth = { viewModel.navigateMonth(-1) },
                    onNextMonth = { viewModel.navigateMonth(1) }
                )
                Column(modifier = Modifier.padding(horizontal = 16.dp)) { // 좌우 패딩 증가
                    WeekDayHeader()
                    CalendarGrid(
                        yearMonth = uiState.currentMonth,
                        selectedDate = uiState.selectedDate,
                        schedules = uiState.schedules,
                        onDateSelected = { viewModel.selectDate(it) }
                    )
                }
            }

            // 2. 기념일 섹션
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) { // 내부 간격 추가
                    SectionHeader("기념일")
                    HorizontalCardRow(items = uiState.anniversaries, emptyText = "등록된 기념일이 없습니다.") { anniversary ->
                        DDayCard(emoji = "💕", title = anniversary.title, dDay = calculateDDay(anniversary.date), color = IeumColors.Primary, onLongClick = { anniversaryToDelete = anniversary })
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionHeader("우리의 버킷리스트")
                    HorizontalCardRow(items = uiState.bucketList, emptyText = "등록된 버킷리스트가 없습니다.") { bucket ->
                        BucketCard(
                            title = bucket.title,
                            isCompleted = bucket.isCompleted,
                            onClick = { viewModel.toggleBucketComplete(bucket.id) },
                            onLongClick = { itemToDelete = bucket }
                        )
                    }
                }
            }

            // 3. 선택된 날짜 일정 섹션
            item {
                Column { // 헤더와 내용 묶기
                    SectionHeader("우리의 일정")
                    if (uiState.selectedDateSchedules.isEmpty()) {
                        EmptyScheduleView("일정이 없습니다.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { // 일정 내 아이템 간격
                            uiState.selectedDateSchedules.forEach { schedule ->
                                ScheduleItem(
                                    schedule = schedule,
                                    onClick = {
                                        selectedSchedule = schedule
                                        activeSheetType = "일정"
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 4. 선택된 날짜 지출 섹션
            item {
                Column {
                    SectionHeader("${uiState.selectedDate.monthValue}월 ${uiState.selectedDate.dayOfMonth}일 지출")
                    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
                    val currentDayString = uiState.selectedDate.format(formatter)
                    val dayExpenses = uiState.expenses.filter { it.date == currentDayString }

                    if (dayExpenses.isEmpty()) {
                        EmptyScheduleView("지출 내역이 없습니다.")
                    } else {
                         Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                             dayExpenses.forEach { expense ->
                                 ExpenseCard(
                                     expense = expense,
                                     onClick = {
                                         selectedExpense = expense
                                         activeSheetType = "지출"
                                     }
                                 )
                             }
                         }
                    }
                }
            }
        }
    }

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("버킷리스트 삭제") },
            text = { Text("'${itemToDelete?.title}' 항목을 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToDelete?.let { viewModel.deleteBucketItem(it.id) }
                        itemToDelete = null
                    }
                ) {
                    Text("삭제", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("취소")
                }
            }
        )
    }

    if (anniversaryToDelete != null) {
        AlertDialog(
            onDismissRequest = { anniversaryToDelete = null },
            title = { Text("기념일 삭제", fontWeight = FontWeight.Bold) },
            text = { Text("'${anniversaryToDelete?.title}' 기념일을 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        anniversaryToDelete?.let { viewModel.deleteAnniversary(it) } // 기존 함수 활용
                        anniversaryToDelete = null
                    }
                ) {
                    Text("삭제", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { anniversaryToDelete = null }) {
                    Text("취소", color = Color.Gray)
                }
            }
        )
    }

    // 통합 바텀 시트 (추가/수정/삭제 대응)
    // CalendarScreen.kt 하단 바텀 시트 호출부
    activeSheetType?.let { type ->
        CommonAddBottomSheet(
            type = type,
            editingExpense = if (type == "지출") selectedExpense else null,
            onDismiss = {
                activeSheetType = null
                selectedExpense = null
                selectedSchedule = null
            },
            onConfirm = { title, date, memo ->
                when {
                    selectedSchedule != null -> {
                        // ✅ 일정 수정 로직 (Repository에 update가 있다면 호출)
                        viewModel.addSchedule(title, date, memo)
                    }
                    selectedExpense != null -> {
                        viewModel.addExpense(title, date, memo)
                    }
                    else -> { // 신규 추가 모드
                        when(type) {
                            "기념일" -> viewModel.addAnniversary(title, date)
                            "버킷리스트" -> viewModel.addBucketList(title)
                            "일정" -> viewModel.addSchedule(title, date, memo)
                            "지출" -> viewModel.addExpense(title, date, memo)
                        }
                    }
                }
                activeSheetType = null
                selectedSchedule = null
            },
            onDelete = {
                if (type == "일정") {
                    selectedSchedule?.let { /* viewModel.deleteSchedule(it.id) 구현 필요 */ }
                } else {
                    selectedExpense?.let { viewModel.deleteExpense(it.id) }
                }
                activeSheetType = null
                selectedSchedule = null
            }
        )
    }
}

@Composable
fun ExpenseCard(
    expense: com.ieum.domain.model.Expense,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() }, // 클릭 시 상세 창 띄움
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), // 배경색을 일정과 같은 흰색으로
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "💸 ${expense.category.name}",
                    color = Color(0xFF8D7B68),
                    fontSize = 12.sp
                )
                Text(
                    text = expense.title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
            Text(
                text = "${expense.amount}원",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE57373),
                fontSize = 16.sp
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BucketCard(
    title: String,
    isCompleted: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) Color(0xFFF5F5F5) else Color(0xFFE8F5E9)
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isCompleted) "✅" else "📌",
                fontSize = 20.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.Center,
                color = if (isCompleted) Color.Gray else Color(0xFF2E7D32)
            )
            Text(
                text = if (isCompleted) "완료" else "미완료",
                fontSize = 10.sp,
                color = if (isCompleted) Color.Gray else Color(0xFF4CAF50)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonAddBottomSheet(
    type: String,
    editingExpense: Expense? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, LocalDate, String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(editingExpense?.title ?: "") }
    var memo by remember { mutableStateOf(editingExpense?.amount?.toString() ?: "") }

    // 날짜 초기값 설정
    val initialDate = if (editingExpense != null) {
        try {
            LocalDate.parse(editingExpense.date, DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        } catch (e: Exception) { LocalDate.now() }
    } else LocalDate.now()

    var selectedDate by remember { mutableStateOf(initialDate) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
        },
        selectedDate.year,
        selectedDate.monthValue - 1,
        selectedDate.dayOfMonth
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFFFFFF9),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.LightGray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (editingExpense != null) "지출 내역 상세" else "$type 추가",
                    color = Color(0xFF8D7B68),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                // 수정 모드일 때만 삭제 버튼 표시
                if (editingExpense != null) {
                    IconButton(onClick = { onDelete?.invoke() }) {
                        Icon(Icons.Default.Delete, contentDescription = "삭제", tint = Color(0xFFE57373))
                    }
                }
            }

            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("제목을 입력하세요", color = Color(0xFFC1B4A5)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color(0xFF5A3E2B),
                    unfocusedTextColor = Color(0xFF5A3E2B)
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            HorizontalDivider(color = Color(0xFFF0E5D8))

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { datePickerDialog.show() }.padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFFA68A64), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(16.dp))
                Text(text = "${selectedDate.year}년 ${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일", color = Color(0xFF5A3E2B), fontSize = 16.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFFA68A64), modifier = Modifier.size(20.dp).padding(top = 4.dp))
                Spacer(Modifier.width(16.dp))
                TextField(
                    value = memo,
                    onValueChange = { memo = it },
                    placeholder = {
                        Text(text = if (type == "지출") "금액을 입력하세요" else "메모를 입력하세요", color = Color(0xFFC1B4A5))
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color(0xFF5A3E2B),
                        unfocusedTextColor = Color(0xFF5A3E2B)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(32.dp))

            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                FloatingActionButton(
                    onClick = { if(title.isNotBlank()) onConfirm(title, selectedDate, memo) },
                    containerColor = Color(0xFFECD4CD),
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp)
                ) {
                    Icon(if (editingExpense != null) Icons.Default.Edit else Icons.Default.Check, contentDescription = null)
                }
            }
        }
    }
}

// --- 아래는 기존과 동일한 컴포넌트들 ---

@Composable
fun MultiFabSection(isOpen: Boolean, onToggle: () -> Unit, onOptionClick: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.End) {
        if (isOpen) {
            val options = listOf(
                FabOption("기념일 추가", Icons.Default.Favorite),
                FabOption("버킷리스트 추가", Icons.AutoMirrored.Filled.List),
                FabOption("일정 추가", Icons.Default.Event),
                FabOption("지출 추가", Icons.Default.Payments)
            )
            options.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp).clickable { onOptionClick(option.label) }
                ) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color.White, shadowElevation = 4.dp) {
                        Text(option.label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    SmallFloatingActionButton(onClick = { onOptionClick(option.label) }, containerColor = Color.White) {
                        Icon(option.icon, contentDescription = null, tint = IeumColors.Primary)
                    }
                }
            }
        }
        FloatingActionButton(onClick = onToggle, containerColor = Color(0xFFECD4CD)) {
            Icon(if (isOpen) Icons.Default.Close else Icons.Default.Add, contentDescription = null, tint = Color.White)
        }
    }
}

data class FabOption(val label: String, val icon: ImageVector)

@Composable
fun SectionHeader(title: String) {
    Text(title, Modifier.padding(16.dp, 12.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
}

@Composable
fun <T> HorizontalCardRow(items: List<T>, emptyText: String, cardContent: @Composable (T) -> Unit) {
    if (items.isEmpty()) {
        Text(emptyText, Modifier.padding(16.dp, 8.dp), fontSize = 13.sp, color = Color.Gray)
    } else {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items.forEach { Box(Modifier.width(160.dp)) { cardContent(it) } }
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DDayCard(emoji: String, title: String, dDay: String, color: Color, onLongClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* 일반 클릭 시 동작이 없다면 비워둠 */ },
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 24.sp)
            Text(title, style = MaterialTheme.typography.labelMedium, maxLines = 1)
            Text(dDay, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun CalendarHeader(currentMonth: YearMonth, onPreviousMonth: () -> Unit, onNextMonth: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        IconButton(onClick = onPreviousMonth) { Icon(Icons.Default.ChevronLeft, null) }
        Text("${currentMonth.year}.${String.format("%02d", currentMonth.monthValue)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        IconButton(onClick = onNextMonth) { Icon(Icons.Default.ChevronRight, null) }
    }
}

@Composable
private fun WeekDayHeader() {
    val days = listOf("일", "월", "화", "수", "목", "금", "토")
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        days.forEachIndexed { i, d ->
            Text(text = d, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = if(i==0) IeumColors.Error else if(i==6) Color(0xFF2196F3) else IeumColors.TextPrimary)
        }
    }
}

@Composable
private fun CalendarGrid(yearMonth: YearMonth, selectedDate: LocalDate?, schedules: List<com.ieum.domain.model.Schedule>, onDateSelected: (LocalDate) -> Unit) {
    val firstDay = yearMonth.atDay(1)
    val firstDayOfWeek = firstDay.dayOfWeek.value % 7
    val rows = (firstDayOfWeek + yearMonth.lengthOfMonth() + 6) / 7
    Column {
        repeat(rows) { r ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { c ->
                    val day = r * 7 + c - firstDayOfWeek + 1
                    if (day in 1..yearMonth.lengthOfMonth()) {
                        val date = yearMonth.atDay(day)
                        CalendarDay(day, date == selectedDate, date == LocalDate.now(), schedules.any { it.date == date }, { onDateSelected(date) }, Modifier.weight(1f))
                    } else Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(day: Int, isSelected: Boolean, isToday: Boolean, hasSchedule: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier.aspectRatio(1f).padding(4.dp).clip(CircleShape).background(if (isSelected) Color(0xFFECD4CD) else Color.Transparent).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(day.toString(), color = if (isSelected) Color.White else Color.Black, fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal)
            if (hasSchedule && !isSelected) Box(Modifier.size(4.dp).clip(CircleShape).background(IeumColors.Primary))
        }
    }
}

@Composable
private fun EmptyScheduleView(text: String) {
    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        Text(text, color = Color.Gray)
    }
}

@Composable
private fun ScheduleItem(
    schedule: com.ieum.domain.model.Schedule,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(4.dp).height(40.dp).background(IeumColors.Primary))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(schedule.title, fontWeight = FontWeight.Bold)
                Text("상세 일정 확인 및 수정", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

private fun calculateDDay(targetDate: LocalDate): String {
    val today = LocalDate.now()
    val days = java.time.temporal.ChronoUnit.DAYS.between(today, targetDate)
    return when {
        days == 0L -> "D-Day"
        days > 0 -> "D-$days"
        else -> "D+${-days}"
    }
}