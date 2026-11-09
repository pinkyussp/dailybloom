package com.example.dailyquotes.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.dailyquotes.data.LocalStore

/** Alarms are cleared on reboot, so re-register the reminder once the device has started. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        ReminderScheduler.apply(context, LocalStore(context).loadSettings())
    }
}
