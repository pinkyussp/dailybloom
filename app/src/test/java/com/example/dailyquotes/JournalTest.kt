package com.example.dailyquotes

import com.example.dailyquotes.data.Entry
import com.example.dailyquotes.ui.Markup
import com.example.dailyquotes.ui.currentStreak
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class JournalTest {

    @Test
    fun plainTextRemovesFormattingMarkers() {
        assertEquals("Hello world and more", Markup.plain("# Hello **world** and *more*"))
        assertEquals("underlined", Markup.plain("__underlined__"))
    }

    @Test
    fun loneAsterisksAreNotFormatting() {
        assertEquals("5 * 3 * 2", Markup.plain("5 * 3 * 2"))
    }

    @Test
    fun streakCountsBackFromToday() {
        val today = LocalDate.of(2026, 11, 9)
        val entries = listOf(today, today.minusDays(1), today.minusDays(3))
            .associateWith { Entry(date = it, title = "", body = "x", mood = null, photos = emptyList()) }
        assertEquals(2, currentStreak(entries, today))
    }

    @Test
    fun streakStillCountsWhenTodayIsNotWrittenYet() {
        val today = LocalDate.of(2026, 11, 9)
        val entries = listOf(today.minusDays(1), today.minusDays(2))
            .associateWith { Entry(date = it, title = "", body = "x", mood = null, photos = emptyList()) }
        assertEquals(2, currentStreak(entries, today))
    }
}
