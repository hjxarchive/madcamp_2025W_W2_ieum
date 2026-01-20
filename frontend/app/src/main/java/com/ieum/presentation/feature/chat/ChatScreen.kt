package com.ieum.presentation.feature.chat

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ieum.domain.model.ChatMessage
import com.ieum.domain.model.MessageType
import com.ieum.domain.repository.ChatConnectionState
import com.ieum.presentation.theme.IeumColors
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 커플 전용 채팅 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel(),
    onNavigateToBudgetPlanning: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    
    val messages = uiState.messages
    val messageText = uiState.inputText
    val partnerName = uiState.partnerName
    val connectionState = uiState.connectionState
    val isPartnerTyping = uiState.isPartnerTyping
    val context = androidx.compose.ui.platform.LocalContext.current

    // 새 메시지가 오면 자동 스크롤
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // 네이버 지도 실행 Intent
    fun shareLocation() {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("nmap://map"))
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        val componentName = intent.resolveActivity(context.packageManager)
        if (componentName != null) {
            context.startActivity(intent)
        } else {
             // 네이버 지도 없으면 스토어 연결 (선택사항)
             val storeIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=com.nhn.android.nmap"))
             context.startActivity(storeIntent)
        }
    }

    // 일정 공유 다이얼로그 상태
    var showScheduleDialog by remember { mutableStateOf(false) }
    // 버킷리스트 추가 바텀시트 상태
    var showBucketSheet by remember { mutableStateOf(false) }
    
    // 일정 공유 Dialog
    if (showScheduleDialog) {
        ScheduleSelectionDialog(
            currentMonth = uiState.sharingYearMonth,
            schedules = uiState.sharingSchedules,
            onDismiss = { showScheduleDialog = false },
            onPreviousMonth = { viewModel.navigateSharingMonth(-1) },
            onNextMonth = { viewModel.navigateSharingMonth(1) },
            onScheduleSelected = { schedule ->
                viewModel.shareSchedule(schedule)
                showScheduleDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F4F0)) // 따뜻한 베이지 톤 배경
    ) {
        // 상단 헤더
        ChatHeader(
            partnerName = partnerName,
            connectionState = connectionState,
            isPartnerTyping = isPartnerTyping,
            onBackClick = onBackClick,
            onReconnect = { viewModel.reconnect() }
        )
        
        // 메시지 목록
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            reverseLayout = false,
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { message ->
                MessageItem(message = message)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        // 입력 영역
        ChatInputBar(
            text = messageText,
            onTextChange = { viewModel.updateInputText(it) },
            onSendClick = {
                if (messageText.isNotBlank()) {
                    viewModel.sendMessage(messageText)
                }
            },
            onShareSchedule = { showScheduleDialog = true },
            onShareLocation = { shareLocation() },
            onAddBucket = { showBucketSheet = true },
            onEditBudget = { onNavigateToBudgetPlanning() }
        )
    }

    if (showBucketSheet) {
        com.ieum.presentation.feature.calendar.CommonAddBottomSheet(
            type = "버킷리스트",
            onDismiss = { showBucketSheet = false },
            onConfirm = { title, _, _, _ ->
                viewModel.addBucketList(title)
                showBucketSheet = false
            }
        )
    }


}

