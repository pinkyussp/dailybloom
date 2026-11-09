package com.example.dailyquotes.data

data class AppSettings(
    val darkTheme: Boolean = false,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0
)
