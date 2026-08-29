package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val songId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playback_history")
data class PlaybackHistoryEntity(
    @PrimaryKey
    val songId: Long,
    val playedAt: Long = System.currentTimeMillis(),
    val lastPositionMs: Long = 0L,
    val playCount: Int = 1
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["playlistId"]), Index(value = ["songId"])]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long,
    val orderIndex: Int = 0
)

@Entity(tableName = "player_settings")
data class PlayerSettingEntity(
    @PrimaryKey
    val key: String,
    val value: String
)

@Entity(tableName = "excluded_songs")
data class ExcludedSongEntity(
    @PrimaryKey
    val songId: Long,
    val songUri: String = "",
    val songTitle: String = "",
    val songArtist: String = "",
    val excludedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_play_counts")
data class UserPlayCountEntity(
    @PrimaryKey
    val songId: Long,
    val playCount: Int = 0,
    val lastPlayedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "song_metadata")
data class SongMetadataEntity(
    @PrimaryKey
    val songId: Long,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val albumArtist: String? = null,
    val genre: String? = null,
    val releaseYear: String? = null,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
    val artworkUri: String? = null,
    val lyrics: String? = null,
    val syncedLyrics: String? = null,
    val isrc: String? = null,
    val musicBrainzId: String? = null,
    val spotifyId: String? = null,
    val youtubeUrl: String? = null,
    val isIdentified: Boolean = false,
    val isManuallyEdited: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)


