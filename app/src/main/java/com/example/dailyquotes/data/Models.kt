package com.example.dailyquotes.data

import java.time.LocalDate

const val MAX_PHOTOS_PER_ENTRY = 10
const val DEFAULT_REMINDER_MESSAGE = "Take a moment to write about your day."

enum class Mood(val label: String) {
    GREAT("Great"),
    GOOD("Good"),
    OKAY("Okay"),
    LOW("Low"),
    ROUGH("Rough")
}

enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class Accent(val label: String) {
    LAVENDER("Lavender"),
    MINT("Mint"),
    PEACH("Peach")
}

/** One day's journal entry. Photos are file names inside the app's private photo folder. */
data class Entry(
    val date: LocalDate,
    val title: String = "",
    val body: String = "",
    val mood: Mood? = null,
    val photos: List<String> = emptyList()
) {
    val isBlank: Boolean
        get() = title.isBlank() && body.isBlank() && mood == null && photos.isEmpty()
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accent: Accent = Accent.LAVENDER,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val reminderMessage: String = DEFAULT_REMINDER_MESSAGE,
    val weekStartsMonday: Boolean = false,
    val showPrompts: Boolean = true,
    val appLockEnabled: Boolean = false
)
