package com.coinflow.expensetracker.ui.screens

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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.coinflow.expensetracker.ui.components.GlassCard
import com.coinflow.expensetracker.ui.theme.BackgroundDark
import com.coinflow.expensetracker.ui.theme.PrimaryCyanBright
import com.coinflow.expensetracker.ui.theme.SecondaryPink
import com.coinflow.expensetracker.ui.theme.SecondaryPinkFixed
import com.coinflow.expensetracker.ui.theme.SurfaceContainerHigh
import com.coinflow.expensetracker.ui.theme.TextPrimary
import com.coinflow.expensetracker.ui.theme.TextVariant
import com.coinflow.expensetracker.ui.viewmodel.ExpenseViewModel

@Composable
fun AnalyticsScreen(
    viewModel: ExpenseViewModel
) {
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val todayTotal by viewModel.todayTotal.collectAsState()
    val currentMonthTotal by viewModel.currentMonthTotal.collectAsState()
    val categoryTotals by viewModel.categoryTotals.collectAsState()

    val sortedCategories = categoryTotals.entries.sortedByDescending { it.value }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            Text(
                text = "EXPENSE ANALYTICS",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = PrimaryCyanBright
            )
        }

        // Period Summary Row (Today vs Monthly)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = "Today",
                                tint = PrimaryCyanBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TODAY",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewModel.formatINR(todayTotal),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }
                }

                GlassCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "This Month",
                                tint = SecondaryPink,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "THIS MONTH",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewModel.formatINR(currentMonthTotal),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Category Breakdown Header
        item {
            Text(
                text = "CATEGORY BREAKDOWN",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = TextVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (sortedCategories.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No category data available yet.", color = TextVariant)
                    }
                }
            }
        } else {
            items(sortedCategories) { (category, amount) ->
                val percentage = if (totalExpenses > 0.0) (amount / totalExpenses).toFloat() else 0f

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceContainerHigh),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PieChart,
                                        contentDescription = category,
                                        tint = SecondaryPinkFixed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = category,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = viewModel.formatINR(amount),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = PrimaryCyanBright
                                )
                                Text(
                                    text = "${(percentage * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { percentage },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = PrimaryCyanBright,
                            trackColor = SurfaceContainerHigh,
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(90.dp)) }
    }
}
