package com.example.metadata.provider

import com.example.metadata.model.MatchConfidence
import com.example.metadata.model.OnlineSongMetadata
import com.example.metadata.util.SongQueryCleaner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class LinkMetadataProvider(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build(),
    private val itunesProvider: ItunesMetadataProvider = ItunesMetadataProvider(client)
) : LinkResolverProvider {

    override fun canHandle(url: String): Boolean {
        val lower = url.lowercase().trim()
        return lower.contains("youtube.com") ||
                lower.contains("youtu.be") ||
                lower.contains("spotify.com")
    }

    override suspend fun resolveLink(url: String): OnlineSongMetadata? = withContext(Dispatchers.IO) {
        val trimmed = url.trim()
        if (!canHandle(trimmed)) return@withContext null

        val isYouTube = trimmed.contains("youtube.com", ignoreCase = true) || trimmed.contains("youtu.be", ignoreCase = true)
        val isSpotify = trimmed.contains("spotify.com", ignoreCase = true)

        if (isYouTube) {
            resolveYouTube(trimmed)
        } else if (isSpotify) {
            resolveSpotify(trimmed)
        } else {
            null
        }
    }

    private suspend fun resolveYouTube(url: String): OnlineSongMetadata? {
        try {
            val encoded = URLEncoder.encode(url, "UTF-8")
            val oembedUrl = "https://www.youtube.com/oembed?url=$encoded&format=json"

            val request = Request.Builder()
                .url(oembedUrl)
                .header("User-Agent", "MusicPlayerAndroid/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            val json = JSONObject(body)

            val rawTitle = json.optString("title", "")
            val authorName = json.optString("author_name", "")
            val thumbnailUrl = json.optString("thumbnail_url", "")

            // Parse artist and title cleanly from YouTube video title
            val (parsedArtist, parsedTitle) = SongQueryCleaner.parseArtistAndTitle(rawTitle, fallbackArtist = authorName)
            val cleanTitle = SongQueryCleaner.cleanSongTitle(parsedTitle)
            val cleanArtist = if (parsedArtist.isNotBlank()) parsedArtist else authorName

            // Try enriching with iTunes data to get full album, genre, year, high-res artwork
            val enrichedList = itunesProvider.searchMetadata(cleanTitle, cleanArtist)
            val topEnriched = enrichedList.firstOrNull()

            return if (topEnriched != null) {
                topEnriched.copy(
                    youtubeUrl = url,
                    artworkUrl = topEnriched.artworkUrl ?: thumbnailUrl.takeIf { it.isNotBlank() },
                    confidence = MatchConfidence.HIGH,
                    confidenceReason = "Resolved from YouTube link and matched official track data."
                )
            } else {
                OnlineSongMetadata(
                    title = cleanTitle,
                    artist = cleanArtist,
                    album = "",
                    albumArtist = cleanArtist,
                    artworkUrl = thumbnailUrl.takeIf { it.isNotBlank() },
                    youtubeUrl = url,
                    sourceName = "YouTube Link",
                    confidence = MatchConfidence.MEDIUM,
                    confidenceReason = "Extracted from YouTube link."
                )
            }
        } catch (e: Exception) {
            return null
        }
    }

    private suspend fun resolveSpotify(url: String): OnlineSongMetadata? {
        try {
            val encoded = URLEncoder.encode(url, "UTF-8")
            val oembedUrl = "https://open.spotify.com/oembed?url=$encoded"

            val request = Request.Builder()
                .url(oembedUrl)
                .header("User-Agent", "MusicPlayerAndroid/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            val json = JSONObject(body)

            val rawTitle = json.optString("title", "")
            val thumbnailUrl = json.optString("thumbnail_url", "")

            // Extract spotify ID
            val spotifyId = Regex("""track/([a-zA-Z0-9]+)""").find(url)?.groupValues?.getOrNull(1)

            val (parsedArtist, parsedTitle) = SongQueryCleaner.parseArtistAndTitle(rawTitle)
            val cleanTitle = SongQueryCleaner.cleanSongTitle(parsedTitle)

            // Try enriching with iTunes
            val enrichedList = itunesProvider.searchMetadata(cleanTitle, parsedArtist)
            val topEnriched = enrichedList.firstOrNull()

            return if (topEnriched != null) {
                topEnriched.copy(
                    spotifyId = spotifyId,
                    artworkUrl = topEnriched.artworkUrl ?: thumbnailUrl.takeIf { it.isNotBlank() },
                    confidence = MatchConfidence.HIGH,
                    confidenceReason = "Resolved from Spotify link."
                )
            } else {
                OnlineSongMetadata(
                    title = cleanTitle,
                    artist = parsedArtist,
                    artworkUrl = thumbnailUrl.takeIf { it.isNotBlank() },
                    spotifyId = spotifyId,
                    sourceName = "Spotify Link",
                    confidence = MatchConfidence.MEDIUM,
                    confidenceReason = "Extracted from Spotify link."
                )
            }
        } catch (e: Exception) {
            return null
        }
    }
}
