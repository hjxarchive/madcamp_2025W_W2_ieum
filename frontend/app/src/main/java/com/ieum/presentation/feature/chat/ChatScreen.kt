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
import com.ieum.presentation.theme.IeumColors
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 커플 전용 채팅 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    
    val messages = uiState.messages
    val messageText = uiState.inputText
    val partnerName = uiState.partnerName
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F4F0)) // 따뜻한 베이지 톤 배경
    ) {
        // 상단 헤더
        ChatHeader(partnerName = partnerName)
        
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
            onAttachClick = { /* 첨부 파일 */ },
            onShareClick = { /* 공유 메뉴 */ }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatHeader(partnerName: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = IeumColors.Background,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = IeumColors.TextPrimary
                )
            }
            
            // 프로필
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(IeumColors.Primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = partnerName.first().toString(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = IeumColors.Primary
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
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
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(IeumColors.Success)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "온라인",
                        style = MaterialTheme.typography.labelSmall,
                        color = IeumColors.TextSecondary
                    )
                }
            }
            
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Outlined.Call,
                    contentDescription = "통화",
                    tint = IeumColors.TextSecondary
                )
            }
            
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = "더보기",
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
        
        // 시간
        Text(
            text = message.timestamp.format(DateTimeFormatter.ofPattern("HH:mm")),
            style = MaterialTheme.typography.labelSmall,
            color = IeumColors.TextSecondary.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun TextMessageBubble(message: ChatMessage) {
    val backgroundColor = if (message.isMe) IeumColors.Primary else Color.White
    val textColor = if (message.isMe) Color.White else IeumColors.TextPrimary
    val shape = if (message.isMe) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
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
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
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
    onAttachClick: () -> Unit,
    onShareClick: () -> Unit
) {
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
            // 공유 버튼 (일정, 장소, 버킷리스트)
            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "공유",
                    tint = IeumColors.Primary
                )
            }
            
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
                        if (text.isNotBlank()) IeumColors.Primary
                        else IeumColors.Primary.copy(alpha = 0.3f)
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
