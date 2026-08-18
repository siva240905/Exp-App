package com.coinflow.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.coinflow.expensetracker.data.model.Expense
import com.coinflow.expensetracker.ui.components.BottomNavigationBar
import com.coinflow.expensetracker.ui.components.Screen
import com.coinflow.expensetracker.ui.screens.AddEditExpenseScreen
import com.coinflow.expensetracker.ui.screens.AnalyticsScreen
import com.coinflow.expensetracker.ui.screens.DashboardScreen
import com.coinflow.expensetracker.ui.screens.ExpenseListScreen
import com.coinflow.expensetracker.ui.screens.SettingsScreen
import com.coinflow.expensetracker.ui.theme.BackgroundDark
import com.coinflow.expensetracker.ui.theme.CoinFlowTheme
import com.coinflow.expensetracker.ui.theme.PrimaryCyanBright
import com.coinflow.expensetracker.ui.viewmodel.ExpenseViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CoinFlowTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: ExpenseViewModel) {
    var currentRoute by remember { mutableStateOf(Screen.Dashboard.route) }
    var showAddModal by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { route -> currentRoute = route }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    expenseToEdit = null
                    showAddModal = true
                },
                shape = CircleShape,
                containerColor = PrimaryCyanBright,
                contentColor = BackgroundDark,
                modifier = Modifier
                    .size(56.dp)
                    .padding(bottom = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Expense",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = BackgroundDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundDark)
        ) {
            when (currentRoute) {
                Screen.Dashboard.route -> DashboardScreen(
                    viewModel = viewModel,
                    onAddExpenseClick = {
                        expenseToEdit = null
                        showAddModal = true
                    },
                    onViewAllClick = { currentRoute = Screen.Expenses.route },
                    onEditExpenseClick = { expense ->
                        expenseToEdit = expense
                        showAddModal = true
                    }
                )
                Screen.Expenses.route -> ExpenseListScreen(
                    viewModel = viewModel,
                    onEditExpenseClick = { expense ->
                        expenseToEdit = expense
                        showAddModal = true
                    }
                )
                Screen.Analytics.route -> AnalyticsScreen(
                    viewModel = viewModel
                )
                Screen.Settings.route -> SettingsScreen(
                    viewModel = viewModel
                )
            }

            // Modal Sheet / Overlay for Add & Edit Expense
            if (showAddModal) {
                AddEditExpenseScreen(
                    viewModel = viewModel,
                    expenseToEdit = expenseToEdit,
                    onDismiss = { showAddModal = false }
                )
            }
        }
    }
}
