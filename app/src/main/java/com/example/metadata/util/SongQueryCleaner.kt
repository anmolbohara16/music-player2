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

    private val LEGITIMATE_VERSIONS = listOf(
        "live", "acoustic", "unplugged", "remix", "instrumental", "slowed", "lofi",
        "speed up", "sped up", "extended", "radio edit", "cover", "reverb", "demo",
        "orchestral", "piano version", "club mix", "dub mix", "vip mix"
    )

    /**
     * Cleans promotional noise while keeping valid audio version identifiers.
     */
    fun cleanSongTitle(rawTitle: String): String {
        var title = rawTitle
            .replace(Regex("""\.(mp3|m4a|flac|wav|ogg|aac|opus|wma)$""", RegexOption.IGNORE_CASE), "")
            .replace('_', ' ')

        // Check if there are bracketed/parenthesized version notes that should be preserved
        val versionNotes = mutableListOf<String>()
        val bracketPattern = Regex("""[\(\[\{](.*?)[\)\]\}]""")
        val matches = bracketPattern.findAll(title)

        for (match in matches) {
            val content = match.groupValues[1].trim()
            val lower = content.lowercase()
            val hasLegitVersion = LEGITIMATE_VERSIONS.any { lower.contains(it) }
            val isJunk = JUNK_PATTERNS.any { it.containsMatchIn(content) }

            if (hasLegitVersion && !isJunk) {
                // Keep legitimate audio versions like "(Live)" or "(Acoustic)"
                versionNotes.add(content)
            }
        }

        // Strip bracketed text first if it is junk
        title = bracketPattern.replace(title) { matchResult ->
            val content = matchResult.groupValues[1].trim()
            val lower = content.lowercase()
            val hasLegitVersion = LEGITIMATE_VERSIONS.any { lower.contains(it) }
            val isJunk = JUNK_PATTERNS.any { it.containsMatchIn(content) }
            if (hasLegitVersion && !isJunk) {
                " (${content}) "
            } else {
                " "
            }
        }

        // Clean any standalone junk words
        for (pattern in JUNK_PATTERNS) {
            title = pattern.replace(title, " ")
        }

        // Clean extra separators like | or - at the end
        title = title.replace(Regex("""\s*[\|\-\~]\s*$"""), "")
            .replace(Regex("""^\s*[\|\-\~]\s*"""), "")
            .replace(Regex("""\s+"""), " ")
            .trim()

        return title.ifBlank { rawTitle.trim() }
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

        val artist = if (fallbackArtist.isNotBlank() && fallbackArtist != "Unknown" && fallbackArtist != "<unknown>") {
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
        val validArtist = if (artist.isNotBlank() && artist != "Unknown" && artist != "<unknown>") artist else ""
        val validAlbum = if (album.isNotBlank() && album != "Unknown" && album != "<unknown>" && album != cleanTitle) album else ""

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
        val cleanTargetTitle = cleanSongTitle(targetTitle).lowercase().trim()
        val cleanCandidateTitle = cleanSongTitle(candidateTitle).lowercase().trim()

        val cleanTargetArtist = targetArtist.lowercase().trim()
        val cleanCandidateArtist = candidateArtist.lowercase().trim()

        val titleMatch = isFuzzyMatch(cleanTargetTitle, cleanCandidateTitle)
        val artistKnown = cleanTargetArtist.isNotBlank() && cleanTargetArtist != "unknown" && cleanTargetArtist != "<unknown>"
        val artistMatch = if (artistKnown) isFuzzyMatch(cleanTargetArtist, cleanCandidateArtist) else false

        var durationClose = false
        var durationDeltaSeconds = 0L
        if (targetDurationMs > 0 && candidateDurationMs > 0) {
            durationDeltaSeconds = abs(targetDurationMs - candidateDurationMs) / 1000
            durationClose = durationDeltaSeconds <= 12
        }

        return when {
            titleMatch && (artistMatch || !artistKnown) && durationClose -> {
                val secStr = if (targetDurationMs > 0) " (duration delta: ${durationDeltaSeconds}s)" else ""
                Pair(MatchConfidence.HIGH, "Title, artist, and audio duration match closely$secStr.")
            }
            titleMatch && artistMatch -> {
                Pair(MatchConfidence.HIGH, "Title and artist match.")
            }
            titleMatch && durationClose -> {
                Pair(MatchConfidence.MEDIUM, "Title matches and duration is close (${durationDeltaSeconds}s diff).")
            }
            titleMatch -> {
                Pair(MatchConfidence.MEDIUM, "Song title matches online record.")
            }
            artistMatch -> {
                Pair(MatchConfidence.LOW, "Artist matches, but title differs.")
            }
            else -> {
                Pair(MatchConfidence.LOW, "Possible match based on keyword search.")
            }
        }
    }

    private fun isFuzzyMatch(s1: String, s2: String): Boolean {
        if (s1 == s2) return true
        if (s1.contains(s2) || s2.contains(s1)) return true

        val words1 = s1.split(Regex("""[\s\-_,.]+""")).filter { it.length > 1 }.toSet()
        val words2 = s2.split(Regex("""[\s\-_,.]+""")).filter { it.length > 1 }.toSet()

        if (words1.isEmpty() || words2.isEmpty()) return false
        val intersection = words1.intersect(words2)
        val ratio = intersection.size.toDouble() / minOf(words1.size, words2.size)
        return ratio >= 0.6
    }
}
