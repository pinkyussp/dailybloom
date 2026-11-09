package com.example.dailyquotes.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Build
import android.content.Context
import android.content.Intent
import com.example.dailyquotes.data.AppSettings
import java.util.Calendar

/**
 * Schedules a once-a-day reminder at the user's chosen time.
 * Each alarm is one-shot; the receiver schedules the next day when it fires.
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
        val trigger = nextTriggerMillis(hour, minute)
        // Exact alarms fire on time even in battery saving. If the user has not allowed
        // exact alarms, fall back to a near-on-time alarm. The receiver re-arms the next day.
        val exact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (exact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pendingIntent)
        }
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
