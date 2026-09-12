package com.example.metadata.service

import com.example.metadata.model.*
import com.example.metadata.provider.*
import com.example.model.Song
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class MetadataSearchService(
    client: OkHttpClient = OkHttpClient.Builder().connectTimeout(8, TimeUnit.SECONDS).readTimeout(8, TimeUnit.SECONDS).callTimeout(15, TimeUnit.SECONDS).build(),
    private val providers: List<MetadataProvider> = listOf(ItunesMetadataProvider(client), MusicBrainzProvider(client)),
    private val lyricsProvider: LyricsProvider = LrclibLyricsProvider(client),
    private val linkProvider: LinkResolverProvider = LinkMetadataProvider(client)
) {
    private val lyricsMutex = Mutex()
    private val lyricCache = linkedMapOf<OnlineSongMetadata, OnlineSongMetadata>()
    private val mutex = Mutex()
    // Bounded session cache, including no-match results. Failed/partial requests remain retryable.
    private val cache = linkedMapOf<List<String>, List<OnlineSongMetadata>>()

    suspend fun searchCandidates(song: Song): List<OnlineSongMetadata> = mutex.withLock {
        val key = listOf(song.title, song.artist, song.album, song.durationMs.toString(), song.isrc.orEmpty())
        cache[key]?.let { return@withLock it }
        val responses = coroutineScope {
            providers.map { provider -> async {
                try { Result.success(provider.searchMetadata(song.title, song.artist, song.album, song.durationMs)) }
                catch (e: CancellationException) { throw e }
                catch (e: Exception) { Result.failure<List<OnlineSongMetadata>>(e) }
            } }.awaitAll()
        }
        val values = responses.flatMap { it.getOrNull().orEmpty() }
        val failure = responses.firstOrNull { it.isFailure }?.exceptionOrNull()
        if (values.isEmpty() && failure != null) throw failure
        var ranked = MetadataMatcher.rank(song, values)
        if (failure != null) ranked = ranked.map { it.copy(
            confidence = if (it.confidence == MatchConfidence.HIGH) MatchConfidence.MEDIUM else it.confidence,
            confidenceReason = it.confidenceReason + " A provider was unavailable; review required.") }
        if (failure == null) {
            if (cache.size >= 250) cache.remove(cache.keys.first())
            cache[key] = ranked
        }
        ranked
    }

    suspend fun enrichLyrics(result: OnlineSongMetadata): OnlineSongMetadata = lyricsMutex.withLock {
        if (result.confidence != MatchConfidence.HIGH) return@withLock result
        lyricCache[result]?.let { return@withLock it }
        try {
            val (plain, synced) = lyricsProvider.fetchLyrics(result.title, result.artist, result.album, result.durationMs)
            result.copy(lyrics = plain, syncedLyrics = synced).also {
                if (lyricCache.size >= 250) lyricCache.remove(lyricCache.keys.first())
                lyricCache[result] = it
            }
        } catch (e: CancellationException) { throw e } catch (_: Exception) { result }
    }

    suspend fun searchOnlineForSong(song: Song): OnlineSongMetadata? = searchCandidates(song).firstOrNull()?.let { enrichLyrics(it) }

    suspend fun identifyUsingLink(url: String, song: Song? = null): OnlineSongMetadata? {
        require(linkProvider.canHandle(url)) { "Use a YouTube video or Spotify track HTTPS link." }
        val resolved = linkProvider.resolveLink(url) ?: return null
        return if (song == null) resolved else MetadataMatcher.rank(song, listOf(resolved)).firstOrNull()?.let {
            // oEmbed does not establish a recording's duration or artist identity.
            it.copy(confidence = MatchConfidence.MEDIUM, confidenceReason = "Provided link: verify the title, artist, version and artwork before applying.")
        }
    }
}
