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
import kotlinx.coroutines.*
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import android.graphics.drawable.BitmapDrawable
import com.example.MainActivity
import com.example.R
import com.example.model.Song

class MusicPlaybackService : Service() {

    private var mediaSession: MediaSession? = null
    private var notificationManager: NotificationManager? = null
    private val artworkScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var artworkJob: Job? = null
    private var artworkKey: Any? = null
    private var cachedArtwork: Bitmap? = null
    private var latestInfo: Triple<String, String, String>? = null
    private var latestPlaying = false

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
            context.stopService(Intent(context, MusicPlaybackService::class.java))
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
                    playerControllerInstance?.let { if (!it.isPlaying.value) it.togglePlayPause() }
                }

                override fun onPause() {
                    playerControllerInstance?.let { if (it.isPlaying.value) it.togglePlayPause() }
                }

                override fun onSkipToNext() {
                    playerControllerInstance?.playNext()
                }

                override fun onSkipToPrevious() {
                    playerControllerInstance?.playPrevious()
                }

                override fun onSeekTo(pos: Long) { playerControllerInstance?.seekTo(pos) }

                override fun onStop() {
                    playerControllerInstance?.stop()
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
                playerControllerInstance?.stop()
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

                latestInfo = Triple(title, artist, album)
                latestPlaying = isPlaying
                val key = artUri?.takeIf { it.isNotBlank() } ?: artRes.takeIf { it != 0 }
                if (key != artworkKey) { artworkJob?.cancel(); cachedArtwork = null; artworkKey = key }
                updateMediaSessionMetadata(title, artist, album, isPlaying)
                startForeground(NOTIFICATION_ID, buildNotification(title, artist, album, isPlaying, artRes, artUri))
                if (cachedArtwork == null && key != null && artworkJob?.isActive != true) {
                    artworkJob = artworkScope.launch {
                        val result = imageLoader.execute(ImageRequest.Builder(this@MusicPlaybackService).data(key).size(256).allowHardware(false).build())
                        if (artworkKey == key && result is SuccessResult) {
                            cachedArtwork = (result.drawable as? BitmapDrawable)?.bitmap
                            latestInfo?.let { (t, a, al) ->
                                updateMediaSessionMetadata(t, a, al, latestPlaying)
                                notificationManager?.notify(NOTIFICATION_ID, buildNotification(t, a, al, latestPlaying, 0, null))
                            }
                        }
                    }
                }
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
                PlaybackState.ACTION_STOP or PlaybackState.ACTION_SEEK_TO

        mediaSession?.setPlaybackState(
            PlaybackState.Builder()
                .setState(state, playerControllerInstance?.currentPositionMs?.value ?: 0L, if (isPlaying) 1.0f else 0.0f)
                .setActions(actions)
                .build()
        )

        mediaSession?.setMetadata(
            MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, title)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadata.METADATA_KEY_ALBUM, album)
                .putLong(MediaMetadata.METADATA_KEY_DURATION, playerControllerInstance?.durationMs?.value ?: 0L)
                .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, cachedArtwork)
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

        val artworkBitmap = cachedArtwork ?: createFallbackArtworkBitmap(title)

        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play

        val builder = (if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, CHANNEL_ID) else Notification.Builder(this))
            .setStyle(Notification.MediaStyle().setMediaSession(mediaSession?.sessionToken).setShowActionsInCompactView(0, 1, 2))
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText(if (album.isNotEmpty()) album else "Personal Music")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setLargeIcon(artworkBitmap)
            .setContentIntent(contentPendingIntent)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setPriority(Notification.PRIORITY_LOW)
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
        artworkScope.cancel()
        mediaSession?.release()
        super.onDestroy()
    }
}
