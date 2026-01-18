package com.ieum.presentation.feature.finance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ieum.presentation.theme.IeumColors

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

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("이 달의 예산", fontWeight = FontWeight.Bold) },
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
                color = IeumColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(40.dp))

            // Budget Slider Section
            Text(
                text = "${String.format("%,d", budget)}원",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = IeumColors.Primary
            )

            Spacer(modifier = Modifier.height(20.dp))

            Slider(
                value = budget.toFloat(),
                onValueChange = { viewModel.setBudget(it.toInt()) },
                valueRange = 100000f..3000000f,
                steps = 28, // (300-10)/10 = 29 steps roughly
                colors = SliderDefaults.colors(
                    thumbColor = IeumColors.Primary,
                    activeTrackColor = IeumColors.Primary,
                    inactiveTrackColor = IeumColors.Primary.copy(alpha=0.3f)
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
                colors = ButtonDefaults.buttonColors(containerColor = IeumColors.Primary)
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
                            Text(
                                "${String.format("%,d", amount)}원", 
                                fontWeight = FontWeight.Bold, 
                                color = IeumColors.TextPrimary
                            )
                        }
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    OutlinedButton(
                        onClick = { /* TODO: Navigate to detail tuning */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, IeumColors.Primary)
                    ) {
                        Text("세부 조정하기", color = IeumColors.Primary)
                    }
                }
            }
        }
    }
}
