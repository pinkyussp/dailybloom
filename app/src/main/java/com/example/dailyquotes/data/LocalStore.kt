package com.example.dailyquotes.data

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/** Persists journal entries, the first day the app was used, and settings. Everything stays on the device. */
class LocalStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ---- Entries -------------------------------------------------------
    // Each day is stored under its own key, so saving one day only writes that day.

    fun loadEntries(): Map<LocalDate, Entry> {
        migrateLegacyEntries()
        val result = HashMap<LocalDate, Entry>()
        prefs.all.forEach { (key, value) ->
            if (!key.startsWith(ENTRY_PREFIX)) return@forEach
            val entry = (value as? String)?.let { parseEntry(it) } ?: return@forEach
            result[entry.date] = entry
        }
        return result
    }

    fun saveEntry(entry: Entry) {
        prefs.edit { putString(ENTRY_PREFIX + entry.date, entryJson(entry).toString()) }
    }

    fun deleteEntry(date: LocalDate) {
        prefs.edit { remove(ENTRY_PREFIX + date) }
    }

    fun deleteAllEntries() {
        val keys = prefs.all.keys.filter { it.startsWith(ENTRY_PREFIX) || it == KEY_ENTRIES }
        prefs.edit { keys.forEach { remove(it) } }
    }

    private fun entryJson(entry: Entry): JSONObject = JSONObject()
        .put("date", entry.date.toString())
        .put("title", entry.title)
        .put("body", entry.body)
        .put("mood", entry.mood?.name ?: "")
        .put("photos", JSONArray(entry.photos))

    private fun parseEntry(raw: String): Entry? = try {
        val obj = JSONObject(raw)
        val photosJson = obj.optJSONArray("photos") ?: JSONArray()
        val moodName = obj.optString("mood")
        Entry(
            date = LocalDate.parse(obj.getString("date")),
            title = obj.optString("title"),
            body = obj.optString("body"),
            mood = Mood.entries.firstOrNull { it.name == moodName },
            photos = List(photosJson.length()) { index -> photosJson.getString(index) }
        )
    } catch (e: Exception) {
        null
    }

    /** Moves the older single-blob storage into per-day keys. Runs once, then does nothing. */
    private fun migrateLegacyEntries() {
        val raw = prefs.getString(KEY_ENTRIES, null) ?: return
        try {
            val root = JSONObject(raw)
            prefs.edit {
                root.keys().forEach { key ->
                    val obj = root.getJSONObject(key)
                    val photosJson = obj.optJSONArray("photos") ?: JSONArray()
                    val moodName = obj.optString("mood")
                    val entry = Entry(
                        date = LocalDate.parse(key),
                        title = obj.optString("title"),
                        body = obj.optString("body"),
                        mood = Mood.entries.firstOrNull { it.name == moodName },
                        photos = List(photosJson.length()) { index -> photosJson.getString(index) }
                    )
                    putString(ENTRY_PREFIX + key, entryJson(entry).toString())
                }
                remove(KEY_ENTRIES)
            }
        } catch (e: Exception) {
            // Leave the old blob in place if it cannot be read.
        }
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
        themeMode = enumOr(KEY_THEME, ThemeMode.entries, ThemeMode.SYSTEM),
        accent = enumOr(KEY_ACCENT, Accent.entries, Accent.LAVENDER),
        reminderEnabled = prefs.getBoolean(KEY_REMINDER_ENABLED, false),
        reminderHour = prefs.getInt(KEY_REMINDER_HOUR, 8),
        reminderMinute = prefs.getInt(KEY_REMINDER_MINUTE, 0),
        reminderMessage = prefs.getString(KEY_REMINDER_MESSAGE, null) ?: DEFAULT_REMINDER_MESSAGE,
        weekStartsMonday = prefs.getBoolean(KEY_WEEK_MONDAY, false),
        showPrompts = prefs.getBoolean(KEY_SHOW_PROMPTS, true),
        appLockEnabled = prefs.getBoolean(KEY_APP_LOCK, false)
    )

    fun saveSettings(settings: AppSettings) {
        prefs.edit {
            putString(KEY_THEME, settings.themeMode.name)
            putString(KEY_ACCENT, settings.accent.name)
            putBoolean(KEY_REMINDER_ENABLED, settings.reminderEnabled)
            putInt(KEY_REMINDER_HOUR, settings.reminderHour)
            putInt(KEY_REMINDER_MINUTE, settings.reminderMinute)
            putString(KEY_REMINDER_MESSAGE, settings.reminderMessage)
            putBoolean(KEY_WEEK_MONDAY, settings.weekStartsMonday)
            putBoolean(KEY_SHOW_PROMPTS, settings.showPrompts)
            putBoolean(KEY_APP_LOCK, settings.appLockEnabled)
        }
    }

    private fun <T : Enum<T>> enumOr(key: String, values: List<T>, default: T): T {
        val name = prefs.getString(key, null) ?: return default
        return values.firstOrNull { it.name == name } ?: default
    }

    private companion object {
        const val PREFS_NAME = "dailybloom"
        const val KEY_ENTRIES = "entries"
        const val ENTRY_PREFIX = "entry:"
        const val KEY_FIRST_DAY = "first_day"
        const val KEY_THEME = "theme_mode"
        const val KEY_ACCENT = "accent"
        const val KEY_REMINDER_ENABLED = "reminder_enabled"
        const val KEY_REMINDER_HOUR = "reminder_hour"
        const val KEY_REMINDER_MINUTE = "reminder_minute"
        const val KEY_REMINDER_MESSAGE = "reminder_message"
        const val KEY_WEEK_MONDAY = "week_starts_monday"
        const val KEY_SHOW_PROMPTS = "show_prompts"
        const val KEY_APP_LOCK = "app_lock"
    }
}
