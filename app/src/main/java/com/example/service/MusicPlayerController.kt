package com.example.service

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.CountDownTimer
import android.util.Log
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
        restoreState()
    }

    private fun restoreState() {
        scope.launch(Dispatchers.IO) {
            val lastSongIdStr = repository.getPlayerSetting("last_song_id")
            val lastPosStr = repository.getPlayerSetting("last_pos")
            val shuffleStr = repository.getPlayerSetting("shuffle")
            val repeatStr = repository.getPlayerSetting("repeat")

            _isShuffle.value = shuffleStr?.toBoolean() ?: false
            _repeatMode.value = when (repeatStr) {
                "ONE" -> RepeatMode.ONE
                "OFF" -> RepeatMode.OFF
                else -> RepeatMode.ALL
            }

            val lastSongId = lastSongIdStr?.toLongOrNull()
            val lastPos = lastPosStr?.toLongOrNull() ?: 0L

            repository.songs.collect { songs ->
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
        MusicPlaybackService.startOrUpdate(context, song, isPlaying = true)

        scope.launch {
            repository.recordPlayback(song.id, 0L)
            repository.savePlayerSetting("last_song_id", song.id.toString())
        }
    }

    private fun startMediaPlayer(song: Song, startPositionMs: Int = 0) {
        stopProgressTracker()
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                if (song.uri.startsWith("content://")) {
                    setDataSource(context, Uri.parse(song.uri))
                } else if (song.uri.startsWith("/")) {
                    val file = File(song.uri)
                    if (file.exists()) {
                        setDataSource(song.uri)
                    } else {
                        // Fallback to sample
                        val sampleFile = com.example.data.SampleAudioProvider.createSampleWavFile(context, "fallback_${song.id}.wav", 220.0, 180)
                        setDataSource(sampleFile.absolutePath)
                    }
                } else {
                    setDataSource(context, Uri.parse(song.uri))
                }

                prepare()
                if (startPositionMs > 0) {
                    seekTo(startPositionMs)
                }
                start()

                setOnCompletionListener {
                    handleSongCompletion()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e("MusicPlayer", "Error in playback what: $what extra: $extra")
                    _isPlaying.value = false
                    true
                }
            }

            _isPlaying.value = true
            _durationMs.value = mediaPlayer?.duration?.toLong()?.takeIf { it > 0 } ?: song.durationMs
            startProgressTracker()
        } catch (e: Exception) {
            Log.e("MusicPlayer", "Failed to start player for ${song.title}", e)
            _isPlaying.value = false
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

        if (player == null) {
            startMediaPlayer(song, _currentPositionMs.value.toInt())
            return
        }

        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
            stopProgressTracker()
            MusicPlaybackService.startOrUpdate(context, song, isPlaying = false)
            scope.launch {
                repository.savePlayerSetting("last_pos", player.currentPosition.toString())
            }
        } else {
            player.start()
            _isPlaying.value = true
            startProgressTracker()
            MusicPlaybackService.startOrUpdate(context, song, isPlaying = true)
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
        if (player != null && player.currentPosition > 3000) {
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
        _currentPositionMs.value = positionMs
        mediaPlayer?.seekTo(positionMs.toInt())
        scope.launch {
            repository.savePlayerSetting("last_pos", positionMs.toString())
        }
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
    }

    fun playNextInQueue(song: Song) {
        val list = _queue.value.toMutableList()
        val insertIndex = (_currentIndex.value + 1).coerceAtMost(list.size)
        list.add(insertIndex, song)
        _queue.value = list
    }

    fun removeFromQueue(index: Int): Boolean {
        if (_queue.value.size <= 5) {
            return false
        }
        val list = _queue.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _queue.value = list
            if (index < _currentIndex.value) {
                _currentIndex.value = (_currentIndex.value - 1).coerceAtLeast(0)
            }
            return true
        }
        return false
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _isPlaying.value = false
        _currentPositionMs.value = 0L
        stopProgressTracker()
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
        stopProgressTracker()
        sleepTimer?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
