package com.example.dailyquotes.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

/**
 * Light formatting stored inside the entry text:
 *   **bold**   *italic*   __underline__   # heading (start of a line)
 * The markers stay in the stored text so nothing is lost; they are shown faded in
 * the editor and removed when the journal is exported as plain text.
 */
object Markup {
    private val bold = Regex("\\*\\*(.+?)\\*\\*")
    private val underline = Regex("__(.+?)__")
    private val italic = Regex("(?<!\\*)\\*(?!\\*)(.+?)(?<!\\*)\\*(?!\\*)")
    private val heading = Regex("(?m)^# (.*)$")

    fun styled(text: String, markerColor: Color): AnnotatedString {
        val builder = AnnotatedString.Builder(text)

        heading.findAll(text).forEach { match ->
            builder.addStyle(
                SpanStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                match.range.first, match.range.last + 1
            )
            builder.addStyle(SpanStyle(color = markerColor), match.range.first, match.range.first + 2)
        }
        bold.findAll(text).forEach { match ->
            builder.addStyle(SpanStyle(fontWeight = FontWeight.Bold), match.groups[1]!!.range.first, match.groups[1]!!.range.last + 1)
            markers(builder, match.range, 2, markerColor)
        }
        underline.findAll(text).forEach { match ->
            builder.addStyle(SpanStyle(textDecoration = TextDecoration.Underline), match.groups[1]!!.range.first, match.groups[1]!!.range.last + 1)
            markers(builder, match.range, 2, markerColor)
        }
        italic.findAll(text).forEach { match ->
            builder.addStyle(SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), match.groups[1]!!.range.first, match.groups[1]!!.range.last + 1)
            markers(builder, match.range, 1, markerColor)
        }
        return builder.toAnnotatedString()
    }

    private fun markers(builder: AnnotatedString.Builder, range: IntRange, size: Int, color: Color) {
        builder.addStyle(SpanStyle(color = color), range.first, range.first + size)
        builder.addStyle(SpanStyle(color = color), range.last + 1 - size, range.last + 1)
    }

    /** Removes the markers so the text reads as plain text. */
    fun plain(text: String): String {
        var out = heading.replace(text) { it.groupValues[1] }
        out = bold.replace(out) { it.groupValues[1] }
        out = underline.replace(out) { it.groupValues[1] }
        out = italic.replace(out) { it.groupValues[1] }
        return out
    }
}

class MarkupTransformation(private val markerColor: Color) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText =
        TransformedText(Markup.styled(text.text, markerColor), OffsetMapping.Identity)
}
