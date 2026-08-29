package com.example.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Playlist
import com.example.model.Song
import com.example.model.SortOption
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicRepository(
    private val context: Context,
    private val database: MusicDatabase,
    private val scope: CoroutineScope
) {
    private val dao = database.musicDao()

    private val _rawSongs = MutableStateFlow<List<Song>>(emptyList())
    val rawSongs: StateFlow<List<Song>> = _rawSongs.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val favoriteIds: Flow<List<Long>> = dao.getAllFavoriteIds()
    val excludedIds: Flow<List<Long>> = dao.getAllExcludedSongIds()
    val userPlayCounts: Flow<Map<Long, Int>> = dao.getAllUserPlayCounts().map { list ->
        list.associate { it.songId to it.playCount }
    }
    val songMetadataMap: Flow<Map<Long, SongMetadataEntity>> = dao.getAllSongMetadata().map { list ->
        list.associateBy { it.songId }
    }

    // Combined songs with favorite flag, play counts, enriched metadata, and filtering excluded/deleted songs
    val songs: StateFlow<List<Song>> = combine(
        _rawSongs,
        favoriteIds,
        excludedIds,
        userPlayCounts,
        songMetadataMap
    ) { list, favIds, exclIds, playCountMap, metaMap ->
        val favSet = favIds.toSet()
        val exclSet = exclIds.toSet()
        list.filterNot { exclSet.contains(it.id) }.map { song ->
            val meta = metaMap[song.id]
            if (meta != null) {
                song.copy(
                    title = meta.title?.takeIf { it.isNotBlank() } ?: song.title,
                    artist = meta.artist?.takeIf { it.isNotBlank() } ?: song.artist,
                    album = meta.album?.takeIf { it.isNotBlank() } ?: song.album,
                    albumArtist = meta.albumArtist ?: song.albumArtist,
                    genre = meta.genre?.takeIf { it.isNotBlank() } ?: song.genre,
                    releaseYear = meta.releaseYear ?: song.releaseYear,
                    trackNumber = meta.trackNumber ?: song.trackNumber,
                    discNumber = meta.discNumber ?: song.discNumber,
                    albumArtUri = meta.artworkUri ?: song.albumArtUri,
                    lyrics = meta.lyrics ?: song.lyrics,
                    syncedLyrics = meta.syncedLyrics ?: song.syncedLyrics,
                    isrc = meta.isrc ?: song.isrc,
                    musicBrainzId = meta.musicBrainzId ?: song.musicBrainzId,
                    spotifyId = meta.spotifyId ?: song.spotifyId,
                    youtubeUrl = meta.youtubeUrl ?: song.youtubeUrl,
                    isIdentified = meta.isIdentified,
                    isManuallyEdited = meta.isManuallyEdited,
                    isFavorite = favSet.contains(song.id),
                    playCount = playCountMap[song.id] ?: 0
                )
            } else {
                song.copy(
                    isFavorite = favSet.contains(song.id),
                    playCount = playCountMap[song.id] ?: 0
                )
            }
        }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val favoriteSongs: Flow<List<Song>> = songs.map { songList ->
        songList.filter { it.isFavorite }
    }

    val playlists: Flow<List<Playlist>> = dao.getAllPlaylists().map { entities ->
        entities.map { entity ->
            Playlist(
                id = entity.id,
                name = entity.name,
                createdAt = entity.createdAt
            )
        }
    }

    val recentlyPlayed: Flow<List<Song>> = combine(
        dao.getRecentlyPlayedHistory(20),
        songs
    ) { historyList, allSongs ->
        val map = allSongs.associateBy { it.id }
        historyList.mapNotNull { history ->
            map[history.songId]
        }
    }

    val recentlyAdded: Flow<List<Song>> = songs.map { list ->
        list.sortedByDescending { it.dateAdded }.take(15)
    }

    val albums: Flow<List<Album>> = songs.map { songList ->
        songList.groupBy { it.album to it.artist }.map { (key, songsInAlbum) ->
            Album(
                name = key.first,
                artist = key.second,
                songCount = songsInAlbum.size,
                albumArtUri = songsInAlbum.firstOrNull { it.albumArtUri != null }?.albumArtUri,
                albumArtRes = songsInAlbum.firstOrNull { it.albumArtRes != null }?.albumArtRes,
                songs = songsInAlbum.sortedBy { it.trackNumber }
            )
        }.sortedBy { it.name.lowercase() }
    }

    val artists: Flow<List<Artist>> = songs.map { songList ->
        songList.groupBy { it.artist }.map { (artistName, songsByArtist) ->
            val albumCount = songsByArtist.map { it.album }.distinct().size
            Artist(
                name = artistName,
                songCount = songsByArtist.size,
                albumCount = albumCount,
                songs = songsByArtist.sortedBy { it.title.lowercase() }
            )
        }.sortedBy { it.name.lowercase() }
    }

    init {
        loadMusic()
    }

    fun loadMusic(onComplete: ((Result<Pair<Int, Int>>) -> Unit)? = null) {
        scope.launch {
            _isLoading.value = true
            try {
                val (newFound, total) = withContext(Dispatchers.IO) {
                    val previousIds = _rawSongs.value.map { it.id }.toSet()
                    val scanned = AudioScanner.scanDeviceAudio(context)
                    val samples = SampleAudioProvider.getSampleSongs(context)

                    // Get excluded songs from Room DB
                    val excludedEntities = try {
                        dao.getExcludedSongEntitiesSync()
                    } catch (e: Exception) {
                        emptyList()
                    }
                    val excludedIds = excludedEntities.map { it.songId }.toSet()
                    val excludedUris = excludedEntities.mapNotNull { it.songUri.takeIf { u -> u.isNotBlank() } }.toSet()
                    val excludedTitles = excludedEntities.mapNotNull {
                        if (it.songTitle.isNotBlank()) "${it.songTitle.lowercase().trim()}|${it.songArtist.lowercase().trim()}" else null
                    }.toSet()

                    // Combine scanned with sample songs, avoiding duplicates and excluded items
                    val combinedAll = if (scanned.isEmpty()) {
                        samples
                    } else {
                        val sampleTitleSet = scanned.map { it.title.lowercase().trim() }.toSet()
                        val uniqueSamples = samples.filterNot { sampleTitleSet.contains(it.title.lowercase().trim()) }
                        scanned + uniqueSamples
                    }

                    val nonExcluded = combinedAll.filterNot { song ->
                        excludedIds.contains(song.id) ||
                        excludedUris.contains(song.uri) ||
                        excludedTitles.contains("${song.title.lowercase().trim()}|${song.artist.lowercase().trim()}")
                    }

                    _rawSongs.value = nonExcluded
                    ensureDefaultPlaylists()

                    val newCount = if (previousIds.isEmpty()) 0 else nonExcluded.count { !previousIds.contains(it.id) }
                    Pair(newCount, nonExcluded.size)
                }
                _isLoading.value = false
                onComplete?.invoke(Result.success(Pair(newFound, total)))
            } catch (e: Exception) {
                Log.e("MusicRepository", "Failed to load/scan music library", e)
                _isLoading.value = false
                onComplete?.invoke(Result.failure(e))
            }
        }
    }

    suspend fun removeSongFromApp(song: Song): Boolean = withContext(Dispatchers.IO) {
        try {
            dao.insertExcludedSong(
                ExcludedSongEntity(
                    songId = song.id,
                    songUri = song.uri,
                    songTitle = song.title.lowercase().trim(),
                    songArtist = song.artist.lowercase().trim()
                )
            )
            _rawSongs.value = _rawSongs.value.filterNot { it.id == song.id }
            true
        } catch (e: Exception) {
            Log.e("MusicRepository", "Failed to remove song from app: ${song.title}", e)
            false
        }
    }

    suspend fun permanentlyDeleteSong(context: Context, song: Song): Boolean = withContext(Dispatchers.IO) {
        try {
            var fileDeleted = false
            val uri = Uri.parse(song.uri)

            // 1. Try deleting via ContentResolver if content URI
            if (song.uri.startsWith("content://")) {
                try {
                    val rows = context.contentResolver.delete(uri, null, null)
                    if (rows > 0) {
                        fileDeleted = true
                    }
                } catch (se: SecurityException) {
                    Log.w("MusicRepository", "SecurityException deleting content uri, falling back to file/exclusion", se)
                } catch (e: Exception) {
                    Log.w("MusicRepository", "Error deleting via ContentResolver", e)
                }
            }

            // 2. Try deleting via direct File path
            if (!fileDeleted) {
                try {
                    val file = if (song.uri.startsWith("file://")) {
                        File(uri.path ?: "")
                    } else if (song.uri.startsWith("/")) {
                        File(song.uri)
                    } else {
                        null
                    }
                    if (file != null && file.exists()) {
                        fileDeleted = file.delete()
                    }
                } catch (e: Exception) {
                    Log.w("MusicRepository", "Error deleting physical file", e)
                }
            }

            // Mark as excluded in DB and remove from favorites & active list
            dao.insertExcludedSong(
                ExcludedSongEntity(
                    songId = song.id,
                    songUri = song.uri,
                    songTitle = song.title.lowercase().trim(),
                    songArtist = song.artist.lowercase().trim()
                )
            )
            dao.removeFavorite(song.id)
            _rawSongs.value = _rawSongs.value.filterNot { it.id == song.id }
            true
        } catch (e: Exception) {
            Log.e("MusicRepository", "Permanently delete song failed for: ${song.title}", e)
            false
        }
    }

    private suspend fun ensureDefaultPlaylists() {
        val currentPlaylists = database.musicDao().getAllPlaylists()
        // Check if there are no playlists yet
        val existing = withContext(Dispatchers.IO) {
            database.musicDao().getRecentlyPlayedHistory(1)
        }
        // If default playlists are desired:
        // We'll let user create or we can create "Favorites" / "Chill Vibes"
    }

    suspend fun toggleFavorite(songId: Long, isFavoriteNow: Boolean) = withContext(Dispatchers.IO) {
        if (isFavoriteNow) {
            dao.removeFavorite(songId)
        } else {
            dao.addFavorite(FavoriteEntity(songId = songId))
        }
    }

    suspend fun incrementUserPlayCount(songId: Long) = withContext(Dispatchers.IO) {
        val current = dao.getUserPlayCount(songId) ?: 0
        dao.insertUserPlayCount(
            UserPlayCountEntity(
                songId = songId,
                playCount = current + 1,
                lastPlayedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun recordPlayback(songId: Long, positionMs: Long = 0L) = withContext(Dispatchers.IO) {
        val existing = dao.getHistoryForSong(songId)
        val playCount = (existing?.playCount ?: 0) + 1
        dao.recordPlayback(
            PlaybackHistoryEntity(
                songId = songId,
                playedAt = System.currentTimeMillis(),
                lastPositionMs = positionMs,
                playCount = playCount
            )
        )
    }

    suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        dao.insertPlaylist(PlaylistEntity(name = name))
    }

    suspend fun renamePlaylist(playlistId: Long, newName: String) = withContext(Dispatchers.IO) {
        dao.updatePlaylistName(playlistId, newName)
    }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        dao.deletePlaylist(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        dao.addSongToPlaylist(
            PlaylistSongCrossRef(
                playlistId = playlistId,
                songId = songId,
                orderIndex = System.currentTimeMillis().toInt()
            )
        )
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        dao.removeSongFromPlaylist(playlistId, songId)
    }

    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> {
        return combine(dao.getSongIdsForPlaylist(playlistId), songs) { songIds, allSongs ->
            val songMap = allSongs.associateBy { it.id }
            songIds.mapNotNull { id -> songMap[id] }
        }
    }

    fun sortSongs(list: List<Song>, sortOption: SortOption): List<Song> {
        return when (sortOption) {
            SortOption.TITLE_ASC -> list.sortedBy { it.title.lowercase() }
            SortOption.TITLE_DESC -> list.sortedByDescending { it.title.lowercase() }
            SortOption.ARTIST -> list.sortedBy { it.artist.lowercase() }
            SortOption.ALBUM -> list.sortedBy { it.album.lowercase() }
            SortOption.DURATION -> list.sortedByDescending { it.durationMs }
            SortOption.DATE_ADDED -> list.sortedByDescending { it.dateAdded }
            SortOption.MOST_PLAYED -> list.sortedWith(
                compareByDescending<Song> { it.playCount }
                    .thenByDescending { it.dateAdded }
                    .thenBy { it.title.lowercase() }
            )
        }
    }

    suspend fun savePlayerSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        dao.saveSetting(PlayerSettingEntity(key, value))
    }

    suspend fun getPlayerSetting(key: String): String? = withContext(Dispatchers.IO) {
        dao.getSetting(key)
    }

    // Metadata Management
    suspend fun saveSongMetadata(metadata: SongMetadataEntity) = withContext(Dispatchers.IO) {
        dao.insertSongMetadata(metadata)
    }

    suspend fun getSongMetadata(songId: Long): SongMetadataEntity? = withContext(Dispatchers.IO) {
        dao.getSongMetadata(songId)
    }

    suspend fun deleteSongMetadata(songId: Long) = withContext(Dispatchers.IO) {
        dao.deleteSongMetadata(songId)
    }
}
