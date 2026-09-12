package com.example.service

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.CountDownTimer
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.MusicRepository
import com.example.model.RepeatMode
import com.example.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class MusicPlayerController(
    private val context: Context,
    private val repository: MusicRepository,
    private val scope: CoroutineScope
) {
    private var mediaPlayer: MediaPlayer? = null
    private var preparing = false
    private var playWhenReady = true
    private var equalizer: android.media.audiofx.Equalizer? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var pausedForAudioFocus = false
    private var duckedForAudioFocus = false
    private var lastPersistedPositionMs = 0L
    private val noisyReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) pauseForInterruption()
        }
    }

    private val audioFocusListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (duckedForAudioFocus) mediaPlayer?.setVolume(1f, 1f)
                duckedForAudioFocus = false
                if (pausedForAudioFocus && _currentSong.value != null) {
                    pausedForAudioFocus = false
                    playWhenReady = true
                    mediaPlayer?.let { player ->
                        if (!preparing) {
                            player.start()
                            _isPlaying.value = true
                            startProgressTracker()
                            updatePlaybackService()
                        }
                    } ?: startMediaPlayer(_currentSong.value!!, _currentPositionMs.value.toInt())
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (_isPlaying.value) {
                    mediaPlayer?.setVolume(0.25f, 0.25f)
                    duckedForAudioFocus = true
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS -> pauseForInterruption()
        }
    }

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(1L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _sleepTimerSecondsLeft = MutableStateFlow<Long?>(null)
    val sleepTimerSecondsLeft: StateFlow<Long?> = _sleepTimerSecondsLeft.asStateFlow()

    private val _equalizerPreset = MutableStateFlow("Balanced")
    val equalizerPreset: StateFlow<String> = _equalizerPreset.asStateFlow()

    private var progressJob: Job? = null
    private var sleepTimer: CountDownTimer? = null
    private var originalUnshuffledQueue: List<Song> = emptyList()
    private var isInitialQueuePopulated = false

    init {
        ContextCompat.registerReceiver(
            context,
            noisyReceiver,
            IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        restoreState()
    }

    private fun requestAudioFocus(): Boolean {
        val result = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                .setOnAudioFocusChangeListener(audioFocusListener)
                .setWillPauseWhenDucked(false)
                .build()
            audioFocusRequest = request
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                audioFocusListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonAudioFocus() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusListener)
        }
    }

    private fun pauseForInterruption() {
        if (!_isPlaying.value && !preparing) return
        pausedForAudioFocus = true
        playWhenReady = false
        persistCurrentPosition()
        try { mediaPlayer?.pause() } catch (e: IllegalStateException) { Log.w("MusicPlayer", "Unable to pause after audio interruption", e) }
        _isPlaying.value = false
        stopProgressTracker()
        updatePlaybackService()
    }

    private fun restoreState() {
        scope.launch {
            val lastSongIdStr = repository.getPlayerSetting("last_song_id")
            val lastPosStr = repository.getPlayerSetting("last_pos")
            val shuffleStr = repository.getPlayerSetting("shuffle")
            val repeatStr = repository.getPlayerSetting("repeat")
            _equalizerPreset.value = repository.getPlayerSetting("equalizer") ?: "Balanced"

            _isShuffle.value = shuffleStr?.toBoolean() ?: false
            _repeatMode.value = when (repeatStr) {
                "ONE" -> RepeatMode.ONE
                "OFF" -> RepeatMode.OFF
                else -> RepeatMode.ALL
            }

            val lastSongId = lastSongIdStr?.toLongOrNull()
            val lastPos = lastPosStr?.toLongOrNull() ?: 0L

            repository.songs.collect { songs ->
                val byId = songs.associateBy { it.id }
                _queue.value = _queue.value.map { byId[it.id] ?: it }
                originalUnshuffledQueue = originalUnshuffledQueue.map { byId[it.id] ?: it }
                _currentSong.value?.let { current ->
                    val updated = byId[current.id] ?: current
                    _currentSong.value = updated
                    if (updated != current && mediaPlayer != null) MusicPlaybackService.startOrUpdate(context, updated, _isPlaying.value)
                }

                if (songs.isNotEmpty() && !isInitialQueuePopulated) {
                    isInitialQueuePopulated = true

                    // Select at least 5 random non-duplicate songs from the available music library
                    val distinctSongs = songs.distinctBy { it.id }
                    val targetCount = if (distinctSongs.size < 5) distinctSongs.size else 5
                    val randomSelected = distinctSongs.shuffled().take(targetCount).toMutableList()

                    // Determine initial loaded song
                    val loadedSong = if (lastSongId != null) {
                        distinctSongs.find { it.id == lastSongId } ?: randomSelected.firstOrNull()
                    } else {
                        randomSelected.firstOrNull()
                    }

                    if (loadedSong != null) {
                        if (!randomSelected.any { it.id == loadedSong.id }) {
                            randomSelected.add(0, loadedSong)
                        }
                        _queue.value = randomSelected
                        originalUnshuffledQueue = randomSelected

                        val initialIndex = randomSelected.indexOfFirst { it.id == loadedSong.id }.coerceAtLeast(0)
                        _currentIndex.value = initialIndex
                        _currentSong.value = loadedSong
                        _durationMs.value = loadedSong.durationMs
                        _currentPositionMs.value = lastPos
                    } else {
                        _queue.value = randomSelected
                        originalUnshuffledQueue = randomSelected
                        _currentIndex.value = 0
                    }
                }
            }
        }
    }

    fun playSong(song: Song, newQueue: List<Song> = emptyList()) {
        val targetQueue = if (newQueue.isNotEmpty()) newQueue else listOf(song)
        originalUnshuffledQueue = targetQueue

        val finalQueue = if (_isShuffle.value) {
            val list = targetQueue.toMutableList()
            list.remove(song)
            list.shuffle()
            listOf(song) + list
        } else {
            targetQueue
        }

        _queue.value = finalQueue
        val index = finalQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        _currentIndex.value = index
        playAtIndex(index)
    }

    fun playAtIndex(index: Int) {
        val queueList = _queue.value
        if (index !in queueList.indices) return

        val song = queueList[index]
        _currentIndex.value = index
        _currentSong.value = song

        startMediaPlayer(song, 0)
        if (mediaPlayer != null) MusicPlaybackService.startOrUpdate(context, song, isPlaying = false)

        scope.launch {
            repository.savePlayerSetting("last_song_id", song.id.toString())
        }
    }

    private fun startMediaPlayer(song: Song, startPositionMs: Int = 0) {
        stopProgressTracker()
        preparing = true
        playWhenReady = true
        _isPlaying.value = false
        _currentPositionMs.value = startPositionMs.toLong()
        try {
            equalizer?.release(); equalizer = null
            mediaPlayer?.release()
            val player = MediaPlayer()
            mediaPlayer = player
            player.setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA).build())
            if (song.uri.startsWith("/")) player.setDataSource(song.uri)
            else player.setDataSource(context, Uri.parse(song.uri))
            player.setOnPreparedListener { prepared ->
                if (mediaPlayer !== prepared) return@setOnPreparedListener
                preparing = false
                if (startPositionMs > 0) prepared.seekTo(startPositionMs)
                _durationMs.value = prepared.duration.toLong().coerceAtLeast(1)
                applyEqualizer()
                if (playWhenReady) {
                    if (!requestAudioFocus()) {
                        playWhenReady = false
                        _isPlaying.value = false
                        updatePlaybackService()
                        return@setOnPreparedListener
                    }
                    prepared.start(); _isPlaying.value = true; startProgressTracker()
                    scope.launch { repository.recordPlayback(song.id, startPositionMs.toLong()) }
                }
                updatePlaybackService()
            }
            player.setOnCompletionListener { handleSongCompletion() }
            player.setOnErrorListener { _, what, extra ->
                Log.e("MusicPlayer", "Playback error $what/$extra")
                preparing = false; _isPlaying.value = false; stopProgressTracker()
                mediaPlayer?.release(); mediaPlayer = null
                android.widget.Toast.makeText(context, "Unable to play this audio file", android.widget.Toast.LENGTH_SHORT).show()
                MusicPlaybackService.stop(context)
                true
            }
            player.prepareAsync()
        } catch (e: Exception) {
            preparing = false; _isPlaying.value = false
            mediaPlayer?.release(); mediaPlayer = null
            Log.e("MusicPlayer", "Unable to load ${song.title}", e)
            android.widget.Toast.makeText(context, "Audio file unavailable. Rescan your library.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSongCompletion() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                mediaPlayer?.seekTo(0)
                mediaPlayer?.start()
                _isPlaying.value = true
            }
            RepeatMode.ALL -> {
                playNext()
            }
            RepeatMode.OFF -> {
                if (_currentIndex.value < _queue.value.lastIndex) {
                    playNext()
                } else {
                    _isPlaying.value = false
                    stopProgressTracker()
                    _currentSong.value?.let { MusicPlaybackService.startOrUpdate(context, it, isPlaying = false) }
                }
            }
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer
        val song = _currentSong.value ?: return
        if (preparing) { playWhenReady = !playWhenReady; return }

        if (player == null) {
            startMediaPlayer(song, _currentPositionMs.value.toInt())
            return
        }

        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
            stopProgressTracker()
            persistCurrentPosition()
            updatePlaybackService()
        } else {
            if (!requestAudioFocus()) return
            player.start()
            _isPlaying.value = true
            startProgressTracker()
            updatePlaybackService()
        }
    }

    fun playNext() {
        val queueList = _queue.value
        if (queueList.isEmpty()) return

        val nextIndex = if (_currentIndex.value + 1 < queueList.size) {
            _currentIndex.value + 1
        } else {
            0
        }
        playAtIndex(nextIndex)
    }

    fun playPrevious() {
        val player = mediaPlayer
        if (!preparing && player != null && player.currentPosition > 3000) {
            // Seek to start if already played > 3 seconds
            seekTo(0)
            return
        }

        val queueList = _queue.value
        if (queueList.isEmpty()) return

        val prevIndex = if (_currentIndex.value - 1 >= 0) {
            _currentIndex.value - 1
        } else {
            queueList.lastIndex
        }
        playAtIndex(prevIndex)
    }

    fun seekTo(positionMs: Long) {
        if (preparing) return
        val clamped = positionMs.coerceIn(0L, _durationMs.value.coerceAtLeast(1L))
        _currentPositionMs.value = clamped
        mediaPlayer?.seekTo(clamped.toInt())
        persistCurrentPosition()
    }

    fun toggleShuffle() {
        val newShuffle = !_isShuffle.value
        _isShuffle.value = newShuffle
        val current = _currentSong.value

        if (newShuffle) {
            val list = _queue.value.toMutableList()
            if (current != null) {
                list.remove(current)
                list.shuffle()
                _queue.value = listOf(current) + list
                _currentIndex.value = 0
            } else {
                list.shuffle()
                _queue.value = list
            }
        } else {
            _queue.value = originalUnshuffledQueue
            _currentIndex.value = originalUnshuffledQueue.indexOfFirst { it.id == current?.id }.coerceAtLeast(0)
        }

        scope.launch {
            repository.savePlayerSetting("shuffle", newShuffle.toString())
        }
    }

    fun toggleRepeat() {
        val next = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _repeatMode.value = next
        scope.launch {
            repository.savePlayerSetting("repeat", next.name)
        }
    }

    fun addToQueue(song: Song) {
        val updated = _queue.value + song
        _queue.value = updated
        originalUnshuffledQueue = originalUnshuffledQueue + song
    }

    fun playNextInQueue(song: Song) {
        val list = _queue.value.toMutableList()
        val insertIndex = (_currentIndex.value + 1).coerceAtMost(list.size)
        list.add(insertIndex, song)
        _queue.value = list
        originalUnshuffledQueue = list
    }

    fun removeFromQueue(index: Int): Boolean {
        val list = _queue.value.toMutableList()
        if (index !in list.indices) return false
        val wasCurrent = index == _currentIndex.value
        val removed = list.removeAt(index)
        _queue.value = list
        originalUnshuffledQueue = originalUnshuffledQueue.toMutableList().apply { remove(removed) }
        if (list.isEmpty()) {
            stop(); _currentSong.value = null; _currentIndex.value = 0
        } else if (wasCurrent) {
            playAtIndex(index.coerceAtMost(list.lastIndex))
        } else if (index < _currentIndex.value) {
            _currentIndex.value -= 1
        }
        return true
    }

    fun clearQueue() {
        stop(); _queue.value = emptyList(); originalUnshuffledQueue = emptyList()
        _currentSong.value = null; _currentIndex.value = 0
    }


    fun stop() {
        persistCurrentPosition()
        preparing = false
        playWhenReady = false
        try {
            mediaPlayer?.stop()
        } catch (e: Exception) {
            Log.w("MusicPlayer", "Stopping an unprepared player", e)
        } finally {
            mediaPlayer?.release()
            mediaPlayer = null
            equalizer?.release()
            equalizer = null
        }
        _isPlaying.value = false
        _currentPositionMs.value = 0L
        stopProgressTracker()
        abandonAudioFocus()
        pausedForAudioFocus = false
        duckedForAudioFocus = false
        MusicPlaybackService.stop(context)
    }

    fun removeSongFromQueueById(songId: Long) {
        val current = _currentSong.value
        val list = _queue.value.filterNot { it.id == songId }
        _queue.value = list
        originalUnshuffledQueue = originalUnshuffledQueue.filterNot { it.id == songId }
        if (current != null) {
            val newIdx = list.indexOfFirst { it.id == current.id }
            if (newIdx >= 0) {
                _currentIndex.value = newIdx
            }
        }
    }

    fun setEqualizerPreset(preset: String) {
        _equalizerPreset.value = preset
        applyEqualizer()
        scope.launch { repository.savePlayerSetting("equalizer", preset) }
    }

    private fun applyEqualizer() {
        val player = mediaPlayer ?: return
        if (preparing) return
        try {
            val effect = equalizer ?: android.media.audiofx.Equalizer(0, player.audioSessionId).also { equalizer = it }
            val gains = when (_equalizerPreset.value) {
                "Bass Boost" -> listOf(.75f, .5f, 0f, .1f, .2f)
                "Synthwave" -> listOf(.6f, .3f, .1f, .5f, .7f)
                "Vocal Clarity" -> listOf(-.2f, .3f, .8f, .4f, .1f)
                "Chill Lo-Fi" -> listOf(.4f, .5f, .2f, -.2f, -.4f)
                "Rock & Metal" -> listOf(.5f, .2f, -.1f, .4f, .6f)
                else -> List(5) { 0f }
            }
            val range = effect.bandLevelRange
            for (band in 0 until effect.numberOfBands) {
                val index = (band * 4 / (effect.numberOfBands - 1).coerceAtLeast(1)).coerceIn(0, 4)
                effect.setBandLevel(band.toShort(), (gains[index] * 1000).toInt().coerceIn(range[0].toInt(), range[1].toInt()).toShort())
            }
            effect.enabled = true
        } catch (e: Exception) {
            Log.w("MusicPlayer", "Equalizer unavailable", e)
            android.widget.Toast.makeText(context, "Equalizer unavailable on this device", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimer?.cancel()
        if (minutes <= 0) {
            _sleepTimerSecondsLeft.value = null
            return
        }

        val durationMillis = minutes * 60 * 1000L
        sleepTimer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _sleepTimerSecondsLeft.value = millisUntilFinished / 1000
            }

            override fun onFinish() {
                _sleepTimerSecondsLeft.value = null
                if (_isPlaying.value) {
                    try {
                        mediaPlayer?.pause()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    _isPlaying.value = false
                    stopProgressTracker()
                    _currentSong.value?.let { MusicPlaybackService.startOrUpdate(context, it, isPlaying = false) }
                }
            }
        }.start()
    }

    fun cancelSleepTimer() {
        sleepTimer?.cancel()
        _sleepTimerSecondsLeft.value = null
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _currentPositionMs.value = player.currentPosition.toLong()
                        if (_currentPositionMs.value - lastPersistedPositionMs >= 5_000L) {
                            persistCurrentPosition()
                        }
                    }
                }
                delay(250)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        persistCurrentPosition()
        equalizer?.release()
        equalizer = null
        stopProgressTracker()
        sleepTimer?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        abandonAudioFocus()
        runCatching { context.unregisterReceiver(noisyReceiver) }
    }

    private fun persistCurrentPosition() {
        val position = runCatching { mediaPlayer?.currentPosition?.toLong() }.getOrNull()
            ?: _currentPositionMs.value
        lastPersistedPositionMs = position
        scope.launch { repository.savePlayerSetting("last_pos", position.toString()) }
    }

    private fun updatePlaybackService() {
        _currentSong.value?.let { MusicPlaybackService.startOrUpdate(context, it, _isPlaying.value) }
    }
}
