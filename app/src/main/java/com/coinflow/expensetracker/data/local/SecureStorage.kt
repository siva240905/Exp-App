package com.coinflow.expensetracker.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureStorage(context: Context) {

    private val sharedPreferences: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_FILENAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback for emulators/devices where KeyStore may fail
            context.getSharedPreferences("${PREFS_FILENAME}_fallback", Context.MODE_PRIVATE)
        }
    }

    fun getGithubToken(): String {
        return sharedPreferences.getString(KEY_GITHUB_TOKEN, "") ?: ""
    }

    fun saveGithubToken(token: String) {
        sharedPreferences.edit().putString(KEY_GITHUB_TOKEN, token.trim()).apply()
    }

    fun getGistId(): String {
        return sharedPreferences.getString(KEY_GIST_ID, "") ?: ""
    }

    fun saveGistId(gistId: String) {
        sharedPreferences.edit().putString(KEY_GIST_ID, gistId.trim()).apply()
    }

    fun getCachedExpensesJson(): String {
        return sharedPreferences.getString(KEY_CACHED_EXPENSES_JSON, "") ?: ""
    }

    fun saveCachedExpensesJson(json: String) {
        sharedPreferences.edit().putString(KEY_CACHED_EXPENSES_JSON, json).apply()
    }

    fun getLastSyncTime(): String {
        return sharedPreferences.getString(KEY_LAST_SYNC_TIME, "") ?: ""
    }

    fun saveLastSyncTime(timeStr: String) {
        sharedPreferences.edit().putString(KEY_LAST_SYNC_TIME, timeStr).apply()
    }

    fun clearCredentials() {
        sharedPreferences.edit()
            .remove(KEY_GITHUB_TOKEN)
            .remove(KEY_GIST_ID)
            .apply()
    }

    companion object {
        private const val PREFS_FILENAME = "coin_flow_secure_prefs"
        private const val KEY_GITHUB_TOKEN = "github_token"
        private const val KEY_GIST_ID = "gist_id"
        private const val KEY_CACHED_EXPENSES_JSON = "cached_expenses_json"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
    }
}
