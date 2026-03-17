package com.sodapop.app.ui.dialogue

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sodapop.app.data.repository.DialogueRepository
import com.sodapop.app.data.repository.LlmRepository
import com.sodapop.app.data.repository.ThoughtRepository
import com.sodapop.app.domain.model.ChatMessage
import com.sodapop.app.domain.model.Dialogue
import com.sodapop.app.domain.model.DialogueMode
import com.sodapop.app.domain.model.Thought
import com.sodapop.app.util.PromptTemplates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DialogueViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val thoughtRepository: ThoughtRepository,
    private val dialogueRepository: DialogueRepository,
    private val llmRepository: LlmRepository
) : ViewModel() {

    private val thoughtId: String = savedStateHandle["thoughtId"] ?: ""
    private val modeStr: String = savedStateHandle["mode"] ?: "EXPANSION"

    val thought = MutableStateFlow<Thought?>(null)
    val dialogue = MutableStateFlow<Dialogue?>(null)
    val messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val streamingContent = MutableStateFlow("")
    val isLoading = MutableStateFlow(false)
    val mode = MutableStateFlow(
        try { DialogueMode.valueOf(modeStr) } catch (e: Exception) { DialogueMode.EXPANSION }
    )

    init {
        viewModelScope.launch {
            thought.value = thoughtRepository.getById(thoughtId)
            thought.value?.let { loadOrCreateDialogue(it) }
        }
    }

    private suspend fun loadOrCreateDialogue(thought: Thought) {
        // Try to load existing dialogue for this thought with the same mode
        val existingDialogues = dialogueRepository.getByThoughtId(thought.id).first()
        val existing = existingDialogues.find { it.mode == mode.value }

        if (existing != null && existing.messages.isNotEmpty()) {
            // Restore history
            dialogue.value = existing
            messages.value = existing.messages
        } else {
            // Start new dialogue
            startNewDialogue(thought)
        }
    }

    private suspend fun startNewDialogue(thought: Thought) {
        val systemMessage = PromptTemplates.dialogueSystem(mode.value, thought.content)
        val initialMessages = listOf(
            systemMessage,
            ChatMessage("user", "请分析我的这个想法，给出你的看法。")
        )

        val newDialogue = Dialogue(
            thoughtId = thought.id,
            messages = initialMessages,
            mode = mode.value
        )
        dialogueRepository.save(newDialogue)
        dialogue.value = newDialogue
        messages.value = initialMessages

        getAiResponse()
    }

    fun sendMessage(content: String) {
        if (content.isBlank() || isLoading.value) return

        val userMessage = ChatMessage("user", content)
        messages.value = messages.value + userMessage

        getAiResponse()
    }

    private fun getAiResponse() {
        isLoading.value = true
        streamingContent.value = ""

        viewModelScope.launch {
            val fullContent = StringBuilder()

            llmRepository.completeStream(messages.value)
                .collect { delta ->
                    fullContent.append(delta)
                    streamingContent.value = fullContent.toString()
                }

            val assistantMessage = ChatMessage("assistant", fullContent.toString())
            messages.value = messages.value + assistantMessage
            streamingContent.value = ""
            isLoading.value = false

            // Save dialogue
            dialogue.value?.let { d ->
                val updated = d.copy(
                    messages = messages.value,
                    updatedAt = System.currentTimeMillis()
                )
                dialogueRepository.update(updated)
                dialogue.value = updated
            }
        }
    }

    fun changeMode(newMode: DialogueMode) {
        mode.value = newMode
        thought.value?.let { t ->
            viewModelScope.launch {
                messages.value = emptyList()
                loadOrCreateDialogue(t)
            }
        }
    }
}
