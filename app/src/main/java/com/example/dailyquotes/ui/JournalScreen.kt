package com.example.dailyquotes.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Title
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.dailyquotes.data.Entry
import com.example.dailyquotes.data.MAX_PHOTOS_PER_ENTRY
import com.example.dailyquotes.data.Mood
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val writingPrompts = listOf(
    "What made you smile today?",
    "What is one thing you are looking forward to?",
    "What did you learn today?",
    "Who did you spend time with today?",
    "What is one small win from today?",
    "What was hard today, and how did you handle it?",
    "What is something you want to remember about today?"
)

/** Today's entry. Changes save automatically as you type. */
@Composable
fun JournalScreen(
    entry: Entry?,
    showPrompt: Boolean,
    streak: Int,
    onUpdate: ((Entry) -> Entry) -> Unit,
    onAddPhotos: (List<Uri>) -> Unit,
    onRemovePhoto: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    var title by remember { mutableStateOf(entry?.title.orEmpty()) }
    var body by remember { mutableStateOf(TextFieldValue(entry?.body.orEmpty())) }

    fun setBody(value: TextFieldValue) {
        body = value
        onUpdate { current -> current.copy(body = value.text) }
    }

    fun insertAtCursor(text: String) {
        val current = body.text
        val start = body.selection.start.coerceIn(0, current.length)
        val end = body.selection.end.coerceIn(0, current.length)
        val newText = current.substring(0, start) + text + current.substring(end)
        setBody(TextFieldValue(newText, TextRange(start + text.length)))
    }

    fun insertLinePrefix(prefix: String) {
        val cursor = body.selection.start.coerceIn(0, body.text.length)
        val atLineStart = cursor == 0 || body.text[cursor - 1] == '\n'
        insertAtCursor(if (atLineStart) prefix else "\n$prefix")
    }

    fun wrapSelection(marker: String) {
        val a = minOf(body.selection.start, body.selection.end).coerceIn(0, body.text.length)
        val b = maxOf(body.selection.start, body.selection.end).coerceIn(0, body.text.length)
        val newText = body.text.substring(0, a) + marker + body.text.substring(a, b) + marker + body.text.substring(b)
        val range = if (a == b) TextRange(a + marker.length) else TextRange(a + marker.length, b + marker.length)
        setBody(TextFieldValue(newText, range))
    }

    val markerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)

    val photos = entry?.photos.orEmpty()
    val remaining = MAX_PHOTOS_PER_ENTRY - photos.size
    val pickPhotos = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = remaining.coerceAtLeast(1))
    ) { uris ->
        if (uris.isNotEmpty()) onAddPhotos(uris)
    }

    val prompt = writingPrompts[today.dayOfYear % writingPrompts.size]

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Journal",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (streak > 0) StreakChip(days = streak)
        }

        Text(
            text = "How are you feeling?",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Mood.entries.forEach { mood ->
                FilterChip(
                    selected = entry?.mood == mood,
                    onClick = {
                        onUpdate { current ->
                            current.copy(mood = if (current.mood == mood) null else mood)
                        }
                    },
                    label = { Text(mood.label) }
                )
            }
        }

        OutlinedTextField(
            value = title,
            onValueChange = { value ->
                title = value
                onUpdate { current -> current.copy(title = value) }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            placeholder = { Text("Title (optional)") }
        )

        OutlinedTextField(
            value = body,
            onValueChange = { value -> setBody(value) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 260.dp),
            shape = RoundedCornerShape(24.dp),
            placeholder = { Text(if (showPrompt) prompt else "Write about your day") },
            visualTransformation = remember(markerColor) { MarkupTransformation(markerColor) }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FormatChip("Bold", Icons.Filled.FormatBold) { wrapSelection("**") }
            FormatChip("Italic", Icons.Filled.FormatItalic) { wrapSelection("*") }
            FormatChip("Underline", Icons.Filled.FormatUnderlined) { wrapSelection("__") }
            FormatChip("Heading", Icons.Filled.Title) { insertLinePrefix("# ") }
            AssistChip(
                onClick = { insertLinePrefix("- ") },
                label = { Text("Bullet") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.FormatListBulleted,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
            AssistChip(
                onClick = { insertLinePrefix("[ ] ") },
                label = { Text("Checklist") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.CheckBox,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
            AssistChip(
                onClick = {
                    pickPhotos.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                enabled = remaining > 0,
                label = { Text(if (remaining > 0) "Add photos" else "Photo limit reached") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        if (photos.isNotEmpty()) {
            Text(
                text = "Photos (${photos.size} of $MAX_PHOTOS_PER_ENTRY)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                photos.forEach { name ->
                    PhotoThumb(
                        fileName = name,
                        size = 112.dp,
                        onRemove = { onRemovePhoto(name) }
                    )
                }
            }
        }

        val words = body.text.trim().split(Regex("\\s+")).count { it.isNotEmpty() }
        Text(
            text = if (words == 0) "Saved automatically" else
                "$words ${if (words == 1) "word" else "words"} · Saved automatically",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FormatChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    )
}
