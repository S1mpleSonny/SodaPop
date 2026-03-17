package com.sodapop.app.data.repository

import com.sodapop.app.data.preferences.UserPreferences
import com.sodapop.app.data.remote.LlmApiService
import com.sodapop.app.data.remote.dto.ApiChatMessage
import com.sodapop.app.data.remote.dto.ChatCompletionRequest
import com.sodapop.app.data.remote.dto.ChatCompletionResponse
import com.sodapop.app.domain.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlmRepository @Inject constructor(
    private val apiService: LlmApiService,
    private val preferences: UserPreferences
) {
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun complete(
        messages: List<ChatMessage>,
        temperature: Float = 1.0f
    ): Result<String> = mutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                val config = preferences.llmConfig.first()
                val request = ChatCompletionRequest(
                    model = config.modelName,
                    messages = messages.map { ApiChatMessage(it.role, it.content) },
                    temperature = temperature,
                    stream = false
                )
                val response = apiService.chatCompletion(request)
                val content = response.choices.firstOrNull()?.message?.content ?: ""
                Result.success(content)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun completeStream(
        messages: List<ChatMessage>,
        temperature: Float = 1.0f
    ): Flow<String> = flow {
        val config = preferences.llmConfig.first()
        val request = ChatCompletionRequest(
            model = config.modelName,
            messages = messages.map { ApiChatMessage(it.role, it.content) },
            temperature = temperature,
            stream = true
        )

        val responseBody = apiService.chatCompletionStream(request)
        val reader = BufferedReader(responseBody.charStream())

        reader.use {
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val data = line ?: continue
                if (!data.startsWith("data: ")) continue
                val jsonStr = data.removePrefix("data: ").trim()
                if (jsonStr == "[DONE]") break

                try {
                    val chunk = json.decodeFromString<ChatCompletionResponse>(jsonStr)
                    val content = chunk.choices.firstOrNull()?.delta?.content
                    if (content != null) {
                        emit(content)
                    }
                } catch (e: Exception) {
                    // Skip malformed chunks
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun testConnection(): Result<String> {
        val messages = listOf(ChatMessage("user", "Say 'hello' in one word."))
        return complete(messages, 1.0f)
    }
}
