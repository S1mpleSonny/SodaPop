package com.sodapop.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sodapop.app.data.repository.PredictionRepository
import com.sodapop.app.data.repository.ThoughtRepository
import com.sodapop.app.util.DateUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PredictionReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val predictionRepository: PredictionRepository,
    private val thoughtRepository: ThoughtRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()
        val overdue = predictionRepository.getOverdueReviews(now)

        overdue.forEach { prediction ->
            val thought = thoughtRepository.getById(prediction.thoughtId)
            showNotification(
                title = "🔮 预测待验证",
                content = thought?.content?.take(50) ?: prediction.predictedOutcome.take(50),
                notificationId = prediction.id.hashCode()
            )
        }

        return Result.success()
    }

    private fun showNotification(title: String, content: String, notificationId: Int) {
        val channelId = "prediction_reminder"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "预测提醒", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .build()

        manager.notify(notificationId, notification)
    }
}
