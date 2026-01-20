package com.ieum.presentation.feature.finance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ieum.presentation.theme.IeumColors
import com.ieum.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetPlanningScreen(
    modifier: Modifier = Modifier,
    viewModel: FinanceViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val budget by viewModel.budget.collectAsStateWithLifecycle()
    val isAiSuggesting by viewModel.isAiSuggesting.collectAsStateWithLifecycle()
    val suggestedCategories by viewModel.suggestedCategories.collectAsStateWithLifecycle()
    
    var isEditing by remember { mutableStateOf(false) }
    var editedCategories by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var showWarning by remember { mutableStateOf(false) }
    var warningMessage by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("이 달의 예산", fontWeight = FontWeight.Bold, color = Color(0xFF5A3E2B)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "이번 달 예산을 정해볼까요?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5A3E2B) // Changed to brown
            )
            
            Spacer(modifier = Modifier.height(40.dp))

            // Budget Slider Section
            Text(
                text = "${String.format("%,d", budget)}원",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5A3E2B) // Changed to brown
            )

            Spacer(modifier = Modifier.height(20.dp))

            Slider(
                value = budget.toFloat(),
                onValueChange = { viewModel.setBudget(it.toInt()) },
                valueRange = 300000f..1000000f,
                steps = 6, // 300k to 1M in 100k increments
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF5A3E2B), // Changed to brown
                    activeTrackColor = Color(0xFF5A3E2B).copy(alpha = 0.6f), // Changed to brown
                    inactiveTrackColor = Color(0xFF5A3E2B).copy(alpha=0.2f) // Changed to lighter brown
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Confirm Button
            Button(
                onClick = { viewModel.confirmBudgetAndSuggest() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE6C8A0)) // Changed to beige
            ) {
                if (isAiSuggesting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("AI가 예산을 배분 중입니다...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text("확정하기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // AI Suggestion Result
            AnimatedVisibility(visible = suggestedCategories.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "AI 제안 예산",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    suggestedCategories.forEach { (category, amount) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category, fontWeight = FontWeight.Medium)
                            
                            if (isEditing) {
                                // 편집 모드: TextField
                                OutlinedTextField(
                                    value = editedCategories[category]?.toString() ?: amount.toString(),
                                    onValueChange = { newValue ->
                                        val intValue = newValue.filter { it.isDigit() }.toIntOrNull() ?: 0
                                        editedCategories = editedCategories + (category to intValue)
                                    },
                                    modifier = Modifier.width(150.dp),
                                    suffix = { Text("원") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            } else {
                                // 보기 모드: 텍스트
                                Text(
                                    "${String.format("%,d", editedCategories[category] ?: amount)}원", 
                                    fontWeight = FontWeight.Bold, 
                                    color = IeumColors.TextPrimary
                                )
                            }
                        }
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    OutlinedButton(
                        onClick = {
                            if (isEditing) {
                                // 저장하기 로직
                                val currentCategories = if (editedCategories.isEmpty()) suggestedCategories else editedCategories
                                val total = currentCategories.values.sum()
                                
                                when {
                                    total > budget -> {
                                        warningMessage = "예산보다 ${String.format("%,d", total - budget)}원 많습니다."
                                        showWarning = true
                                    }
                                    total < budget -> {
                                        warningMessage = "예산보다 ${String.format("%,d", budget - total)}원 적습니다."
                                        showWarning = true
                                    }
                                    else -> {
                                        // 저장 성공
                                        isEditing = false
                                        showWarning = false
                                    }
                                }
                            } else {
                                // 세부 조정하기 로직
                                if (editedCategories.isEmpty()) {
                                    editedCategories = suggestedCategories.toMap()
                                }
                                isEditing = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF5A3E2B))
                    ) {
                        Text(
                            if (isEditing) "저장하기" else "세부 조정하기", 
                            color = Color(0xFF5A3E2B)
                        )
                    }
                    
                    // 경고 메시지
                    if (showWarning) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = warningMessage,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                }
            }
        }
    }
}
