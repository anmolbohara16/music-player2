package com.example.ui.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MusicDatabase
import com.example.data.MusicRepository
import com.example.data.SongMetadataEntity
import com.example.metadata.model.OnlineSongMetadata
import com.example.metadata.service.MetadataSearchService
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Playlist
import com.example.model.RepeatMode
import com.example.model.Song
import com.example.model.SortOption
import com.example.service.MusicPlaybackService
import com.example.service.MusicPlayerController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class MainTab(val title: String) {
    HOME("Home"),
    SONGS("Songs"),
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    PLAYLISTS("Playlists"),
    FAVORITES("Favorites")
}

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MusicDatabase.getDatabase(application)
    val repository = MusicRepository(application, database, viewModelScope)
    val controller = MusicPlayerController(application, repository, viewModelScope)

    init {
        MusicPlaybackService.playerControllerInstance = controller
    }

    // Repository flows
    val allSongs: StateFlow<List<Song>> = repository.songs
    val favoriteSongs: StateFlow<List<Song>> = repository.favoriteSongs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val recentlyPlayed: StateFlow<List<Song>> = repository.recentlyPlayed.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val recentlyAdded: StateFlow<List<Song>> = repository.recentlyAdded.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val albums: StateFlow<List<Album>> = repository.albums.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val artists: StateFlow<List<Artist>> = repository.artists.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val playlists: StateFlow<List<Playlist>> = repository.playlists.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val isLoading: StateFlow<Boolean> = repository.isLoading

    // Controller flows
    val currentSong: StateFlow<Song?> = controller.currentSong
    val isPlaying: StateFlow<Boolean> = controller.isPlaying
    val currentPositionMs: StateFlow<Long> = controller.currentPositionMs
    val durationMs: StateFlow<Long> = controller.durationMs
    val queue: StateFlow<List<Song>> = controller.queue
    val currentIndex: StateFlow<Int> = controller.currentIndex
    val isShuffle: StateFlow<Boolean> = controller.isShuffle
    val repeatMode: StateFlow<RepeatMode> = controller.repeatMode
    val sleepTimerSecondsLeft: StateFlow<Long?> = controller.sleepTimerSecondsLeft
    val equalizerPreset: StateFlow<String> = controller.equalizerPreset

    // Navigation & Screen states
    private val _activeTab = MutableStateFlow(MainTab.SONGS)
    val activeTab: StateFlow<MainTab> = _activeTab.asStateFlow()

    private val _selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedAlbum: StateFlow<Album?> = _selectedAlbum.asStateFlow()

    private val _selectedArtist = MutableStateFlow<Artist?>(null)
    val selectedArtist: StateFlow<Artist?> = _selectedArtist.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    val playlistSongs: StateFlow<List<Song>> = _selectedPlaylist.flatMapLatest { playlist ->
        if (playlist != null) {
            repository.getSongsForPlaylist(playlist.id)
        } else {
            MutableStateFlow(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Sheets and Dialogs
    private val _isNowPlayingExpanded = MutableStateFlow(false)
    val isNowPlayingExpanded: StateFlow<Boolean> = _isNowPlayingExpanded.asStateFlow()

    private val _isPlayerOptionsOpen = MutableStateFlow(false)
    val isPlayerOptionsOpen: StateFlow<Boolean> = _isPlayerOptionsOpen.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _isQueueSheetOpen = MutableStateFlow(false)
    val isQueueSheetOpen: StateFlow<Boolean> = _isQueueSheetOpen.asStateFlow()

    private val _isAddToPlaylistDialogOpen = MutableStateFlow(false)
    val isAddToPlaylistDialogOpen: StateFlow<Boolean> = _isAddToPlaylistDialogOpen.asStateFlow()

    private val _songForAddToPlaylist = MutableStateFlow<Song?>(null)
    val songForAddToPlaylist: StateFlow<Song?> = _songForAddToPlaylist.asStateFlow()

    private val _isCreatePlaylistDialogOpen = MutableStateFlow(false)
    val isCreatePlaylistDialogOpen: StateFlow<Boolean> = _isCreatePlaylistDialogOpen.asStateFlow()

    private val _isSleepTimerDialogOpen = MutableStateFlow(false)
    val isSleepTimerDialogOpen: StateFlow<Boolean> = _isSleepTimerDialogOpen.asStateFlow()

    private val _isEqualizerDialogOpen = MutableStateFlow(false)
    val isEqualizerDialogOpen: StateFlow<Boolean> = _isEqualizerDialogOpen.asStateFlow()

    private val _isSortDialogOpen = MutableStateFlow(false)
    val isSortDialogOpen: StateFlow<Boolean> = _isSortDialogOpen.asStateFlow()

    private val _isSongInfoDialogOpen = MutableStateFlow(false)
    val isSongInfoDialogOpen: StateFlow<Boolean> = _isSongInfoDialogOpen.asStateFlow()

    private val _songForInfo = MutableStateFlow<Song?>(null)
    val songForInfo: StateFlow<Song?> = _songForInfo.asStateFlow()

    private val _isDeleteSongDialogOpen = MutableStateFlow(false)
    val isDeleteSongDialogOpen: StateFlow<Boolean> = _isDeleteSongDialogOpen.asStateFlow()

    private val _songForDelete = MutableStateFlow<Song?>(null)
    val songForDelete: StateFlow<Song?> = _songForDelete.asStateFlow()

    // Metadata Search & Identification states
    private val metadataService = MetadataSearchService()

    private val _isMetadataSearchOpen = MutableStateFlow(false)
    val isMetadataSearchOpen: StateFlow<Boolean> = _isMetadataSearchOpen.asStateFlow()

    private val _isSearchingMetadata = MutableStateFlow(false)
    val isSearchingMetadata: StateFlow<Boolean> = _isSearchingMetadata.asStateFlow()

    private val _metadataSearchResult = MutableStateFlow<OnlineSongMetadata?>(null)
    val metadataSearchResult: StateFlow<OnlineSongMetadata?> = _metadataSearchResult.asStateFlow()

    private val _metadataSearchError = MutableStateFlow<String?>(null)
    val metadataSearchError: StateFlow<String?> = _metadataSearchError.asStateFlow()

    private val _songForMetadata = MutableStateFlow<Song?>(null)
    val songForMetadata: StateFlow<Song?> = _songForMetadata.asStateFlow()

    private val _isIdentifyByLinkOpen = MutableStateFlow(false)
    val isIdentifyByLinkOpen: StateFlow<Boolean> = _isIdentifyByLinkOpen.asStateFlow()

    private val _isResolvingLink = MutableStateFlow(false)
    val isResolvingLink: StateFlow<Boolean> = _isResolvingLink.asStateFlow()

    private val _isEditMetadataOpen = MutableStateFlow(false)
    val isEditMetadataOpen: StateFlow<Boolean> = _isEditMetadataOpen.asStateFlow()

    private val _songForEditMetadata = MutableStateFlow<Song?>(null)
    val songForEditMetadata: StateFlow<Song?> = _songForEditMetadata.asStateFlow()

    private val _isLyricsViewerOpen = MutableStateFlow(false)
    val isLyricsViewerOpen: StateFlow<Boolean> = _isLyricsViewerOpen.asStateFlow()

    private val _songForLyrics = MutableStateFlow<Song?>(null)
    val songForLyrics: StateFlow<Song?> = _songForLyrics.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.TITLE_ASC)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    // Filtered / Sorted songs for Songs screen
    val sortedSongs: StateFlow<List<Song>> = combine(allSongs, _sortOption) { list, sort ->
        repository.sortSongs(list, sort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search results across Songs, Albums, Artists
    val searchResultsSongs: StateFlow<List<Song>> = combine(allSongs, _searchQuery) { list, query ->
        if (query.isBlank()) emptyList()
        else list.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.artist.contains(query, ignoreCase = true) ||
            it.album.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun showToast(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
        }
    }

    // Tab Navigation
    fun setActiveTab(tab: MainTab) {
        _activeTab.value = tab
        _selectedAlbum.value = null
        _selectedArtist.value = null
        _selectedPlaylist.value = null
        _isSearchActive.value = false
    }

    fun openAlbum(album: Album) {
        _selectedAlbum.value = album
    }

    fun closeAlbum() {
        _selectedAlbum.value = null
    }

    fun openArtist(artist: Artist) {
        _selectedArtist.value = artist
    }

    fun closeArtist() {
        _selectedArtist.value = null
    }

    fun openPlaylist(playlist: Playlist) {
        _selectedPlaylist.value = playlist
    }

    fun closePlaylist() {
        _selectedPlaylist.value = null
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = ""
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
        _isSortDialogOpen.value = false
    }

    fun setNowPlayingExpanded(expanded: Boolean) {
        _isNowPlayingExpanded.value = expanded
    }

    fun openPlayerOptions() {
        _isPlayerOptionsOpen.value = true
    }

    fun closePlayerOptions() {
        _isPlayerOptionsOpen.value = false
    }

    fun openQueueSheet() {
        _isQueueSheetOpen.value = true
    }

    fun closeQueueSheet() {
        _isQueueSheetOpen.value = false
    }

    fun openAddToPlaylist(song: Song) {
        _songForAddToPlaylist.value = song
        _isAddToPlaylistDialogOpen.value = true
    }

    fun closeAddToPlaylist() {
        _songForAddToPlaylist.value = null
        _isAddToPlaylistDialogOpen.value = false
    }

    fun openCreatePlaylistDialog() {
        _isCreatePlaylistDialogOpen.value = true
    }

    fun closeCreatePlaylistDialog() {
        _isCreatePlaylistDialogOpen.value = false
    }

    fun openSleepTimerDialog() {
        _isSleepTimerDialogOpen.value = true
    }

    fun closeSleepTimerDialog() {
        _isSleepTimerDialogOpen.value = false
    }

    fun openEqualizerDialog() {
        _isEqualizerDialogOpen.value = true
    }

    fun closeEqualizerDialog() {
        _isEqualizerDialogOpen.value = false
    }

    fun openSortDialog() {
        _isSortDialogOpen.value = true
    }

    fun closeSortDialog() {
        _isSortDialogOpen.value = false
    }

    fun openSongInfo(song: Song) {
        _songForInfo.value = song
        _isSongInfoDialogOpen.value = true
    }

    fun closeSongInfo() {
        _songForInfo.value = null
        _isSongInfoDialogOpen.value = false
    }

    fun openDeleteSongDialog(song: Song) {
        if (currentSong.value?.id == song.id) {
            showToast("This song is currently in the player. Play another song before removing it.")
            return
        }
        _songForDelete.value = song
        _isDeleteSongDialogOpen.value = true
    }

    fun closeDeleteSongDialog() {
        _songForDelete.value = null
        _isDeleteSongDialogOpen.value = false
    }

    // Playback actions
    fun playSong(song: Song, contextQueue: List<Song> = emptyList(), userInitiated: Boolean = true) {
        if (userInitiated) {
            viewModelScope.launch {
                repository.incrementUserPlayCount(song.id)
            }
        }
        controller.playSong(song, contextQueue)
    }

    fun playQueueIndex(index: Int) {
        controller.playAtIndex(index)
    }

    fun togglePlayPause() {
        controller.togglePlayPause()
    }

    fun playNext() {
        controller.playNext()
    }

    fun playPrevious() {
        controller.playPrevious()
    }

    fun seekTo(positionMs: Long) {
        controller.seekTo(positionMs)
    }

    fun toggleShuffle() {
        controller.toggleShuffle()
    }

    fun toggleRepeat() {
        controller.toggleRepeat()
    }

    fun addToQueue(song: Song) {
        controller.addToQueue(song)
        showToast("Added to queue")
    }

    fun playNextInQueue(song: Song) {
        controller.playNextInQueue(song)
        showToast("Playing next")
    }

    fun removeFromQueue(index: Int) {
        if (queue.value.size <= 5) {
            showToast("At least 5 songs must remain in the queue.")
            return
        }
        val removed = controller.removeFromQueue(index)
        if (!removed) {
            showToast("At least 5 songs must remain in the queue.")
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song.id, song.isFavorite)
            if (song.isFavorite) {
                showToast("Removed from favorites")
            } else {
                showToast("Added to favorites")
            }
        }
    }

    fun createPlaylist(name: String, initialSong: Song? = null) {
        viewModelScope.launch {
            val id = repository.createPlaylist(name)
            initialSong?.let {
                repository.addSongToPlaylist(id, it.id)
            }
            closeCreatePlaylistDialog()
            showToast("Playlist created")
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
            closeAddToPlaylist()
            showToast("Song added to playlist")
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
            showToast("Song removed from playlist")
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            if (_selectedPlaylist.value?.id == playlistId) {
                _selectedPlaylist.value = null
            }
            showToast("Playlist deleted")
        }
    }

    fun removeSongFromApp(song: Song) {
        if (currentSong.value?.id == song.id) {
            showToast("This song is currently in the player. Play another song before removing it.")
            closeDeleteSongDialog()
            return
        }
        viewModelScope.launch {
            val success = repository.removeSongFromApp(song)
            if (success) {
                controller.removeSongFromQueueById(song.id)
                showToast("Removed from app: ${song.title}")
            } else {
                showToast("Remove failed")
            }
            closeDeleteSongDialog()
        }
    }

    fun permanentlyDeleteSong(song: Song) {
        if (currentSong.value?.id == song.id) {
            showToast("This song is currently in the player. Play another song before removing it.")
            closeDeleteSongDialog()
            return
        }
        viewModelScope.launch {
            val success = repository.permanentlyDeleteSong(getApplication(), song)
            if (success) {
                controller.removeSongFromQueueById(song.id)
                showToast("Permanently deleted: ${song.title}")
            } else {
                showToast("Delete failed")
            }
            closeDeleteSongDialog()
        }
    }

    fun setSleepTimer(minutes: Int) {
        if (minutes <= 0) return
        controller.setSleepTimer(minutes)
        closeSleepTimerDialog()
        val unit = if (minutes == 1) "minute" else "minutes"
        showToast("Sleep timer set for $minutes $unit")
    }

    fun cancelSleepTimer() {
        controller.cancelSleepTimer()
        closeSleepTimerDialog()
        showToast("Sleep timer turned off")
    }

    fun setEqualizerPreset(preset: String) {
        controller.setEqualizerPreset(preset)
        closeEqualizerDialog()
        showToast("Equalizer: $preset")
    }

    fun refreshMusicLibrary() {
        if (_isScanning.value) return
        _isScanning.value = true
        showToast("Scanning music...")
        repository.loadMusic { result ->
            _isScanning.value = false
            result.fold(
                onSuccess = { (newFound, _) ->
                    if (newFound > 0) {
                        showToast("$newFound new songs found")
                    } else {
                        showToast("No new songs found")
                    }
                },
                onFailure = {
                    showToast("Unable to scan music")
                }
            )
        }
    }

    // Metadata Search & Identification
    fun searchSongOnWeb(song: Song) {
        _songForMetadata.value = song
        _isMetadataSearchOpen.value = true
        _isSearchingMetadata.value = true
        _metadataSearchResult.value = null
        _metadataSearchError.value = null

        viewModelScope.launch {
            try {
                val result = metadataService.searchOnlineForSong(song)
                _isSearchingMetadata.value = false
                if (result != null) {
                    _metadataSearchResult.value = result
                } else {
                    _metadataSearchError.value = "No matching track found online."
                }
            } catch (e: Exception) {
                _isSearchingMetadata.value = false
                _metadataSearchError.value = "Unable to connect to search servers: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }

    fun retryMetadataSearch() {
        val song = _songForMetadata.value ?: return
        searchSongOnWeb(song)
    }

    fun closeMetadataSearch() {
        _isMetadataSearchOpen.value = false
        _isSearchingMetadata.value = false
        _metadataSearchResult.value = null
        _metadataSearchError.value = null
        _songForMetadata.value = null
    }

    fun applyOnlineMetadata(song: Song, result: OnlineSongMetadata, acceptedFields: Set<String>) {
        viewModelScope.launch {
            val title = if (acceptedFields.contains("title")) result.title else song.title
            val artist = if (acceptedFields.contains("artist")) result.artist else song.artist
            val album = if (acceptedFields.contains("album")) result.album else song.album
            val albumArtist = if (acceptedFields.contains("albumArtist")) result.albumArtist else song.albumArtist
            val genre = if (acceptedFields.contains("genre")) result.genre else song.genre
            val releaseYear = if (acceptedFields.contains("year")) result.releaseYear else song.releaseYear
            val trackNumber = if (acceptedFields.contains("trackNumber")) result.trackNumber else song.trackNumber
            val artworkUri = if (acceptedFields.contains("artwork")) result.artworkUrl else song.albumArtUri
            val lyrics = if (acceptedFields.contains("lyrics")) result.lyrics else song.lyrics
            val syncedLyrics = if (acceptedFields.contains("lyrics")) result.syncedLyrics else song.syncedLyrics

            val entity = SongMetadataEntity(
                songId = song.id,
                title = title,
                artist = artist,
                album = album,
                albumArtist = albumArtist,
                genre = genre,
                releaseYear = releaseYear,
                trackNumber = trackNumber,
                discNumber = result.discNumber,
                artworkUri = artworkUri,
                lyrics = lyrics,
                syncedLyrics = syncedLyrics,
                isrc = result.isrc ?: song.isrc,
                musicBrainzId = result.musicBrainzId ?: song.musicBrainzId,
                spotifyId = result.spotifyId ?: song.spotifyId,
                youtubeUrl = result.youtubeUrl ?: song.youtubeUrl,
                isIdentified = true,
                isManuallyEdited = false
            )

            repository.saveSongMetadata(entity)
            closeMetadataSearch()
            showToast("Metadata updated: $title")
        }
    }

    fun openIdentifyByLink(song: Song) {
        _songForMetadata.value = song
        _isIdentifyByLinkOpen.value = true
    }

    fun closeIdentifyByLink() {
        _isIdentifyByLinkOpen.value = false
        _isResolvingLink.value = false
    }

    fun resolveLinkForSong(url: String) {
        val song = _songForMetadata.value ?: return
        if (url.isBlank()) {
            showToast("Please enter a valid link")
            return
        }

        _isResolvingLink.value = true
        viewModelScope.launch {
            try {
                val resolved = metadataService.identifyUsingLink(url, song)
                _isResolvingLink.value = false
                if (resolved != null) {
                    closeIdentifyByLink()
                    _songForMetadata.value = song
                    _isMetadataSearchOpen.value = true
                    _metadataSearchResult.value = resolved
                } else {
                    showToast("Could not retrieve track information from link.")
                }
            } catch (e: Exception) {
                _isResolvingLink.value = false
                showToast("Error processing link: ${e.localizedMessage}")
            }
        }
    }

    fun openEditMetadata(song: Song) {
        _songForEditMetadata.value = song
        _isEditMetadataOpen.value = true
    }

    fun closeEditMetadata() {
        _isEditMetadataOpen.value = false
        _songForEditMetadata.value = null
    }

    fun saveManualMetadata(metadata: SongMetadataEntity) {
        viewModelScope.launch {
            repository.saveSongMetadata(metadata)
            closeEditMetadata()
            showToast("Metadata saved")
        }
    }

    fun resetMetadata(song: Song) {
        viewModelScope.launch {
            repository.deleteSongMetadata(song.id)
            closeEditMetadata()
            showToast("Metadata reset to original audio tags")
        }
    }

    fun openLyricsViewer(song: Song) {
        _songForLyrics.value = song
        _isLyricsViewerOpen.value = true
    }

    fun closeLyricsViewer() {
        _isLyricsViewerOpen.value = false
        _songForLyrics.value = null
    }

    override fun onCleared() {
        super.onCleared()
        controller.release()
    }
}
