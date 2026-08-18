package com.coinflow.expensetracker.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureStorage(context: Context) {

    private val sharedPreferences: SharedPreferences by lazy {
        createPreferencesSafely(context)
    }

    private fun createPreferencesSafely(context: Context): SharedPreferences {
        return try {
            createEncryptedPrefs(context)
        } catch (e: Throwable) {
            try {
                // Delete corrupt encrypted prefs file if keystore was corrupted or reinstalled
                context.deleteSharedPreferences(PREFS_FILENAME)
                createEncryptedPrefs(context)
            } catch (e2: Throwable) {
                // Safe fallback to standard private shared preferences
                context.getSharedPreferences("${PREFS_FILENAME}_fallback", Context.MODE_PRIVATE)
            }
        }
    }

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILENAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getGithubToken(): String {
        return try { sharedPreferences.getString(KEY_GITHUB_TOKEN, "") ?: "" } catch (e: Throwable) { "" }
    }

    fun saveGithubToken(token: String) {
        try { sharedPreferences.edit().putString(KEY_GITHUB_TOKEN, token.trim()).apply() } catch (e: Throwable) {}
    }

    fun getGistId(): String {
        return try { sharedPreferences.getString(KEY_GIST_ID, "") ?: "" } catch (e: Throwable) { "" }
    }

    fun saveGistId(gistId: String) {
        try { sharedPreferences.edit().putString(KEY_GIST_ID, gistId.trim()).apply() } catch (e: Throwable) {}
    }

    fun getCachedExpensesJson(): String {
        return try { sharedPreferences.getString(KEY_CACHED_EXPENSES_JSON, "") ?: "" } catch (e: Throwable) { "" }
    }

    fun saveCachedExpensesJson(json: String) {
        try { sharedPreferences.edit().putString(KEY_CACHED_EXPENSES_JSON, json).apply() } catch (e: Throwable) {}
    }

    fun getLastSyncTime(): String {
        return try { sharedPreferences.getString(KEY_LAST_SYNC_TIME, "") ?: "" } catch (e: Throwable) { "" }
    }

    fun saveLastSyncTime(timeStr: String) {
        try { sharedPreferences.edit().putString(KEY_LAST_SYNC_TIME, timeStr).apply() } catch (e: Throwable) {}
    }

    fun clearCredentials() {
        try {
            sharedPreferences.edit()
                .remove(KEY_GITHUB_TOKEN)
                .remove(KEY_GIST_ID)
                .apply()
        } catch (e: Throwable) {}
    }

    companion object {
        private const val PREFS_FILENAME = "coin_flow_secure_prefs"
        private const val KEY_GITHUB_TOKEN = "github_token"
        private const val KEY_GIST_ID = "gist_id"
        private const val KEY_CACHED_EXPENSES_JSON = "cached_expenses_json"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
    }
}
