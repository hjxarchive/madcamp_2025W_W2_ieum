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
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    var showFabMenu by remember { mutableStateOf(false) }
    var activeSheetType by remember { mutableStateOf<String?>(null) }

    // 선택된 지출 상태 관리 (수정/삭제용)
    var selectedExpense by remember { mutableStateOf<Expense?>(null) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }
    var itemToDelete by remember { mutableStateOf<com.ieum.domain.model.BucketItem?>(null) }
    var anniversaryToDelete by remember { mutableStateOf<Anniversary?>(null) }
    var selectedSchedule by remember { mutableStateOf<com.ieum.domain.model.Schedule?>(null) }

    // 화면이 다시 활성화될 때 데이터 새로고침 (커플 간 동기화)
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.onScreenResumed()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
                                     },
                                     onLongClick = {
                                         expenseToDelete = expense
                                     }
                                 )
                             }
                         }
                    }
                }
            }
        }
    }

    if (expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("지출 내역 삭제", fontWeight = FontWeight.Bold) },
            text = { Text("'${expenseToDelete?.title}' 항목을 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        expenseToDelete?.let { viewModel.deleteExpense(it.id) }
                        expenseToDelete = null
                    }
                ) {
                    Text("삭제", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("취소")
                }
            }
        )
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
            editingSchedule = if (type == "일정") selectedSchedule else null,
            onDismiss = {
                activeSheetType = null
                selectedExpense = null
                selectedSchedule = null
            },
            onConfirm = { title, date, memo, category ->
                when {
                    selectedSchedule != null -> {
                        // 수정: 기존 것 삭제 후 새로 추가
                        viewModel.deleteSchedule(selectedSchedule!!.id)
                        viewModel.addSchedule(title, date, memo)
                    }
                    selectedExpense != null -> {
                        // 수정: 기존 것 삭제 후 새로 추가
                        viewModel.deleteExpense(selectedExpense!!.id)
                        viewModel.addExpense(title, date, memo, category)
                    }
                    else -> { // 신규 추가 모드
                        when(type) {
                            "기념일" -> viewModel.addAnniversary(title, date)
                            "버킷리스트" -> viewModel.addBucketList(title)
                            "일정" -> viewModel.addSchedule(title, date, memo)
                            "지출" -> viewModel.addExpense(title, date, memo, category)
                        }
                    }
                }
                activeSheetType = null
                selectedSchedule = null
                selectedExpense = null
            },
            onDelete = {
                when {
                    selectedSchedule != null -> viewModel.deleteSchedule(selectedSchedule!!.id)
                    selectedExpense != null -> viewModel.deleteExpense(selectedExpense!!.id)
                }
                activeSheetType = null
                selectedSchedule = null
                selectedExpense = null
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpenseCard(
    expense: com.ieum.domain.model.Expense,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val mainBrown = Color(0xFF5A3E2B)
    val categoryColor = mainBrown.copy(alpha = 0.7f) // "아주 조금 연한 색"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 왼쪽에 19폰트로 세부내역
            Text(
                text = expense.title,
                fontSize = 19.sp,
                fontWeight = FontWeight.Medium,
                color = mainBrown,
                modifier = Modifier.weight(1f)
            )

            // 오른쪽에 항목 이름(세부 사항)과 가격
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = expense.category.label,
                    color = categoryColor,
                    fontSize = 12.sp
                )
                Text(
                    text = "￦ ${String.format("%,d", expense.amount)}",
                    fontWeight = FontWeight.Bold,
                    color = mainBrown,
                    fontSize = 16.sp
                )
            }
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
    editingExpense: com.ieum.domain.model.Expense? = null,
    editingSchedule: com.ieum.domain.model.Schedule? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, LocalDate, String, com.ieum.domain.model.ExpenseCategory?) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 수정 모드 여부 판단
    val isEditMode = editingExpense != null || editingSchedule != null

    var title by remember { mutableStateOf(editingExpense?.title ?: editingSchedule?.title ?: "") }
    var memo by remember { mutableStateOf(editingExpense?.amount?.toString() ?: editingSchedule?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(editingExpense?.category ?: com.ieum.domain.model.ExpenseCategory.FOOD) }
    var amountError by remember { mutableStateOf<String?>(null) }

    // 날짜 초기값 설정
    val initialDate = when {
        editingExpense != null -> {
            try {
                LocalDate.parse(editingExpense.date, DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            } catch (e: Exception) { LocalDate.now() }
        }
        editingSchedule != null -> editingSchedule.date
        else -> LocalDate.now()
    }

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
                // 지출 추가 옆에 세부 사항 리스트 (지출일 때만 표시)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when {
                            editingExpense != null -> "지출 내역 상세"
                            editingSchedule != null -> "일정 상세"
                            else -> "$type 추가"
                        },
                        color = Color(0xFF8D7B68),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (type == "지출") {
                        Spacer(modifier = Modifier.width(12.dp))
                        // 카테고리 선택 리스트 (가로 스크롤)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            com.ieum.domain.model.ExpenseCategory.entries.filter { it != com.ieum.domain.model.ExpenseCategory.OTHER }.forEach { category ->
                                val isSelected = selectedCategory == category
                                val color = Color(android.graphics.Color.parseColor(category.colorHex))
                                
                                Surface(
                                    onClick = { selectedCategory = category },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) color else color.copy(alpha = 0.2f),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = category.label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else color
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 수정 모드일 때만 삭제 버튼 표시 (지출 또는 일정)
                if (isEditMode) {
                    IconButton(onClick = { onDelete?.invoke() }) {
                        Icon(Icons.Default.Delete, contentDescription = "삭제", tint = Color(0xFFE57373))
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                if (title.isEmpty()) {
                    Text(
                        text = "제목을 입력하세요",
                        color = Color(0xFFC1B4A5),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color(0xFF5A3E2B),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            HorizontalDivider(color = Color(0xFFF0E5D8))

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { datePickerDialog.show() }.padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFFA68A64), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(16.dp))
                Text(text = "${selectedDate.year}년 ${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일", color = Color(0xFF5A3E2B), fontSize = 16.sp)
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFFA68A64), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(16.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (memo.isEmpty()) {
                            Text(
                                text = if (type == "지출") "금액을 입력하세요" else "메모를 입력하세요",
                                color = Color(0xFFC1B4A5),
                                fontSize = 16.sp
                            )
                        }
                        androidx.compose.foundation.text.BasicTextField(
                            value = memo,
                            onValueChange = { newValue ->
                                if (type == "지출") {
                                    // 지출일 때는 숫자만 허용
                                    if (newValue.isEmpty()) {
                                        memo = newValue
                                        amountError = null
                                    } else if (newValue.all { it.isDigit() }) {
                                        memo = newValue
                                        amountError = null
                                    } else {
                                        amountError = "숫자만 입력해주세요"
                                    }
                                } else {
                                    memo = newValue
                                }
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color(0xFF5A3E2B),
                                fontSize = 16.sp
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = if (type == "지출") androidx.compose.ui.text.input.KeyboardType.Number else androidx.compose.ui.text.input.KeyboardType.Text
                            )
                        )
                    }
                }

                // 에러 메시지 표시
                if (amountError != null && type == "지출") {
                    Row(
                        modifier = Modifier.padding(start = 36.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFE57373),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = amountError!!,
                            color = Color(0xFFE57373),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                FloatingActionButton(
                    onClick = { 
                        if(title.isNotBlank()) {
                            onConfirm(title, selectedDate, memo, if(type == "지출") selectedCategory else null)
                        } 
                    },
                    containerColor = Color(0xFFECD4CD),
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp)
                ) {
                    Icon(if (isEditMode) Icons.Default.Edit else Icons.Default.Check, contentDescription = null)
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