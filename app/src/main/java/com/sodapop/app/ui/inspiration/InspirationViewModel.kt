package com.sodapop.app.ui.inspiration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sodapop.app.data.repository.LlmRepository
import com.sodapop.app.data.repository.ThoughtRepository
import com.sodapop.app.domain.model.Thought
import com.sodapop.app.util.DateUtils
import com.sodapop.app.util.PromptTemplates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

@Serializable
private data class ConnectionResult(
    val connections: List<Connection> = emptyList()
)

@Serializable
private data class Connection(
    val recentThoughtId: String = "",
    val explanation: String = ""
)

data class InspirationConnection(
    val thought: Thought,
    val explanation: String
)

@HiltViewModel
class InspirationViewModel @Inject constructor(
    private val thoughtRepository: ThoughtRepository,
    private val llmRepository: LlmRepository
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }

    val currentThought = MutableStateFlow<Thought?>(null)
    val connections = MutableStateFlow<List<InspirationConnection>>(emptyList())
    val isLoadingConnections = MutableStateFlow(false)
    val streamingContent = MutableStateFlow("")
    val isEmpty = MutableStateFlow(false)

    init {
        loadRandomThought()
    }

    fun loadRandomThought() {
        viewModelScope.launch {
            val random = thoughtRepository.getRandom(1).firstOrNull()
            if (random == null) {
                isEmpty.value = true
                return@launch
            }
            isEmpty.value = false
            currentThought.value = random
            connections.value = emptyList()
            streamingContent.value = ""
            findConnections(random)
        }
    }

    private fun findConnections(oldThought: Thought) {
        isLoadingConnections.value = true
        viewModelScope.launch {
            val recent = thoughtRepository.getByDateRange(
                DateUtils.todayStart() - 7 * 24 * 60 * 60 * 1000L,
                DateUtils.todayEnd()
            ).filter { it.id != oldThought.id }.take(10)

            if (recent.isEmpty()) {
                isLoadingConnections.value = false
                return@launch
            }

            val messages = PromptTemplates.findConnections(
                oldThought = oldThought.content,
                oldDate = DateUtils.formatDate(oldThought.createdAt),
                recentThoughts = recent.map { it.id to it.content }
            )

            val fullContent = StringBuilder()
            llmRepository.completeStream(messages).collect { delta ->
                fullContent.append(delta)
                streamingContent.value = fullContent.toString()
            }

            // Stream finished, parse
            val response = fullContent.toString()
            try {
                val result = json.decodeFromString<ConnectionResult>(com.sodapop.app.util.JsonExtractor.extract(response))
                val mapped = result.connections.mapNotNull { conn ->
                    val thought = recent.find { it.id == conn.recentThoughtId }
                    thought?.let { InspirationConnection(it, conn.explanation) }
                }
                connections.value = mapped
            } catch (e: Exception) {
                // Parse failed, keep streaming content visible as fallback
            }

            streamingContent.value = ""
            isLoadingConnections.value = false
        }
    }
}
