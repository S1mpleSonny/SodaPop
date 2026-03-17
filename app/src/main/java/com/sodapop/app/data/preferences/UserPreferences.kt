package com.sodapop.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.sodapop.app.domain.model.LlmConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    private val securePrefs: SharedPreferences by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "secure_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        private val KEY_BASE_URL = stringPreferencesKey("llm_base_url")
        private val KEY_MODEL_NAME = stringPreferencesKey("llm_model_name")
        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_DAILY_SUMMARY_HOUR = intPreferencesKey("daily_summary_hour")
        private val KEY_WEEKLY_SUMMARY_DAY = intPreferencesKey("weekly_summary_day")
        private val KEY_INSPIRATION_ENABLED = booleanPreferencesKey("inspiration_replay_enabled")
        private const val SECURE_KEY_API_KEY = "llm_api_key"
    }

    val llmConfig: Flow<LlmConfig> = dataStore.data.map { prefs ->
        LlmConfig(
            baseUrl = prefs[KEY_BASE_URL] ?: "",
            apiKey = securePrefs.getString(SECURE_KEY_API_KEY, "")?.trim() ?: "",
            modelName = (prefs[KEY_MODEL_NAME] ?: "").trim()
        )
    }

    val theme: Flow<String> = dataStore.data.map { it[KEY_THEME] ?: "system" }
    val dailySummaryHour: Flow<Int> = dataStore.data.map { it[KEY_DAILY_SUMMARY_HOUR] ?: 21 }
    val weeklySummaryDay: Flow<Int> = dataStore.data.map { it[KEY_WEEKLY_SUMMARY_DAY] ?: 7 }
    val inspirationEnabled: Flow<Boolean> = dataStore.data.map { it[KEY_INSPIRATION_ENABLED] ?: true }

    suspend fun updateLlmConfig(config: LlmConfig) {
        dataStore.edit { prefs ->
            prefs[KEY_BASE_URL] = config.baseUrl.trim()
            prefs[KEY_MODEL_NAME] = config.modelName.trim()
        }
        securePrefs.edit().putString(SECURE_KEY_API_KEY, config.apiKey).apply()
    }

    suspend fun updateTheme(theme: String) {
        dataStore.edit { it[KEY_THEME] = theme }
    }

    suspend fun updateDailySummaryHour(hour: Int) {
        dataStore.edit { it[KEY_DAILY_SUMMARY_HOUR] = hour }
    }

    suspend fun updateWeeklySummaryDay(day: Int) {
        dataStore.edit { it[KEY_WEEKLY_SUMMARY_DAY] = day }
    }

    suspend fun updateInspirationEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_INSPIRATION_ENABLED] = enabled }
    }

    fun getApiKeySync(): String = securePrefs.getString(SECURE_KEY_API_KEY, "") ?: ""
    fun getBaseUrlSync(): String = "" // Will be read from dataStore synchronously via runBlocking in interceptor

    suspend fun getBaseUrl(): String {
        var url = ""
        dataStore.data.collect { prefs ->
            url = prefs[KEY_BASE_URL] ?: ""
            return@collect
        }
        return url
    }
}
