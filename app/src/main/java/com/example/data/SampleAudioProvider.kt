package com.example.data

import android.content.Context
import com.example.R
import com.example.model.Song
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sin

object SampleAudioProvider {

    /**
     * Generates a pleasant synthesized audio melody wave file locally in app cache
     * so MediaPlayer can play real music offline even if no files exist on the emulator/device.
     */
    fun createSampleWavFile(context: Context, filename: String, baseFrequency: Double, durationSeconds: Int = 180): File {
        val file = File(context.cacheDir, filename)
        if (file.exists() && file.length() > 1000) {
            return file
        }

        try {
            val sampleRate = 44100
            val numSamples = sampleRate * durationSeconds
            val audioData = ShortArray(numSamples)

            // Musical chords progression (C major / A minor pentatonic / Chill vibes)
            val chordOffsets = doubleArrayOf(1.0, 1.25, 1.5, 1.875, 2.0, 1.5, 1.333, 1.125)
            val stepSamples = (sampleRate * 2.5).toInt() // 2.5 seconds per chord change

            for (i in 0 until numSamples) {
                val step = (i / stepSamples) % chordOffsets.size
                val freq1 = baseFrequency * chordOffsets[step]
                val freq2 = freq1 * 1.5 // fifth harmonic
                val freq3 = freq1 * 0.5 // sub-bass

                val t = i.toDouble() / sampleRate
                // Smooth envelope to avoid clicks
                val envelope = 0.8 + 0.2 * sin(2.0 * Math.PI * 0.5 * t)
                val s1 = sin(2.0 * Math.PI * freq1 * t)
                val s2 = 0.4 * sin(2.0 * Math.PI * freq2 * t)
                val s3 = 0.3 * sin(2.0 * Math.PI * freq3 * t)

                val combined = (s1 + s2 + s3) / 1.7 * envelope
                audioData[i] = (combined * 24000).toInt().coerceIn(-32768, 32767).toShort()
            }

            // Write 16-bit PCM WAV
            FileOutputStream(file).use { out ->
                val byteRate = sampleRate * 2
                val dataSize = numSamples * 2
                val totalSize = dataSize + 36

                val header = ByteArray(44)
                // "RIFF"
                header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
                header[4] = (totalSize and 0xff).toByte()
                header[5] = ((totalSize shr 8) and 0xff).toByte()
                header[6] = ((totalSize shr 16) and 0xff).toByte()
                header[7] = ((totalSize shr 24) and 0xff).toByte()
                // "WAVE"
                header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
                // "fmt "
                header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
                header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0 // 16 for PCM
                header[20] = 1; header[21] = 0 // PCM format = 1
                header[22] = 1; header[23] = 0 // mono = 1
                header[24] = (sampleRate and 0xff).toByte()
                header[25] = ((sampleRate shr 8) and 0xff).toByte()
                header[26] = ((sampleRate shr 16) and 0xff).toByte()
                header[27] = ((sampleRate shr 24) and 0xff).toByte()
                header[28] = (byteRate and 0xff).toByte()
                header[29] = ((byteRate shr 8) and 0xff).toByte()
                header[30] = ((byteRate shr 16) and 0xff).toByte()
                header[31] = ((byteRate shr 24) and 0xff).toByte()
                header[32] = 2; header[33] = 0 // block align = 2
                header[34] = 16; header[35] = 0 // bits per sample = 16
                // "data"
                header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
                header[40] = (dataSize and 0xff).toByte()
                header[41] = ((dataSize shr 8) and 0xff).toByte()
                header[42] = ((dataSize shr 16) and 0xff).toByte()
                header[43] = ((dataSize shr 24) and 0xff).toByte()

                out.write(header)
                val buffer = ByteArray(audioData.size * 2)
                for (idx in audioData.indices) {
                    val sample = audioData[idx].toInt()
                    buffer[idx * 2] = (sample and 0xff).toByte()
                    buffer[idx * 2 + 1] = ((sample shr 8) and 0xff).toByte()
                }
                out.write(buffer)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return file
    }

    fun getSampleSongs(context: Context): List<Song> {
        val synthwaveFile = createSampleWavFile(context, "synthwave_neon.wav", 220.0, 214)
        val sunsetFile = createSampleWavFile(context, "acoustic_sunset.wav", 196.0, 186)
        val lofiFile = createSampleWavFile(context, "cosmic_lofi.wav", 174.6, 245)
        val midnightFile = createSampleWavFile(context, "midnight_echoes.wav", 261.6, 198)
        val etherealFile = createSampleWavFile(context, "ethereal_dream.wav", 293.6, 230)
        val pulseFile = createSampleWavFile(context, "neon_pulse.wav", 246.9, 175)

        val now = System.currentTimeMillis()

        return listOf(
            Song(
                id = -101L,
                title = "Neon Boulevard",
                artist = "Solaris Drive",
                album = "Cyber Dreams",
                durationMs = 214000L,
                uri = synthwaveFile.absolutePath,
                albumArtRes = R.drawable.cover_synthwave,
                dateAdded = now - 1000 * 60 * 60 * 2,
                trackNumber = 1,
                genre = "Synthwave",
                bitRate = "320 kbps",
                sampleRate = "44.1 kHz",
                format = "FLAC"
            ),
            Song(
                id = -102L,
                title = "Golden Horizon",
                artist = "Autumn Echoes",
                album = "Valley of Sunsets",
                durationMs = 186000L,
                uri = sunsetFile.absolutePath,
                albumArtRes = R.drawable.cover_acoustic,
                dateAdded = now - 1000 * 60 * 60 * 24 * 2,
                trackNumber = 2,
                genre = "Acoustic / Indie",
                bitRate = "320 kbps",
                sampleRate = "48.0 kHz",
                format = "ALAC"
            ),
            Song(
                id = -103L,
                title = "Cosmic Reverie",
                artist = "Starlight Lofi",
                album = "Deep Space Chill",
                durationMs = 245000L,
                uri = lofiFile.absolutePath,
                albumArtRes = R.drawable.cover_lofi,
                dateAdded = now - 1000 * 60 * 60 * 24 * 5,
                trackNumber = 1,
                genre = "Lo-Fi Hip-Hop",
                bitRate = "320 kbps",
                sampleRate = "44.1 kHz",
                format = "MP3"
            ),
            Song(
                id = -104L,
                title = "Midnight In Tokyo",
                artist = "Solaris Drive",
                album = "Cyber Dreams",
                durationMs = 198000L,
                uri = midnightFile.absolutePath,
                albumArtRes = R.drawable.cover_synthwave,
                dateAdded = now - 1000 * 60 * 60 * 24 * 8,
                trackNumber = 3,
                genre = "Synthwave",
                bitRate = "320 kbps",
                sampleRate = "44.1 kHz",
                format = "MP3"
            ),
            Song(
                id = -105L,
                title = "Whispers in the Pine",
                artist = "Autumn Echoes",
                album = "Valley of Sunsets",
                durationMs = 230000L,
                uri = etherealFile.absolutePath,
                albumArtRes = R.drawable.cover_acoustic,
                dateAdded = now - 1000 * 60 * 60 * 24 * 12,
                trackNumber = 4,
                genre = "Folk / Ambient",
                bitRate = "320 kbps",
                sampleRate = "48.0 kHz",
                format = "WAV"
            ),
            Song(
                id = -106L,
                title = "Orbiting Saturn",
                artist = "Starlight Lofi",
                album = "Deep Space Chill",
                durationMs = 175000L,
                uri = pulseFile.absolutePath,
                albumArtRes = R.drawable.cover_lofi,
                dateAdded = now - 1000 * 60 * 60 * 24 * 15,
                trackNumber = 2,
                genre = "Ambient Chill",
                bitRate = "320 kbps",
                sampleRate = "44.1 kHz",
                format = "FLAC"
            )
        )
    }
}
