package com.example.dailyquotes.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyquotes.data.AppSettings
import com.example.dailyquotes.data.LocalStore
import com.example.dailyquotes.data.Quote
import com.example.dailyquotes.data.QuoteApi
import com.example.dailyquotes.reminder.ReminderScheduler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TodayUiState(
    val quote: Quote? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/** Holds all app state. Screens observe the flows and call the functions below. */
class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val store = LocalStore(application)

    private val _today = MutableStateFlow(TodayUiState())
    val today: StateFlow<TodayUiState> = _today.asStateFlow()

    private val _favourites = MutableStateFlow(store.loadFavourites())
    val favourites: StateFlow<List<Quote>> = _favourites.asStateFlow()

    private val _settings = MutableStateFlow(store.loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        // Reuse today's saved quote; only hit the network when there isn't one yet.
        val saved = store.loadTodayQuote()
        if (saved != null) {
            _today.value = TodayUiState(quote = saved)
        } else {
            fetchNewQuote()
        }
    }

    // ---- Today ---------------------------------------------------------

    fun fetchNewQuote() {
        if (_today.value.isLoading) return
        viewModelScope.launch {
            _today.update { it.copy(isLoading = true, error = null) }
            try {
                val quote = QuoteApi.fetchRandomQuote()
                store.saveTodayQuote(quote)
                _today.value = TodayUiState(quote = quote)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _today.update {
                    it.copy(
                        isLoading = false,
                        error = "Couldn't load a quote. Check your connection and try again."
                    )
                }
            }
        }
    }

    // ---- Favourites ----------------------------------------------------

    fun toggleFavourite(quote: Quote) {
        val current = _favourites.value
        val updated = if (current.any { it.id == quote.id }) {
            current.filterNot { it.id == quote.id }
        } else {
            listOf(quote) + current // newest first
        }
        saveFavourites(updated)
    }

    fun removeFavourite(quote: Quote) {
        saveFavourites(_favourites.value.filterNot { it.id == quote.id })
    }

    private fun saveFavourites(quotes: List<Quote>) {
        _favourites.value = quotes
        store.saveFavourites(quotes)
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
