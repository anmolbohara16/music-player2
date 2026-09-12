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
        val uri = runCatching { java.net.URI(url.trim()) }.getOrNull() ?: return false
        if (uri.scheme != "https" || uri.userInfo != null) return false
        return when (uri.host?.lowercase()) {
            "youtube.com", "www.youtube.com", "m.youtube.com", "music.youtube.com" ->
                (uri.path == "/watch" && uri.rawQuery.orEmpty().split("&").any { it.startsWith("v=") && it.length > 2 }) || uri.path.startsWith("/shorts/")
            "youtu.be" -> uri.path.length > 1
            "open.spotify.com" -> Regex("/track/[a-zA-Z0-9]+/?").matches(uri.path)
            else -> false
        }
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

            val body = client.awaitResponse(request).use { response ->
                if (!response.isSuccessful) throw MetadataHttpException(response.code)
                response.body?.string()
            } ?: return null
            val json = JSONObject(body)

            val rawTitle = json.optString("title", "")
            val authorName = json.optString("author_name", "")
            val thumbnailUrl = json.optString("thumbnail_url", "")

            // Parse artist and title cleanly from YouTube video title
            val (parsedArtist, parsedTitle) = SongQueryCleaner.parseArtistAndTitle(rawTitle, fallbackArtist = authorName)
            val cleanTitle = SongQueryCleaner.cleanSongTitle(parsedTitle)
            val cleanArtist = if (parsedArtist.isNotBlank()) parsedArtist else authorName

            return OnlineSongMetadata(
                title = cleanTitle, artist = cleanArtist,
                artworkUrl = thumbnailUrl.takeIf { it.isNotBlank() }, youtubeUrl = url,
                sourceName = "YouTube Link", confidence = MatchConfidence.MEDIUM,
                confidenceReason = "Video title and channel credit; verify artist and version."
            )
        } catch (e: Exception) {
            throw e
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

            val body = client.awaitResponse(request).use { response ->
                if (!response.isSuccessful) throw MetadataHttpException(response.code)
                response.body?.string()
            } ?: return null
            val json = JSONObject(body)

            val rawTitle = json.optString("title", "")
            val thumbnailUrl = json.optString("thumbnail_url", "")

            // Extract spotify ID
            val spotifyId = Regex("""track/([a-zA-Z0-9]+)""").find(url)?.groupValues?.getOrNull(1)

            val (parsedArtist, parsedTitle) = SongQueryCleaner.parseArtistAndTitle(rawTitle)
            val cleanTitle = SongQueryCleaner.cleanSongTitle(parsedTitle)

            return OnlineSongMetadata(
                title = cleanTitle, artist = parsedArtist,
                artworkUrl = thumbnailUrl.takeIf { it.isNotBlank() }, spotifyId = spotifyId,
                sourceName = "Spotify Link", confidence = MatchConfidence.MEDIUM,
                confidenceReason = "Spotify embed supplies limited metadata; missing fields are left unchanged."
            )
        } catch (e: Exception) {
            throw e
        }
    }
}
