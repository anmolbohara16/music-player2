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

        if (cleanTitle.isBlank()) return@withContext Pair(null, null)

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

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val json = JSONObject(body)
                        val plain = json.optString("plainLyrics", "").takeIf { it.isNotBlank() }
                        val synced = json.optString("syncedLyrics", "").takeIf { it.isNotBlank() }
                        if (plain != null || synced != null) {
                            return@withContext Pair(plain, synced)
                        }
                    }
                }
            }
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

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val jsonArray = JSONArray(body)
                        if (jsonArray.length() > 0) {
                            val first = jsonArray.getJSONObject(0)
                            val plain = first.optString("plainLyrics", "").takeIf { it.isNotBlank() }
                            val synced = first.optString("syncedLyrics", "").takeIf { it.isNotBlank() }
                            return@withContext Pair(plain, synced)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore
        }

        Pair(null, null)
    }
}
