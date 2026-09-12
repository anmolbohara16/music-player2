package com.example.metadata.provider

import com.example.metadata.util.SongQueryCleaner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class LrclibLyricsProvider(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(7, TimeUnit.SECONDS)
        .readTimeout(7, TimeUnit.SECONDS)
        .build()
) : LyricsProvider {

    override val providerName: String = "LRCLIB"

    override suspend fun fetchLyrics(
        title: String,
        artist: String,
        album: String,
        durationMs: Long
    ): Pair<String?, String?> = withContext(Dispatchers.IO) {
        val cleanTitle = SongQueryCleaner.cleanSongTitle(title)
        val cleanArtist = if (artist != "Unknown" && artist != "<unknown>") artist else ""

        if (cleanTitle.isBlank() || !SongQueryCleaner.useful(cleanArtist) || durationMs <= 0) return@withContext Pair(null, null)

        // Try direct exact get first
        try {
            val queryParams = StringBuilder("track_name=").append(URLEncoder.encode(cleanTitle, "UTF-8"))
            if (cleanArtist.isNotBlank()) {
                queryParams.append("&artist_name=").append(URLEncoder.encode(cleanArtist, "UTF-8"))
            }
            if (album.isNotBlank() && album != "Unknown" && album != "<unknown>") {
                queryParams.append("&album_name=").append(URLEncoder.encode(album, "UTF-8"))
            }
            if (durationMs > 0) {
                val durationSec = (durationMs / 1000).toInt()
                queryParams.append("&duration=").append(durationSec)
            }

            val url = "https://lrclib.net/api/get?$queryParams"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "MusicPlayerAndroid/1.0 (https://github.com/aistudio/musicplayer)")
                .build()

            client.awaitResponse(request).use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val json = JSONObject(body)
                        val plain = json.optString("plainLyrics", "").takeIf { SongQueryCleaner.useful(it) }
                        val synced = json.optString("syncedLyrics", "").takeIf { SongQueryCleaner.useful(it) }
                        if (matches(json, title, artist, durationMs) && (plain != null || synced != null)) {
                            return@withContext Pair(plain, synced)
                        }
                    }
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            // Ignore and fallback to search
        }

        // Fallback: search query
        try {
            val searchQuery = if (cleanArtist.isNotBlank()) "$cleanArtist $cleanTitle" else cleanTitle
            val searchUrl = "https://lrclib.net/api/search?q=" + URLEncoder.encode(searchQuery, "UTF-8")
            val request = Request.Builder()
                .url(searchUrl)
                .header("User-Agent", "MusicPlayerAndroid/1.0 (https://github.com/aistudio/musicplayer)")
                .build()

            client.awaitResponse(request).use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val jsonArray = JSONArray(body)
                        if (jsonArray.length() > 0) {
                            val first = (0 until jsonArray.length()).map { jsonArray.getJSONObject(it) }
                                .firstOrNull { matches(it, title, artist, durationMs) } ?: return@withContext Pair(null, null)
                            val plain = first.optString("plainLyrics", "").takeIf { SongQueryCleaner.useful(it) }
                            val synced = first.optString("syncedLyrics", "").takeIf { SongQueryCleaner.useful(it) }
                            return@withContext Pair(plain, synced)
                        }
                    }
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            // Ignore
        }

        Pair(null, null)
    }
    private fun matches(json: JSONObject, title: String, artist: String, durationMs: Long): Boolean =
        SongQueryCleaner.evaluateMatch(title, artist, durationMs, json.optString("trackName"),
            json.optString("artistName"), (json.optDouble("duration", 0.0) * 1000).toLong()).first == com.example.metadata.model.MatchConfidence.HIGH
}
