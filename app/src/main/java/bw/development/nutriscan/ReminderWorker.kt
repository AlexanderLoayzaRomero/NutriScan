package bw.development.nutriscan

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import bw.development.nutriscan.data.AppDatabase
import bw.development.nutriscan.data.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar

class ReminderWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val context = applicationContext

        val db = AppDatabase.getDatabase(context)
        val prefs = UserPreferencesRepository(context)

        val goal = prefs.calorieGoalFlow.first()

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
        val end = cal.timeInMillis

        val items = db.foodItemDao().getAllFoodItemsForDay(start, end).first()
        val total = items.sumOf { it.calories }
        val remaining = goal - total

        val title: String
        val message: String

        if (remaining > 0) {
            title = "🥑 NutriScan: ¡No olvides cenar!"
            message = "Aún te quedan $remaining kcal para alcanzar tu meta de hoy."
        } else {
            title = "🎉 ¡Meta cumplida!"
            message = "Has alcanzado tu objetivo calórico de hoy. ¡Sigue así!"
        }

        sendNotification(context, title, message)

        return Result.success()
    }

    private fun sendNotification(context: Context, title: String, message: String) {
        val channelId = "nutriscan_daily_reminder"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios Diarios",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_nutriscan_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}