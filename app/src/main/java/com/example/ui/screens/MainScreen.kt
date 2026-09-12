package com.example.ui.screens

import com.example.ui.components.*
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
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsStateWithLifecycle()
    val isNowPlayingExpanded by viewModel.isNowPlayingExpanded.collectAsStateWithLifecycle()
    val isPlayerOptionsOpen by viewModel.isPlayerOptionsOpen.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
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

    val isMetadataSearchOpen by viewModel.isMetadataSearchOpen.collectAsStateWithLifecycle()
    val isSearchingMetadata by viewModel.isSearchingMetadata.collectAsStateWithLifecycle()
    val metadataSearchResult by viewModel.metadataSearchResult.collectAsStateWithLifecycle()
    val metadataSearchError by viewModel.metadataSearchError.collectAsStateWithLifecycle()
    val songForMetadata by viewModel.songForMetadata.collectAsStateWithLifecycle()
    val isIdentifyByLinkOpen by viewModel.isIdentifyByLinkOpen.collectAsStateWithLifecycle()
    val isResolvingLink by viewModel.isResolvingLink.collectAsStateWithLifecycle()
    val isEditMetadataOpen by viewModel.isEditMetadataOpen.collectAsStateWithLifecycle()
    val songForEditMetadata by viewModel.songForEditMetadata.collectAsStateWithLifecycle()
    val isLyricsViewerOpen by viewModel.isLyricsViewerOpen.collectAsStateWithLifecycle()
    val songForLyrics by viewModel.songForLyrics.collectAsStateWithLifecycle()
    val metadataCandidates by viewModel.metadataCandidates.collectAsStateWithLifecycle()
    val isSavingMetadata by viewModel.isSavingMetadata.collectAsStateWithLifecycle()
    val librarySearch by viewModel.librarySearch.collectAsStateWithLifecycle()
    val isLibrarySearchOpen by viewModel.isLibrarySearchOpen.collectAsStateWithLifecycle()
    val excludedSongs by viewModel.excludedSongs.collectAsStateWithLifecycle()

    // Handle System Back Button
    BackHandler(
        enabled = isNowPlayingExpanded || isSearchActive || isSettingsOpen || selectedAlbum != null || selectedArtist != null || selectedPlaylist != null || activeTab != MainTab.SONGS
    ) {
        when {
            isNowPlayingExpanded -> viewModel.setNowPlayingExpanded(false)
            isSearchActive -> viewModel.setSearchActive(false)
            isSettingsOpen -> viewModel.closeSettings()
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
        NavItem(MainTab.PROFILE, Icons.AutoMirrored.Rounded.QueueMusic, "Library")
    )

    Box(Modifier.fillMaxSize()) {
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
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Medium else androidx.compose.ui.text.font.FontWeight.Normal),
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
                isSettingsOpen -> {
                    SettingsScreen(
                        equalizerPreset = equalizerPreset,
                        sleepTimerSecondsLeft = sleepTimerSecondsLeft,
                        isScanning = isScanning,
                        isBatchSearching = librarySearch.running,
                        songCount = allSongs.size,
                        onBack = viewModel::closeSettings,
                        onOpenEqualizer = viewModel::openEqualizerDialog,
                        onOpenSleepTimer = viewModel::openSleepTimerDialog,
                        onRescanLibrary = viewModel::refreshMusicLibrary,
                        onOpenWebSearch = viewModel::openLibrarySearch
                    )
                }
                isSearchActive -> {
                    SearchScreen(
                        query = searchQuery,
                        results = searchResults,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onQueryChange = { viewModel.setSearchQuery(it) },
                        onOpenAlbum = { viewModel.setSearchActive(false); viewModel.openAlbum(it) },
                        onOpenArtist = { viewModel.setSearchActive(false); viewModel.openArtist(it) },
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
                            val albumSongs = allSongs.filter { it.album == selectedAlbum!!.name && it.artist == selectedAlbum!!.artist }.shuffled()
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
                        MainTab.PROFILE -> {
                            ProfileScreen(allSongs, albums, artists, playlists, favoriteSongs, recentlyPlayed,
                                excludedSongs, currentSong, isPlaying, librarySearch.running, isScanning,
                                onPlaySong = { song, list -> viewModel.playSong(song, list) },
                                onNavigateToSongs = { viewModel.setActiveTab(MainTab.SONGS) },
                                onNavigateToFavorites = { viewModel.setActiveTab(MainTab.FAVORITES) },
                                onNavigateToPlaylists = { viewModel.setActiveTab(MainTab.PLAYLISTS) },
                                onOpenSettings = viewModel::openSettings,
                                onNavigateToMostPlayed = { viewModel.setSortOption(com.example.model.SortOption.MOST_PLAYED); viewModel.setActiveTab(MainTab.SONGS) },
                                onLibraryWebSearch = viewModel::openLibrarySearch,
                                onRescanLibrary = viewModel::refreshMusicLibrary,
                                onRestoreExcludedSong = viewModel::restoreExcludedSong,
                                onOpenEqualizer = viewModel::openEqualizerDialog,
                                onOpenSleepTimer = viewModel::openSleepTimerDialog,
                                onToggleFavorite = viewModel::toggleFavorite,
                                onAddToQueue = viewModel::addToQueue,
                                onPlayNext = viewModel::playNextInQueue,
                                onAddToPlaylist = viewModel::openAddToPlaylist,
                                onShowSongInfo = viewModel::openSongInfo)
                        }
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
                                onOpenProfile = { viewModel.setActiveTab(MainTab.PROFILE) },
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
                                isScanning = isScanning || isLoading,
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
                                onRescanLibrary = { viewModel.refreshMusicLibrary() },
                                onLibraryWebSearch = { viewModel.openLibrarySearch() },
                                onSearchWeb = viewModel::searchSongOnWeb,
                                onIdentifyUsingLink = viewModel::openIdentifyByLink,
                                onEditMetadata = viewModel::openEditMetadata,
                                onOpenLyrics = viewModel::openLyricsViewer
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
                    onOpenOptions = { viewModel.openPlayerOptions() },
                    onOpenQueue = viewModel::openQueueSheet,
                    onOpenLyrics = { currentSong?.let(viewModel::openLyricsViewer) }
                )
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
            onSearchWeb = { viewModel.searchSongOnWeb(currentSong!!) },
            onIdentifyUsingLink = { viewModel.openIdentifyByLink(currentSong!!) },
            onEditMetadata = { viewModel.openEditMetadata(currentSong!!) },
            onOpenLyrics = { viewModel.openLyricsViewer(currentSong!!) },
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
            onClearQueue = viewModel::clearQueue,
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
            onEdit = { val song = songForInfo!!; viewModel.closeSongInfo(); viewModel.openEditMetadata(song) },
            onIdentify = { val song = songForInfo!!; viewModel.closeSongInfo(); viewModel.openIdentifyByLink(song) },
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
    if (isLibrarySearchOpen && !isMetadataSearchOpen) {
        LibrarySearchDialog(librarySearch, viewModel::cancelLibrarySearch, viewModel::dismissLibrarySearch, viewModel::reviewBatchEntry,
            onRestart = viewModel::newLibrarySearch)
    }
    if (isMetadataSearchOpen && songForMetadata != null) {
        MetadataSearchResultDialog(songForMetadata!!, isSearchingMetadata, metadataSearchResult, metadataSearchError,
            onApplyMetadata = { result, fields -> viewModel.applyOnlineMetadata(songForMetadata!!, result, fields) },
            onRetrySearch = viewModel::retryMetadataSearch,
            onOpenIdentifyByLink = { val song = songForMetadata!!; viewModel.closeMetadataSearch(); viewModel.openIdentifyByLink(song) },
            onDismiss = viewModel::closeMetadataSearch, candidates = metadataCandidates,
            onSelectCandidate = viewModel::selectMetadataCandidate, isSaving = isSavingMetadata)
    }
    if (isIdentifyByLinkOpen && songForMetadata != null) {
        IdentifyByLinkDialog(songForMetadata!!, isResolvingLink, viewModel::resolveLinkForSong, viewModel::closeIdentifyByLink)
    }
    if (isEditMetadataOpen && songForEditMetadata != null) {
        EditMetadataDialog(songForEditMetadata!!, viewModel::saveManualMetadata,
            { viewModel.resetMetadata(songForEditMetadata!!) }, viewModel::closeEditMetadata)
    }
    if (isLyricsViewerOpen && songForLyrics != null) {
        LyricsViewerDialog(songForLyrics!!,
            { val song = songForLyrics!!; viewModel.closeLyricsViewer(); viewModel.searchSongOnWeb(song) },
            { val song = songForLyrics!!; viewModel.closeLyricsViewer(); viewModel.openEditMetadata(song) },
            viewModel::closeLyricsViewer,
            currentPositionMs = if (currentSong?.id == songForLyrics!!.id) currentPositionMs else 0L,
            isPlaying = currentSong?.id == songForLyrics!!.id && isPlaying)
    }

}
