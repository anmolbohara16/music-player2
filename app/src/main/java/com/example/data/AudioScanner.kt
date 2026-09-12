package com.example.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.model.Song
import java.io.File

object AudioScanner {

    // Directories and paths that must never be scanned as music
    private val EXCLUDED_PATH_KEYWORDS = listOf(
        "whatsapp",
        "telegram",
        "viber",
        "signal",
        "snapchat",
        "tiktok",
        "instagram",
        "messenger",
        "discord",
        "facebook",
        "notification",
        "notifications",
        "ringtone",
        "ringtones",
        "alarm",
        "alarms",
        "call_rec",
        "callrec",
        "call_record",
        "call record",
        "recording",
        "recordings",
        "voicerecorder",
        "voice recorder",
        "sound recorder",
        "voicenote",
        "voice note",
        "voice-note",
        "audiomemo",
        "voice_memo",
        "android/data",
        "android/media",
        "system/media",
        "sound_effect",
        "sound effect",
        "/sfx/",
        "cache",
        "temp"
    )

    // Prefix patterns typical for voice notes, system sounds, or non-music recordings
    private val EXCLUDED_TITLE_PREFIXES = listOf(
        "AUD-",
        "PTT-",
        "VOICE-",
        "VOICE_",
        "REC_",
        "Recording_",
        "Call_",
        "Call @",
        "CallWith",
        "SND_",
        "tone_",
        "noti_",
        "alarm_"
    )

    fun scanDeviceAudio(context: Context): List<Song> {
        val songList = mutableListOf<Song>()
        val contentResolver = context.contentResolver

        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA
        )

        // Strict media query:
        // 1. Must be flagged as music
        // 2. Must be at least 30 seconds (excludes ringtones, notification chirps, UI sound effects)
        // 3. Must be at least 400KB in file size (excludes tiny clips)
        // 4. Must not be ringtone, notification, alarm, or podcast
        val selection = buildString {
            append("${MediaStore.Audio.Media.IS_MUSIC} != 0")
            append(" AND ${MediaStore.Audio.Media.DURATION} >= 30000")
            append(" AND ${MediaStore.Audio.Media.SIZE} >= 400000")
            append(" AND (${MediaStore.Audio.Media.IS_RINGTONE} = 0 OR ${MediaStore.Audio.Media.IS_RINGTONE} IS NULL)")
            append(" AND (${MediaStore.Audio.Media.IS_NOTIFICATION} = 0 OR ${MediaStore.Audio.Media.IS_NOTIFICATION} IS NULL)")
            append(" AND (${MediaStore.Audio.Media.IS_ALARM} = 0 OR ${MediaStore.Audio.Media.IS_ALARM} IS NULL)")
            append(" AND (${MediaStore.Audio.Media.IS_PODCAST} = 0 OR ${MediaStore.Audio.Media.IS_PODCAST} IS NULL)")
        }
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                val artworkBaseUri = Uri.parse("content://media/external/audio/albumart")

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val rawTitle = cursor.getString(titleColumn)?.trim() ?: ""
                    val rawArtist = cursor.getString(artistColumn)?.trim() ?: ""
                    val rawAlbum = cursor.getString(albumColumn)?.trim() ?: ""
                    val duration = cursor.getLong(durationColumn)
                    val albumId = cursor.getLong(albumIdColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn) * 1000L
                    val track = cursor.getInt(trackColumn)
                    val mimeType = cursor.getString(mimeTypeColumn) ?: "audio/mp3"
                    val fileSize = cursor.getLong(sizeColumn)
                    val filePath = cursor.getString(dataColumn)?.lowercase() ?: ""

                    // Perform post-query path & filename validation to exclude WhatsApp, voice notes, etc.
                    if (isExcludedAudioFile(filePath, rawTitle, duration, fileSize)) {
                        continue
                    }

                    // Clean and refine song metadata
                    val (cleanTitle, cleanArtist, cleanAlbum) = resolveMusicMetadata(rawTitle, rawArtist, rawAlbum, filePath)

