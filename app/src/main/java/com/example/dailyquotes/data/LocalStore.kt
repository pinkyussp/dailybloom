package com.example.dailyquotes.data

import android.content.Context
import androidx.core.content.edit
import org.json.JSONObject
import java.time.LocalDate

/**
 * Persists notes, the first day the app was used, and settings in SharedPreferences.
 * Everything stays on the device.
 */
class LocalStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ---- Notes ---------------------------------------------------------

    /** All notes keyed by date. Days without a note are absent from the map. */
    fun loadNotes(): Map<LocalDate, String> {
        val raw = prefs.getString(KEY_NOTES, null) ?: return emptyMap()
        return try {
            val json = JSONObject(raw)
            json.keys().asSequence().associate { key ->
                LocalDate.parse(key) to json.getString(key)
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun saveNotes(notes: Map<LocalDate, String>) {
        val json = JSONObject()
        notes.forEach { (date, text) -> json.put(date.toString(), text) }
        prefs.edit { putString(KEY_NOTES, json.toString()) }
    }

    // ---- First day -----------------------------------------------------

    /** The first day the app was opened. Days before this are never counted as missed. */
    fun loadFirstDay(): LocalDate? =
        prefs.getString(KEY_FIRST_DAY, null)?.let { LocalDate.parse(it) }

    fun saveFirstDay(date: LocalDate) {
        prefs.edit { putString(KEY_FIRST_DAY, date.toString()) }
    }

    // ---- Settings ------------------------------------------------------

    fun loadSettings(): AppSettings = AppSettings(
        darkTheme = prefs.getBoolean(KEY_DARK_THEME, false),
        reminderEnabled = prefs.getBoolean(KEY_REMINDER_ENABLED, false),
        reminderHour = prefs.getInt(KEY_REMINDER_HOUR, 8),
        reminderMinute = prefs.getInt(KEY_REMINDER_MINUTE, 0)
    )

    fun saveSettings(settings: AppSettings) {
        prefs.edit {
            putBoolean(KEY_DARK_THEME, settings.darkTheme)
            putBoolean(KEY_REMINDER_ENABLED, settings.reminderEnabled)
            putInt(KEY_REMINDER_HOUR, settings.reminderHour)
            putInt(KEY_REMINDER_MINUTE, settings.reminderMinute)
        }
    }

    private companion object {
        const val PREFS_NAME = "dailybloom"
        const val KEY_NOTES = "notes"
        const val KEY_FIRST_DAY = "first_day"
        const val KEY_DARK_THEME = "dark_theme"
        const val KEY_REMINDER_ENABLED = "reminder_enabled"
        const val KEY_REMINDER_HOUR = "reminder_hour"
        const val KEY_REMINDER_MINUTE = "reminder_minute"
    }
}
