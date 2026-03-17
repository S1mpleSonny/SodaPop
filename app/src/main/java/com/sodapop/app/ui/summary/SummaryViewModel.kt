package com.sodapop.app.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sodapop.app.data.repository.LlmRepository
import com.sodapop.app.data.repository.SummaryRepository
import com.sodapop.app.data.repository.ThoughtRepository
import com.sodapop.app.domain.model.MemoryLayer
import com.sodapop.app.domain.model.Summary
import com.sodapop.app.domain.model.SummaryType
import com.sodapop.app.domain.model.Thought
import com.sodapop.app.util.DateUtils
import com.sodapop.app.util.PromptTemplates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

@Serializable
private data class DailySummaryResult(
    val summary: String = "",
    val themes: List<String> = emptyList(),
    val questions: List<String> = emptyList(),
    val promote_to_topic: List<PromotionSuggestion> = emptyList(),
    val promote_to_belief: List<PromotionSuggestion> = emptyList()
)

@Serializable
private data class PromotionSuggestion(
    val index: Int = 0,
    val reason: String = ""
)

data class PromotionItem(
    val thought: Thought,
    val targetLayer: MemoryLayer,
    val reason: String
)

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val summaryRepository: SummaryRepository,
    private val thoughtRepository: ThoughtRepository,
    private val llmRepository: LlmRepository
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }

    val summaries: StateFlow<List<Summary>> = summaryRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val isGenerating = MutableStateFlow(false)
    val streamingContent = MutableStateFlow("")
    val error = MutableStateFlow<String?>(null)
    val promotionSuggestions = MutableStateFlow<List<PromotionItem>>(emptyList())

    fun generateDailySummary() {
        viewModelScope.launch {
            isGenerating.value = true
            streamingContent.value = ""
            error.value = null
            promotionSuggestions.value = emptyList()
            try {
                val start = DateUtils.todayStart()
                val end = DateUtils.todayEnd()

                val existing = summaryRepository.getByTypeAndPeriod(SummaryType.DAILY, start)
                if (existing != null) {
                    error.value = "今日总结已生成"
                    isGenerating.value = false
                    return@launch
                }

                val thoughts = thoughtRepository.getByDateRange(start, end)
                if (thoughts.isEmpty()) {
                    error.value = "今天还没有记录任何想法"
                    isGenerating.value = false
                    return@launch
                }

                val messages = PromptTemplates.dailySummary(
                    thoughts.map { it.createdAt to it.content }
                )

                val fullContent = StringBuilder()
                llmRepository.completeStream(messages).collect { delta ->
                    fullContent.append(delta)
                    streamingContent.value = fullContent.toString()
                }

                val response = fullContent.toString()
                try {
                    val result = json.decodeFromString<DailySummaryResult>(com.sodapop.app.util.JsonExtractor.extract(response))
                    val summary = Summary(
                        type = SummaryType.DAILY,
                        content = result.summary,
                        themes = result.themes,
                        questions = result.questions,
                        periodStart = start,
                        periodEnd = end
                    )
                    summaryRepository.save(summary)

                    // Build promotion suggestions
                    val suggestions = mutableListOf<PromotionItem>()
                    result.promote_to_topic.forEach { s ->
                        val idx = s.index - 1  // Prompt uses 1-based index
                        if (idx in thoughts.indices && thoughts[idx].layer == MemoryLayer.FRAGMENT) {
                            suggestions.add(PromotionItem(thoughts[idx], MemoryLayer.TOPIC, s.reason))
                        }
                    }
                    result.promote_to_belief.forEach { s ->
                        val idx = s.index - 1
                        if (idx in thoughts.indices && thoughts[idx].layer != MemoryLayer.BELIEF) {
                            suggestions.add(PromotionItem(thoughts[idx], MemoryLayer.BELIEF, s.reason))
                        }
                    }
                    promotionSuggestions.value = suggestions
                } catch (e: Exception) {
                    val summary = Summary(
                        type = SummaryType.DAILY,
                        content = response,
                        periodStart = start,
                        periodEnd = end
                    )
                    summaryRepository.save(summary)
                }
                streamingContent.value = ""
            } catch (e: Exception) {
                error.value = "生成失败: ${e.message}"
            } finally {
                isGenerating.value = false
            }
        }
    }

    fun acceptPromotion(item: PromotionItem) {
        viewModelScope.launch {
            val updated = item.thought.copy(
                layer = item.targetLayer,
                updatedAt = System.currentTimeMillis()
            )
            thoughtRepository.update(updated)
            // Remove from suggestions
            promotionSuggestions.value = promotionSuggestions.value.filter { it.thought.id != item.thought.id }
        }
    }

    fun dismissPromotion(item: PromotionItem) {
        promotionSuggestions.value = promotionSuggestions.value.filter { it.thought.id != item.thought.id }
    }
}
