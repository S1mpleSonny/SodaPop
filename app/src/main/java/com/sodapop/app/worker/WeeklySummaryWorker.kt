package com.sodapop.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sodapop.app.data.repository.LlmRepository
import com.sodapop.app.data.repository.SummaryRepository
import com.sodapop.app.data.repository.ThoughtRepository
import com.sodapop.app.domain.model.Summary
import com.sodapop.app.domain.model.SummaryType
import com.sodapop.app.util.DateUtils
import com.sodapop.app.util.PromptTemplates
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class WeeklySummaryResult(
    val overview: String = "",
    val trends: List<String> = emptyList(),
    val evolution: List<String> = emptyList(),
    val suggestions: List<String> = emptyList()
)

@HiltWorker
class WeeklySummaryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val thoughtRepository: ThoughtRepository,
    private val summaryRepository: SummaryRepository,
    private val llmRepository: LlmRepository
) : CoroutineWorker(context, params) {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun doWork(): Result {
        val weekStart = DateUtils.weekStart()
        val now = System.currentTimeMillis()

        if (summaryRepository.getByTypeAndPeriod(SummaryType.WEEKLY, weekStart) != null) {
            return Result.success()
        }

        val thoughts = thoughtRepository.getByDateRange(weekStart, now)
        if (thoughts.isEmpty()) return Result.success()

        val messages = PromptTemplates.weeklySummary(
            dailySummaries = emptyList(), // Could fetch daily summaries here
            thoughts = thoughts.map { it.content }
        )

        llmRepository.complete(messages)
            .onSuccess { response ->
                try {
                    val result = json.decodeFromString<WeeklySummaryResult>(com.sodapop.app.util.JsonExtractor.extract(response))
                    val summary = Summary(
                        type = SummaryType.WEEKLY,
                        content = result.overview,
                        themes = result.trends,
                        questions = result.suggestions,
                        periodStart = weekStart,
                        periodEnd = now
                    )
                    summaryRepository.save(summary)
                    showNotification("每周总结已生成", "查看本周思维趋势")
                } catch (e: Exception) {
                    val summary = Summary(
                        type = SummaryType.WEEKLY,
                        content = response,
                        periodStart = weekStart,
                        periodEnd = now
                    )
                    summaryRepository.save(summary)
                }
            }
            .onFailure {
                return Result.retry()
            }

        return Result.success()
    }

    private fun showNotification(title: String, content: String) {
        val channelId = "weekly_summary"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "每周总结", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .build()

        manager.notify(1002, notification)
    }
}
