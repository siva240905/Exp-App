package com.coinflow.expensetracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coinflow.expensetracker.data.model.Expense
import com.coinflow.expensetracker.ui.components.CustomKeypad
import com.coinflow.expensetracker.ui.theme.BackgroundDark
import com.coinflow.expensetracker.ui.theme.BorderOutline
import com.coinflow.expensetracker.ui.theme.ErrorRed
import com.coinflow.expensetracker.ui.theme.PrimaryCyanBright
import com.coinflow.expensetracker.ui.theme.SecondaryPink
import com.coinflow.expensetracker.ui.theme.SurfaceContainer
import com.coinflow.expensetracker.ui.theme.TextPrimary
import com.coinflow.expensetracker.ui.theme.TextVariant
import com.coinflow.expensetracker.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddEditExpenseScreen(
    viewModel: ExpenseViewModel,
    expenseToEdit: Expense? = null,
    initialType: String = Expense.TYPE_SEND,
    onDismiss: () -> Unit
) {
    var amountStr by remember { mutableStateOf(expenseToEdit?.amount?.let { if (it == 0.0) "0" else it.toString() } ?: "0") }
    var selectedType by remember { mutableStateOf(expenseToEdit?.type ?: initialType) }
    var selectedCategory by remember { mutableStateOf(expenseToEdit?.category ?: "Food") }
    var selectedPaymentMethod by remember { mutableStateOf(expenseToEdit?.paymentMethod ?: "UPI") }
    var description by remember { mutableStateOf(expenseToEdit?.description ?: "") }
    var dateStr by remember { mutableStateOf(expenseToEdit?.date ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }

    var showSuccessOverlay by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val categories = listOf(
        CategoryItem("Food", Icons.Default.Fastfood),
        CategoryItem("Fuel", Icons.Default.LocalGasStation),
        CategoryItem("Shop", Icons.Default.ShoppingBag),
        CategoryItem("Bills", Icons.Default.Receipt)
    )

    val paymentMethods = listOf("UPI", "Cash", "Credit Card", "Debit Card", "Net Banking")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextVariant
                    )
                }

                Text(
                    text = if (expenseToEdit == null) "NEW ENTRY" else "EDIT ENTRY",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryCyanBright,
                    letterSpacing = 2.sp
                )

                if (expenseToEdit != null) {
                    IconButton(
                        onClick = {
                            viewModel.deleteExpense(expenseToEdit.id)
                            onDismiss()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = ErrorRed
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // Type Toggle (SEND vs RECEIVE)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val isSend = selectedType == Expense.TYPE_SEND
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSend) PrimaryCyanBright.copy(alpha = 0.25f) else SurfaceContainer)
                        .border(1.dp, if (isSend) PrimaryCyanBright else BorderOutline, RoundedCornerShape(12.dp))
                        .clickable { selectedType = Expense.TYPE_SEND }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowOutward,
                            contentDescription = "Send",
                            tint = if (isSend) PrimaryCyanBright else TextVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SEND (Debit)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isSend) PrimaryCyanBright else TextVariant
                        )
                    }
                }

                val isReceive = selectedType == Expense.TYPE_RECEIVE
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isReceive) SecondaryPink.copy(alpha = 0.25f) else SurfaceContainer)
                        .border(1.dp, if (isReceive) SecondaryPink else BorderOutline, RoundedCornerShape(12.dp))
                        .clickable { selectedType = Expense.TYPE_RECEIVE }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Receive",
                            tint = if (isReceive) SecondaryPink else TextVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RECEIVE (Credit)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isReceive) SecondaryPink else TextVariant
                        )
                    }
                }
            }

            // Amount Display & Category Selection
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "AMOUNT",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextVariant,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "₹",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextVariant,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = if (amountStr == "0") "0.00" else amountStr,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (selectedType == Expense.TYPE_RECEIVE) SecondaryPink else PrimaryCyanBright
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Chips
                Text(
                    text = "CATEGORY",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextVariant,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { item ->
                        val isSelected = selectedCategory.equals(item.name, ignoreCase = true)
                        val chipBg = if (isSelected) PrimaryCyanBright.copy(alpha = 0.2f) else SurfaceContainer
                        val chipBorder = if (isSelected) PrimaryCyanBright else BorderOutline
                        val contentColor = if (isSelected) PrimaryCyanBright else TextVariant

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(chipBg)
                                .border(1.dp, chipBorder, RoundedCornerShape(12.dp))
                                .clickable { selectedCategory = item.name }
                                .padding(vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.name,
                                tint = if (isSelected) PrimaryCyanBright else SecondaryPink,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.name,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = contentColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Method Selector
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(paymentMethods) { method ->
                        val isSelected = selectedPaymentMethod == method
                        val chipBg = if (isSelected) PrimaryCyanBright.copy(alpha = 0.2f) else SurfaceContainer
                        val chipBorder = if (isSelected) PrimaryCyanBright else BorderOutline

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(chipBg)
                                .border(1.dp, chipBorder, RoundedCornerShape(20.dp))
                                .clickable { selectedPaymentMethod = method }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = method,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (isSelected) PrimaryCyanBright else TextVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Note / Description (Optional)", color = TextVariant) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryCyanBright,
                        unfocusedBorderColor = BorderOutline,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Numeric Keypad & Submit Action
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainer.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                CustomKeypad(
                    onKeyClick = { key ->
                        if (amountStr == "0" && key != ".") {
                            amountStr = key
                        } else if (amountStr.length < 8) {
                            if (key == "." && amountStr.contains(".")) return@CustomKeypad
                            amountStr += key
                        }
                    },
                    onDeleteClick = {
                        if (amountStr.length > 1) {
                            amountStr = amountStr.dropLast(1)
                        } else {
                            amountStr = "0"
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val amount = amountStr.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            if (expenseToEdit == null) {
                                viewModel.addExpense(
                                    amount = amount,
                                    category = selectedCategory,
                                    description = description,
                                    date = dateStr,
                                    paymentMethod = selectedPaymentMethod,
                                    type = selectedType
                                )
                            } else {
                                viewModel.updateExpense(
                                    expenseToEdit.copy(
                                        amount = amount,
                                        category = selectedCategory,
                                        description = description,
                                        paymentMethod = selectedPaymentMethod,
                                        type = selectedType
                                    )
                                )
                            }

                            showSuccessOverlay = true
                            scope.launch {
                                delay(1200)
                                showSuccessOverlay = false
                                onDismiss()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedType == Expense.TYPE_RECEIVE) SecondaryPink else PrimaryCyanBright
                    )
                ) {
                    Text(
                        text = if (expenseToEdit == null) "CONFIRM TRANSACTION" else "SAVE CHANGES",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = BackgroundDark,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Success State Overlay Screen
        AnimatedVisibility(
            visible = showSuccessOverlay,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(PrimaryCyanBright.copy(alpha = 0.2f))
                            .border(2.dp, PrimaryCyanBright, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = PrimaryCyanBright,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Logged Successfully",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Transaction synced to cloud ledger.",
                        fontSize = 14.sp,
                        color = TextVariant
                    )
                }
            }
        }
    }
}

private data class CategoryItem(val name: String, val icon: ImageVector)
