package com.sodapop.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sodapop.app.R
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
private data class DailySummaryResult(
    val summary: String = "",
    val themes: List<String> = emptyList(),
    val questions: List<String> = emptyList()
)

@HiltWorker
class DailySummaryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val thoughtRepository: ThoughtRepository,
    private val summaryRepository: SummaryRepository,
    private val llmRepository: LlmRepository
) : CoroutineWorker(context, params) {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun doWork(): Result {
        val start = DateUtils.todayStart()
        val end = DateUtils.todayEnd()

        // Skip if already generated
        if (summaryRepository.getByTypeAndPeriod(SummaryType.DAILY, start) != null) {
            return Result.success()
        }

        val thoughts = thoughtRepository.getByDateRange(start, end)
        if (thoughts.isEmpty()) return Result.success()

        val messages = PromptTemplates.dailySummary(
            thoughts.map { it.createdAt to it.content }
        )

        llmRepository.complete(messages)
            .onSuccess { response ->
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
                    showNotification("每日总结已生成", result.themes.joinToString("、"))
                } catch (e: Exception) {
                    val summary = Summary(
                        type = SummaryType.DAILY,
                        content = response,
                        periodStart = start,
                        periodEnd = end
                    )
                    summaryRepository.save(summary)
                    showNotification("每日总结已生成", "查看今日思维回顾")
                }
            }
            .onFailure {
                return Result.retry()
            }

        return Result.success()
    }

    private fun showNotification(title: String, content: String) {
        val channelId = "daily_summary"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "每日总结", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .build()

        manager.notify(1001, notification)
    }
}
