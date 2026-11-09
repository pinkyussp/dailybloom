package com.example.dailyquotes.ui

/**
 * Opening the photo picker or the share sheet takes the app out of the foreground,
 * which would otherwise re-lock it. Set [skipNextStop] right before launching one of those.
 */
object LockGuard {
    @Volatile
    var skipNextStop = false
}
