package com.sodapop.app.domain.model

data class LlmConfig(
    val baseUrl: String = "",
    val apiKey: String = "",
    val modelName: String = ""
) {
    val isConfigured: Boolean
        get() = baseUrl.isNotBlank() && apiKey.isNotBlank() && modelName.isNotBlank()
}
