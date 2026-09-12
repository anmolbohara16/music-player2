package com.example

import com.example.model.Song
import com.example.data.SongMetadataEntity
import com.example.metadata.model.*
import com.example.metadata.service.*
import com.example.metadata.util.SongQueryCleaner
import com.example.metadata.provider.LinkMetadataProvider
import org.junit.Assert.*
import org.junit.Test

class MetadataMatchingTest {
    private val song = Song(1, "Northern Lights", "Artist A", "Album", 180000, "content://audio/1")
    private val exact = OnlineSongMetadata("Northern Lights", "Artist A", "Album", durationMs = 180000, sourceName = "A")
    private fun match(candidate: OnlineSongMetadata, local: Song = song) = MetadataMatcher.rank(local, listOf(candidate)).first()

    @Test fun exactMatchRequiresAllSignals() { assertEquals(MatchConfidence.HIGH, match(exact).confidence) }
    @Test fun differentArtistIsLow() { assertEquals(MatchConfidence.LOW, match(exact.copy(artist = "Artist B")).confidence) }
    @Test fun versionsNeverCollapse() {
        listOf("Live", "Acoustic", "Unplugged", "Remastered", "Slowed", "Lo-Fi", "Speed Up", "Sped Up", "Remix").forEach {
            assertEquals(it, MatchConfidence.LOW, match(exact.copy(title = "Northern Lights ($it)")).confidence)
            assertTrue(SongQueryCleaner.cleanSongTitle("Northern Lights ($it) [Official Video]").contains(it))
        }
    }
    @Test fun originalAndRemasterDifferBothDirections() {
        assertEquals(MatchConfidence.LOW, match(exact, song.copy(title = "Northern Lights (Remastered)")).confidence)
    }
    @Test fun largeDurationDifferenceCannotBeHigh() { assertEquals(MatchConfidence.LOW, match(exact.copy(durationMs = 230000)).confidence) }
    @Test fun missingLocalArtistRequiresReview() { assertEquals(MatchConfidence.MEDIUM, match(exact, song.copy(artist = "Unknown")).confidence) }
    @Test fun missingOnlineDurationRequiresReview() { assertEquals(MatchConfidence.MEDIUM, match(exact.copy(durationMs = 0)).confidence) }
    @Test fun substringIsNotIdentity() { assertNotEquals(MatchConfidence.HIGH, match(exact.copy(title = "Northern Lights Forever")).confidence) }
    @Test fun providerDisagreementRequiresReview() {
        val ranked = MetadataMatcher.rank(song.copy(album = "Unknown"), listOf(exact, exact.copy(album = "Other release", sourceName = "B")))
        assertTrue(ranked.all { it.confidence != MatchConfidence.HIGH })
    }
    @Test fun missingArtworkAndLyricsNeverEraseValues() {
        val old = SongMetadataEntity(1, artworkUri = "file://cover", lyrics = "Correct lyrics", discNumber = 2, isManuallyEdited = true)
        val merged = MetadataUpdatePolicy.merge(1, old, exact, setOf("artwork", "lyrics", "discNumber"))
        assertEquals(old.artworkUri, merged.artworkUri); assertEquals(old.lyrics, merged.lyrics)
        assertEquals(2, merged.discNumber); assertTrue(merged.isManuallyEdited)
    }
    @Test fun selectedFieldsOnlyAndConcurrentEditsPreserved() {
        val old = SongMetadataEntity(1, title = "Manual title", discNumber = 3, spotifyId = "original", isManuallyEdited = true)
        val merged = MetadataUpdatePolicy.merge(1, old, exact.copy(genre = "Folk", discNumber = 1, spotifyId = "other"), setOf("genre"))
        assertEquals("Folk", merged.genre); assertEquals("Manual title", merged.title)
        assertEquals(3, merged.discNumber); assertEquals("original", merged.spotifyId); assertTrue(merged.isManuallyEdited)
    }
    @Test fun usefulExistingValuesNeedReview() {
        assertFalse(MetadataUpdatePolicy.canAutoApply(song.copy(isManuallyEdited = true), match(exact)))
        assertFalse(MetadataUpdatePolicy.canAutoApply(song, match(exact).copy(artworkUrl = "https://cover" ).let { it.copy(album = "Other") }))
    }
    @Test fun unknownCreditsAreNotSearchKeywords() {
        assertEquals("Northern Lights", SongQueryCleaner.buildSearchQuery("Northern Lights", "Unknown Artist", "Unknown Album"))
    }
    @Test fun emptyProviderValuesHaveNoChanges() { assertTrue(MetadataUpdatePolicy.changes(song, OnlineSongMetadata("", "")).isEmpty()) }
    @Test fun linkHostValidation() {
        val provider = LinkMetadataProvider()
        assertTrue(provider.canHandle("https://youtu.be/abc123"))
        assertTrue(provider.canHandle("https://open.spotify.com/track/abc123"))
        listOf("https://youtube.com.evil.test/watch?v=1", "https://evil.test/?youtube.com", "http://youtu.be/a", "https://open.spotify.com/album/abc").forEach { assertFalse(it, provider.canHandle(it)) }
    }
}
