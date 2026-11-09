package com.example.dailyquotes.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Turns the journal into a plain text document. Photos are not included. */
object JournalExporter {

    private val dateFormat = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")
    private val divider = "=".repeat(48)

    fun buildText(entries: Collection<Entry>): String {
        val out = StringBuilder()
        out.appendLine("Dailybloom journal")
        out.appendLine("Exported on ${LocalDate.now().format(dateFormat)}")
        out.appendLine()

        entries.sortedBy { it.date }.forEach { entry ->
            out.appendLine(divider)
            out.appendLine(entry.date.format(dateFormat))
            entry.mood?.let { out.appendLine("Mood: ${it.label}") }
            if (entry.title.isNotBlank()) {
                out.appendLine()
                out.appendLine(entry.title.trim())
            }
            if (entry.body.isNotBlank()) {
                out.appendLine()
                out.appendLine(entry.body.trim())
            }
            if (entry.photos.isNotEmpty()) {
                out.appendLine()
                out.appendLine("[${entry.photos.size} photo(s) not included in text export]")
            }
            out.appendLine()
        }
        return out.toString()
    }
}
