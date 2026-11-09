package com.example.dailyquotes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dailyquotes.data.Quote

@Composable
fun SavedScreen(
    favourites: List<Quote>,
    onRemove: (Quote) -> Unit,
    onCopy: (Quote) -> Unit,
    onShare: (Quote) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Saved",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = countLabel(favourites.size),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (favourites.isEmpty()) {
                Text(
                    text = "No saved quotes yet.\nTap the heart on any quote to keep it here.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favourites, key = { it.id }) { quote ->
                        SavedQuoteItem(
                            quote = quote,
                            onRemove = onRemove,
                            onCopy = onCopy,
                            onShare = onShare
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedQuoteItem(
    quote: Quote,
    onRemove: (Quote) -> Unit,
    onCopy: (Quote) -> Unit,
    onShare: (Quote) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "“${quote.text}”",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "— ${quote.author}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { onCopy(quote) }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy quote")
                }
                IconButton(onClick = { onShare(quote) }) {
                    Icon(Icons.Filled.Share, contentDescription = "Share quote")
                }
                IconButton(onClick = { onRemove(quote) }) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Remove from favourites",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun countLabel(count: Int): String = when (count) {
    0 -> "Your favourites will appear here"
    1 -> "1 saved quote"
    else -> "$count saved quotes"
}
