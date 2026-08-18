package com.coinflow.expensetracker.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coinflow.expensetracker.data.model.Expense
import com.coinflow.expensetracker.ui.components.GlassCard
import com.coinflow.expensetracker.ui.components.SyncStatusBanner
import com.coinflow.expensetracker.ui.theme.BackgroundDark
import com.coinflow.expensetracker.ui.theme.BorderOutline
import com.coinflow.expensetracker.ui.theme.PrimaryCyanBright
import com.coinflow.expensetracker.ui.theme.SurfaceContainer
import com.coinflow.expensetracker.ui.theme.TextPrimary
import com.coinflow.expensetracker.ui.theme.TextVariant
import com.coinflow.expensetracker.ui.viewmodel.ExpenseViewModel

@Composable
fun ExpenseListScreen(
    viewModel: ExpenseViewModel,
    onEditExpenseClick: (Expense) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val expenses by viewModel.filteredExpenses.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    val categories = listOf("All", "Food", "Fuel", "Shop", "Bills", "Transport", "Entertainment", "Health", "Others")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "EXPENSES & SEARCH",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = PrimaryCyanBright
        )

        Spacer(modifier = Modifier.height(12.dp))

        SyncStatusBanner(
            syncState = syncState,
            onRefreshClick = { viewModel.syncFromGist() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search description, category, UPI...", color = TextVariant) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = PrimaryCyanBright
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = TextVariant
                        )
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryCyanBright,
                unfocusedBorderColor = BorderOutline,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                val chipBg = if (isSelected) PrimaryCyanBright.copy(alpha = 0.2f) else SurfaceContainer
                val chipBorder = if (isSelected) PrimaryCyanBright else BorderOutline

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(chipBg)
                        .border(1.dp, chipBorder, RoundedCornerShape(20.dp))
                        .clickable { viewModel.setCategoryFilter(category) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = category,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (isSelected) PrimaryCyanBright else TextVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // List Header Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${expenses.size} Transactions",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = TextVariant
            )
            Text(
                text = "Total: ${viewModel.formatINR(expenses.sumOf { it.amount })}",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = PrimaryCyanBright
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Expenses List
        if (expenses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "No Expenses",
                        tint = TextVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No matching expenses found",
                        color = TextVariant,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(expenses) { expense ->
                    ExpenseItemCard(
                        expense = expense,
                        formatINR = { viewModel.formatINR(it) },
                        onClick = { onEditExpenseClick(expense) }
                    )
                }
                item { Spacer(modifier = Modifier.height(90.dp)) }
            }
        }
    }
}
