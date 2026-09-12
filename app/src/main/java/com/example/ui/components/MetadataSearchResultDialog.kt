package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalConfiguration
import com.example.metadata.model.*
import com.example.metadata.service.MetadataUpdatePolicy
import com.example.model.Song

@Composable
fun MetadataSearchResultDialog(
    song: Song, isSearching: Boolean, searchResult: OnlineSongMetadata?, searchError: String?,
    onApplyMetadata: (OnlineSongMetadata, Set<String>) -> Unit, onRetrySearch: () -> Unit,
    onOpenIdentifyByLink: () -> Unit, onDismiss: () -> Unit,
    candidates: List<OnlineSongMetadata> = emptyList(), onSelectCandidate: (OnlineSongMetadata) -> Unit = {},
    isSaving: Boolean = false
) {
    val changes = remember(song, searchResult) { searchResult?.let { MetadataUpdatePolicy.changes(song, it) }.orEmpty() }
    var selected by remember(song.id, searchResult) { mutableStateOf(emptySet<String>()) }
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        modifier = Modifier.testTag("metadata_search_dialog"),
        title = { Text("Review online metadata") },
        text = {
            LazyColumn(
                Modifier.fillMaxWidth().heightIn(max = (LocalConfiguration.current.screenHeightDp * 0.68f).dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Text(song.title, style = MaterialTheme.typography.titleMedium) }
                if (isSearching) item { LinearProgressIndicator(Modifier.fillMaxWidth()); Text("Finding a recording that matches…") }
                if (searchError != null) item { Text(searchError, color = MaterialTheme.colorScheme.error) }
                if (searchResult != null) {
                    item {
                        Text(searchResult.sourceName, color = MaterialTheme.colorScheme.primary)
                        Text(searchResult.confidence.label, style = MaterialTheme.typography.titleSmall)
                        Text(searchResult.confidenceReason)
                        val durationDelta = if (song.durationMs > 0 && searchResult.durationMs > 0) {
                            kotlin.math.abs(song.durationMs - searchResult.durationMs) / 1000
                        } else null
                        Text("Local ${song.formattedDuration} · Online ${searchResult.formattedDuration}" +
                            (durationDelta?.let { " · Δ ${it}s" } ?: ""))
                        Text(
                            when {
                                !searchResult.syncedLyrics.isNullOrBlank() -> "Lyrics: synced lyrics available"
                                !searchResult.lyrics.isNullOrBlank() -> "Lyrics: plain lyrics available"
                                else -> "Lyrics: not found"
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (candidates.size > 1) item {
                        Text("Other candidates", style = MaterialTheme.typography.labelLarge)
                        candidates.take(10).forEach { candidate ->
                            TextButton(onClick = { onSelectCandidate(candidate) }, enabled = !isSaving) {
                                Text("${candidate.title} · ${candidate.artist}\n${candidate.album} · ${candidate.sourceName}")
                            }
                        }
                    }
                    item {
                        Text("Choose what changes. Your audio file stays untouched.")
                        TextButton(onClick = { onApplyMetadata(searchResult, changes.map { it.key }.toSet()) }, enabled = !isSaving && changes.isNotEmpty()) { Text("Apply all fields") }
                    }
                    if (changes.isEmpty()) item { Text("No changed fields were provided by this candidate.") }
                    items(changes, key = { it.key }) { change ->
                        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
                            Column(Modifier.fillMaxWidth().clickable(enabled = !isSaving) {
                                selected = if (change.key in selected) selected - change.key else selected + change.key
                            }.padding(12.dp)) {
                                Row {
                                    Checkbox(checked = change.key in selected, onCheckedChange = null)
                                    Text(change.label, modifier = Modifier.padding(top = 12.dp), style = MaterialTheme.typography.titleSmall)
                                }
                                Text("CURRENT", style = MaterialTheme.typography.labelSmall)
                                Text(change.current?.takeIf { it.isNotBlank() } ?: "Not set")
                                if (change.key == "artwork") AlbumArtImage(song, Modifier.size(96.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("FOUND ONLINE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                if (change.key == "artwork") AlbumArtFromData(null, change.found, Modifier.size(120.dp))
                                else Text(change.found)
                            }
                        }
                    }
                } else if (!isSearching) item {
                    TextButton(onClick = onRetrySearch) { Text("Try search again") }
                    TextButton(onClick = onOpenIdentifyByLink) { Text("Identify Using Link") }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = searchResult != null && selected.isNotEmpty() && !isSaving,
                onClick = { searchResult?.let { onApplyMetadata(it, selected) } }) { Text(if (isSaving) "Saving…" else "Apply selected") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancel") } }
    )
}