@Composable
fun ScheduleSelectionDialog(
    currentMonth: java.time.YearMonth,
    schedules: List<com.ieum.domain.model.Schedule>,
    onDismiss: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onScheduleSelected: (com.ieum.domain.model.Schedule) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 월 네비게이션
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPreviousMonth) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "이전 달")
                    }
                    Text(
                        text = "${currentMonth.year}년 ${currentMonth.monthValue}월",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onNextMonth) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "다음 달")
                    }
                }
                
                HorizontalDivider()
                
                // 일정 목록
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (schedules.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("일정이 없습니다.", color = Color.Gray)
                            }
                        }
                    } else {
                        items(schedules) { schedule ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onScheduleSelected(schedule) },
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(32.dp)
                                            .background(Color(android.graphics.Color.parseColor(schedule.colorHex)))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = schedule.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = schedule.date.toString(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기", color = IeumColors.TextSecondary)
            }
        },
        containerColor = Color(0xFFF8F4F0)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatHeader(
    partnerName: String,
    connectionState: ChatConnectionState,
    isPartnerTyping: Boolean,
    onBackClick: () -> Unit,
    onReconnect: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = IeumColors.Background,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = IeumColors.TextPrimary
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = partnerName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = IeumColors.TextPrimary
                )

                // 연결 상태 또는 타이핑 인디케이터 표시
                when {
                    isPartnerTyping -> {
                        Text(
                            text = "입력 중...",
                            style = MaterialTheme.typography.bodySmall,
                            color = IeumColors.Primary
                        )
                    }
                    connectionState == ChatConnectionState.CONNECTED -> {
                        Text(
                            text = "온라인",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    connectionState == ChatConnectionState.CONNECTING -> {
                        Text(
                            text = "연결 중...",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFA726)
                        )
                    }
                    connectionState == ChatConnectionState.ERROR -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onReconnect() }
                        ) {
                            Text(
                                text = "연결 실패 - 탭하여 재연결",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE53935)
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = "오프라인",
                            style = MaterialTheme.typography.bodySmall,
                            color = IeumColors.TextSecondary
                        )
                    }
                }
            }

            // 돋보기 (검색) 아이콘
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "검색",
                    tint = IeumColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun MessageItem(message: ChatMessage) {
    val alignment = if (message.isMe) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        when (message.type) {
            MessageType.TEXT -> TextMessageBubble(message)
            MessageType.SHARED_SCHEDULE -> SharedScheduleBubble(message)
            MessageType.SHARED_PLACE -> SharedPlaceBubble(message)
            MessageType.SHARED_BUCKET -> SharedBucketBubble(message)
            MessageType.IMAGE -> ImageMessageBubble(message)
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 시간 및 읽음 상태
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
        ) {
            // 내가 보낸 메시지의 경우 읽음 상태 표시
            if (message.isMe && message.isRead) {
                Text(
                    text = "읽음",
                    style = MaterialTheme.typography.labelSmall,
                    color = IeumColors.Primary.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = message.timestamp.format(DateTimeFormatter.ofPattern("HH:mm")),
                style = MaterialTheme.typography.labelSmall,
                color = IeumColors.TextSecondary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun TextMessageBubble(message: ChatMessage) {
    val backgroundColor = if (message.isMe) Color(0xFFEBC1B3) else Color.White
    val textColor = IeumColors.TextPrimary
    val shape = if (message.isMe) {
        RoundedCornerShape(16.dp, 16.dp, 6.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 6.dp)
    }
    
    Surface(
        shape = shape,
        color = backgroundColor,
        shadowElevation = 1.dp
    ) {
        Text(
            text = message.content,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun SharedScheduleBubble(message: ChatMessage) {
    val shape = if (message.isMe) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }
    
    Card(
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = IeumColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "일정 공유",
                    style = MaterialTheme.typography.labelMedium,
                    color = IeumColors.Primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message.sharedData?.get("title") ?: "",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = IeumColors.TextPrimary
            )
            
            Text(
                text = message.sharedData?.get("date") ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = IeumColors.TextSecondary
            )
        }
    }
}

@Composable
private fun SharedPlaceBubble(message: ChatMessage) {
    val shape = if (message.isMe) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }
    
    Card(
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Place,
                    contentDescription = null,
                    tint = IeumColors.Accent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "장소 공유",
                    style = MaterialTheme.typography.labelMedium,
                    color = IeumColors.Accent
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message.sharedData?.get("name") ?: "",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = IeumColors.TextPrimary
            )
            
            Text(
                text = message.sharedData?.get("address") ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = IeumColors.TextSecondary
            )
        }
    }
}

@Composable
private fun SharedBucketBubble(message: ChatMessage) {
    val shape = if (message.isMe) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }
    
    Card(
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = IeumColors.Secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "버킷리스트 공유",
                    style = MaterialTheme.typography.labelMedium,
                    color = IeumColors.Secondary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message.sharedData?.get("title") ?: "",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = IeumColors.TextPrimary
            )
        }
    }
}

