package com.ieum.presentation.feature.finance

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ieum.presentation.theme.IeumColors
import java.text.NumberFormat
import java.util.*

/**
 * 재정 관리 화면
 * PDF 기반: 이 달의 소비, 예산 설정, 카테고리별 소비
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    modifier: Modifier = Modifier,
    viewModel: FinanceViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var currentBudget by remember { mutableIntStateOf(50000) }
    
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IeumColors.Background)
    ) {
        // 상단 헤더
        FinanceHeader(
            onSettingsClick = { showBudgetDialog = true }
        )
        
        // 탭 선택
        FinanceTabRow(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            when (selectedTab) {
                0 -> MonthlySpendingContent(budget = currentBudget)
                1 -> TotalSpendingContent()
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
    
    // 예산 설정 다이얼로그
    if (showBudgetDialog) {
        BudgetSettingDialog(
            currentBudget = currentBudget,
            onDismiss = { showBudgetDialog = false },
            onConfirm = { newBudget ->
                currentBudget = newBudget
                showBudgetDialog = false
            }
        )
    }
}

@Composable
private fun FinanceHeader(
    onSettingsClick: () -> Unit
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
            Text(
                text = "재정 관리",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = IeumColors.TextPrimary
            )
            
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "설정",
                    tint = IeumColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun FinanceTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("이 달의 소비", "전체 소비")
    
    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = IeumColors.Background,
        contentColor = IeumColors.Primary
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                },
                selectedContentColor = IeumColors.Primary,
                unselectedContentColor = IeumColors.TextSecondary
            )
        }
    }
}

/**
 * 이 달의 소비 탭 내용
 */
@Composable
private fun MonthlySpendingContent(budget: Int) {
    val totalSpent = 32000
    val remainingBudget = budget - totalSpent
    val spentPercentage = (totalSpent.toFloat() / budget * 100).coerceIn(0f, 100f)
    
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // 예산 카드
        BudgetCard(
            budget = budget,
            spent = totalSpent,
            remaining = remainingBudget,
            percentage = spentPercentage
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 카테고리별 소비
        CategorySpendingSection()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 최근 지출 내역
        RecentExpenseSection()
    }
}

@Composable
private fun BudgetCard(
    budget: Int,
    spent: Int,
    remaining: Int,
    percentage: Float
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "이번 달 예산",
                style = MaterialTheme.typography.labelLarge,
                color = IeumColors.TextSecondary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${numberFormat.format(budget)}원",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = IeumColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 원형 프로그레스
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // 배경 원
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 12.dp,
                    color = IeumColors.Primary.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )
                
                // 진행 원
                CircularProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 12.dp,
                    color = if (percentage > 80) IeumColors.Error else IeumColors.Primary,
                    strokeCap = StrokeCap.Round
                )
                
                // 중앙 텍스트
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${percentage.toInt()}%",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = IeumColors.TextPrimary
                    )
                    Text(
                        text = "사용",
                        style = MaterialTheme.typography.labelMedium,
                        color = IeumColors.TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 사용/잔여 금액
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "사용 금액",
                        style = MaterialTheme.typography.labelMedium,
                        color = IeumColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${numberFormat.format(spent)}원",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = IeumColors.Primary
                    )
                }
                
                Divider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp),
                    color = IeumColors.TextSecondary.copy(alpha = 0.2f)
                )
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "남은 금액",
                        style = MaterialTheme.typography.labelMedium,
                        color = IeumColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${numberFormat.format(remaining)}원",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (remaining > 0) IeumColors.Success else IeumColors.Error
                    )
                }
            }
        }
    }
}

/**
 * 카테고리별 소비 섹션
 */
