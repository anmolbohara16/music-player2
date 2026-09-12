package com.example

import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import com.example.metadata.model.OnlineSongMetadata
import com.example.metadata.service.MetadataUpdatePolicy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MetadataDatabaseTest {
    @Test fun playbackEventEntityCreatesMigrationCompatibleIndexes() {
        val db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), MusicDatabase::class.java
        ).build()
        try {
            val indexes = buildSet {
                db.openHelper.writableDatabase.query("PRAGMA index_list(`playback_events`)").use { cursor ->
                    val nameColumn = cursor.getColumnIndexOrThrow("name")
                    while (cursor.moveToNext()) add(cursor.getString(nameColumn))
                }
            }
            assertTrue(indexes.contains("index_playback_events_songId"))
            assertTrue(indexes.contains("index_playback_events_playedAt"))
        } finally {
            db.close()
        }
    }

    @Test fun playbackEventsPersistAlongsideLegacyHistory() = runTest {
        val db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), MusicDatabase::class.java).build()
        try {
            val dao = db.musicDao()
            dao.insertPlaybackEvent(PlaybackEventEntity(songId = 11, positionMs = 42000))
            dao.recordPlayback(PlaybackHistoryEntity(songId = 11, lastPositionMs = 42000))

            assertEquals(11L, dao.getRecentPlaybackEvents(1).first().single().songId)
            assertEquals(42000L, dao.getHistoryForSong(11)?.lastPositionMs)
        } finally { db.close() }
    }

    @Test fun atomicSelectiveUpdateAndRollback() = runTest {
        val db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), MusicDatabase::class.java).build()
        try {
            val dao = db.musicDao()
            val original = SongMetadataEntity(7, title = "Manual", lyrics = "Saved lyrics", discNumber = 2, isManuallyEdited = true)
            dao.insertSongMetadata(original)
            db.withTransaction {
                dao.insertSongMetadata(MetadataUpdatePolicy.merge(7, dao.getSongMetadata(7),
                    OnlineSongMetadata("Online title", "Artist", genre = "Jazz"), setOf("genre")))
            }
            val saved = dao.getSongMetadata(7)!!
            assertEquals("Manual", saved.title); assertEquals("Jazz", saved.genre)
            assertEquals("Saved lyrics", saved.lyrics); assertEquals(2, saved.discNumber); assertTrue(saved.isManuallyEdited)
            try {
                db.withTransaction { dao.insertSongMetadata(saved.copy(title = "Partial")); throw IllegalStateException("Failure") }
            } catch (_: IllegalStateException) { }
            assertEquals(saved, dao.getSongMetadata(7))
        } finally { db.close() }
    }
}
