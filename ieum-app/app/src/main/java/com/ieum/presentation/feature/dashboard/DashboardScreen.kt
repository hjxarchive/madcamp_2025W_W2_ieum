package com.ieum.presentation.feature.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ieum.presentation.theme.IeumColors

/**
 * 대시보드 화면
 * PDF 기반: 취향, 위치공유, 버킷리스트 추가, 예산, 데이팅 추천, 서로 일정, 기념일
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userName: String = "지민",
    partnerName: String = "수현",
    dDay: Int = 365,
    onMenuClick: (DashboardMenu) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IeumColors.Background)
            .verticalScroll(scrollState)
    ) {
        // 상단 헤더
        DashboardHeader(
            userName = userName,
            partnerName = partnerName,
            dDay = dDay
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 퀵 액션 그리드
        QuickActionGrid(onMenuClick = onMenuClick)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 이번 주 추천 데이트
        WeeklyDateRecommendation()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 최근 추억
        RecentMemories()
        
        Spacer(modifier = Modifier.height(100.dp)) // 하단 네비게이션 여백
    }
}

@Composable
private fun DashboardHeader(
    userName: String,
    partnerName: String,
    dDay: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        IeumColors.Primary.copy(alpha = 0.15f),
                        IeumColors.Background
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column {
            // 커플 프로필
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 내 프로필
                ProfileAvatar(
                    name = userName,
                    color = IeumColors.Primary
                )
                
                // 하트 연결 아이콘
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = IeumColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "D+$dDay",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = IeumColors.Primary
                    )
                }
                
                // 파트너 프로필
                ProfileAvatar(
                    name = partnerName,
                    color = IeumColors.Secondary
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // 알림 아이콘
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "알림",
                        tint = IeumColors.TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 인사 메시지
            Text(
                text = "안녕하세요, $userName 님 💕",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = IeumColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "오늘도 사랑 가득한 하루 되세요!",
                style = MaterialTheme.typography.bodyMedium,
                color = IeumColors.TextSecondary
            )
        }
    }
}

@Composable
private fun ProfileAvatar(
    name: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f))
                .border(2.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.first().toString(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = IeumColors.TextPrimary
        )
    }
}

/**
 * 퀵 액션 메뉴 그리드
 */
@Composable
private fun QuickActionGrid(
    onMenuClick: (DashboardMenu) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "바로가기",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = IeumColors.TextPrimary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            userScrollEnabled = false
        ) {
            items(DashboardMenu.entries) { menu ->
                QuickActionItem(
                    menu = menu,
                    onClick = { onMenuClick(menu) }
                )
            }
        }
    }
}

@Composable
private fun QuickActionItem(
    menu: DashboardMenu,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(menu.backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = menu.icon,
                contentDescription = menu.title,
                tint = menu.iconColor,
                modifier = Modifier.size(28.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = menu.title,
            style = MaterialTheme.typography.labelSmall,
            color = IeumColors.TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * 대시보드 메뉴 아이템
 */
enum class DashboardMenu(
    val title: String,
    val icon: ImageVector,
    val backgroundColor: Color,
    val iconColor: Color
) {
    PREFERENCE(
        title = "취향",
        icon = Icons.Outlined.Favorite,
        backgroundColor = Color(0xFFFFE0E0),
        iconColor = IeumColors.Primary
    ),
    LOCATION(
        title = "위치공유",
        icon = Icons.Outlined.LocationOn,
        backgroundColor = Color(0xFFE0F2E9),
        iconColor = IeumColors.Accent
    ),
    BUCKET_LIST(
        title = "버킷리스트",
        icon = Icons.Outlined.CheckCircle,
        backgroundColor = Color(0xFFE8E0FF),
        iconColor = IeumColors.Secondary
    ),
    BUDGET(
        title = "예산",
        icon = Icons.Outlined.AccountBalanceWallet,
        backgroundColor = Color(0xFFFFF3E0),
        iconColor = Color(0xFFFF9800)
    ),
    DATE_RECOMMEND(
        title = "데이팅 추천",
        icon = Icons.Outlined.Explore,
        backgroundColor = Color(0xFFE3F2FD),
        iconColor = Color(0xFF2196F3)
    ),
    SCHEDULE(
        title = "서로 일정",
        icon = Icons.Outlined.CalendarMonth,
        backgroundColor = Color(0xFFE0F7FA),
        iconColor = Color(0xFF00BCD4)
    ),
    ANNIVERSARY(
        title = "기념일",
        icon = Icons.Outlined.Cake,
        backgroundColor = Color(0xFFFCE4EC),
        iconColor = Color(0xFFE91E63)
    ),
    SETTINGS(
        title = "설정",
        icon = Icons.Outlined.Settings,
        backgroundColor = Color(0xFFF5F5F5),
        iconColor = Color(0xFF757575)
    )
}

/**
 * 이번 주 추천 데이트 섹션
 */
@Composable
private fun WeeklyDateRecommendation() {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "이번 주 추천 데이트",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = IeumColors.TextPrimary
            )
            
            TextButton(onClick = { }) {
                Text(
                    text = "더보기",
                    style = MaterialTheme.typography.labelMedium,
                    color = IeumColors.Primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 썸네일
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    IeumColors.Primary.copy(alpha = 0.3f),
                                    IeumColors.Secondary.copy(alpha = 0.3f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Restaurant,
                        contentDescription = null,
                        tint = IeumColors.Primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "성수동 카페 투어",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = IeumColors.TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "분위기 좋은 카페 3곳 코스",
                        style = MaterialTheme.typography.bodySmall,
                        color = IeumColors.TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryChip(text = "카페", color = IeumColors.CategoryCafe)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "예상 35,000원",
                            style = MaterialTheme.typography.labelSmall,
                            color = IeumColors.TextSecondary
                        )
                    }
                }
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = IeumColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    text: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * 최근 추억 섹션
 */
@Composable
private fun RecentMemories() {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "최근 추억",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = IeumColors.TextPrimary
            )
            
            TextButton(onClick = { }) {
                Text(
                    text = "전체보기",
                    style = MaterialTheme.typography.labelMedium,
                    color = IeumColors.Primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(3) { index ->
                MemoryCard(
                    modifier = Modifier.weight(1f),
                    imageIndex = index
                )
            }
        }
    }
}

@Composable
private fun MemoryCard(
    modifier: Modifier = Modifier,
    imageIndex: Int
) {
    val colors = listOf(
        IeumColors.Primary.copy(alpha = 0.3f),
        IeumColors.Secondary.copy(alpha = 0.3f),
        IeumColors.Accent.copy(alpha = 0.3f)
    )
    
    Card(
        modifier = modifier
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors[imageIndex % colors.size]
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                tint = IeumColors.TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
