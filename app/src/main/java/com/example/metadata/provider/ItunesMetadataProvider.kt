package com.example.metadata.provider

import com.example.metadata.model.OnlineSongMetadata
import com.example.metadata.util.SongQueryCleaner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class ItunesMetadataProvider(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()
) : MetadataProvider {

    override val providerName: String = "iTunes"

    override suspend fun searchMetadata(
        title: String,
        artist: String,
        album: String,
        durationMs: Long
    ): List<OnlineSongMetadata> = withContext(Dispatchers.IO) {
        val query = SongQueryCleaner.buildSearchQuery(title, artist, album)
        if (query.isBlank()) return@withContext emptyList()

        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = "https://itunes.apple.com/search?term=$encodedQuery&entity=song&limit=6"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "MusicPlayerAndroid/1.0")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val root = JSONObject(body)
                val resultsArray = root.optJSONArray("results") ?: return@withContext emptyList()

                val list = mutableListOf<OnlineSongMetadata>()
                for (i in 0 until resultsArray.length()) {
                    val item = resultsArray.getJSONObject(i)
                    val rawTrackName = item.optString("trackName", "")
                    val artistName = item.optString("artistName", "")
                    val collectionName = item.optString("collectionName", "")
                    val primaryGenre = item.optString("primaryGenreName", "")
                    val releaseDate = item.optString("releaseDate", "")
                    val trackNumber = item.optInt("trackNumber", 0)
                    val discNumber = item.optInt("discNumber", 1)
                    val trackTimeMillis = item.optLong("trackTimeMillis", 0L)
                    val rawArtwork = item.optString("artworkUrl100", "")

                    val highResArtwork = if (rawArtwork.isNotBlank()) {
                        rawArtwork.replace("100x100bb.jpg", "600x600bb.jpg")
                            .replace("100x100bb.png", "600x600bb.png")
                    } else null

                    val releaseYear = if (releaseDate.length >= 4) releaseDate.substring(0, 4) else ""
                    val cleanTrackTitle = SongQueryCleaner.cleanSongTitle(rawTrackName)

                    val (confidence, reason) = SongQueryCleaner.evaluateMatch(
                        targetTitle = title,
                        targetArtist = artist,
                        targetDurationMs = durationMs,
                        candidateTitle = cleanTrackTitle,
                        candidateArtist = artistName,
                        candidateDurationMs = trackTimeMillis
                    )

                    list.add(
                        OnlineSongMetadata(
                            title = cleanTrackTitle,
                            artist = artistName,
                            album = collectionName,
                            albumArtist = artistName,
                            genre = primaryGenre,
                            releaseYear = releaseYear,
                            trackNumber = trackNumber,
                            discNumber = discNumber,
                            durationMs = trackTimeMillis,
                            artworkUrl = highResArtwork,
                            sourceName = providerName,
                            confidence = confidence,
                            confidenceReason = reason
                        )
                    )
                }
                list
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
