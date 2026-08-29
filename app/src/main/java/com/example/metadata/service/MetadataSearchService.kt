package com.example.metadata.service

import com.example.metadata.model.MatchConfidence
import com.example.metadata.model.OnlineSongMetadata
import com.example.metadata.provider.ItunesMetadataProvider
import com.example.metadata.provider.LinkMetadataProvider
import com.example.metadata.provider.LrclibLyricsProvider
import com.example.metadata.provider.MetadataProvider
import com.example.metadata.provider.MusicBrainzProvider
import com.example.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class MetadataSearchService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build(),
    private val itunesProvider: ItunesMetadataProvider = ItunesMetadataProvider(client),
    private val musicBrainzProvider: MusicBrainzProvider = MusicBrainzProvider(client),
    private val lyricsProvider: LrclibLyricsProvider = LrclibLyricsProvider(client),
    private val linkProvider: LinkMetadataProvider = LinkMetadataProvider(client, itunesProvider)
) {
    private val cache = ConcurrentHashMap<String, OnlineSongMetadata>()

    suspend fun searchOnlineForSong(song: Song): OnlineSongMetadata? = withContext(Dispatchers.IO) {
        val cacheKey = "${song.title}|${song.artist}|${song.album}|${song.durationMs}"
        cache[cacheKey]?.let { return@withContext it }

        val candidates = mutableListOf<OnlineSongMetadata>()

        // Search primary provider (iTunes) and MusicBrainz concurrently
        coroutineScope {
            val itunesDeferred = async {
                try {
                    itunesProvider.searchMetadata(song.title, song.artist, song.album, song.durationMs)
                } catch (e: Exception) {
                    emptyList()
                }
            }

            val musicBrainzDeferred = async {
                try {
                    musicBrainzProvider.searchMetadata(song.title, song.artist, song.album, song.durationMs)
                } catch (e: Exception) {
                    emptyList()
                }
            }

            val itunesResults = itunesDeferred.await()
            val musicBrainzResults = musicBrainzDeferred.await()

            candidates.addAll(itunesResults)
            candidates.addAll(musicBrainzResults)
        }

        if (candidates.isEmpty()) {
            return@withContext null
        }

        // Rank candidates: High confidence first, then lowest duration difference
        val sortedCandidates = candidates.sortedWith(
            compareBy<OnlineSongMetadata> {
                when (it.confidence) {
                    MatchConfidence.HIGH -> 0
                    MatchConfidence.MEDIUM -> 1
                    MatchConfidence.LOW -> 2
                }
            }.thenBy {
                if (song.durationMs > 0 && it.durationMs > 0) {
                    kotlin.math.abs(song.durationMs - it.durationMs)
                } else {
                    0L
                }
            }
        )

        var bestMatch = sortedCandidates.first()

        // Fetch lyrics for the top candidate asynchronously
        try {
            val (plainLyrics, syncedLyrics) = lyricsProvider.fetchLyrics(
                title = bestMatch.title,
                artist = bestMatch.artist,
                album = bestMatch.album,
                durationMs = if (bestMatch.durationMs > 0) bestMatch.durationMs else song.durationMs
            )
            bestMatch = bestMatch.copy(
                lyrics = plainLyrics,
                syncedLyrics = syncedLyrics
            )
        } catch (e: Exception) {
            // Lyrics failure should never block metadata
        }

        cache[cacheKey] = bestMatch
        bestMatch
    }

    suspend fun identifyUsingLink(url: String, song: Song? = null): OnlineSongMetadata? = withContext(Dispatchers.IO) {
        if (!linkProvider.canHandle(url)) return@withContext null

        val resolved = linkProvider.resolveLink(url) ?: return@withContext null

        // Fetch lyrics if available
        var finalResult = resolved
        try {
            val (plainLyrics, syncedLyrics) = lyricsProvider.fetchLyrics(
                title = resolved.title,
                artist = resolved.artist,
                album = resolved.album,
                durationMs = if (resolved.durationMs > 0) resolved.durationMs else (song?.durationMs ?: 0L)
            )
            finalResult = resolved.copy(
                lyrics = plainLyrics,
                syncedLyrics = syncedLyrics
            )
        } catch (e: Exception) {
            // Ignore
        }

        finalResult
    }

    /**
     * Foundation for future Batch Library Search (processes one at a time with rate-limiting).
     */
    suspend fun searchBatch(
        songs: List<Song>,
        onProgress: (current: Int, total: Int, currentSong: Song, result: OnlineSongMetadata?) -> Boolean
    ) = withContext(Dispatchers.IO) {
        val total = songs.size
        for (i in songs.indices) {
            val song = songs[i]
            val result = searchOnlineForSong(song)
            val shouldContinue = onProgress(i + 1, total, song, result)
            if (!shouldContinue) break
            // Respect API rate limits between requests
            delay(500)
        }
    }
}
