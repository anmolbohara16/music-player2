package com.example

import com.example.metadata.model.*
import com.example.metadata.provider.*
import com.example.metadata.service.*
import com.example.model.Song
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class LibraryMetadataSearchTest {
    private fun song(id: Long) = Song(id, "Track $id", "Artist", "", 180000, "local")
    private fun result(song: Song) = OnlineSongMetadata(song.title, song.artist, album = "Found album", durationMs = song.durationMs, confidence = MatchConfidence.HIGH)
    @Test fun batchReportsMultipleOutcomesAndDeduplicates() = runTest {
        var calls = 0
        var final = LibrarySearchState()
        val batch = LibraryMetadataSearch({ s -> calls++; when(s.id) {
            1L -> listOf(result(s)); 2L -> emptyList(); 3L -> listOf(result(s).copy(confidence = MatchConfidence.LOW))
            else -> throw IOException("Offline")
        } }, { it }, { _, _, _ -> true })
        batch.run(listOf(song(1), song(1), song(2), song(3), song(4))) { final = it }
        assertEquals(4, calls); assertFalse(final.running); assertEquals(4, final.completed)
        assertEquals(listOf(BatchOutcome.UPDATED, BatchOutcome.NO_MATCH, BatchOutcome.REVIEW, BatchOutcome.FAILED), final.entries.map { it.outcome })
    }
    @Test fun rateLimitIsFailureWithBoundedDelay() = runTest {
        var final = LibrarySearchState()
        LibraryMetadataSearch({ throw MetadataHttpException(429, 2) }, { it }, { _, _, _ -> true })
            .run(listOf(song(1))) { final = it }
        assertEquals(BatchOutcome.FAILED, final.entries.single().outcome)
        assertTrue(testScheduler.currentTime >= 2000)
    }
    @Test fun cancellationLeavesUnprocessedSongsUnchanged() = runTest {
        var final = LibrarySearchState()
        val job = launch { LibraryMetadataSearch({ delay(10000); listOf(result(it)) }, { it }, { _, _, _ -> error("must not apply") })
            .run(listOf(song(1), song(2))) { final = it } }
        testScheduler.runCurrent(); job.cancelAndJoin()
        assertTrue(final.cancelled); assertFalse(final.running); assertEquals(0, final.completed)
    }
    @Test fun failedAutomaticGuardRoutesToReview() = runTest {
        var final = LibrarySearchState()
        LibraryMetadataSearch({ listOf(result(it)) }, { it }, { _, _, _ -> false }).run(listOf(song(1))) { final = it }
        assertEquals(BatchOutcome.REVIEW, final.entries.single().outcome)
    }
    @Test fun providerFailureCannotPretendNoMatchOrHighConfidence() = runTest {
        val fail = object : MetadataProvider {
            override val providerName = "offline"
            override suspend fun searchMetadata(title: String, artist: String, album: String, durationMs: Long): List<OnlineSongMetadata> = throw IOException("Offline")
        }
        val good = object : MetadataProvider {
            override val providerName = "good"
            override suspend fun searchMetadata(title: String, artist: String, album: String, durationMs: Long) = listOf(OnlineSongMetadata(title, artist, durationMs = durationMs))
        }
        assertEquals(MatchConfidence.MEDIUM, MetadataSearchService(providers = listOf(fail, good)).searchCandidates(song(1)).first().confidence)
        try { MetadataSearchService(providers = listOf(fail)).searchCandidates(song(1)); fail("Expected IOException") } catch (_: IOException) { }
    }
    @Test fun successfulAndNoMatchSearchesAreCached() = runTest {
        var calls = 0
        val provider = object : MetadataProvider {
            override val providerName = "test"
            override suspend fun searchMetadata(title: String, artist: String, album: String, durationMs: Long): List<OnlineSongMetadata> { calls++; return emptyList() }
        }
        val service = MetadataSearchService(providers = listOf(provider))
        service.searchCandidates(song(1)); service.searchCandidates(song(1)); assertEquals(1, calls)
    }
}
