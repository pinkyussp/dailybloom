package com.example.dailyquotes.data

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate

/**
 * Persists today's quote, favourites and settings in SharedPreferences.
 */
class LocalStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ---- Today's quote -------------------------------------------------

    /** Returns the quote saved for today, or null if none was saved today. */
    fun loadTodayQuote(): Quote? {
        val raw = prefs.getString(KEY_TODAY_QUOTE, null) ?: return null
        return try {
            val json = JSONObject(raw)
            if (json.getString("date") != today()) {
                null
            } else {
                Quote.fromStorageJson(json.getJSONObject("quote"))
            }
        } catch (e: JSONException) {
            null
        }
    }

    fun saveTodayQuote(quote: Quote) {
        val json = JSONObject()
            .put("date", today())
            .put("quote", quote.toStorageJson())
        prefs.edit { putString(KEY_TODAY_QUOTE, json.toString()) }
    }

    // ---- Favourites ----------------------------------------------------

    fun loadFavourites(): List<Quote> {
        val raw = prefs.getString(KEY_FAVOURITES, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                Quote.fromStorageJson(array.getJSONObject(index))
            }
        } catch (e: JSONException) {
            emptyList()
        }
    }

    fun saveFavourites(quotes: List<Quote>) {
        val array = JSONArray()
        quotes.forEach { array.put(it.toStorageJson()) }
        prefs.edit { putString(KEY_FAVOURITES, array.toString()) }
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

    private fun today(): String = LocalDate.now().toString()

    private companion object {
        const val PREFS_NAME = "daily_quotes"
        const val KEY_TODAY_QUOTE = "today_quote"
        const val KEY_FAVOURITES = "favourites"
        const val KEY_DARK_THEME = "dark_theme"
        const val KEY_REMINDER_ENABLED = "reminder_enabled"
        const val KEY_REMINDER_HOUR = "reminder_hour"
        const val KEY_REMINDER_MINUTE = "reminder_minute"
    }
}
