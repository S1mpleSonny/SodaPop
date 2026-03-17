package com.sodapop.app.ui.prediction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sodapop.app.data.repository.LlmRepository
import com.sodapop.app.data.repository.PredictionRepository
import com.sodapop.app.data.repository.ThoughtRepository
import com.sodapop.app.domain.model.Prediction
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
private data class PredictionAnalysisResult(
    val accuracy: Float = 0f,
    val analysis: String = "",
    val biases: List<String> = emptyList(),
    val lessons: List<String> = emptyList()
)

@HiltViewModel
class PredictionViewModel @Inject constructor(
    private val predictionRepository: PredictionRepository,
    private val thoughtRepository: ThoughtRepository,
    private val llmRepository: LlmRepository
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }

    val pendingPredictions: StateFlow<List<Prediction>> = predictionRepository.getPending()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val reviewedPredictions: StateFlow<List<Prediction>> = predictionRepository.getReviewed()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val isAnalyzing = MutableStateFlow(false)
    val streamingContent = MutableStateFlow("")
    val averageAccuracy = MutableStateFlow<Float?>(null)

    init {
        viewModelScope.launch {
            averageAccuracy.value = predictionRepository.getAverageAccuracy()
        }
    }

    fun reviewPrediction(predictionId: String, actualOutcome: String) {
        viewModelScope.launch {
            isAnalyzing.value = true
            streamingContent.value = ""
            val prediction = predictionRepository.getById(predictionId) ?: run {
                isAnalyzing.value = false
                return@launch
            }
            val thought = thoughtRepository.getById(prediction.thoughtId)

            val messages = PromptTemplates.analyzePrediction(
                thoughtContent = thought?.content ?: "",
                predictedOutcome = prediction.predictedOutcome,
                actualOutcome = actualOutcome,
                predictionDate = DateUtils.formatDate(thought?.createdAt ?: 0)
            )

            val fullContent = StringBuilder()
            llmRepository.completeStream(messages).collect { delta ->
                fullContent.append(delta)
                streamingContent.value = fullContent.toString()
            }

            // Stream finished, parse and save
            val response = fullContent.toString()
            try {
                val result = json.decodeFromString<PredictionAnalysisResult>(com.sodapop.app.util.JsonExtractor.extract(response))
                val updated = prediction.copy(
                    actualOutcome = actualOutcome,
                    accuracy = result.accuracy,
                    llmAnalysis = result.analysis,
                    reviewedAt = System.currentTimeMillis()
                )
                predictionRepository.update(updated)
            } catch (e: Exception) {
                val updated = prediction.copy(
                    actualOutcome = actualOutcome,
                    llmAnalysis = response,
                    reviewedAt = System.currentTimeMillis()
                )
                predictionRepository.update(updated)
            }

            averageAccuracy.value = predictionRepository.getAverageAccuracy()
            streamingContent.value = ""
            isAnalyzing.value = false
        }
    }
}
