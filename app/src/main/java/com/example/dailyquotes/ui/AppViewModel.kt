package com.example.dailyquotes.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyquotes.data.Accent
import com.example.dailyquotes.data.AppSettings
import com.example.dailyquotes.data.DEFAULT_REMINDER_MESSAGE
import com.example.dailyquotes.data.Entry
import com.example.dailyquotes.data.JournalExporter
import com.example.dailyquotes.data.LocalStore
import com.example.dailyquotes.data.MAX_PHOTOS_PER_ENTRY
import com.example.dailyquotes.data.PhotoStore
import com.example.dailyquotes.data.ThemeMode
import com.example.dailyquotes.reminder.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** Holds all app state. Screens observe the flows and call the functions below. */
class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val store = LocalStore(application)

    private val _entries = MutableStateFlow(store.loadEntries())
    val entries: StateFlow<Map<LocalDate, Entry>> = _entries.asStateFlow()

    // Separate, narrow flows so typing only refreshes the journal, not the whole app.
    private val _todayEntry = MutableStateFlow(_entries.value[LocalDate.now()])
    val todayEntry: StateFlow<Entry?> = _todayEntry.asStateFlow()

    private val _streak = MutableStateFlow(currentStreak(_entries.value, LocalDate.now()))
    val streak: StateFlow<Int> = _streak.asStateFlow()

    private val _firstDay = MutableStateFlow(
        store.loadFirstDay() ?: LocalDate.now().also { store.saveFirstDay(it) }
    )
    val firstDay: StateFlow<LocalDate> = _firstDay.asStateFlow()

    private val _settings = MutableStateFlow(store.loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _locked = MutableStateFlow(store.loadSettings().appLockEnabled)
    val locked: StateFlow<Boolean> = _locked.asStateFlow()

    private var saveJob: Job? = null
    private val dirtyDates = mutableSetOf<LocalDate>()

    // ---- Journal (today only) ------------------------------------------

    /** Applies a change to today's entry. Past days can never be edited. */
    fun updateToday(transform: (Entry) -> Entry) {
        val date = LocalDate.now()
        val current = _entries.value[date] ?: Entry(date = date)
        val updated = transform(current)

        val map = _entries.value.toMutableMap()
        if (updated.isBlank) {
            map.remove(date)
        } else {
            map[date] = updated
        }
        _entries.value = map
        dirtyDates += date
        _todayEntry.value = if (updated.isBlank) null else updated
        _streak.value = currentStreak(map, date)
        scheduleSave()
    }

    /** Copies picked photos into app storage and attaches them to today's entry. */
    fun addPhotos(uris: List<Uri>) {
        val room = MAX_PHOTOS_PER_ENTRY - (_entries.value[LocalDate.now()]?.photos?.size ?: 0)
        val toImport = uris.take(room.coerceAtLeast(0))
        if (toImport.isEmpty()) return

        viewModelScope.launch {
            val names = withContext(Dispatchers.IO) {
                toImport.mapNotNull { PhotoStore.importFromUri(getApplication(), it) }
            }
            if (names.isNotEmpty()) {
                updateToday { entry ->
                    entry.copy(photos = (entry.photos + names).take(MAX_PHOTOS_PER_ENTRY))
                }
            }
        }
    }

    fun removePhoto(name: String) {
        PhotoStore.delete(getApplication(), name)
        updateToday { entry -> entry.copy(photos = entry.photos - name) }
    }

    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(400)
            val changed = dirtyDates.toList()
            dirtyDates.clear()
            val snapshot = _entries.value
            // Only the days that changed are written, and off the UI thread.
            withContext(Dispatchers.IO) {
                changed.forEach { date ->
                    val entry = snapshot[date]
                    if (entry != null) store.saveEntry(entry) else store.deleteEntry(date)
                }
            }
        }
    }

    // ---- Export and data -----------------------------------------------

    fun exportText(): String = JournalExporter.buildText(_entries.value.values)

    fun deleteAllData() {
        saveJob?.cancel()
        _entries.value = emptyMap()
        _todayEntry.value = null
        _streak.value = 0
        dirtyDates.clear()
        store.deleteAllEntries()
        PhotoStore.deleteAll(getApplication())
    }

    // ---- App lock ------------------------------------------------------

    fun setUnlocked() {
        _locked.value = false
    }

    fun lockIfEnabled() {
        if (_settings.value.appLockEnabled) _locked.value = true
    }

    // ---- Settings ------------------------------------------------------

    fun setThemeMode(mode: ThemeMode) = updateSettings { it.copy(themeMode = mode) }

    fun setAccent(accent: Accent) = updateSettings { it.copy(accent = accent) }

    fun setReminderEnabled(enabled: Boolean) = updateSettings { it.copy(reminderEnabled = enabled) }

    fun setReminderTime(hour: Int, minute: Int) = updateSettings {
        it.copy(reminderHour = hour, reminderMinute = minute)
    }

    fun setReminderMessage(message: String) = updateSettings {
        it.copy(reminderMessage = message.trim().ifEmpty { DEFAULT_REMINDER_MESSAGE })
    }

    fun setWeekStartsMonday(enabled: Boolean) = updateSettings { it.copy(weekStartsMonday = enabled) }

    fun setShowPrompts(enabled: Boolean) = updateSettings { it.copy(showPrompts = enabled) }

    fun setAppLock(enabled: Boolean) {
        updateSettings { it.copy(appLockEnabled = enabled) }
        if (!enabled) _locked.value = false
    }

    private fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val updated = transform(_settings.value)
        _settings.value = updated
        store.saveSettings(updated)
        ReminderScheduler.apply(getApplication(), updated)
    }
}
