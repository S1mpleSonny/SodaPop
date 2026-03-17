package com.sodapop.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sodapop.app.data.repository.ThoughtRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class InspirationReplayWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val thoughtRepository: ThoughtRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val thought = thoughtRepository.getRandom(1).firstOrNull() ?: return Result.success()

        showNotification(
            title = "💡 灵感回放",
            content = thought.content.take(80)
        )

        return Result.success()
    }

    private fun showNotification(title: String, content: String) {
        val channelId = "inspiration_replay"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "灵感回放", NotificationManager.IMPORTANCE_LOW)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .build()

        manager.notify(1003, notification)
    }
}
