package com.example

import androidx.compose.runtime.Composable
import androidx.compose.material3.Surface
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.model.*
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.*

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w393dp-h852dp-mdpi", sdk = [36])
class ScreenGalleryTest {
    @get:Rule val rule = createComposeRule()
    private val songs = listOf(Song(1, "Northern Lights", "The Evening Set", "Afterglow", 183000, "local", isFavorite = true, lyrics = "A little light in the evening"),
        Song(2, "Slow Sunday", "Mira", "Small Hours", 210000, "local"))
    private val album = Album("Afterglow", "The Evening Set", 1, songs = songs.take(1))
    private val artist = Artist("The Evening Set", 1, 1, songs.take(1))
    private val playlist = Playlist(1, "Evening favorites", 2)
    private fun capture(name: String, content: @Composable () -> Unit) {
        rule.setContent { MyApplicationTheme(darkTheme = false) { Surface { content() } } }
        rule.onRoot().captureRoboImage(filePath = "build/outputs/screenshots/$name.png")
    }
    @Test fun createPlaylistRenders() = capture("CreatePlaylistDialog") { CreatePlaylistDialog({}, {}) }
    @Test fun homeScreenRenders() = capture("HomeScreen") {
        HomeScreen(
            allSongs = songs,
            recentlyPlayed = songs,
            recentlyAdded = songs,
            favoriteSongs = songs,
            playlists = listOf(playlist),
            currentSong = songs.first(),
            isPlaying = false,
            onPlaySong = { _, _ -> },
            onShuffleAll = {},
            onOpenSearch = {},
            onOpenPlaylist = { _ -> },
            onCreatePlaylist = {},
            onToggleFavorite = { _ -> },
            onAddToQueue = { _ -> },
            onPlayNext = { _ -> },
            onAddToPlaylist = { _ -> },
            onShowSongInfo = { _ -> }
        )
    }
    @Test fun albumsScreenRenders() = capture("AlbumsScreen") {
        AlbumsScreen(
            albums = listOf(album),
            onOpenAlbum = { _ -> }
        )
    }
    @Test fun albumDetailScreenRenders() = capture("AlbumDetailScreen") {
        AlbumDetailScreen(
            album = album,
            songs = songs,
            currentSong = songs.first(),
            isPlaying = false,
            onBack = {},
            onPlaySong = { _, _ -> },
            onShuffleAll = {},
            onToggleFavorite = { _ -> },
            onAddToQueue = { _ -> },
            onPlayNext = { _ -> },
            onAddToPlaylist = { _ -> },
            onShowSongInfo = { _ -> }
        )
    }
    @Test fun artistsScreenRenders() = capture("ArtistsScreen") {
        ArtistsScreen(
            artists = listOf(artist),
            onOpenArtist = { _ -> }
        )
    }
    @Test fun artistDetailScreenRenders() = capture("ArtistDetailScreen") {
        ArtistDetailScreen(
            artist = artist,
            songs = songs,
            currentSong = songs.first(),
            isPlaying = false,
            onBack = {},
            onPlaySong = { _, _ -> },
            onToggleFavorite = { _ -> },
            onAddToQueue = { _ -> },
            onPlayNext = { _ -> },
            onAddToPlaylist = { _ -> },
            onShowSongInfo = { _ -> }
        )
    }
    @Test fun favoritesScreenRenders() = capture("FavoritesScreen") {
        FavoritesScreen(
            favoriteSongs = songs,
            currentSong = songs.first(),
            isPlaying = false,
            onPlaySong = { _, _ -> },
            onShuffleAll = {},
            onToggleFavorite = { _ -> },
            onAddToQueue = { _ -> },
            onPlayNext = { _ -> },
            onAddToPlaylist = { _ -> },
            onShowSongInfo = { _ -> }
        )
    }
    @Test fun playlistsScreenRenders() = capture("PlaylistsScreen") {
        PlaylistsScreen(
            playlists = listOf(playlist),
            onOpenPlaylist = { _ -> },
            onCreatePlaylist = {}
        )
    }
    @Test fun playlistDetailScreenRenders() = capture("PlaylistDetailScreen") {
        PlaylistDetailScreen(
            playlist = playlist,
            songs = songs,
            currentSong = songs.first(),
            isPlaying = false,
            onBack = {},
            onPlaySong = { _, _ -> },
            onShuffleAll = {},
            onDeletePlaylist = {},
            onRemoveSong = { _ -> },
            onToggleFavorite = { _ -> },
            onAddToQueue = { _ -> },
            onPlayNext = { _ -> },
            onAddToPlaylist = { _ -> },
            onShowSongInfo = { _ -> }
        )
    }
    @Test fun searchScreenRenders() = capture("SearchScreen") {
        SearchScreen(
            query = "Afterglow",
            results = songs,
            currentSong = songs.first(),
            isPlaying = false,
            onQueryChange = { _ -> },
            onBack = {},
            onPlaySong = { _, _ -> },
            onToggleFavorite = { _ -> },
            onAddToQueue = { _ -> },
            onPlayNext = { _ -> },
            onAddToPlaylist = { _ -> },
            onShowSongInfo = { _ -> }
        )
    }
    @Test fun profileScreenRenders() = capture("ProfileScreen") {
        ProfileScreen(
            allSongs = songs,
            albums = listOf(album),
            artists = listOf(artist),
            playlists = listOf(playlist),
            favoriteSongs = songs,
            recentlyPlayed = songs,
            excludedSongs = emptyList(),
            currentSong = songs.first(),
            isPlaying = false,
            isBatchSearching = false,
            isScanning = false,
            onPlaySong = { _, _ -> },
            onNavigateToSongs = {},
            onNavigateToFavorites = {},
            onNavigateToPlaylists = {},
            onNavigateToMostPlayed = {},
            onLibraryWebSearch = {},
            onRescanLibrary = {},
            onRestoreExcludedSong = { _ -> },
            onOpenEqualizer = {},
            onOpenSleepTimer = {},
            onToggleFavorite = { _ -> },
            onAddToQueue = { _ -> },
            onPlayNext = { _ -> },
            onAddToPlaylist = { _ -> },
            onShowSongInfo = { _ -> }
        )
    }
    @Test fun queueSheetRenders() = capture("QueueSheet") {
        QueueSheet(
            queue = songs,
            currentIndex = 0,
            isPlaying = false,
            onDismiss = {},
            onSelectIndex = { _ -> },
            onRemoveFromQueue = { _ -> },
            onShuffleQueue = {}
        )
    }
    @Test fun songInfoDialogRenders() = capture("SongInfoDialog") {
        SongInfoDialog(
            song = songs.first(),
            onDismiss = {}
        )
    }
    @Test fun identifyByLinkDialogRenders() = capture("IdentifyByLinkDialog") {
        IdentifyByLinkDialog(
            song = songs.first(),
            isResolving = false,
            onResolveLink = { _ -> },
            onDismiss = {}
        )
    }
    @Test fun lyricsViewerDialogRenders() = capture("LyricsViewerDialog") {
        LyricsViewerDialog(
            song = songs.first(),
            onSearchOnlineLyrics = {},
            onEditLyrics = {},
            onDismiss = {}
        )
    }
    @Test fun miniPlayerRenders() = capture("MiniPlayer") {
        MiniPlayer(
            currentSong = songs.first(),
            isPlaying = false,
            currentPositionMs = 30000L,
            durationMs = 183000L,
            onExpandNowPlaying = {},
            onTogglePlayPause = {},
            onPlayNext = {},
            onToggleFavorite = { _ -> }
        )
    }
}
