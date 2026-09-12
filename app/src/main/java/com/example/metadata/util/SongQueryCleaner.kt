package com.example.metadata.util

import com.example.metadata.model.MatchConfidence
import kotlin.math.abs

object SongQueryCleaner {

    private val JUNK_PATTERNS = listOf(
        Regex("""(?i)\b(official\s+video|official\s+music\s+video|official\s+audio|music\s+video|official\s+mv|clip\s+officiel)\b"""),
        Regex("""(?i)\b(lyric\s+video|lyrics\s+video|with\s+lyrics|lyrics|lyric)\b"""),
        Regex("""(?i)\b(4k|hd|1080p|720p|uhd|hq|high\s+quality)\b"""),
        Regex("""(?i)\b(full\s+song|full\s+audio|audio\s+only|video)\b"""),
        Regex("""(?i)\b(free\s+download|visualizer|visualiser)\b""")
    )

    fun cleanSongTitle(rawTitle: String): String {
        var title = rawTitle.replace(Regex("""\.(mp3|m4a|flac|wav|ogg|aac|opus|wma)$""", RegexOption.IGNORE_CASE), "")
            .replace('_', ' ')
        // Remove promotional words only; unknown bracketed text may be part of the title.
        JUNK_PATTERNS.forEach { title = it.replace(title, " ") }
        return title.replace(Regex("""[\(\[\{]\s*[\)\]\}]"""), " ")
            .replace(Regex("""\s+"""), " ").trim(' ', '|', '-')
    }

    fun normalized(value: String): String = cleanSongTitle(value).lowercase(java.util.Locale.ROOT)
        .replace(Regex("""[^\p{L}\p{N}]+"""), " ").trim()

    fun useful(value: String?): Boolean = !value.isNullOrBlank() &&
        value.trim().lowercase(java.util.Locale.ROOT) !in setOf("unknown", "<unknown>", "unknown artist", "unknown album", "null")

    fun versions(value: String): Set<String> {
        val normalized = normalized(value)
        val patterns = mapOf(
            "live" to "live", "acoustic" to "acoustic", "unplugged" to "unplugged",
            "remaster" to "remaster(?:ed)?", "slowed" to "slow(?:ed)?", "lofi" to "lo ?fi",
            "speed" to "(?:sped|speed(?:ed)?) up", "remix" to "remix", "instrumental" to "instrumental",
            "cover" to "cover", "demo" to "demo", "radio" to "radio edit", "extended" to "extended",
            "reverb" to "reverb", "mono" to "mono", "stereo" to "stereo"
        )
        return patterns.filterValues { Regex("\\b(?:$it)\\b").containsMatchIn(normalized) }.keys
    }

    /**
     * Parses a string that might contain "Artist - Title" or "Title by Artist".
     */
    fun parseArtistAndTitle(rawString: String, fallbackArtist: String = ""): Pair<String, String> {
        val clean = cleanSongTitle(rawString)
        val hyphenSplit = clean.split(Regex("""\s+-\s+"""))
        if (hyphenSplit.size >= 2) {
            val artist = hyphenSplit[0].trim()
            val title = hyphenSplit.subList(1, hyphenSplit.size).joinToString(" - ").trim()
            if (artist.isNotBlank() && title.isNotBlank()) {
                return Pair(artist, title)
            }
        }

        val bySplit = clean.split(Regex("""(?i)\s+by\s+"""))
        if (bySplit.size == 2) {
            val title = bySplit[0].trim()
            val artist = bySplit[1].trim()
            if (artist.isNotBlank() && title.isNotBlank()) {
                return Pair(artist, title)
            }
        }

        val artist = if (useful(fallbackArtist)) {
            fallbackArtist
        } else {
            ""
        }

        return Pair(artist, clean)
    }

    /**
     * Builds an effective search query from the song information.
     */
    fun buildSearchQuery(title: String, artist: String, album: String = ""): String {
        val cleanTitle = cleanSongTitle(title)
        val validArtist = if (useful(artist)) artist else ""
        val validAlbum = if (useful(album) && album != cleanTitle) album else ""

        val parts = mutableListOf<String>()
        if (validArtist.isNotBlank()) parts.add(validArtist)
        if (cleanTitle.isNotBlank()) parts.add(cleanTitle)
        if (validAlbum.isNotBlank() && parts.size < 2) parts.add(validAlbum)

        return parts.joinToString(" ").ifBlank { cleanTitle }
    }

    /**
     * Calculates match confidence and human-readable explanation based on title, artist, and duration.
     */
    fun evaluateMatch(
        targetTitle: String,
        targetArtist: String,
        targetDurationMs: Long,
        candidateTitle: String,
        candidateArtist: String,
        candidateDurationMs: Long
    ): Pair<MatchConfidence, String> {
        val titleMatch = normalized(targetTitle) == normalized(candidateTitle) && useful(targetTitle)
        val artistMatch = useful(targetArtist) && useful(candidateArtist) && normalized(targetArtist) == normalized(candidateArtist)
        val versionMatch = versions(targetTitle) == versions(candidateTitle)
        val durationKnown = targetDurationMs > 0 && candidateDurationMs > 0
        val delta = abs(targetDurationMs - candidateDurationMs)
        val durationClose = durationKnown && delta <= 3000
        return when {
            !versionMatch -> MatchConfidence.LOW to "Different recording version; review required."
            durationKnown && delta > 10000 -> MatchConfidence.LOW to "Duration differs by ${delta / 1000}s."
            useful(targetArtist) && useful(candidateArtist) && !artistMatch -> MatchConfidence.LOW to "Artist differs; review required."
            titleMatch && artistMatch && durationClose -> MatchConfidence.HIGH to "Title, artist, version and duration agree (Δ ${delta / 1000}s)."
            titleMatch -> MatchConfidence.MEDIUM to "Title agrees; artist or duration needs verification."
            else -> MatchConfidence.LOW to "Search suggestion only; verify every field."
        }
    }
}
