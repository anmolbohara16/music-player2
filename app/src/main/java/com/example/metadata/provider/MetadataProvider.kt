package com.example.metadata.provider

import com.example.metadata.model.OnlineSongMetadata

interface MetadataProvider {
    val providerName: String
    suspend fun searchMetadata(
        title: String,
        artist: String,
        album: String = "",
        durationMs: Long = 0L
    ): List<OnlineSongMetadata>
}

interface LyricsProvider {
    val providerName: String
    suspend fun fetchLyrics(
        title: String,
        artist: String,
        album: String = "",
        durationMs: Long = 0L
    ): Pair<String?, String?> // Pair(plainLyrics, syncedLyrics)
}

interface LinkResolverProvider {
    fun canHandle(url: String): Boolean
    suspend fun resolveLink(url: String): OnlineSongMetadata?
}
