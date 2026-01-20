package com.ieum.presentation.feature.memory

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ieum.presentation.theme.IeumColors

// ViewMode enum
enum class ViewMode {
    MAP, GALLERY, TIMELINE
}

/**
 * 지도 갤러리 화면 (추억 아카이브)
 * 위치 기반 사진 갤러리 + 한 줄 코멘트
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapGalleryScreen(
    modifier: Modifier = Modifier,
    viewModel: MemoryViewModel = hiltViewModel()
) {
    var viewMode by remember { mutableStateOf(ViewMode.MAP) }
    var selectedMemory by remember { mutableStateOf<Memory?>(null) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IeumColors.Background)
    ) {
        // 상단 헤더
        MapGalleryHeader(
            viewMode = viewMode,
            onViewModeChange = { viewMode = it }
        )
        
        // 콘텐츠
        when (viewMode) {
            ViewMode.MAP -> MapView(
                memories = sampleMemories,
                onMemoryClick = { selectedMemory = it }
            )
            ViewMode.GALLERY -> GalleryView(
                memories = sampleMemories,
                onMemoryClick = { selectedMemory = it }
            )
            ViewMode.TIMELINE -> TimelineView(
                memories = sampleMemories,
                onMemoryClick = { selectedMemory = it }
            )
        }
    }
    
    // 추억 상세 바텀시트
    selectedMemory?.let { memory ->
        MemoryDetailSheet(
            memory = memory,
            onDismiss = { selectedMemory = null }
        )
    }
}

@Composable
private fun MapGalleryHeader(
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = IeumColors.Background,
        shadowElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "추억 갤러리",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = IeumColors.TextPrimary
                )
                
                IconButton(onClick = { /* 추억 추가 */ }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "추억 추가",
                        tint = IeumColors.Primary
                    )
                }
            }
            
            // 뷰 모드 탭
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ViewMode.entries.forEach { mode ->
                    FilterChip(
                        selected = viewMode == mode,
                        onClick = { onViewModeChange(mode) },
                        label = { Text(mode.label) },
                        leadingIcon = {
                            Icon(
                                imageVector = mode.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IeumColors.Primary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

/**
 * 지도 뷰
 */
@Composable
private fun MapView(
    memories: List<Memory>,
    onMemoryClick: (Memory) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 지도 배경 (실제로는 Google Maps 사용)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE8F5E9))
        ) {
            // 지도 플레이스홀더
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Map,
                    contentDescription = null,
                    tint = IeumColors.TextSecondary.copy(alpha = 0.3f),
                    modifier = Modifier.size(120.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "지도에서 추억을 확인하세요",
                    style = MaterialTheme.typography.bodyLarge,
                    color = IeumColors.TextSecondary
                )
            }
        }
        
        // 추억 마커들 (시뮬레이션)
        memories.forEachIndexed { index, memory ->
            MemoryMarker(
                memory = memory,
                onClick = { onMemoryClick(memory) },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x = (50 + index * 80).dp,
                        y = (100 + index * 60).dp
                    )
            )
        }
        
        // 하단 추억 미리보기 카드
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp)
        ) {
            HorizontalMemoryPreview(
                memories = memories,
                onMemoryClick = onMemoryClick
            )
        }
    }
}

@Composable
private fun MemoryMarker(
    memory: Memory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 마커
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            IeumColors.Primary,
                            IeumColors.PrimaryDark
                        )
                    )
                )
                .border(3.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // 마커 꼬리
        Box(
            modifier = Modifier
                .size(12.dp)
                .offset(y = (-4).dp)
                .background(
                    color = IeumColors.Primary,
                    shape = RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)
                )
        )
    }
}

@Composable
private fun HorizontalMemoryPreview(
    memories: List<Memory>,
    onMemoryClick: (Memory) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            memories.forEach { memory ->
                MemoryPreviewCard(
                    memory = memory,
                    onClick = { onMemoryClick(memory) }
                )
            }
        }
    }
}

