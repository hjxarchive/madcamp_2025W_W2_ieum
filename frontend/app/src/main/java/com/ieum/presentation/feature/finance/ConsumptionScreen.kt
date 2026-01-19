package com.ieum.presentation.feature.finance

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ieum.domain.model.Expense
import com.ieum.presentation.theme.IeumColors
import com.ieum.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// User requested global color
val MainBrown = Color(0xFF5A3E2B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumptionScreen(
    modifier: Modifier = Modifier,
    viewModel: FinanceViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val expenses by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val totalAmount by viewModel.thisMonthTotal.collectAsStateWithLifecycle()
    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    
    // Check if next month has history (simple logic: current month < actual current month? or just let it show empty)
    // User requirement: "next month shows no history" -> naturally handled by filtering if no data.

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "이 달의 소비", 
                        fontWeight = FontWeight.Bold,
                        color = MainBrown
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MainBrown)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = IeumColors.Background
                )
            )
        },
        containerColor = IeumColors.Background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image
            Image(
                painter = painterResource(id = R.drawable.background2),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            // Month Navigation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                IconButton(onClick = { viewModel.previousMonth() }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Prev", tint = MainBrown)
                }
                Text(
                    text = "${currentMonth.monthValue}월",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MainBrown,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = MainBrown)
                }
            }

            // Summary Card
            ConsumptionSummaryCard(totalAmount, expenses)
            
            Spacer(modifier = Modifier.height(16.dp))

            // Expense List
            ExpenseList(expenses)
            }
        }
    }
}

@Composable
private fun ConsumptionSummaryCard(totalAmount: Int, expenses: List<Expense>) {
    // Removed Card wrapper - no white background
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "총 지출액",
            style = MaterialTheme.typography.titleMedium,
            color = MainBrown
        )
        Text(
            text = "${String.format("%,d", totalAmount)}원",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MainBrown
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Pie Chart - Centered
        Box(
            modifier = Modifier
                .size(220.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if(expenses.isNotEmpty()) {
                DonutChart(expenses)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(4.dp, Color.LightGray.copy(alpha=0.3f), CircleShape)
                )
                Text("지출 내역 없음", color = MainBrown)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Category Legend
        if(expenses.isNotEmpty()) {
            CategoryLegend(expenses)
        }
    }
}

@Composable
private fun DonutChart(expenses: List<Expense>) {
    val categoryTotals = expenses.groupBy { it.category }
        .mapValues { it.value.sumOf { e -> e.amount } }
    val total = categoryTotals.values.sum().toFloat()
    
    // Ensure "Photo" and other categories have correct colors from the Enum definition
    // We can rely on ExpenseCategory.colorHex, parsing it:
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        var startAngle = -90f
        val strokeWidth = 40.dp.toPx() // Thicker for donut
        
        categoryTotals.entries.sortedByDescending { it.value }.forEach { entry ->
            val sweepAngle = if (total > 0) (entry.value / total) * 360f else 0f
            val colorHex = entry.key.colorHex
            val color = try {
                Color(android.graphics.Color.parseColor(colorHex))
            } catch (e: Exception) { Color.Gray }
            
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth),
                topLeft = Offset(strokeWidth/2, strokeWidth/2),
                size = Size(size.width - strokeWidth, size.height - strokeWidth)
            )
            startAngle += sweepAngle
        }
    }
}


@Composable
private fun CategoryLegend(expenses: List<Expense>) {
    val categoryTotals = expenses.groupBy { it.category }
        .mapValues { it.value.sumOf { e -> e.amount } }
    val total = categoryTotals.values.sum().toFloat()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categoryTotals.entries.sortedByDescending { it.value }.forEach { entry ->
            val percentage = if (total > 0) (entry.value / total * 100).toInt() else 0
            val colorHex = entry.key.colorHex
            val color = try {
                Color(android.graphics.Color.parseColor(colorHex))
            } catch (e: Exception) { Color.Gray }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(color, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = entry.key.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MainBrown
                    )
                }
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MainBrown
                )
            }
        }
    }
}

@Composable
private fun ExpenseList(expenses: List<Expense>) {
    // Group by Date
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    val groupedExpenses = expenses
        .mapNotNull { 
            try {
                val date = LocalDate.parse(it.date, formatter)
                date to it
            } catch (e: Exception) { null }
        }
        .sortedByDescending { it.first }
        .groupBy { it.first }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        if(expenses.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                     Text("소비 내역이 없습니다.", color = Color.Gray)
                }
            }
        }
        
        groupedExpenses.forEach { (date, dailyExpenses) ->
            item {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MainBrown, // Requested color
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
            items(dailyExpenses.map { it.second }) { expense ->
                ExpenseItemRow(expense)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ExpenseItemRow(expense: Expense) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = expense.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = expense.category.label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        Text(
            text = "-${String.format("%,d", expense.amount)}원",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MainBrown // Requested color
        )
    }
}
