package com.coinflow.expensetracker.data.model

import com.google.gson.annotations.SerializedName

data class GistResponse(
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("files")
    val files: Map<String, GistFile> = emptyMap(),
    
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class GistFile(
    @SerializedName("filename")
    val filename: String = "",
    
    @SerializedName("content")
    val content: String? = null,
    
    @SerializedName("raw_url")
    val rawUrl: String? = null
)

data class GistPatchRequest(
    @SerializedName("description")
    val description: String = "Coin Flow Expense Tracker Data",
    
    @SerializedName("files")
    val files: Map<String, GistFileContent>
)

data class GistCreateRequest(
    @SerializedName("description")
    val description: String = "Coin Flow Expense Tracker Database",
    
    @SerializedName("public")
    val public: Boolean = false,
    
    @SerializedName("files")
    val files: Map<String, GistFileContent>
)

data class GistFileContent(
    @SerializedName("content")
    val content: String
)

sealed class SyncState {
    data class Synced(val lastSyncTime: String) : SyncState()
    object Syncing : SyncState()
    data class Failed(val error: String) : SyncState()
    object Idle : SyncState()
}
