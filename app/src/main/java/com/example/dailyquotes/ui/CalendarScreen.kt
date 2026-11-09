package com.example.dailyquotes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private enum class DayStatus { Written, Missed, Pending, Future, BeforeStart }

/** Works out how a day should look: written, missed, still open, in the future, or before the app. */
private fun statusOf(
    date: LocalDate,
    today: LocalDate,
    firstDay: LocalDate,
    notes: Map<LocalDate, String>
): DayStatus = when {
    date.isAfter(today) -> DayStatus.Future
    notes.containsKey(date) -> DayStatus.Written
    date == today -> DayStatus.Pending
    date.isBefore(firstDay) -> DayStatus.BeforeStart
    else -> DayStatus.Missed
}

/** Month view of notes. Tap a day to read what was written, or see that it was missed. */
@Composable
fun CalendarScreen(
    notes: Map<LocalDate, String>,
    firstDay: LocalDate,
    today: LocalDate,
    modifier: Modifier = Modifier
) {
    var month by remember { mutableStateOf(YearMonth.from(today)) }
    var selected by remember { mutableStateOf<LocalDate?>(null) }

    val monthDays = (1..month.lengthOfMonth()).map { month.atDay(it) }
    val leadingBlanks = month.atDay(1).dayOfWeek.value % 7 // Sunday is the first column
    val cells: List<LocalDate?> = List(leadingBlanks) { null } + monthDays
    val weeks = cells.chunked(7).map { week ->
        week + List<LocalDate?>(7 - week.size) { null }
    }

    val written = monthDays.count { statusOf(it, today, firstDay, notes) == DayStatus.Written }
    val missed = monthDays.count { statusOf(it, today, firstDay, notes) == DayStatus.Missed }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Calendar",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { month = month.minusMonths(1) }) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
            }
            Text(
                text = month.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = { month = month.plusMonths(1) }) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
            }
        }

        Text(
            text = "$written written · $missed missed",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    DayCell(
                        date = date,
                        status = date?.let { statusOf(it, today, firstDay, notes) },
                        isToday = date == today,
                        isSelected = date != null && date == selected,
                        onClick = { selected = date },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        val chosen = selected
        if (chosen != null) {
            DayDetail(
                date = chosen,
                status = statusOf(chosen, today, firstDay, notes),
                note = notes[chosen]
            )
        } else {
            Text(
                text = "Tap a day to see what you wrote.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate?,
    status: DayStatus?,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)

    val fill = if (status == DayStatus.Written) scheme.primaryContainer else Color.Transparent
    val textColor = when (status) {
        DayStatus.Written -> scheme.onPrimaryContainer
        DayStatus.Future, DayStatus.BeforeStart, null -> scheme.onSurfaceVariant.copy(alpha = 0.5f)
        else -> scheme.onSurface
    }
    val dotColor = when (status) {
        DayStatus.Written -> scheme.primary
        DayStatus.Missed -> scheme.error
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(3.dp)
            .clip(shape)
            .background(fill)
            .border(
                width = when {
                    isSelected -> 2.dp
                    isToday -> 1.dp
                    else -> 0.dp
                },
                color = if (isSelected) scheme.primary else scheme.outline,
                shape = shape
            )
            .clickable(enabled = date != null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isToday) FontWeight.Bold else null,
                    color = textColor
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(dotColor, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun DayDetail(date: LocalDate, status: DayStatus, note: String?) {
    val scheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = statusLabel(status),
                style = MaterialTheme.typography.labelLarge,
                color = when (status) {
                    DayStatus.Written -> scheme.primary
                    DayStatus.Missed -> scheme.error
                    else -> scheme.onSurfaceVariant
                }
            )
            if (note != null) {
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurface
                )
            }
        }
    }
}

private fun statusLabel(status: DayStatus): String = when (status) {
    DayStatus.Written -> "Note written"
    DayStatus.Missed -> "Missed, no note was written"
    DayStatus.Pending -> "Not written yet today"
    DayStatus.Future -> "Coming up"
    DayStatus.BeforeStart -> "Before you started Dailybloom"
}