@Composable
private fun CategorySpendingSection() {
    val categories = listOf(
        ExpenseCategory("식비", 15000, IeumColors.CategoryFood),
        ExpenseCategory("카페", 8000, IeumColors.CategoryCafe),
        ExpenseCategory("술", 5000, IeumColors.CategoryDrink),
        ExpenseCategory("문화생활", 4000, IeumColors.CategoryCulture),
        ExpenseCategory("여행", 0, IeumColors.CategoryTravel)
    )
    
    Column {
        Text(
            text = "카테고리별 소비",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = IeumColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                categories.forEachIndexed { index, category ->
                    CategorySpendingItem(category = category)
                    if (index < categories.lastIndex) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySpendingItem(category: ExpenseCategory) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 카테고리 색상 인디케이터
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(category.color)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium,
            color = IeumColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = "${numberFormat.format(category.amount)}원",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = if (category.amount > 0) IeumColors.TextPrimary else IeumColors.TextSecondary
        )
    }
}

/**
 * 최근 지출 내역 섹션
 */
@Composable
private fun RecentExpenseSection() {
    val expenses = listOf(
        Expense(1, "점심 식사", "식비", 12000, "2026.02.15"),
        Expense(2, "카페 라떼", "카페", 5500, "2026.02.14"),
        Expense(3, "맥주", "술", 8000, "2026.02.13"),
        Expense(4, "영화 관람", "문화생활", 15000, "2026.02.12")
    )
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "최근 지출",
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
        
        expenses.forEach { expense ->
            ExpenseItem(expense = expense)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ExpenseItem(expense: Expense) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
    val categoryColor = when (expense.category) {
        "식비" -> IeumColors.CategoryFood
        "카페" -> IeumColors.CategoryCafe
        "술" -> IeumColors.CategoryDrink
        "문화생활" -> IeumColors.CategoryCulture
        "여행" -> IeumColors.CategoryTravel
        else -> IeumColors.TextSecondary
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 카테고리 아이콘
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(categoryColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (expense.category) {
                        "식비" -> Icons.Outlined.Restaurant
                        "카페" -> Icons.Outlined.LocalCafe
                        "술" -> Icons.Outlined.LocalBar
                        "문화생활" -> Icons.Outlined.TheaterComedy
                        "여행" -> Icons.Outlined.Flight
                        else -> Icons.Outlined.Receipt
                    },
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = IeumColors.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = expense.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = IeumColors.TextSecondary
                )
            }
            
            Text(
                text = "-${numberFormat.format(expense.amount)}원",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = IeumColors.Error
            )
        }
    }
}

/**
 * 전체 소비 탭 내용
 */
@Composable
private fun TotalSpendingContent() {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // 월별 통계
        MonthlyStatistics()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 올해 총 지출
        YearlyTotalCard()
    }
}

@Composable
private fun MonthlyStatistics() {
    val monthlyData = listOf(
        MonthlySpending("1월", 45000),
        MonthlySpending("2월", 32000),
        MonthlySpending("3월", 0),
        MonthlySpending("4월", 0),
        MonthlySpending("5월", 0),
        MonthlySpending("6월", 0)
    )
    val maxSpending = monthlyData.maxOf { it.amount }.coerceAtLeast(1)
    
    Column {
        Text(
            text = "월별 소비 현황",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = IeumColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    monthlyData.forEach { data ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 바 그래프
                            Box(
                                modifier = Modifier
                                    .width(32.dp)
                                    .height(
                                        ((data.amount.toFloat() / maxSpending) * 100)
                                            .coerceAtLeast(4f).dp
                                    )
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(
                                        if (data.amount > 0) IeumColors.Primary
                                        else IeumColors.TextSecondary.copy(alpha = 0.2f)
                                    )
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = data.month,
                                style = MaterialTheme.typography.labelSmall,
                                color = IeumColors.TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YearlyTotalCard() {
    val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)
    val totalAmount = 77000
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = IeumColors.Primary.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "2026년 총 지출",
                style = MaterialTheme.typography.labelLarge,
                color = IeumColors.TextSecondary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${numberFormat.format(totalAmount)}원",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = IeumColors.Primary
            )
        }
    }
}

/**
 * 예산 설정 다이얼로그
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetSettingDialog(
    currentBudget: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var budgetText by remember { mutableStateOf(currentBudget.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "이번 달 예산을 정해볼까요?",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        },
        text = {
            Column {
                Text(
                    text = "월간 데이트 비용 한도를 설정하세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = IeumColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it.filter { c -> c.isDigit() } },
                    label = { Text("예산 금액") },
                    suffix = { Text("원") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IeumColors.Primary,
                        focusedLabelColor = IeumColors.Primary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    budgetText.toIntOrNull()?.let { onConfirm(it) }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = IeumColors.Primary
                )
            ) {
                Text("확인하기")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "취소",
                    color = IeumColors.TextSecondary
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

// 데이터 클래스들
data class ExpenseCategory(
    val name: String,
    val amount: Int,
    val color: Color
)

data class Expense(
    val id: Int,
    val title: String,
    val category: String,
    val amount: Int,
    val date: String
)

data class MonthlySpending(
    val month: String,
    val amount: Int
)
