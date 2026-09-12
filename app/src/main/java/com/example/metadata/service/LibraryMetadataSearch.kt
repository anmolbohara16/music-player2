package com.example.metadata.service

import com.example.metadata.model.OnlineSongMetadata
import com.example.metadata.provider.MetadataHttpException
import com.example.model.Song
import kotlinx.coroutines.*

enum class BatchOutcome(val label: String) { UPDATED("Updated"), CORRECT("Already correct"), REVIEW("Needs review"), NO_MATCH("No match"), FAILED("Failed"), SKIPPED("Skipped") }
data class BatchEntry(val song: Song, val outcome: BatchOutcome, val candidates: List<OnlineSongMetadata> = emptyList(), val detail: String = "")
data class LibrarySearchState(
    val running: Boolean = false, val total: Int = 0, val currentSong: String = "",
    val entries: List<BatchEntry> = emptyList(), val cancelled: Boolean = false
) { val completed: Int get() = entries.size }

class LibraryMetadataSearch(
    private val search: suspend (Song) -> List<OnlineSongMetadata>,
    private val enrich: suspend (OnlineSongMetadata) -> OnlineSongMetadata,
    private val apply: suspend (Song, OnlineSongMetadata, Set<String>) -> Boolean
) {
    suspend fun run(songs: List<Song>, onState: (LibrarySearchState) -> Unit) {
        var state = LibrarySearchState(running = true, total = songs.distinctBy { it.id }.size)
        onState(state)
        try {
            for (song in songs.distinctBy { it.id }) {
                currentCoroutineContext().ensureActive()
                state = state.copy(currentSong = song.title); onState(state)
                val entry = try {
                    val candidates = searchWithRetry(song)
                    val best = candidates.firstOrNull()?.let { if (song.lyrics.isNullOrBlank() && song.syncedLyrics.isNullOrBlank()) enrich(it) else it }
                    when {
                        best == null -> BatchEntry(song, BatchOutcome.NO_MATCH)
                        best.confidence == com.example.metadata.model.MatchConfidence.HIGH && MetadataUpdatePolicy.changes(song, best).isEmpty() -> BatchEntry(song, BatchOutcome.CORRECT)
                        MetadataUpdatePolicy.canAutoApply(song, best) -> {
                            val enriched = best
                            if (MetadataUpdatePolicy.canAutoApply(song, enriched) && apply(song, enriched, MetadataUpdatePolicy.changes(song, enriched).map { it.key }.toSet()))
                                BatchEntry(song, BatchOutcome.UPDATED)
                            else BatchEntry(song, BatchOutcome.REVIEW, listOf(enriched) + candidates.drop(1))
                        }
                        else -> BatchEntry(song, BatchOutcome.REVIEW, listOf(best) + candidates.drop(1))
                    }
                } catch (e: CancellationException) { throw e }
                catch (e: Exception) {
                    BatchEntry(song, BatchOutcome.FAILED, detail = e.message ?: "Search failed")
                }
                state = state.copy(entries = state.entries + entry); onState(state)
            }
            state = state.copy(running = false, currentSong = ""); onState(state)
        } catch (e: CancellationException) {
            onState(state.copy(running = false, cancelled = true, currentSong = ""))
            throw e
        }
    }

    private suspend fun searchWithRetry(song: Song): List<OnlineSongMetadata> {
        var attempt = 0
        while (true) {
            try {
                return search(song)
            } catch (e: CancellationException) {
                throw e
            } catch (e: MetadataHttpException) {
                if (e.status != 429 && e.status != 503 || attempt >= 2) throw e
                val retrySeconds = e.retryAfterSeconds?.coerceIn(1, 60) ?: (2L shl attempt)
                attempt++
                delay(retrySeconds * 1000L)
            }
        }
    }
}
