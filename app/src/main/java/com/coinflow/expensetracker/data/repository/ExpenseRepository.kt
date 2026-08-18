package com.coinflow.expensetracker.data.repository

import com.coinflow.expensetracker.data.local.SecureStorage
import com.coinflow.expensetracker.data.model.Expense
import com.coinflow.expensetracker.data.model.ExpenseContainer
import com.coinflow.expensetracker.data.model.GistCreateRequest
import com.coinflow.expensetracker.data.model.GistFileContent
import com.coinflow.expensetracker.data.model.GistPatchRequest
import com.coinflow.expensetracker.data.model.SyncState
import com.coinflow.expensetracker.data.remote.GitHubGistService
import com.coinflow.expensetracker.data.remote.NetworkClient
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseRepository(
    private val secureStorage: SecureStorage,
    private val apiService: GitHubGistService = NetworkClient.gistService
) {
    private val gson = Gson()
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    init {
        loadCachedExpenses()
        val lastSync = secureStorage.getLastSyncTime()
        if (lastSync.isNotEmpty()) {
            _syncState.value = SyncState.Synced(lastSync)
        }
        // Auto-fetch on startup if credentials exist
        if (hasCredentials()) {
            syncFromGist()
        }
    }

    fun hasCredentials(): Boolean {
        return secureStorage.getGithubToken().isNotBlank() && secureStorage.getGistId().isNotBlank()
    }

    private fun loadCachedExpenses() {
        val cachedJson = secureStorage.getCachedExpensesJson()
        if (cachedJson.isNotBlank()) {
            try {
                val container = gson.fromJson(cachedJson, ExpenseContainer::class.java)
                _expenses.value = (container?.expenses ?: emptyList()).sortedByDescending { it.date }
            } catch (e: Exception) {
                _expenses.value = emptyList()
            }
        }
    }

    private fun saveLocalCache(list: List<Expense>) {
        val container = ExpenseContainer(expenses = list)
        val json = gson.toJson(container)
        secureStorage.saveCachedExpensesJson(json)
    }

    private fun getAuthHeader(): String {
        val token = secureStorage.getGithubToken()
        return if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
    }

    fun syncFromGist() {
        if (!hasCredentials()) {
            _syncState.value = SyncState.Failed("GitHub Token or Gist ID missing")
            return
        }

        repositoryScope.launch {
            _syncState.value = SyncState.Syncing
            try {
                val gistId = secureStorage.getGistId()
                val response = apiService.getGist(gistId, getAuthHeader())

                if (response.isSuccessful && response.body() != null) {
                    val gist = response.body()!!
                    val file = gist.files["expenses.json"] ?: gist.files.values.firstOrNull()

                    if (file != null && !file.content.isNullOrBlank()) {
                        val container = gson.fromJson(file.content, ExpenseContainer::class.java)
                        val newList = (container.expenses ?: emptyList()).sortedByDescending { it.date }
                        
                        withContext(Dispatchers.Main) {
                            _expenses.value = newList
                            saveLocalCache(newList)
                            val nowStr = getCurrentTimestamp()
                            secureStorage.saveLastSyncTime(nowStr)
                            _syncState.value = SyncState.Synced(nowStr)
                        }
                    } else {
                        // Empty or non-existent expense file, push current local data
                        pushToGistInternal()
                    }
                } else {
                    val errorMsg = "HTTP ${response.code()}: ${response.errorBody()?.string() ?: "Failed to fetch Gist"}"
                    _syncState.value = SyncState.Failed(errorMsg)
                }
            } catch (e: Exception) {
                _syncState.value = SyncState.Failed(e.localizedMessage ?: "Network connection error")
            }
        }
    }

    fun pushToGist() {
        repositoryScope.launch {
            pushToGistInternal()
        }
    }

    private suspend fun pushToGistInternal() {
        if (!hasCredentials()) {
            _syncState.value = SyncState.Failed("GitHub Token or Gist ID missing")
            return
        }

        _syncState.value = SyncState.Syncing
        try {
            val gistId = secureStorage.getGistId()
            val container = ExpenseContainer(expenses = _expenses.value)
            val jsonContent = gson.toJson(container)

            val patchRequest = GistPatchRequest(
                description = "Coin Flow Expense Tracker Database",
                files = mapOf("expenses.json" to GistFileContent(content = jsonContent))
            )

            val response = apiService.updateGist(gistId, getAuthHeader(), patchRequest)

            if (response.isSuccessful) {
                val nowStr = getCurrentTimestamp()
                secureStorage.saveLastSyncTime(nowStr)
                withContext(Dispatchers.Main) {
                    _syncState.value = SyncState.Synced(nowStr)
                }
            } else {
                val errorMsg = "HTTP ${response.code()}: ${response.errorBody()?.string() ?: "Failed to update Gist"}"
                withContext(Dispatchers.Main) {
                    _syncState.value = SyncState.Failed(errorMsg)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                _syncState.value = SyncState.Failed(e.localizedMessage ?: "Network error during sync")
            }
        }
    }

    suspend fun createNewGist(token: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val formattedToken = if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
            val container = ExpenseContainer(expenses = _expenses.value)
            val jsonContent = gson.toJson(container)

            val createRequest = GistCreateRequest(
                description = "Coin Flow Personal Expense Database",
                public = false,
                files = mapOf("expenses.json" to GistFileContent(content = jsonContent))
            )

            val response = apiService.createGist(formattedToken, createRequest)
            if (response.isSuccessful && response.body() != null) {
                val gistId = response.body()!!.id
                secureStorage.saveGithubToken(token)
                secureStorage.saveGistId(gistId)
                val nowStr = getCurrentTimestamp()
                secureStorage.saveLastSyncTime(nowStr)
                _syncState.value = SyncState.Synced(nowStr)
                Result.success(gistId)
            } else {
                Result.failure(Exception("Failed to create Gist (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun addExpense(expense: Expense) {
        val updated = (_expenses.value + expense).sortedByDescending { it.date }
        _expenses.value = updated
        saveLocalCache(updated)
        if (hasCredentials()) {
            pushToGist()
        }
    }

    fun updateExpense(updatedExpense: Expense) {
        val updated = _expenses.value.map {
            if (it.id == updatedExpense.id) updatedExpense else it
        }.sortedByDescending { it.date }
        _expenses.value = updated
        saveLocalCache(updated)
        if (hasCredentials()) {
            pushToGist()
        }
    }

    fun deleteExpense(expenseId: String) {
        val updated = _expenses.value.filter { it.id != expenseId }
        _expenses.value = updated
        saveLocalCache(updated)
        if (hasCredentials()) {
            pushToGist()
        }
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }
}
