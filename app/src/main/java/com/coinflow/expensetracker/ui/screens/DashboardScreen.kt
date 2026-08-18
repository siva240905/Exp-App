package com.coinflow.expensetracker.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coinflow.expensetracker.R
import com.coinflow.expensetracker.data.model.Expense
import com.coinflow.expensetracker.ui.components.GlassCard
import com.coinflow.expensetracker.ui.components.SyncStatusBanner
import com.coinflow.expensetracker.ui.theme.BackgroundDark
import com.coinflow.expensetracker.ui.theme.PrimaryCyanBright
import com.coinflow.expensetracker.ui.theme.PrimaryCyanFixed
import com.coinflow.expensetracker.ui.theme.SecondaryPinkFixed
import com.coinflow.expensetracker.ui.theme.SurfaceContainer
import com.coinflow.expensetracker.ui.theme.SurfaceContainerHigh
import com.coinflow.expensetracker.ui.theme.TextPrimary
import com.coinflow.expensetracker.ui.theme.TextVariant
import com.coinflow.expensetracker.ui.viewmodel.ExpenseViewModel

@Composable
fun DashboardScreen(
    viewModel: ExpenseViewModel,
    onAddExpenseClick: () -> Unit,
    onViewAllClick: () -> Unit,
    onEditExpenseClick: (Expense) -> Unit
) {
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val todayTotal by viewModel.todayTotal.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val expenses by viewModel.filteredExpenses.collectAsState()

    val recentExpenses = expenses.take(5)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(12.dp)) }

        // Top Header with Logo & Greeting
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo),
                        contentDescription = "Coin Flow Logo",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Good Day,",
                            fontSize = 14.sp,
                            color = TextVariant,
                            fontFamily = FontFamily.SansSerif
                        )
                        Text(
                            text = "Coin Flow",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.syncFromGist() },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Sync",
                        tint = PrimaryCyanBright
                    )
                }
            }
        }

        // Sync Status Banner
        item {
            SyncStatusBanner(
                syncState = syncState,
                onRefreshClick = { viewModel.syncFromGist() }
            )
        }

        // Balance Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                isGlowing = true
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "TOTAL EXPENSES",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextVariant,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(PrimaryCyanBright)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = viewModel.formatINR(totalExpenses),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = PrimaryCyanBright
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Trending",
                            tint = SecondaryPinkFixed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Today: ${viewModel.formatINR(todayTotal)}",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = SecondaryPinkFixed
                        )
                    }
                }
            }
        }

        // Quick Actions (Sync & Add)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassCard(
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.syncFromGist() }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync",
                                tint = PrimaryCyanBright,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sync Gist",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }

                GlassCard(
                    modifier = Modifier.weight(1f),
                    onClick = onAddExpenseClick
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = SecondaryPinkFixed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Add Entry",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Recent Activity Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT ACTIVITY",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextVariant,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "VIEW ALL",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryCyanFixed,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .padding(4.dp)
                        .background(SurfaceContainer.copy(alpha = 0.5f))
                        .padding(horizontal = 6.dp)
                        .background(BackgroundDark)
                )
            }
        }

        // Recent Activity List Items
        if (recentExpenses.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "No Expenses",
                            tint = TextVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No expenses added yet",
                            color = TextVariant,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onAddExpenseClick,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyanBright)
                        ) {
                            Text(text = "Add First Expense", color = BackgroundDark)
                        }
                    }
                }
            }
        } else {
            items(recentExpenses) { expense ->
                ExpenseItemCard(
                    expense = expense,
                    formatINR = { viewModel.formatINR(it) },
                    onClick = { onEditExpenseClick(expense) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(90.dp)) }
    }
}

@Composable
fun ExpenseItemCard(
    expense: Expense,
    formatINR: (Double) -> String,
    onClick: () -> Unit
) {
    val categoryIcon = when (expense.category.lowercase()) {
        "food" -> Icons.Default.Fastfood
        "fuel" -> Icons.Default.LocalGasStation
        "shop", "shopping" -> Icons.Default.ShoppingBag
        else -> Icons.Default.ArrowOutward
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        cornerRadius = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = expense.category,
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = expense.description.ifBlank { expense.category },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${expense.date} • ${expense.paymentMethod}",
                        fontSize = 12.sp,
                        color = TextVariant
                    )
                }
            }

            Text(
                text = "-${formatINR(expense.amount)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
            )
        }
    }
}
