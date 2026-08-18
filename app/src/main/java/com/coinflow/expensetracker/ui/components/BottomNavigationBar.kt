package com.coinflow.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coinflow.expensetracker.ui.theme.BorderOutline
import com.coinflow.expensetracker.ui.theme.PrimaryCyanBright
import com.coinflow.expensetracker.ui.theme.SurfaceDark
import com.coinflow.expensetracker.ui.theme.TextVariant

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    object Expenses : Screen("expenses", "Expenses", Icons.Default.AccountBalanceWallet)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.BarChart)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Screen.Dashboard,
        Screen.Expenses,
        Screen.Analytics,
        Screen.Settings
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark.copy(alpha = 0.95f))
            .border(1.dp, BorderOutline, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val isSelected = currentRoute == screen.route
                val contentColor = if (isSelected) PrimaryCyanBright else TextVariant
                val containerBg = if (isSelected) PrimaryCyanBright.copy(alpha = 0.15f) else androidx.compose.ui.graphics.Color.Transparent

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(containerBg)
                        .clickable { onNavigate(screen.route) }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        tint = contentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = screen.title,
                        color = contentColor,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
