package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Song
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.CreatePlaylistDialog
import com.example.ui.components.DeleteSongDialog
import com.example.ui.components.EqualizerDialog
import com.example.ui.components.MiniPlayer
import com.example.ui.components.NowPlayingSheet
import com.example.ui.components.PlayerOptionsBottomSheet
import com.example.ui.components.QueueSheet
import com.example.ui.components.SleepTimerDialog
import com.example.ui.components.SongInfoDialog
import com.example.ui.components.SortOptionDialog
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlassActiveNav
import com.example.ui.theme.GlassBottomNavBg
import com.example.ui.theme.GlassCardBorderSubtle
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.MusicViewModel

data class NavItem(
    val tab: MainTab,
    val icon: ImageVector,
    val label: String
)

@Composable
fun MainScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val allSongs by viewModel.allSongs.collectAsStateWithLifecycle()
    val sortedSongs by viewModel.sortedSongs.collectAsStateWithLifecycle()
    val favoriteSongs by viewModel.favoriteSongs.collectAsStateWithLifecycle()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsStateWithLifecycle()
    val recentlyAdded by viewModel.recentlyAdded.collectAsStateWithLifecycle()
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val playlistSongs by viewModel.playlistSongs.collectAsStateWithLifecycle()

    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.currentPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val queue by viewModel.queue.collectAsStateWithLifecycle()
    val currentIndex by viewModel.currentIndex.collectAsStateWithLifecycle()
    val isShuffle by viewModel.isShuffle.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val sleepTimerSecondsLeft by viewModel.sleepTimerSecondsLeft.collectAsStateWithLifecycle()
    val equalizerPreset by viewModel.equalizerPreset.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()

    val selectedAlbum by viewModel.selectedAlbum.collectAsStateWithLifecycle()
    val selectedArtist by viewModel.selectedArtist.collectAsStateWithLifecycle()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsStateWithLifecycle()
    val isNowPlayingExpanded by viewModel.isNowPlayingExpanded.collectAsStateWithLifecycle()
    val isPlayerOptionsOpen by viewModel.isPlayerOptionsOpen.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val isQueueSheetOpen by viewModel.isQueueSheetOpen.collectAsStateWithLifecycle()
    val isAddToPlaylistDialogOpen by viewModel.isAddToPlaylistDialogOpen.collectAsStateWithLifecycle()
    val songForAddToPlaylist by viewModel.songForAddToPlaylist.collectAsStateWithLifecycle()
    val isCreatePlaylistDialogOpen by viewModel.isCreatePlaylistDialogOpen.collectAsStateWithLifecycle()
    val isSleepTimerDialogOpen by viewModel.isSleepTimerDialogOpen.collectAsStateWithLifecycle()
    val isEqualizerDialogOpen by viewModel.isEqualizerDialogOpen.collectAsStateWithLifecycle()
    val isSortDialogOpen by viewModel.isSortDialogOpen.collectAsStateWithLifecycle()
    val isSongInfoDialogOpen by viewModel.isSongInfoDialogOpen.collectAsStateWithLifecycle()
    val songForInfo by viewModel.songForInfo.collectAsStateWithLifecycle()
    val isDeleteSongDialogOpen by viewModel.isDeleteSongDialogOpen.collectAsStateWithLifecycle()
    val songForDelete by viewModel.songForDelete.collectAsStateWithLifecycle()
    val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResultsSongs.collectAsStateWithLifecycle()

    // Handle System Back Button
    BackHandler(
        enabled = isNowPlayingExpanded || isSearchActive || selectedAlbum != null || selectedArtist != null || selectedPlaylist != null || activeTab != MainTab.SONGS
    ) {
        when {
            isNowPlayingExpanded -> viewModel.setNowPlayingExpanded(false)
            isSearchActive -> viewModel.setSearchActive(false)
            selectedAlbum != null -> viewModel.closeAlbum()
            selectedArtist != null -> viewModel.closeArtist()
            selectedPlaylist != null -> viewModel.closePlaylist()
            activeTab != MainTab.SONGS -> viewModel.setActiveTab(MainTab.SONGS)
        }
    }

    val navItems = listOf(
        NavItem(MainTab.HOME, Icons.Rounded.Home, "Home"),
        NavItem(MainTab.SONGS, Icons.Rounded.MusicNote, "Songs"),
        NavItem(MainTab.ALBUMS, Icons.Rounded.Album, "Albums"),
        NavItem(MainTab.ARTISTS, Icons.Rounded.Person, "Artists"),
        NavItem(MainTab.PLAYLISTS, Icons.AutoMirrored.Rounded.QueueMusic, "Playlists"),
        NavItem(MainTab.FAVORITES, Icons.Rounded.Favorite, "Favorites")
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                // Mini Player docked right above bottom navigation bar
                if (!isNowPlayingExpanded) {
                    MiniPlayer(
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        currentPositionMs = currentPositionMs,
                        durationMs = durationMs,
                        onExpandNowPlaying = { viewModel.setNowPlayingExpanded(true) },
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onPlayNext = { viewModel.playNext() },
                        onToggleFavorite = { viewModel.toggleFavorite(it) }
                    )
                }

                // Frosted Glass Bottom Navigation Bar
                NavigationBar(
                    containerColor = GlassBottomNavBg,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bottom_nav_bar")
                ) {
                    navItems.forEach { item ->
                        val selected = activeTab == item.tab && !isSearchActive && selectedAlbum == null && selectedArtist == null && selectedPlaylist == null
                        NavigationBarItem(
                            selected = selected,
                            onClick = { viewModel.setActiveTab(item.tab) },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AccentPurple,
                                selectedTextColor = AccentPurple,
                                unselectedIconColor = TextTertiary,
                                unselectedTextColor = TextTertiary,
                                indicatorColor = GlassActiveNav
                            ),
                            modifier = Modifier.testTag("nav_item_${item.tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main content depending on active views
            when {
                isSearchActive -> {
                    SearchScreen(
                        query = searchQuery,
                        results = searchResults,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onQueryChange = { viewModel.setSearchQuery(it) },
                        onBack = { viewModel.setSearchActive(false) },
                        onPlaySong = { song, list -> viewModel.playSong(song, list) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onAddToQueue = { viewModel.addToQueue(it) },
                        onPlayNext = { viewModel.playNextInQueue(it) },
                        onAddToPlaylist = { viewModel.openAddToPlaylist(it) },
                        onShowSongInfo = { viewModel.openSongInfo(it) },
                        onDeleteSong = { viewModel.openDeleteSongDialog(it) }
                    )
                }
                selectedAlbum != null -> {
                    AlbumDetailScreen(
                        album = selectedAlbum!!,
                        songs = allSongs,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onBack = { viewModel.closeAlbum() },
                        onPlaySong = { song, list -> viewModel.playSong(song, list, userInitiated = true) },
                        onShuffleAll = {
                            val albumSongs = allSongs.filter { it.album == selectedAlbum!!.name }.shuffled()
                            albumSongs.firstOrNull()?.let { viewModel.playSong(it, albumSongs, userInitiated = false) }
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onAddToQueue = { viewModel.addToQueue(it) },
                        onPlayNext = { viewModel.playNextInQueue(it) },
                        onAddToPlaylist = { viewModel.openAddToPlaylist(it) },
                        onShowSongInfo = { viewModel.openSongInfo(it) },
                        onDeleteSong = { viewModel.openDeleteSongDialog(it) }
                    )
                }
                selectedArtist != null -> {
                    ArtistDetailScreen(
                        artist = selectedArtist!!,
                        songs = allSongs,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onBack = { viewModel.closeArtist() },
                        onPlaySong = { song, list -> viewModel.playSong(song, list, userInitiated = true) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onAddToQueue = { viewModel.addToQueue(it) },
                        onPlayNext = { viewModel.playNextInQueue(it) },
                        onAddToPlaylist = { viewModel.openAddToPlaylist(it) },
                        onShowSongInfo = { viewModel.openSongInfo(it) },
                        onDeleteSong = { viewModel.openDeleteSongDialog(it) }
                    )
                }
                selectedPlaylist != null -> {
                    PlaylistDetailScreen(
                        playlist = selectedPlaylist!!,
                        songs = playlistSongs,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onBack = { viewModel.closePlaylist() },
                        onPlaySong = { song, list -> viewModel.playSong(song, list, userInitiated = true) },
                        onShuffleAll = {
                            val shuffled = playlistSongs.shuffled()
                            shuffled.firstOrNull()?.let { viewModel.playSong(it, shuffled, userInitiated = false) }
                        },
                        onDeletePlaylist = { viewModel.deletePlaylist(selectedPlaylist!!.id) },
                        onRemoveSong = { viewModel.removeSongFromPlaylist(selectedPlaylist!!.id, it.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onAddToQueue = { viewModel.addToQueue(it) },
                        onPlayNext = { viewModel.playNextInQueue(it) },
                        onAddToPlaylist = { viewModel.openAddToPlaylist(it) },
                        onShowSongInfo = { viewModel.openSongInfo(it) }
                    )
                }
                else -> {
                    when (activeTab) {
                        MainTab.HOME -> {
                            HomeScreen(
                                allSongs = allSongs,
                                recentlyPlayed = recentlyPlayed,
                                recentlyAdded = recentlyAdded,
                                favoriteSongs = favoriteSongs,
                                playlists = playlists,
                                currentSong = currentSong,
                                isPlaying = isPlaying,
                                onPlaySong = { song, list -> viewModel.playSong(song, list, userInitiated = true) },
                                onShuffleAll = {
                                    val shuffled = allSongs.shuffled()
                                    shuffled.firstOrNull()?.let { viewModel.playSong(it, shuffled, userInitiated = false) }
                                },
                                onOpenSearch = { viewModel.setSearchActive(true) },
                                onOpenPlaylist = { viewModel.openPlaylist(it) },
                                onCreatePlaylist = { viewModel.openCreatePlaylistDialog() },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onAddToQueue = { viewModel.addToQueue(it) },
                                onPlayNext = { viewModel.playNextInQueue(it) },
                                onAddToPlaylist = { viewModel.openAddToPlaylist(it) },
                                onShowSongInfo = { viewModel.openSongInfo(it) },
                                onDeleteSong = { viewModel.openDeleteSongDialog(it) }
                            )
                        }
                        MainTab.SONGS -> {
                            SongsScreen(
                                songs = sortedSongs,
                                currentSong = currentSong,
                                isPlaying = isPlaying,
                                currentSortOption = sortOption,
                                isScanning = isScanning,
                                onPlaySong = { song, list -> viewModel.playSong(song, list, userInitiated = true) },
                                onPlayAll = {
                                    sortedSongs.firstOrNull()?.let { viewModel.playSong(it, sortedSongs, userInitiated = false) }
                                },
                                onShuffleAll = {
                                    val shuffled = sortedSongs.shuffled()
                                    shuffled.firstOrNull()?.let { viewModel.playSong(it, shuffled, userInitiated = false) }
                                },
                                onOpenSortDialog = { viewModel.openSortDialog() },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onAddToQueue = { viewModel.addToQueue(it) },
                                onPlayNext = { viewModel.playNextInQueue(it) },
                                onAddToPlaylist = { viewModel.openAddToPlaylist(it) },
                                onShowSongInfo = { viewModel.openSongInfo(it) },
                                onDeleteSong = { viewModel.openDeleteSongDialog(it) },
                                onRescanLibrary = { viewModel.refreshMusicLibrary() }
                            )
                        }
                        MainTab.ALBUMS -> {
                            AlbumsScreen(
                                albums = albums,
                                onOpenAlbum = { viewModel.openAlbum(it) }
                            )
                        }
                        MainTab.ARTISTS -> {
                            ArtistsScreen(
                                artists = artists,
                                onOpenArtist = { viewModel.openArtist(it) }
                            )
                        }
                        MainTab.PLAYLISTS -> {
                            PlaylistsScreen(
                                playlists = playlists,
                                onOpenPlaylist = { viewModel.openPlaylist(it) },
                                onCreatePlaylist = { viewModel.openCreatePlaylistDialog() }
                            )
                        }
                        MainTab.FAVORITES -> {
                            FavoritesScreen(
                                favoriteSongs = favoriteSongs,
                                currentSong = currentSong,
                                isPlaying = isPlaying,
                                onPlaySong = { song, list -> viewModel.playSong(song, list, userInitiated = true) },
                                onShuffleAll = {
                                    val shuffled = favoriteSongs.shuffled()
                                    shuffled.firstOrNull()?.let { viewModel.playSong(it, shuffled, userInitiated = false) }
                                },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onAddToQueue = { viewModel.addToQueue(it) },
                                onPlayNext = { viewModel.playNextInQueue(it) },
                                onAddToPlaylist = { viewModel.openAddToPlaylist(it) },
                                onShowSongInfo = { viewModel.openSongInfo(it) },
                                onDeleteSong = { viewModel.openDeleteSongDialog(it) }
                            )
                        }
                    }
                }
            }

            // Fullscreen Now Playing Overlay
            AnimatedVisibility(
                visible = isNowPlayingExpanded && currentSong != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                NowPlayingSheet(
                    song = currentSong,
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    isShuffle = isShuffle,
                    repeatMode = repeatMode,
                    onClose = { viewModel.setNowPlayingExpanded(false) },
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onPlayNext = { viewModel.playNext() },
                    onPlayPrevious = { viewModel.playPrevious() },
                    onSeekTo = { viewModel.seekTo(it) },
                    onToggleShuffle = { viewModel.toggleShuffle() },
                    onToggleRepeat = { viewModel.toggleRepeat() },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onOpenOptions = { viewModel.openPlayerOptions() }
                )
            }
        }
    }

    // Modal bottom sheets & Dialogs
    if (isPlayerOptionsOpen && currentSong != null) {
        PlayerOptionsBottomSheet(
            song = currentSong!!,
            isFavorite = currentSong!!.isFavorite,
            sleepTimerSecondsLeft = sleepTimerSecondsLeft,
            equalizerPreset = equalizerPreset,
            queueCount = queue.size,
            onDismiss = { viewModel.closePlayerOptions() },
            onOpenSongInfo = { viewModel.openSongInfo(currentSong!!) },
            onOpenQueue = { viewModel.openQueueSheet() },
            onOpenAddToPlaylist = { viewModel.openAddToPlaylist(currentSong!!) },
            onToggleFavorite = { viewModel.toggleFavorite(currentSong!!) },
            onOpenSleepTimer = { viewModel.openSleepTimerDialog() },
            onOpenEqualizer = { viewModel.openEqualizerDialog() }
        )
    }

    if (isQueueSheetOpen) {
        QueueSheet(
            queue = queue,
            currentIndex = currentIndex,
            isPlaying = isPlaying,
            onDismiss = { viewModel.closeQueueSheet() },
            onSelectIndex = { viewModel.playQueueIndex(it) },
            onRemoveFromQueue = { viewModel.removeFromQueue(it) },
            onShuffleQueue = { viewModel.toggleShuffle() }
        )
    }

    if (isAddToPlaylistDialogOpen && songForAddToPlaylist != null) {
        AddToPlaylistDialog(
            song = songForAddToPlaylist,
            playlists = playlists,
            onDismiss = { viewModel.closeAddToPlaylist() },
            onSelectPlaylist = { playlist ->
                viewModel.addSongToPlaylist(playlist.id, songForAddToPlaylist!!.id)
            },
            onCreateNewPlaylist = {
                viewModel.closeAddToPlaylist()
                viewModel.openCreatePlaylistDialog()
            }
        )
    }

    if (isCreatePlaylistDialogOpen) {
        CreatePlaylistDialog(
            onDismiss = { viewModel.closeCreatePlaylistDialog() },
            onCreate = { name ->
                viewModel.createPlaylist(name, songForAddToPlaylist)
            }
        )
    }

    if (isSleepTimerDialogOpen) {
        SleepTimerDialog(
            sleepTimerSecondsLeft = sleepTimerSecondsLeft,
            onDismiss = { viewModel.closeSleepTimerDialog() },
            onSetTimerMinutes = { viewModel.setSleepTimer(it) },
            onCancelTimer = { viewModel.cancelSleepTimer() }
        )
    }

    if (isEqualizerDialogOpen) {
        EqualizerDialog(
            currentPreset = equalizerPreset,
            onDismiss = { viewModel.closeEqualizerDialog() },
            onSelectPreset = { viewModel.setEqualizerPreset(it) }
        )
    }

    if (isSortDialogOpen) {
        SortOptionDialog(
            currentSort = sortOption,
            onDismiss = { viewModel.closeSortDialog() },
            onSelectSort = { viewModel.setSortOption(it) }
        )
    }

    if (isSongInfoDialogOpen && songForInfo != null) {
        SongInfoDialog(
            song = songForInfo,
            onDismiss = { viewModel.closeSongInfo() }
        )
    }

    if (isDeleteSongDialogOpen && songForDelete != null) {
        DeleteSongDialog(
            song = songForDelete!!,
            onDismiss = { viewModel.closeDeleteSongDialog() },
            onRemoveFromApp = { viewModel.removeSongFromApp(songForDelete!!) },
            onPermanentlyDelete = { viewModel.permanentlyDeleteSong(songForDelete!!) }
        )
    }
}
