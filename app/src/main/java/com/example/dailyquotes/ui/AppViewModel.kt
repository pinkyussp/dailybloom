package com.example.dailyquotes.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.dailyquotes.data.AppSettings
import com.example.dailyquotes.data.LocalStore
import com.example.dailyquotes.reminder.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

/** Holds all app state. Screens observe the flows and call the functions below. */
class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val store = LocalStore(application)

    private val _notes = MutableStateFlow(store.loadNotes())
    val notes: StateFlow<Map<LocalDate, String>> = _notes.asStateFlow()

    private val _firstDay = MutableStateFlow(
        store.loadFirstDay() ?: LocalDate.now().also { store.saveFirstDay(it) }
    )
    val firstDay: StateFlow<LocalDate> = _firstDay.asStateFlow()

    private val _settings = MutableStateFlow(store.loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    // ---- Notes ---------------------------------------------------------

    /** Saves the note for [date]. Blank text removes the note for that day. */
    fun saveNote(date: LocalDate, text: String) {
        val trimmed = text.trim()
        val updated = _notes.value.toMutableMap()
        if (trimmed.isEmpty()) {
            updated.remove(date)
        } else {
            updated[date] = trimmed
        }
        _notes.value = updated
        store.saveNotes(updated)
    }

    // ---- Settings ------------------------------------------------------

    fun setDarkTheme(enabled: Boolean) = updateSettings { it.copy(darkTheme = enabled) }

    fun setReminderEnabled(enabled: Boolean) = updateSettings { it.copy(reminderEnabled = enabled) }

    fun setReminderTime(hour: Int, minute: Int) = updateSettings {
        it.copy(reminderHour = hour, reminderMinute = minute)
    }

    private fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val updated = transform(_settings.value)
        _settings.value = updated
        store.saveSettings(updated)
        ReminderScheduler.apply(getApplication(), updated)
    }
}