                    val contentUri = ContentUris.withAppendedId(collection, id)
                    val albumArtUri = if (albumId > 0) ContentUris.withAppendedId(artworkBaseUri, albumId).toString() else null

                    val format = when {
                        mimeType.contains("flac", ignoreCase = true) || filePath.endsWith(".flac") -> "FLAC"
                        mimeType.contains("wav", ignoreCase = true) || filePath.endsWith(".wav") -> "WAV"
                        mimeType.contains("m4a", ignoreCase = true) || filePath.endsWith(".m4a") -> "M4A"
                        mimeType.contains("aac", ignoreCase = true) || filePath.endsWith(".aac") -> "AAC"
                        mimeType.contains("ogg", ignoreCase = true) || filePath.endsWith(".ogg") -> "OGG"
                        mimeType.contains("opus", ignoreCase = true) || filePath.endsWith(".opus") -> "OPUS"
                        else -> "MP3"
                    }

                    songList.add(
                        Song(
                            id = id,
                            title = cleanTitle,
                            artist = cleanArtist,
                            album = cleanAlbum,
                            durationMs = duration,
                            uri = contentUri.toString(),
                            albumArtUri = albumArtUri,
                            dateAdded = dateAdded,
                            trackNumber = track % 1000,
                            discNumber = (track / 1000).coerceAtLeast(1),
                            format = format,
                            bitRate = "Unknown",
                            sampleRate = "Unknown"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return songList
    }

    /**
     * Checks if the scanned file is a voice message, ringtone, sound effect, or non-music file.
     */
    private fun isExcludedAudioFile(filePath: String, title: String, durationMs: Long, fileSize: Long): Boolean {
        // Exclude short clips < 30 seconds
        if (durationMs < 30000L) return true

        // Exclude very small files < 400 KB
        if (fileSize < 400000L) return true

        // Check path keywords
        for (excludedKeyword in EXCLUDED_PATH_KEYWORDS) {
            if (filePath.contains(excludedKeyword)) {
                return true
            }
        }

        // Check excluded title prefixes (e.g. WhatsApp audio AUD-2023..., voice notes PTT-..., recordings REC_...)
        for (prefix in EXCLUDED_TITLE_PREFIXES) {
            if (title.startsWith(prefix, ignoreCase = true)) {
                return true
            }
        }

        // Exclude filenames with obvious WhatsApp or voice note pattern
        if (title.matches(Regex("^(AUD|PTT)-\\d{8}-WA\\d+.*", RegexOption.IGNORE_CASE))) {
            return true
        }

        // Exclude pure numerical audio filenames like "1694829104820"
        if (title.matches(Regex("^\\d{10,18}$"))) {
            return true
        }

        return false
    }

    /**
     * Extracts and cleans up title, artist, and album from raw metadata or file naming.
     */
    private fun resolveMusicMetadata(
        rawTitle: String,
        rawArtist: String,
        rawAlbum: String,
        filePath: String
    ): Triple<String, String, String> {
        var title = rawTitle.ifEmpty {
            if (filePath.isNotEmpty()) {
                File(filePath).nameWithoutExtension
            } else "Unknown Title"
        }

        var artist = if (rawArtist.isBlank() || rawArtist == "<unknown>" || rawArtist.equals("Unknown Artist", ignoreCase = true)) {
            ""
        } else rawArtist

        val album = if (rawAlbum.isBlank() || rawAlbum == "<unknown>" || rawAlbum.equals("Unknown Album", ignoreCase = true)) {
            "Unknown Album"
        } else rawAlbum

        // Check if title has "Artist - Title" format (common when tags are stored in filename)
        if (artist.isEmpty() && title.contains(" - ")) {
            val parts = title.split(" - ", limit = 2)
            if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
                artist = parts[0].trim()
                title = parts[1].trim()
            }
        }

        if (artist.isEmpty()) {
            artist = "Unknown Artist"
        }

        return Triple(title, artist, album)
    }
}
