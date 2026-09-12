package com.example

import android.app.Application
import com.example.data.*
import com.example.service.*
import kotlinx.coroutines.*

class MusicApplication : Application() {
    val playback by lazy { PlaybackRuntime(this) }
}

class PlaybackRuntime(application: Application) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val repository = MusicRepository(application, MusicDatabase.getDatabase(application), scope)
    val controller = MusicPlayerController(application, repository, scope)
    init { MusicPlaybackService.playerControllerInstance = controller }
}
