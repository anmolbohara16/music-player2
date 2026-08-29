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

class MusicBrainzProvider(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()
) : MetadataProvider {

    override val providerName: String = "MusicBrainz"

    override suspend fun searchMetadata(
        title: String,
        artist: String,
        album: String,
        durationMs: Long
    ): List<OnlineSongMetadata> = withContext(Dispatchers.IO) {
        val cleanTitle = SongQueryCleaner.cleanSongTitle(title)
        val cleanArtist = if (artist != "Unknown" && artist != "<unknown>") artist else ""

        val queryBuilder = StringBuilder()
        queryBuilder.append("recording:\"").append(cleanTitle.replace("\"", "\\\"")).append("\"")
        if (cleanArtist.isNotBlank()) {
            queryBuilder.append(" AND artist:\"").append(cleanArtist.replace("\"", "\\\"")).append("\"")
        }

        val encodedQuery = URLEncoder.encode(queryBuilder.toString(), "UTF-8")
        val url = "https://musicbrainz.org/ws/2/recording/?query=$encodedQuery&fmt=json&limit=5"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "MusicPlayerAndroid/1.0 (https://github.com/aistudio/musicplayer)")
            .header("Accept", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val root = JSONObject(body)
                val recordings = root.optJSONArray("recordings") ?: return@withContext emptyList()

                val results = mutableListOf<OnlineSongMetadata>()
                for (i in 0 until recordings.length()) {
                    val rec = recordings.getJSONObject(i)
                    val recTitle = rec.optString("title", "")
                    val recId = rec.optString("id", "")
                    val recLength = rec.optLong("length", 0L)

                    var recArtist = ""
                    val artistCredit = rec.optJSONArray("artist-credit")
                    if (artistCredit != null && artistCredit.length() > 0) {
                        val artistObj = artistCredit.getJSONObject(0)
                        recArtist = artistObj.optString("name", "")
                    }

                    var recAlbum = ""
                    var releaseYear = ""
                    var releaseId = ""
                    var trackNumber = 0

                    val releases = rec.optJSONArray("releases")
                    if (releases != null && releases.length() > 0) {
                        val firstRelease = releases.getJSONObject(0)
                        recAlbum = firstRelease.optString("title", "")
                        releaseId = firstRelease.optString("id", "")
                        val date = firstRelease.optString("date", "")
                        if (date.length >= 4) {
                            releaseYear = date.substring(0, 4)
                        }

                        val media = firstRelease.optJSONArray("media")
                        if (media != null && media.length() > 0) {
                            val firstMedia = media.getJSONObject(0)
                            val track = firstMedia.optJSONArray("track")
                            if (track != null && track.length() > 0) {
                                trackNumber = track.getJSONObject(0).optInt("number", 0)
                            }
                        }
                    }

                    var isrc: String? = null
                    val isrcs = rec.optJSONArray("isrcs")
                    if (isrcs != null && isrcs.length() > 0) {
                        isrc = isrcs.optString(0)
                    }

                    val artworkUrl = if (releaseId.isNotBlank()) {
                        "https://coverartarchive.org/release/$releaseId/front-500"
                    } else null

                    val (confidence, reason) = SongQueryCleaner.evaluateMatch(
                        targetTitle = title,
                        targetArtist = artist,
                        targetDurationMs = durationMs,
                        candidateTitle = recTitle,
                        candidateArtist = recArtist,
                        candidateDurationMs = recLength
                    )

                    results.add(
                        OnlineSongMetadata(
                            title = SongQueryCleaner.cleanSongTitle(recTitle),
                            artist = recArtist,
                            album = recAlbum,
                            albumArtist = recArtist,
                            releaseYear = releaseYear,
                            trackNumber = trackNumber,
                            durationMs = recLength,
                            artworkUrl = artworkUrl,
                            isrc = isrc,
                            musicBrainzId = recId,
                            sourceName = providerName,
                            confidence = confidence,
                            confidenceReason = reason
                        )
                    )
                }
                results
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
