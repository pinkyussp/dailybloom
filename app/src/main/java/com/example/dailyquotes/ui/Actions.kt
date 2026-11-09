package com.example.dailyquotes.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File

/** Writes the journal text to a file and opens the share sheet so it can be saved anywhere. */
fun shareJournal(context: Context, text: String) {
    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    val file = File(dir, "Dailybloom-journal.txt")
    file.writeText(text)

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Export journal"))
}

/** Opens this app's notification settings so the user can turn notifications on. */
fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    context.startActivity(intent)
}
