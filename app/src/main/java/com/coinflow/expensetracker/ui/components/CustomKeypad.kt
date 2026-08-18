package com.coinflow.expensetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coinflow.expensetracker.ui.theme.BorderOutline
import com.coinflow.expensetracker.ui.theme.ErrorRed
import com.coinflow.expensetracker.ui.theme.SurfaceContainer
import com.coinflow.expensetracker.ui.theme.TextPrimary

@Composable
fun CustomKeypad(
    onKeyClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "DEL")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    KeypadButton(
                        text = key,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (key == "DEL") {
                                onDeleteClick()
                            } else {
                                onKeyClick(key)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .height(58.dp)
            .clip(shape)
            .background(SurfaceContainer)
            .border(BorderStroke(1.dp, BorderOutline), shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (text == "DEL") {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Delete",
                tint = ErrorRed
            )
        } else {
            Text(
                text = text,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = if (text == ".") Color.White.copy(alpha = 0.7f) else TextPrimary
            )
        }
    }
}
