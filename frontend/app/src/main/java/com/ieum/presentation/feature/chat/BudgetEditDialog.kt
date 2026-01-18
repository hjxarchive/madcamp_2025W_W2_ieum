package com.ieum.presentation.feature.chat

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ieum.presentation.theme.IeumColors
import androidx.compose.foundation.layout.*

@Composable
fun BudgetEditDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "이번 달 예산 설정",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    "새로운 예산 금액을 입력해주세요.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) amountText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("예산 금액") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = IeumColors.Primary,
                        cursorColor = IeumColors.Primary,
                        focusedLabelColor = IeumColors.Primary
                    ),
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toIntOrNull()
                    if (amount != null) {
                        onConfirm(amount)
                    }
                }
            ) {
                Text("수정", color = IeumColors.Primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = Color.Gray)
            }
        },
        containerColor = Color.White
    )
}
