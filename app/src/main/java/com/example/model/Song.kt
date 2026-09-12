package com.example.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String = "",
    val durationMs: Long,
    val uri: String,
    val albumArtUri: String? = null,
    val albumArtRes: Int? = null,
    val fallbackArtworkUri: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val trackNumber: Int = 0,
    val discNumber: Int = 1,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val genre: String = "Unknown",
    val bitRate: String = "Unknown",
    val sampleRate: String = "Unknown",
    val format: String = "MP3",
    val albumArtist: String? = null,
    val releaseYear: String? = null,
    val isrc: String? = null,
    val musicBrainzId: String? = null,
    val spotifyId: String? = null,
    val youtubeUrl: String? = null,
    val lyrics: String? = null,
    val syncedLyrics: String? = null,
    val isIdentified: Boolean = false,
    val isManuallyEdited: Boolean = false
) {
    val formattedDuration: String
        get() {
            val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}

data class Album(
    val name: String,
    val artist: String,
    val songCount: Int,
    val albumArtUri: String? = null,
    val albumArtRes: Int? = null,
    val songs: List<Song> = emptyList()
)

data class Artist(
    val name: String,
    val songCount: Int,
    val albumCount: Int,
    val songs: List<Song> = emptyList()
)

data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int = 0,
    val coverArtRes: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isCustom: Boolean = true
)

enum class RepeatMode {
    OFF, ALL, ONE
}

enum class SortOption(val displayName: String) {
    TITLE_ASC("Title (A-Z)"),
    TITLE_DESC("Title (Z-A)"),
    ARTIST("Artist"),
    ALBUM("Album"),
    DURATION("Duration"),
    DATE_ADDED("Recently Added"),
    MOST_PLAYED("Most Played")
}