@Composable
private fun ImageMessageBubble(message: ChatMessage) {
    val shape = if (message.isMe) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }
    
    Card(
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(200.dp, 150.dp)
                .background(IeumColors.Primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                tint = IeumColors.Primary,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onShareSchedule: () -> Unit,
    onShareLocation: () -> Unit,
    onAddBucket: () -> Unit,
    onEditBudget: () -> Unit
) {
    val darkBeige = Color(0xFFE0C4BB) // 0xFFECD4CD 보다 약간 어두운 색

    Column(modifier = Modifier.fillMaxWidth()) {
        
        // 상단 버튼 4개 (투명 배경)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 버튼 스타일: 꽉 채운 색상, 약간 더 크게
            @Composable
            fun ActionButton(text: String, onClick: () -> Unit) {
                Surface(
                        onClick = onClick,
                        shape = RoundedCornerShape(20.dp),
                        color = darkBeige, // Filled color
                        shadowElevation = 2.dp
                ) {
                    Text(
                        text = text,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), // Increased padding
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White, // White text for contrast
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Row scrollable if needed, but 4 buttons might fit or wrap. 
            // The user didn't mention scrolling, but 4 large buttons might overflow. 
            // Let's use a horizontal scroll row just in case.
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton("일정 공유", onShareSchedule)
                ActionButton("위치 공유", onShareLocation)
                ActionButton("버킷리스트 추가", onAddBucket)
                ActionButton("예산 수정", onEditBudget)
            }
        }
        
        // 입력 필드 (흰색 배경)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 입력 필드
                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = "메시지를 입력하세요",
                            color = IeumColors.TextSecondary.copy(alpha = 0.6f)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = false,
                    maxLines = 4
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // 전송 버튼
                IconButton(
                    onClick = onSendClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (text.isNotBlank()) darkBeige
                            else darkBeige.copy(alpha = 0.3f)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "전송",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

/* 중복 클래스 제거 - domain.model.ChatMessage 사용
// 데이터 클래스
data class ChatMessage(
    val id: Int,
    val content: String,
    val isMe: Boolean,
    val timestamp: LocalDateTime,
    val type: MessageType = MessageType.TEXT,
    val sharedData: Map<String, String>? = null
)

enum class MessageType {
    TEXT, SHARED_SCHEDULE, SHARED_PLACE, SHARED_BUCKET, IMAGE
}

// 샘플 데이터
private val sampleMessages = listOf(
    ChatMessage(1, "오늘 저녁에 뭐 먹을까?", false, LocalDateTime.now().minusHours(2)),
    ChatMessage(2, "음... 파스타 어때?", true, LocalDateTime.now().minusHours(2).plusMinutes(5)),
    ChatMessage(3, "좋아! 성수동에 새로 생긴 곳 가보자", false, LocalDateTime.now().minusHours(1).plusMinutes(50)),
    ChatMessage(
        4, "", true, LocalDateTime.now().minusHours(1).plusMinutes(45),
        MessageType.SHARED_PLACE,
        mapOf("name" to "파스타 명가", "address" to "성수동 123-45")
    ),
    ChatMessage(5, "여기 분위기 좋아 보인다!", false, LocalDateTime.now().minusHours(1).plusMinutes(40)),
    ChatMessage(
        6, "", false, LocalDateTime.now().minusMinutes(30),
        MessageType.SHARED_SCHEDULE,
        mapOf("title" to "저녁 데이트", "date" to "오늘 18:00")
    ),
    ChatMessage(7, "알았어 이따 봐! 💕", true, LocalDateTime.now().minusMinutes(25))
)
*/
