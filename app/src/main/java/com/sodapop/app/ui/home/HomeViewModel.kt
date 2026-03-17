package com.sodapop.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sodapop.app.data.repository.LlmRepository
import com.sodapop.app.data.repository.ThoughtRepository
import com.sodapop.app.domain.model.Thought
import com.sodapop.app.util.PromptTemplates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val thoughtRepository: ThoughtRepository,
    private val llmRepository: LlmRepository
) : AndroidViewModel(application) {

    private val json = Json { ignoreUnknownKeys = true }

    val thoughts: StateFlow<List<Thought>> = thoughtRepository.getAllThoughts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val inputText = MutableStateFlow("")
    val isSaving = MutableStateFlow(false)

    fun updateInput(text: String) {
        inputText.value = text
    }

    fun quickSave() {
        val text = inputText.value.trim()
        if (text.isBlank()) return

        isSaving.value = true
        viewModelScope.launch {
            val thought = Thought(content = text, rawContent = text)
            thoughtRepository.save(thought)
            inputText.value = ""
            isSaving.value = false

            // Auto-tag in background
            launch { autoTag(thought) }
        }
    }

    private suspend fun autoTag(thought: Thought) {
        val messages = PromptTemplates.autoTag(thought.content)
        llmRepository.complete(messages).onSuccess { response ->
            try {
                val tags: List<String> = json.decodeFromString(com.sodapop.app.util.JsonExtractor.extract(response))
                val updated = thought.copy(tags = tags, updatedAt = System.currentTimeMillis())
                thoughtRepository.update(updated)
            } catch (_: Exception) {}
        }
    }

    fun deleteThought(thought: Thought) {
        viewModelScope.launch {
            thoughtRepository.delete(thought)
        }
    }
}
