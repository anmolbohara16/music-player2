package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.metadata.service.*

@Composable
fun LibrarySearchDialog(state: LibrarySearchState, onCancel: () -> Unit, onDismiss: () -> Unit, onReview: (BatchEntry) -> Unit, onRestart: () -> Unit = {}) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (state.running) "Searching your library" else if (state.cancelled) "Search cancelled" else "Library search complete") },
        text = {
            LazyColumn(
                Modifier.fillMaxWidth().heightIn(max = (LocalConfiguration.current.screenHeightDp * 0.68f).dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    LinearProgressIndicator(progress = { if (state.total == 0) 0f else state.completed.toFloat() / state.total }, modifier = Modifier.fillMaxWidth())
                    Text("${state.completed} / ${state.total} songs")
                    if (state.running) Text(state.currentSong, style = MaterialTheme.typography.titleMedium)
                    Text("Strong matches fill missing fields. Existing values and uncertain matches need your review.")
                }
                item {
                    BatchOutcome.entries.forEach { outcome ->
                        Text("${outcome.label}: ${state.entries.count { it.outcome == outcome }}")
                    }
                    if (state.cancelled) Text("Not processed: ${state.total - state.completed}")
                }
                items(state.entries, key = { it.song.id }) { entry ->
                    Column {
                        Text(entry.song.title, style = MaterialTheme.typography.titleSmall)
                        Text(entry.outcome.label, color = MaterialTheme.colorScheme.primary)
                        if (entry.detail.isNotBlank()) Text(entry.detail)
                        if (entry.outcome == BatchOutcome.REVIEW) TextButton(enabled = !state.running, onClick = { onReview(entry) }) { Text("Review candidates") }
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = onDismiss) { Text(if (state.running) "Keep browsing" else "Done") } },
        dismissButton = { if (state.running) TextButton(onClick = onCancel) { Text("Cancel search") } else TextButton(onClick = onRestart) { Text("Search again") } })
}
