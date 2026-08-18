package com.coinflow.expensetracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.coinflow.expensetracker.data.local.SecureStorage
import com.coinflow.expensetracker.data.model.Expense
import com.coinflow.expensetracker.data.model.SyncState
import com.coinflow.expensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val secureStorage = SecureStorage(application.applicationContext)
    val repository = ExpenseRepository(secureStorage)

    val syncState: StateFlow<SyncState> = repository.syncState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    val githubToken = MutableStateFlow(secureStorage.getGithubToken())
    val gistId = MutableStateFlow(secureStorage.getGistId())

    // Combined filtered expenses
    val filteredExpenses: StateFlow<List<Expense>> = combine(
        repository.expenses,
        _searchQuery,
        _selectedCategoryFilter
    ) { list, query, category ->
        list.filter { expense ->
            val matchesQuery = query.isBlank() ||
                    expense.description.contains(query, ignoreCase = true) ||
                    expense.category.contains(query, ignoreCase = true) ||
                    expense.paymentMethod.contains(query, ignoreCase = true)

            val matchesCategory = category == "All" || expense.category.equals(category, ignoreCase = true)

            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Net balance = Total Received - Total Sent
    val netBalance: StateFlow<Double> = repository.expenses.combine(_searchQuery) { list, _ ->
        val received = list.filter { it.type == Expense.TYPE_RECEIVE }.sumOf { it.amount }
        val sent = list.filter { it.type == Expense.TYPE_SEND }.sumOf { it.amount }
        received - sent
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    // Total Sent (Expenses)
    val totalSent: StateFlow<Double> = repository.expenses.combine(_searchQuery) { list, _ ->
        list.filter { it.type == Expense.TYPE_SEND }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val totalExpenses: StateFlow<Double> = totalSent

    // Total Received (Income)
    val totalReceived: StateFlow<Double> = repository.expenses.combine(_searchQuery) { list, _ ->
        list.filter { it.type == Expense.TYPE_RECEIVE }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    // Category breakdown totals for Sent expenses
    val categoryTotals: StateFlow<Map<String, Double>> = repository.expenses.combine(_searchQuery) { list, _ ->
        list.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    // Today's Sent total
    val todayTotal: StateFlow<Double> = repository.expenses.combine(_searchQuery) { list, _ ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        list.filter { it.date == todayStr && it.type == Expense.TYPE_SEND }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    // Current month Sent total
    val currentMonthTotal: StateFlow<Double> = repository.expenses.combine(_searchQuery) { list, _ ->
        val currentMonthPrefix = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        list.filter { it.date.startsWith(currentMonthPrefix) && it.type == Expense.TYPE_SEND }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun syncFromGist() {
        repository.syncFromGist()
    }

    fun pushToGist() {
        repository.pushToGist()
    }

    fun addExpense(
        amount: Double,
        category: String,
        description: String,
        date: String,
        paymentMethod: String,
        type: String = Expense.TYPE_SEND
    ) {
        val dateValue = if (date.isBlank()) {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        } else date

        val newExpense = Expense(
            id = UUID.randomUUID().toString(),
            amount = amount,
            category = category,
            description = description.ifBlank { if (type == Expense.TYPE_RECEIVE) "Money Received" else category },
            date = dateValue,
            paymentMethod = paymentMethod,
            type = type
        )
        repository.addExpense(newExpense)
    }

    fun updateExpense(expense: Expense) {
        repository.updateExpense(expense)
    }

    fun deleteExpense(id: String) {
        repository.deleteExpense(id)
    }

    fun saveCredentials(token: String, id: String) {
        val cleanToken = token.trim()
        val cleanId = id.trim()
        secureStorage.saveGithubToken(cleanToken)
        secureStorage.saveGistId(cleanId)
        githubToken.value = cleanToken
        gistId.value = cleanId
        if (cleanToken.isNotBlank() && cleanId.isNotBlank()) {
            syncFromGist()
        }
    }

    fun createNewGist(token: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val cleanToken = token.trim()
            val result = repository.createNewGist(cleanToken)
            if (result.isSuccess) {
                val newId = result.getOrNull() ?: ""
                githubToken.value = cleanToken
                gistId.value = newId
                onResult(true, "Successfully created Gist: $newId")
            } else {
                onResult(false, result.exceptionOrNull()?.localizedMessage ?: "Failed to create Gist")
            }
        }
    }

    fun clearCredentials() {
        secureStorage.clearCredentials()
        githubToken.value = ""
        gistId.value = ""
    }

    fun formatINR(amount: Double): String {
        val formatter = java.text.NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        return formatter.format(amount).replace("INR", "₹")
    }
}
