package com.example.metadata.model

enum class MatchConfidence(val label: String) {
    HIGH("High confidence match"),
    MEDIUM("Possible match — please verify"),
    LOW("Low confidence match")
}

data class OnlineSongMetadata(
    val title: String,
    val artist: String,
    val album: String = "",
    val albumArtist: String = "",
    val genre: String = "",
    val releaseYear: String = "",
    val trackNumber: Int = 0,
    val discNumber: Int = 0,
    val durationMs: Long = 0L,
    val artworkUrl: String? = null,
    val lyrics: String? = null,
    val syncedLyrics: String? = null,
    val isrc: String? = null,
    val musicBrainzId: String? = null,
    val spotifyId: String? = null,
    val youtubeUrl: String? = null,
    val sourceName: String = "",
    val confidence: MatchConfidence = MatchConfidence.LOW,
    val confidenceReason: String = ""
) {
    val formattedDuration: String
        get() {
            if (durationMs <= 0) return "--:--"
            val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}
