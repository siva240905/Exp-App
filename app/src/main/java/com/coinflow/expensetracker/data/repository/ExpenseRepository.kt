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
            val list = parseJsonToExpenses(cachedJson)
            _expenses.value = list.sortedByDescending { it.date }
        }
    }

    private fun saveLocalCache(list: List<Expense>) {
        val container = ExpenseContainer(expenses = list)
        val json = gson.toJson(container)
        secureStorage.saveCachedExpensesJson(json)
    }

    fun parseJsonToExpenses(jsonStr: String): List<Expense> {
        if (jsonStr.isBlank()) return emptyList()
        val list = mutableListOf<Expense>()
        try {
            val jsonElement = com.google.gson.JsonParser.parseString(jsonStr)
            if (jsonElement.isJsonObject) {
                val jsonObject = jsonElement.asJsonObject
                val arrayElement = jsonObject.getAsJsonArray("transactions")
                    ?: jsonObject.getAsJsonArray("expenses")

                if (arrayElement != null) {
                    for (element in arrayElement) {
                        if (!element.isJsonObject) continue
                        val obj = element.asJsonObject

                        val id = if (obj.has("id") && !obj.get("id").isJsonNull) obj.get("id").asString else java.util.UUID.randomUUID().toString()
                        val rawType = if (obj.has("type") && !obj.get("type").isJsonNull) obj.get("type").asString else Expense.TYPE_SEND
                        val type = when (rawType.lowercase()) {
                            "expense", "send", "debit" -> Expense.TYPE_SEND
                            "income", "receive", "credit" -> Expense.TYPE_RECEIVE
                            else -> Expense.TYPE_SEND
                        }

                        val amount = if (obj.has("amount") && !obj.get("amount").isJsonNull) obj.get("amount").asDouble else 0.0
                        val date = if (obj.has("date") && !obj.get("date").isJsonNull) obj.get("date").asString else SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val category = if (obj.has("category") && !obj.get("category").isJsonNull) obj.get("category").asString else "Other"

                        val paymentMethod = when {
                            obj.has("method") && !obj.get("method").isJsonNull -> obj.get("method").asString
                            obj.has("paymentMethod") && !obj.get("paymentMethod").isJsonNull -> obj.get("paymentMethod").asString
                            else -> "Bank Transfer"
                        }

                        val description = when {
                            obj.has("note") && !obj.get("note").isJsonNull -> obj.get("note").asString
                            obj.has("description") && !obj.get("description").isJsonNull -> obj.get("description").asString
                            else -> ""
                        }

                        list.add(
                            Expense(
                                id = id,
                                amount = amount,
                                category = category,
                                description = description,
                                date = date,
                                paymentMethod = paymentMethod,
                                type = type
                            )
                        )
                    }
                }
            } else if (jsonElement.isJsonArray) {
                val arrayElement = jsonElement.asJsonArray
                for (element in arrayElement) {
                    if (!element.isJsonObject) continue
                    val obj = element.asJsonObject
                    val id = if (obj.has("id") && !obj.get("id").isJsonNull) obj.get("id").asString else java.util.UUID.randomUUID().toString()
                    val rawType = if (obj.has("type") && !obj.get("type").isJsonNull) obj.get("type").asString else Expense.TYPE_SEND
                    val type = when (rawType.lowercase()) {
                        "expense", "send", "debit" -> Expense.TYPE_SEND
                        "income", "receive", "credit" -> Expense.TYPE_RECEIVE
                        else -> Expense.TYPE_SEND
                    }

                    val amount = if (obj.has("amount") && !obj.get("amount").isJsonNull) obj.get("amount").asDouble else 0.0
                    val date = if (obj.has("date") && !obj.get("date").isJsonNull) obj.get("date").asString else SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val category = if (obj.has("category") && !obj.get("category").isJsonNull) obj.get("category").asString else "Other"

                    val paymentMethod = when {
                        obj.has("method") && !obj.get("method").isJsonNull -> obj.get("method").asString
                        obj.has("paymentMethod") && !obj.get("paymentMethod").isJsonNull -> obj.get("paymentMethod").asString
                        else -> "Bank Transfer"
                    }

                    val description = when {
                        obj.has("note") && !obj.get("note").isJsonNull -> obj.get("note").asString
                        obj.has("description") && !obj.get("description").isJsonNull -> obj.get("description").asString
                        else -> ""
                    }

                    list.add(
                        Expense(
                            id = id,
                            amount = amount,
                            category = category,
                            description = description,
                            date = date,
                            paymentMethod = paymentMethod,
                            type = type
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun importJsonData(jsonStr: String): Int {
        val parsedList = parseJsonToExpenses(jsonStr)
        if (parsedList.isNotEmpty()) {
            val sortedList = parsedList.sortedByDescending { it.date }
            _expenses.value = sortedList
            saveLocalCache(sortedList)
            if (hasCredentials()) {
                pushToGist()
            }
            return sortedList.size
        }
        return 0
    }

    private fun getPrimaryAuthHeader(token: String = secureStorage.getGithubToken()): String {
        val clean = token.trim()
            .replace("^(token|bearer)\\s+".toRegex(RegexOption.IGNORE_CASE), "")
            .trim()
        if (clean.isBlank()) return ""
        return if (clean.startsWith("github_pat_")) "Bearer $clean" else "token $clean"
    }

    private fun getFallbackAuthHeader(token: String = secureStorage.getGithubToken()): String {
        val clean = token.trim()
            .replace("^(token|bearer)\\s+".toRegex(RegexOption.IGNORE_CASE), "")
            .trim()
        if (clean.isBlank()) return ""
        return if (clean.startsWith("github_pat_")) "token $clean" else "Bearer $clean"
    }

    private fun parseHttpError(code: Int, rawError: String?): String {
        return when (code) {
            401 -> "Invalid GitHub Token (401 Unauthorized). Please check your token & 'gist' permission in Settings."
            404 -> "Gist Not Found (404). Verify Gist ID or tap Auto-Create Gist."
            403 -> "Access Denied (403). Ensure GitHub Token has 'gist' scope enabled."
            else -> "HTTP $code: ${rawError ?: "GitHub API Error"}"
        }
    }

    fun syncFromGist() {
        if (!hasCredentials()) {
            _syncState.value = SyncState.Failed("GitHub Token or Gist ID missing in Settings")
            return
        }

        repositoryScope.launch {
            _syncState.value = SyncState.Syncing
            try {
                val gistId = secureStorage.getGistId().trim()
                var response = apiService.getGist(gistId, getPrimaryAuthHeader())

                // Retry with fallback auth format if primary returns 401
                if (response.code() == 401) {
                    response = apiService.getGist(gistId, getFallbackAuthHeader())
                }

                if (response.isSuccessful && response.body() != null) {
                    val gist = response.body()!!
                    val file = gist.files["expenses.json"] ?: gist.files.values.firstOrNull()

                    if (file != null && !file.content.isNullOrBlank()) {
                        val newList = parseJsonToExpenses(file.content).sortedByDescending { it.date }

                        withContext(Dispatchers.Main) {
                            _expenses.value = newList
                            saveLocalCache(newList)
                            val nowStr = getCurrentTimestamp()
                            secureStorage.saveLastSyncTime(nowStr)
                            _syncState.value = SyncState.Synced(nowStr)
                        }
                    } else {
                        pushToGistInternal()
                    }
                } else {
                    val errorMsg = parseHttpError(response.code(), response.errorBody()?.string())
                    withContext(Dispatchers.Main) {
                        _syncState.value = SyncState.Failed(errorMsg)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _syncState.value = SyncState.Failed(e.localizedMessage ?: "Network connection error")
                }
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
            _syncState.value = SyncState.Failed("GitHub Token or Gist ID missing in Settings")
            return
        }

        _syncState.value = SyncState.Syncing
        try {
            val gistId = secureStorage.getGistId().trim()
            val container = ExpenseContainer(expenses = _expenses.value)
            val jsonContent = gson.toJson(container)

            val patchRequest = GistPatchRequest(
                description = "Coin Flow Expense Tracker Database",
                files = mapOf("expenses.json" to GistFileContent(content = jsonContent))
            )

            var response = apiService.updateGist(gistId, getPrimaryAuthHeader(), patchRequest)
            if (response.code() == 401) {
                response = apiService.updateGist(gistId, getFallbackAuthHeader(), patchRequest)
            }

            if (response.isSuccessful) {
                val nowStr = getCurrentTimestamp()
                secureStorage.saveLastSyncTime(nowStr)
                withContext(Dispatchers.Main) {
                    _syncState.value = SyncState.Synced(nowStr)
                }
            } else {
                val errorMsg = parseHttpError(response.code(), response.errorBody()?.string())
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
            val cleanToken = token.trim()
                .replace("^(token|bearer)\\s+".toRegex(RegexOption.IGNORE_CASE), "")
                .trim()

            val primaryHeader = getPrimaryAuthHeader(cleanToken)
            val fallbackHeader = getFallbackAuthHeader(cleanToken)

            val container = ExpenseContainer(expenses = _expenses.value)
            val jsonContent = gson.toJson(container)

            val createRequest = GistCreateRequest(
                description = "Coin Flow Personal Expense Database",
                public = false,
                files = mapOf("expenses.json" to GistFileContent(content = jsonContent))
            )

            var response = apiService.createGist(primaryHeader, createRequest)
            if (response.code() == 401) {
                response = apiService.createGist(fallbackHeader, createRequest)
            }

            if (response.isSuccessful && response.body() != null) {
                val gistId = response.body()!!.id
                secureStorage.saveGithubToken(cleanToken)
                secureStorage.saveGistId(gistId)
                val nowStr = getCurrentTimestamp()
                secureStorage.saveLastSyncTime(nowStr)
                withContext(Dispatchers.Main) {
                    _syncState.value = SyncState.Synced(nowStr)
                }
                Result.success(gistId)
            } else {
                val errorMsg = parseHttpError(response.code(), response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
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
