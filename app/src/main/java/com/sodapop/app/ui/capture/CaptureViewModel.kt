package com.sodapop.app.ui.capture

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sodapop.app.data.repository.LlmRepository
import com.sodapop.app.data.repository.ThoughtRepository
import com.sodapop.app.domain.model.Thought
import com.sodapop.app.domain.model.ThoughtType
import com.sodapop.app.util.PromptTemplates
import com.sodapop.app.util.VoiceRecognizer
import com.sodapop.app.util.VoiceResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class CaptureViewModel @Inject constructor(
    application: Application,
    private val thoughtRepository: ThoughtRepository,
    private val llmRepository: LlmRepository
) : AndroidViewModel(application) {

    private val voiceRecognizer = VoiceRecognizer(application)
    private val json = Json { ignoreUnknownKeys = true }

    val content = MutableStateFlow("")
    val thoughtType = MutableStateFlow(ThoughtType.IDEA)
    val isListening = MutableStateFlow(false)
    val isSaving = MutableStateFlow(false)

    private val _savedEvent = MutableStateFlow(false)
    val savedEvent: StateFlow<Boolean> = _savedEvent

    // For prediction
    val reviewDateMillis = MutableStateFlow<Long?>(null)

    fun updateContent(text: String) {
        content.value = text
    }

    fun updateType(type: ThoughtType) {
        thoughtType.value = type
    }

    fun updateReviewDate(millis: Long?) {
        reviewDateMillis.value = millis
    }

    fun toggleVoiceInput() {
        if (isListening.value) {
            voiceRecognizer.stopListening()
            isListening.value = false
        } else {
            isListening.value = true
            viewModelScope.launch {
                voiceRecognizer.startListening().collect { result ->
                    when (result) {
                        is VoiceResult.Partial -> content.value = result.text
                        is VoiceResult.Final -> {
                            content.value = result.text
                            isListening.value = false
                        }
                        is VoiceResult.Error -> {
                            isListening.value = false
                        }
                    }
                }
            }
        }
    }

    fun save() {
        val text = content.value.trim()
        if (text.isBlank()) return

        isSaving.value = true
        viewModelScope.launch {
            val thought = Thought(
                content = text,
                rawContent = text,
                type = thoughtType.value
            )
            thoughtRepository.save(thought)

            // Auto-tag in background
            launch {
                autoTag(thought)
            }

            // Create prediction record if needed
            if (thoughtType.value == ThoughtType.PREDICTION) {
                reviewDateMillis.value?.let { reviewDate ->
                    val prediction = com.sodapop.app.domain.model.Prediction(
                        thoughtId = thought.id,
                        predictedOutcome = text,
                        reviewDate = reviewDate
                    )
                    // Would save via PredictionRepository - will be wired in Phase 5
                }
            }

            isSaving.value = false
            _savedEvent.value = true
        }
    }

    private suspend fun autoTag(thought: Thought) {
        val messages = PromptTemplates.autoTag(thought.content)
        llmRepository.complete(messages).onSuccess { response ->
            try {
                val tags: List<String> = json.decodeFromString(com.sodapop.app.util.JsonExtractor.extract(response))
                val updated = thought.copy(
                    tags = tags,
                    updatedAt = System.currentTimeMillis()
                )
                thoughtRepository.update(updated)
            } catch (e: Exception) {
                // Tagging failed silently
            }
        }
    }

    fun resetSavedEvent() {
        _savedEvent.value = false
    }
}
