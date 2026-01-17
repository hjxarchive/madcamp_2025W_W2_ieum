package com.ieum.presentation.feature.profile

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ieum.presentation.theme.IeumColors

/**
 * 마이페이지 (커플 프로필)
 * "서로가 꾸며주는" 상대방 프로필 + MBTI 궁합 분석
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IeumColors.Background)
            .verticalScroll(scrollState)
    ) {
        // 프로필 헤더
        ProfileHeader()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // MBTI 궁합
        MBTICompatibilityCard()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 취향 섹션
        PreferenceSection()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 위시리스트
        WishlistSection()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 설정 메뉴
        SettingsSection()
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun ProfileHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        IeumColors.Primary.copy(alpha = 0.2f),
                        IeumColors.Background
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 설정 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "설정",
                        tint = IeumColors.TextSecondary
                    )
                }
            }
            
            // 커플 프로필
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 내 프로필
                ProfileCard(
                    name = "지민",
                    nickname = "귀요미",
                    mbti = "ENFP",
                    color = IeumColors.Primary,
                    isEditable = false
                )
                
                // 하트 연결
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = IeumColors.Primary,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "D+365",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = IeumColors.Primary
                    )
                }
                
                // 파트너 프로필
                ProfileCard(
                    name = "수현",
                    nickname = "멋쟁이",
                    mbti = "INTJ",
                    color = IeumColors.Secondary,
                    isEditable = true
                )
            }
        }
    }
}

@Composable
private fun ProfileCard(
    name: String,
    nickname: String,
    mbti: String,
    color: Color,
    isEditable: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 프로필 이미지
        Box(
            contentAlignment = Alignment.BottomEnd
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f))
                    .border(3.dp, color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.first().toString(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = color
                )
            }
            
            if (isEditable) {
                // 수정 버튼
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(IeumColors.Primary)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "수정",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 이름
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = IeumColors.TextPrimary
        )
        
        // 별명 (상대방이 지어준)
        Text(
            text = "\"$nickname\"",
            style = MaterialTheme.typography.bodySmall,
            color = IeumColors.TextSecondary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // MBTI 칩
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.2f)
        ) {
            Text(
                text = mbti,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

/**
 * MBTI 궁합 분석 카드
 */
@Composable
private fun MBTICompatibilityCard() {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MBTI 궁합 분석",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = IeumColors.TextPrimary
            )
            
            TextButton(onClick = { }) {
                Text(
                    text = "자세히",
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
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 궁합도
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ENFP",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = IeumColors.Primary
                    )
                    
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = IeumColors.Primary,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .size(24.dp)
                    )
                    
                    Text(
                        text = "INTJ",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = IeumColors.Secondary
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 궁합 점수
                Text(
                    text = "85%",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = IeumColors.Primary
                )
                
                Text(
                    text = "환상의 궁합",
                    style = MaterialTheme.typography.titleSmall,
                    color = IeumColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 설명
                Text(
                    text = "서로 다른 매력으로 끌리는 커플!\n감성적인 ENFP와 논리적인 INTJ의 만남은\n서로를 성장시키는 환상의 조합이에요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = IeumColors.TextPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 카테고리별 궁합
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CompatibilityItem("대화", 90, IeumColors.Success)
                    CompatibilityItem("갈등해결", 75, IeumColors.Warning)
                    CompatibilityItem("장기연애", 88, IeumColors.Primary)
                }
            }
        }
    }
}

@Composable
private fun CompatibilityItem(
    label: String,
    score: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(60.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 6.dp,
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )
            
            Text(
                text = "$score",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = IeumColors.TextSecondary
        )
    }
}

/**
 * 취향 섹션
 */
@Composable
private fun PreferenceSection() {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "우리의 취향",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = IeumColors.TextPrimary
            )
            
            TextButton(onClick = { }) {
                Text(
                    text = "수정",
                    color = IeumColors.Primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PreferenceCard(
                title = "데이트 스타일",
                items = listOf("카페 투어", "맛집 탐방", "영화"),
                color = IeumColors.Primary,
                modifier = Modifier.weight(1f)
            )
            
            PreferenceCard(
                title = "선물 취향",
                items = listOf("실용적인 것", "향수", "디저트"),
                color = IeumColors.Secondary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PreferenceCard(
    title: String,
    items: List<String>,
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
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = color
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            items.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodySmall,
                        color = IeumColors.TextPrimary
                    )
                }
            }
        }
    }
}

/**
 * 위시리스트 섹션
 */
@Composable
private fun WishlistSection() {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "선물 위시리스트",
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
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                listOf(
                    WishItem("에어팟 프로", "전자기기", "359,000원"),
                    WishItem("조말론 향수", "뷰티", "198,000원"),
                    WishItem("무선 키보드", "전자기기", "129,000원")
                ).forEach { item ->
                    WishlistItemRow(item = item)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun WishlistItemRow(item: WishItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 아이콘
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(IeumColors.Primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CardGiftcard,
                contentDescription = null,
                tint = IeumColors.Primary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = IeumColors.TextPrimary
            )
            
            Text(
                text = item.category,
                style = MaterialTheme.typography.labelSmall,
                color = IeumColors.TextSecondary
            )
        }
        
        Text(
            text = item.price,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = IeumColors.Primary
        )
    }
}

/**
 * 설정 섹션
 */
@Composable
private fun SettingsSection() {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "설정",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = IeumColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                SettingsItem(
                    icon = Icons.Outlined.Notifications,
                    title = "알림 설정"
                )
                HorizontalDivider(color = IeumColors.TextSecondary.copy(alpha = 0.1f))
                SettingsItem(
                    icon = Icons.Outlined.Lock,
                    title = "개인정보 설정"
                )
                HorizontalDivider(color = IeumColors.TextSecondary.copy(alpha = 0.1f))
                SettingsItem(
                    icon = Icons.Outlined.Help,
                    title = "고객센터"
                )
                HorizontalDivider(color = IeumColors.TextSecondary.copy(alpha = 0.1f))
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    title = "앱 정보"
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = IeumColors.TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = IeumColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = IeumColors.TextSecondary.copy(alpha = 0.5f)
        )
    }
}

// 데이터 클래스
data class WishItem(
    val name: String,
    val category: String,
    val price: String
)
