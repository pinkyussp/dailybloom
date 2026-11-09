package com.example.dailyquotes.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.dailyquotes.data.AppSettings
import java.util.Calendar

/**
 * Schedules a once-a-day reminder at the user's chosen time.
 * Uses inexact repeating alarms, so no special exact-alarm permission is needed.
 */
object ReminderScheduler {

    private const val REQUEST_CODE = 1001

    /** Makes the alarm match the settings: scheduled when enabled, cancelled otherwise. */
    fun apply(context: Context, settings: AppSettings) {
        if (settings.reminderEnabled) {
            schedule(context, settings.reminderHour, settings.reminderMinute)
        } else {
            cancel(context)
        }
    }

    private fun schedule(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = reminderIntent(context)
        alarmManager.cancel(pendingIntent)
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            nextTriggerMillis(hour, minute),
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java)
            .cancel(reminderIntent(context))
    }

    private fun reminderIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    /** The next occurrence of hour:minute, today if still ahead, otherwise tomorrow. */
    private fun nextTriggerMillis(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }
}
