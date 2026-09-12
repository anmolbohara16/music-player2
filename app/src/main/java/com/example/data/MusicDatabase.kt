package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavoriteEntity::class,
        PlaybackHistoryEntity::class,
        PlaybackEventEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        PlayerSettingEntity::class,
        ExcludedSongEntity::class,
        UserPlayCountEntity::class,
        SongMetadataEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao

    companion object {
        @Volatile
        private var INSTANCE: MusicDatabase? = null

        fun getDatabase(context: Context): MusicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicDatabase::class.java,
                    "music_player_db"
                ).addMigrations(MIGRATION_4_5).build()
                INSTANCE = instance
                instance
            }
        }

        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS playback_events (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        songId INTEGER NOT NULL,
                        playedAt INTEGER NOT NULL,
                        positionMs INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_playback_events_songId ON playback_events(songId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_playback_events_playedAt ON playback_events(playedAt)")
            }
        }
    }
}
