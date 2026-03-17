package com.sodapop.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sodapop.app.data.preferences.UserPreferences
import com.sodapop.app.data.repository.LlmRepository
import com.sodapop.app.domain.model.LlmConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: UserPreferences,
    private val llmRepository: LlmRepository
) : ViewModel() {

    val llmConfig: StateFlow<LlmConfig> = preferences.llmConfig
        .stateIn(viewModelScope, SharingStarted.Lazily, LlmConfig())

    val theme: StateFlow<String> = preferences.theme
        .stateIn(viewModelScope, SharingStarted.Lazily, "system")

    val testResult = MutableStateFlow<String?>(null)
    val isTesting = MutableStateFlow(false)

    fun updateLlmConfig(config: LlmConfig) {
        viewModelScope.launch {
            preferences.updateLlmConfig(config)
        }
    }

    fun updateTheme(theme: String) {
        viewModelScope.launch {
            preferences.updateTheme(theme)
        }
    }

    fun testConnection() {
        isTesting.value = true
        testResult.value = null
        viewModelScope.launch {
            llmRepository.testConnection()
                .onSuccess {
                    testResult.value = "✅ 连接成功: $it"
                }
                .onFailure { e ->
                    testResult.value = "❌ 连接失败: ${e.message}"
                }
            isTesting.value = false
        }
    }
}
