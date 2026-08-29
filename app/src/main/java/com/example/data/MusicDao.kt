package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    // Favorites
    @Query("SELECT songId FROM favorites ORDER BY addedAt DESC")
    fun getAllFavoriteIds(): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    fun isFavorite(songId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE songId = :songId")
    suspend fun removeFavorite(songId: Long)

    // Playback History
    @Query("SELECT * FROM playback_history ORDER BY playedAt DESC LIMIT :limit")
    fun getRecentlyPlayedHistory(limit: Int = 30): Flow<List<PlaybackHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordPlayback(history: PlaybackHistoryEntity)

    @Query("SELECT * FROM playback_history WHERE songId = :songId LIMIT 1")
    suspend fun getHistoryForSong(songId: Long): PlaybackHistoryEntity?

    @Query("DELETE FROM playback_history")
    suspend fun clearHistory()

    // Playlists
    @Query("SELECT * FROM playlists ORDER BY createdAt ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :name WHERE id = :playlistId")
    suspend fun updatePlaylistName(playlistId: Long, name: String)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    // Playlist Songs
    @Query("SELECT songId FROM playlist_songs WHERE playlistId = :playlistId ORDER BY orderIndex ASC")
    fun getSongIdsForPlaylist(playlistId: Long): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId")
    fun getPlaylistSongCount(playlistId: Long): Flow<Int>

    // Settings (Last song, last position, shuffle, repeat)
    @Query("SELECT value FROM player_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: PlayerSettingEntity)

    // Excluded / Removed Songs
    @Query("SELECT songId FROM excluded_songs")
    fun getAllExcludedSongIds(): Flow<List<Long>>

    @Query("SELECT songId FROM excluded_songs")
    suspend fun getExcludedSongIdsSync(): List<Long>

    @Query("SELECT * FROM excluded_songs")
    suspend fun getExcludedSongEntitiesSync(): List<ExcludedSongEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExcludedSong(excluded: ExcludedSongEntity)

    @Query("DELETE FROM excluded_songs WHERE songId = :songId")
    suspend fun removeExcludedSong(songId: Long)

    // Manual User Play Counts
    @Query("SELECT * FROM user_play_counts")
    fun getAllUserPlayCounts(): Flow<List<UserPlayCountEntity>>

    @Query("SELECT * FROM user_play_counts")
    suspend fun getAllUserPlayCountsSync(): List<UserPlayCountEntity>

    @Query("SELECT playCount FROM user_play_counts WHERE songId = :songId LIMIT 1")
    suspend fun getUserPlayCount(songId: Long): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPlayCount(entity: UserPlayCountEntity)

    // Enriched Song Metadata
    @Query("SELECT * FROM song_metadata")
    fun getAllSongMetadata(): Flow<List<SongMetadataEntity>>

    @Query("SELECT * FROM song_metadata")
    suspend fun getAllSongMetadataSync(): List<SongMetadataEntity>

    @Query("SELECT * FROM song_metadata WHERE songId = :songId LIMIT 1")
    suspend fun getSongMetadata(songId: Long): SongMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongMetadata(metadata: SongMetadataEntity)

    @Query("DELETE FROM song_metadata WHERE songId = :songId")
    suspend fun deleteSongMetadata(songId: Long)
}
