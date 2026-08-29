package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.model.Song

class MusicPlaybackService : Service() {

    private var mediaSession: MediaSession? = null
    private var notificationManager: NotificationManager? = null

    companion object {
        const val CHANNEL_ID = "music_player_channel"
        const val NOTIFICATION_ID = 101

        const val ACTION_PLAY_PAUSE = "com.example.musicplayer.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.musicplayer.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.musicplayer.ACTION_PREVIOUS"
        const val ACTION_STOP = "com.example.musicplayer.ACTION_STOP"
        const val ACTION_UPDATE = "com.example.musicplayer.ACTION_UPDATE"

        const val EXTRA_SONG_TITLE = "extra_song_title"
        const val EXTRA_SONG_ARTIST = "extra_song_artist"
        const val EXTRA_SONG_ALBUM = "extra_song_album"
        const val EXTRA_IS_PLAYING = "extra_is_playing"
        const val EXTRA_ART_RES = "extra_art_res"
        const val EXTRA_ART_URI = "extra_art_uri"

        var playerControllerInstance: MusicPlayerController? = null

        fun startOrUpdate(
            context: Context,
            song: Song,
            isPlaying: Boolean
        ) {
            val intent = Intent(context, MusicPlaybackService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_SONG_TITLE, song.title)
                putExtra(EXTRA_SONG_ARTIST, song.artist)
                putExtra(EXTRA_SONG_ALBUM, song.album)
                putExtra(EXTRA_IS_PLAYING, isPlaying)
                putExtra(EXTRA_ART_RES, song.albumArtRes ?: 0)
                putExtra(EXTRA_ART_URI, song.albumArtUri)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, MusicPlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        initMediaSession()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Playback controls and current song information"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun initMediaSession() {
        mediaSession = MediaSession(this, "MusicPlayerSession").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    playerControllerInstance?.togglePlayPause()
                }

                override fun onPause() {
                    playerControllerInstance?.togglePlayPause()
                }

                override fun onSkipToNext() {
                    playerControllerInstance?.playNext()
                }

                override fun onSkipToPrevious() {
                    playerControllerInstance?.playPrevious()
                }

                override fun onStop() {
                    stopSelf()
                }
            })
            isActive = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            ACTION_PLAY_PAUSE -> {
                playerControllerInstance?.togglePlayPause()
            }
            ACTION_NEXT -> {
                playerControllerInstance?.playNext()
            }
            ACTION_PREVIOUS -> {
                playerControllerInstance?.playPrevious()
            }
            ACTION_STOP -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_UPDATE -> {
                val title = intent.getStringExtra(EXTRA_SONG_TITLE) ?: "Music Player"
                val artist = intent.getStringExtra(EXTRA_SONG_ARTIST) ?: "Unknown Artist"
                val album = intent.getStringExtra(EXTRA_SONG_ALBUM) ?: ""
                val isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, false)
                val artRes = intent.getIntExtra(EXTRA_ART_RES, 0)
                val artUri = intent.getStringExtra(EXTRA_ART_URI)

                updateMediaSessionMetadata(title, artist, album, isPlaying)
                val notification = buildNotification(title, artist, album, isPlaying, artRes, artUri)
                startForeground(NOTIFICATION_ID, notification)
            }
        }

        return START_STICKY
    }

    private fun updateMediaSessionMetadata(
        title: String,
        artist: String,
        album: String,
        isPlaying: Boolean
    ) {
        val state = if (isPlaying) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED
        val actions = PlaybackState.ACTION_PLAY or
                PlaybackState.ACTION_PAUSE or
                PlaybackState.ACTION_PLAY_PAUSE or
                PlaybackState.ACTION_SKIP_TO_NEXT or
                PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                PlaybackState.ACTION_STOP

        mediaSession?.setPlaybackState(
            PlaybackState.Builder()
                .setState(state, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                .setActions(actions)
                .build()
        )

        mediaSession?.setMetadata(
            MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, title)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadata.METADATA_KEY_ALBUM, album)
                .build()
        )
    }

    private fun buildNotification(
        title: String,
        artist: String,
        album: String,
        isPlaying: Boolean,
        artRes: Int,
        artUri: String?
    ): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_PREVIOUS }
        val prevPendingIntent = PendingIntent.getService(this, 1, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val playPauseIntent = Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_PLAY_PAUSE }
        val playPausePendingIntent = PendingIntent.getService(this, 2, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val nextIntent = Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_NEXT }
        val nextPendingIntent = PendingIntent.getService(this, 3, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val stopIntent = Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(this, 4, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        var artworkBitmap: Bitmap? = null
        try {
            if (artRes != 0) {
                artworkBitmap = BitmapFactory.decodeResource(resources, artRes)
            } else if (!artUri.isNullOrEmpty()) {
                val input = contentResolver.openInputStream(Uri.parse(artUri))
                artworkBitmap = BitmapFactory.decodeStream(input)
                input?.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (artworkBitmap == null) {
            artworkBitmap = createFallbackArtworkBitmap(title)
        }

        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText(if (album.isNotEmpty()) album else "Personal Music")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setLargeIcon(artworkBitmap)
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(isPlaying)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
            .addAction(playPauseIcon, if (isPlaying) "Pause" else "Play", playPausePendingIntent)
            .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Close", stopPendingIntent)

        return builder.build()
    }

    private fun createFallbackArtworkBitmap(title: String): Bitmap {
        val size = 256
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = 0xFF191C2B.toInt()
            isAntiAlias = true
        }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)

        val textPaint = Paint().apply {
            color = 0xFF9D65FF.toInt()
            textSize = 96f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val letter = title.firstOrNull()?.uppercase() ?: "M"
        canvas.drawText(letter, size / 2f, size / 2f + 32f, textPaint)
        return bitmap
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        mediaSession?.release()
        super.onDestroy()
    }
}
