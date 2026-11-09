package com.example.dailyquotes.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.dailyquotes.data.Quote

fun copyQuote(context: Context, quote: Quote) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(ClipData.newPlainText("Quote", quote.shareText()))
    Toast.makeText(context, "Quote copied", Toast.LENGTH_SHORT).show()
}

fun shareQuote(context: Context, quote: Quote) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, quote.shareText())
    }
    context.startActivity(Intent.createChooser(intent, "Share quote"))
}