@Composable
private fun MemoryPreviewCard(
    memory: Memory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = memory.color.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 이미지 플레이스홀더
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.3f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(memory.color.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    tint = memory.color,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = memory.placeName,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = IeumColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = memory.comment,
                style = MaterialTheme.typography.labelSmall,
                color = IeumColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 갤러리 뷰 (그리드)
 */
@Composable
private fun GalleryView(
    memories: List<Memory>,
    onMemoryClick: (Memory) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(memories) { memory ->
            GalleryItem(
                memory = memory,
                onClick = { onMemoryClick(memory) }
            )
        }
    }
}

@Composable
private fun GalleryItem(
    memory: Memory,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .background(memory.color.copy(alpha = 0.3f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = null,
            tint = memory.color,
            modifier = Modifier.size(36.dp)
        )
        
        // 위치 배지
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = memory.placeName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * 타임라인 뷰
 */
@Composable
private fun TimelineView(
    memories: List<Memory>,
    onMemoryClick: (Memory) -> Unit
) {
    val groupedMemories = memories.groupBy { it.date.substring(0, 7) } // YYYY-MM
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        groupedMemories.forEach { (month, monthMemories) ->
            // 월 헤더
            Text(
                text = month.replace("-", "년 ") + "월",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = IeumColors.TextPrimary,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            monthMemories.forEach { memory ->
                TimelineItem(
                    memory = memory,
                    onClick = { onMemoryClick(memory) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun TimelineItem(
    memory: Memory,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 타임라인 라인
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(IeumColors.Primary)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(100.dp)
                    .background(IeumColors.Primary.copy(alpha = 0.3f))
            )
        }
        
        // 콘텐츠 카드
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp)
            ) {
                // 썸네일
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(memory.color.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = memory.color,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = memory.placeName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = IeumColors.TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = memory.comment,
                        style = MaterialTheme.typography.bodySmall,
                        color = IeumColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = memory.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = IeumColors.TextSecondary.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * 추억 상세 바텀시트
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemoryDetailSheet(
    memory: Memory,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // 이미지
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(memory.color.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    tint = memory.color,
                    modifier = Modifier.size(64.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 장소명
            Text(
                text = memory.placeName,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = IeumColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 위치 정보
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Place,
                    contentDescription = null,
                    tint = IeumColors.TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = memory.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = IeumColors.TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 한 줄 코멘트
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = IeumColors.Primary.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = "\"${memory.comment}\"",
                    style = MaterialTheme.typography.bodyLarge,
                    color = IeumColors.TextPrimary,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 날짜
            Text(
                text = memory.date,
                style = MaterialTheme.typography.labelMedium,
                color = IeumColors.TextSecondary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 액션 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Edit, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("수정")
                }
                
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IeumColors.Primary
                    )
                ) {
                    Icon(Icons.Outlined.Share, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("공유")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ViewMode label과 icon을 위한 확장 함수
val ViewMode.label: String
    get() = when(this) {
        ViewMode.MAP -> "지도"
        ViewMode.GALLERY -> "갤러리"
        ViewMode.TIMELINE -> "타임라인"
    }

val ViewMode.icon: ImageVector
    get() = when(this) {
        ViewMode.MAP -> Icons.Outlined.Map
        ViewMode.GALLERY -> Icons.Outlined.GridView
        ViewMode.TIMELINE -> Icons.Outlined.Timeline
    }

// 데이터 클래스 (UI용)
data class Memory(
    val id: Int,
    val placeName: String,
    val address: String,
    val comment: String,
    val date: String,
    val latitude: Double,
    val longitude: Double,
    val color: Color,
    val imageUrl: String? = null
)

// 샘플 데이터
private val sampleMemories = listOf(
    Memory(1, "성수동 카페", "서울 성동구 성수동", "우리의 첫 데이트 장소 ❤️", "2026-02-14", 37.5445, 127.0567, IeumColors.Primary),
    Memory(2, "한강공원", "서울 영등포구 여의도동", "피크닉하던 날", "2026-02-10", 37.5284, 126.9343, IeumColors.Secondary),
    Memory(3, "남산타워", "서울 용산구 남산공원길", "야경이 예뻤던 곳", "2026-01-25", 37.5512, 126.9882, IeumColors.Accent),
    Memory(4, "홍대 거리", "서울 마포구 서교동", "맛있는 거 많이 먹은 날", "2026-01-15", 37.5563, 126.9237, IeumColors.CategoryFood),
    Memory(5, "경복궁", "서울 종로구 세종로", "한복 입고 산책", "2025-12-20", 37.5796, 126.9770, IeumColors.CategoryCulture)
)
