package com.example.dailyquotes.reminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.dailyquotes.MainActivity
import com.example.dailyquotes.R
import com.example.dailyquotes.data.LocalStore
import com.example.dailyquotes.reminder.ReminderScheduler
import java.time.LocalDate

/**
 * Fired by AlarmManager at the reminder time. Prompts the user to write today's entry,
 * unless today's entry is already written.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val store = LocalStore(context)
        val settings = store.loadSettings()
        // Arm tomorrow's reminder first, so the next one is set even if this one is skipped.
        ReminderScheduler.apply(context, settings)
        if (!settings.reminderEnabled) return

        val todayEntry = store.loadEntries()[LocalDate.now()]
        if (todayEntry != null && !todayEntry.isBlank) return

        showNotification(context, settings.reminderMessage)
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(context: Context, message: String) {
        // Android 13+ requires the user to grant notification permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        createChannel(context)

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(message)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Daily reminder",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "A daily reminder to write your journal entry"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "daily_reminder"
        const val NOTIFICATION_ID = 2001
    }
}
