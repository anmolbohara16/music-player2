package com.example.metadata.service

import com.example.metadata.model.*
import com.example.metadata.util.SongQueryCleaner as Cleaner
import com.example.model.Song

object MetadataMatcher {
    fun rank(song: Song, candidates: List<OnlineSongMetadata>): List<OnlineSongMetadata> {
        val scored = candidates.filter { Cleaner.useful(it.title) }.map { candidate ->
            val (confidence, reason) = Cleaner.evaluateMatch(song.title, song.artist, song.durationMs,
                candidate.title, candidate.artist, candidate.durationMs)
            val albumConflict = Cleaner.useful(song.album) && Cleaner.useful(candidate.album) &&
                Cleaner.normalized(song.album) != Cleaner.normalized(candidate.album)
            val idConflict = Cleaner.useful(song.isrc) && Cleaner.useful(candidate.isrc) && song.isrc != candidate.isrc
            candidate.copy(confidence = if (idConflict) MatchConfidence.LOW else if (albumConflict && confidence == MatchConfidence.HIGH) MatchConfidence.MEDIUM else confidence,
                confidenceReason = reason + if (albumConflict || idConflict) " Album or recording identifier differs." else "")
        }.sortedWith(compareBy<OnlineSongMetadata> { it.confidence.ordinal }
            .thenBy { if (it.durationMs > 0) kotlin.math.abs(song.durationMs - it.durationMs) else Long.MAX_VALUE })
        val best = scored.firstOrNull() ?: return emptyList()
        val conflicts = scored.filter { it.sourceName != best.sourceName && it.confidence != MatchConfidence.LOW }.any {
            listOf(it.album to best.album, it.releaseYear to best.releaseYear, it.isrc to best.isrc).any { (a, b) ->
                Cleaner.useful(a) && Cleaner.useful(b) && Cleaner.normalized(a!!) != Cleaner.normalized(b!!)
            }
        }
        return if (conflicts) scored.map { it.copy(confidence = if (it.confidence == MatchConfidence.HIGH) MatchConfidence.MEDIUM else it.confidence,
            confidenceReason = it.confidenceReason + " Providers disagree; choose fields after review.") } else scored
    }
}
