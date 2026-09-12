package com.example.metadata.service

import com.example.data.SongMetadataEntity
import com.example.metadata.model.*
import com.example.metadata.util.SongQueryCleaner as Cleaner
import com.example.model.Song

data class MetadataChange(val key: String, val label: String, val current: String?, val found: String)

object MetadataUpdatePolicy {
    fun changes(song: Song, online: OnlineSongMetadata): List<MetadataChange> = listOf(
        MetadataChange("title", "Title", song.title, online.title),
        MetadataChange("artist", "Artist", song.artist, online.artist),
        MetadataChange("album", "Album", song.album, online.album),
        MetadataChange("albumArtist", "Album artist", song.albumArtist, online.albumArtist),
        MetadataChange("genre", "Genre", song.genre, online.genre),
        MetadataChange("year", "Release year", song.releaseYear, online.releaseYear),
        MetadataChange("trackNumber", "Track", song.trackNumber.takeIf { it > 0 }?.toString(), online.trackNumber.takeIf { it > 0 }?.toString().orEmpty()),
        MetadataChange("discNumber", "Disc", song.discNumber.takeIf { it > 0 }?.toString(), online.discNumber.takeIf { it > 0 }?.toString().orEmpty()),
        MetadataChange("artwork", "Artwork", song.albumArtUri, online.artworkUrl.orEmpty()),
        MetadataChange("lyrics", "Lyrics", song.lyrics, online.lyrics.orEmpty()),
        MetadataChange("syncedLyrics", "Synced lyrics", song.syncedLyrics, online.syncedLyrics.orEmpty()),
        MetadataChange("isrc", "ISRC", song.isrc, online.isrc.orEmpty()),
        MetadataChange("musicBrainzId", "MusicBrainz ID", song.musicBrainzId, online.musicBrainzId.orEmpty()),
        MetadataChange("spotifyId", "Spotify ID", song.spotifyId, online.spotifyId.orEmpty()),
        MetadataChange("youtubeUrl", "YouTube link", song.youtubeUrl, online.youtubeUrl.orEmpty())
    ).filter { Cleaner.useful(it.found) && it.current != it.found }

    fun canAutoApply(song: Song, online: OnlineSongMetadata): Boolean =
        online.confidence == MatchConfidence.HIGH && !song.isManuallyEdited && changes(song, online).all {
            !Cleaner.useful(it.current) || (it.key in setOf("title", "artist") && Cleaner.normalized(it.current!!) == Cleaner.normalized(it.found))
        }

    /** Merge only selected nonempty fields into the latest override record, never a stale UI snapshot. */
    fun merge(songId: Long, existing: SongMetadataEntity?, online: OnlineSongMetadata, selected: Set<String>): SongMetadataEntity {
        val old = existing ?: SongMetadataEntity(songId)
        fun value(key: String, found: String?, current: String?): String? = if (key in selected && Cleaner.useful(found)) found else current
        fun number(key: String, found: Int, current: Int?): Int? = if (key in selected && found > 0) found else current
        return old.copy(
            title = value("title", online.title, old.title), artist = value("artist", online.artist, old.artist),
            album = value("album", online.album, old.album), albumArtist = value("albumArtist", online.albumArtist, old.albumArtist),
            genre = value("genre", online.genre, old.genre), releaseYear = value("year", online.releaseYear, old.releaseYear),
            trackNumber = number("trackNumber", online.trackNumber, old.trackNumber), discNumber = number("discNumber", online.discNumber, old.discNumber),
            artworkUri = value("artwork", online.artworkUrl, old.artworkUri), lyrics = value("lyrics", online.lyrics, old.lyrics),
            syncedLyrics = value("syncedLyrics", online.syncedLyrics, old.syncedLyrics), isrc = value("isrc", online.isrc, old.isrc),
            musicBrainzId = value("musicBrainzId", online.musicBrainzId, old.musicBrainzId), spotifyId = value("spotifyId", online.spotifyId, old.spotifyId),
            youtubeUrl = value("youtubeUrl", online.youtubeUrl, old.youtubeUrl), isIdentified = true,
            updatedAt = System.currentTimeMillis()
        )
    }
}
