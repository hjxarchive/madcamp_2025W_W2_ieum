package com.ieum.presentation.feature.bucket

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ieum.presentation.theme.IeumColors

/**
 * 버킷리스트 화면
 * 함께 이루고 싶은 목표 리스트 관리
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BucketListScreen(
    modifier: Modifier = Modifier,
    viewModel: BucketViewModel = hiltViewModel()
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(BucketFilter.ALL) }
    
    // 일단 로컬 샘플 데이터 사용 (ViewModel 적용은 추후)
    val bucketItems = remember { mutableStateListOf(*sampleBucketItems.toTypedArray()) }
    
    val completedCount = bucketItems.count { it.isCompleted }
    val totalCount = bucketItems.size
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IeumColors.Background)
    ) {
        // 상단 헤더
        BucketHeader(
            completedCount = completedCount,
            totalCount = totalCount,
            onAddClick = { showAddDialog = true }
        )
        
        // 진행률 카드
        ProgressCard(
            completed = completedCount,
            total = totalCount
        )
        
        // 필터 탭
        FilterRow(
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it }
        )
        
        // 버킷리스트 목록
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val filteredItems = when (selectedFilter) {
                BucketFilter.ALL -> bucketItems
                BucketFilter.IN_PROGRESS -> bucketItems.filter { !it.isCompleted }
                BucketFilter.COMPLETED -> bucketItems.filter { it.isCompleted }
            }
            
            items(filteredItems, key = { it.id }) { item ->
                BucketItemCard(
                    item = item,
                    onToggleComplete = { 
                        val index = bucketItems.indexOfFirst { it.id == item.id }
                        if (index != -1) {
                            bucketItems[index] = item.copy(isCompleted = !item.isCompleted)
                        }
                    },
                    onDelete = {
                        bucketItems.removeIf { it.id == item.id }
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
    
    // 버킷리스트 추가 다이얼로그
    if (showAddDialog) {
        AddBucketDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, category ->
                bucketItems.add(
                    BucketItem(
                        id = bucketItems.size + 1,
                        title = title,
                        category = category,
                        isCompleted = false,
                        createdAt = "2026.02.16"
                    )
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun BucketHeader(
    completedCount: Int,
    totalCount: Int,
    onAddClick: () -> Unit
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
            Column {
                Text(
                    text = "버킷리스트",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = IeumColors.TextPrimary
                )
                Text(
                    text = "함께 이루고 싶은 목표들",
                    style = MaterialTheme.typography.bodySmall,
                    color = IeumColors.TextSecondary
                )
            }
            
            FilledIconButton(
                onClick = onAddClick,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = IeumColors.Primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "추가",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun ProgressCard(
    completed: Int,
    total: Int
) {
    val progress = if (total > 0) completed.toFloat() / total else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "progress"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "우리의 목표 달성률",
                        style = MaterialTheme.typography.labelLarge,
                        color = IeumColors.TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "${(animatedProgress * 100).toInt()}",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = IeumColors.Primary
                        )
                        Text(
                            text = "%",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = IeumColors.Primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
                
                // 원형 진행률
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 8.dp,
                        color = IeumColors.Primary.copy(alpha = 0.2f)
                    )
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 8.dp,
                        color = IeumColors.Primary
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$completed",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = IeumColors.Primary
                        )
                        Text(
                            text = "/ $total",
                            style = MaterialTheme.typography.labelSmall,
                            color = IeumColors.TextSecondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 프로그레스 바
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = IeumColors.Primary,
                trackColor = IeumColors.Primary.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
private fun FilterRow(
    selectedFilter: BucketFilter,
    onFilterSelected: (BucketFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BucketFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = IeumColors.Primary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BucketItemCard(
    item: BucketItem,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (item.isCompleted) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted) 
                IeumColors.Success.copy(alpha = 0.1f) 
            else 
                Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (item.isCompleted) 0.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 체크박스
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggleComplete() },
                colors = CheckboxDefaults.colors(
                    checkedColor = IeumColors.Success,
                    uncheckedColor = IeumColors.TextSecondary
                )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 콘텐츠
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (item.isCompleted) 
                            TextDecoration.LineThrough 
                        else 
                            TextDecoration.None
                    ),
                    color = if (item.isCompleted) 
                        IeumColors.TextSecondary 
                    else 
                        IeumColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 카테고리 칩
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = item.category.color.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = item.category.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = item.category.color,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = item.createdAt,
                        style = MaterialTheme.typography.labelSmall,
                        color = IeumColors.TextSecondary.copy(alpha = 0.7f)
                    )
                }
            }
            
            // 더보기 메뉴
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "더보기",
                        tint = IeumColors.TextSecondary
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("수정") },
                        onClick = { showMenu = false },
                        leadingIcon = {
                            Icon(Icons.Outlined.Edit, null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("삭제", color = IeumColors.Error) },
                        onClick = {
                            onDelete()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.Delete, null, tint = IeumColors.Error)
                        }
                    )
                }
            }
        }
        
        // 완료 시 축하 효과
        if (item.isCompleted) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Celebration,
                    contentDescription = null,
                    tint = IeumColors.Success,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "목표 달성! 🎉",
                    style = MaterialTheme.typography.labelSmall,
                    color = IeumColors.Success
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBucketDialog(
    onDismiss: () -> Unit,
    onAdd: (String, BucketCategory) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(BucketCategory.TRAVEL) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "버킷리스트 추가",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("목표를 입력하세요") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IeumColors.Primary,
                        focusedLabelColor = IeumColors.Primary
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "카테고리",
                    style = MaterialTheme.typography.labelLarge,
                    color = IeumColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BucketCategory.entries.take(3).forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category.label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = category.color,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BucketCategory.entries.drop(3).forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category.label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = category.color,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (title.isNotBlank()) {
                        onAdd(title, selectedCategory)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IeumColors.Primary
                )
            ) {
                Text("추가하기")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = IeumColors.TextSecondary)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

// 필터 타입
enum class BucketFilter(val label: String) {
    ALL("전체"),
    IN_PROGRESS("진행중"),
    COMPLETED("완료")
}

// 카테고리
enum class BucketCategory(val label: String, val color: Color) {
    TRAVEL("여행", IeumColors.CategoryTravel),
    FOOD("맛집", IeumColors.CategoryFood),
    CULTURE("문화", IeumColors.CategoryCulture),
    ACTIVITY("액티비티", IeumColors.Accent),
    SPECIAL("특별한날", IeumColors.Primary)
}

// 데이터 클래스 (UI용 - ViewModel 적용 후 domain 모델로 대체 예정)
data class BucketItem(
    val id: Int,
    val title: String,
    val category: BucketCategory,
    val isCompleted: Boolean,
    val createdAt: String,
    val completedAt: String? = null
)

// 샘플 데이터
private val sampleBucketItems = listOf(
    BucketItem(1, "제주도 여행 가기 ✈️", BucketCategory.TRAVEL, false, "2026.01.15"),
    BucketItem(2, "한강에서 피크닉하기 🧺", BucketCategory.ACTIVITY, true, "2026.01.10", "2026.02.14"),
    BucketItem(3, "미쉐린 레스토랑 가보기 🍽️", BucketCategory.FOOD, false, "2026.01.20"),
    BucketItem(4, "뮤지컬 함께 보기 🎭", BucketCategory.CULTURE, true, "2026.01.05", "2026.02.10"),
    BucketItem(5, "100일 기념 커플링 맞추기 💍", BucketCategory.SPECIAL, true, "2025.12.20", "2026.01.25"),
    BucketItem(6, "스키장 가기 ⛷️", BucketCategory.ACTIVITY, false, "2026.02.01"),
    BucketItem(7, "부산 해운대 일출 보기 🌅", BucketCategory.TRAVEL, false, "2026.02.05"),
    BucketItem(8, "요리 클래스 같이 듣기 👨‍🍳", BucketCategory.ACTIVITY, false, "2026.02.10")
)
