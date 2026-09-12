package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Song
import com.example.model.SortOption
import com.example.ui.components.SongListItem
import com.example.ui.components.AppScreenHeader
import com.example.ui.components.CollectionPlaybackActions
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleDarkText
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlassCardBackground
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlassCardBorderSubtle

@Composable
fun SongsScreen(
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    currentSortOption: SortOption,
    isScanning: Boolean = false,
    onPlaySong: (Song, List<Song>) -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onOpenSortDialog: () -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onShowSongInfo: (Song) -> Unit,
    onSearchWeb: ((Song) -> Unit)? = null,
    onIdentifyUsingLink: ((Song) -> Unit)? = null,
    onEditMetadata: ((Song) -> Unit)? = null,
    onOpenLyrics: ((Song) -> Unit)? = null,
    onDeleteSong: (Song) -> Unit,
    onRescanLibrary: () -> Unit,
    onLibraryWebSearch: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Whenever sort option changes, immediately reset scroll position to the top
    LaunchedEffect(currentSortOption) {
        listState.scrollToItem(0)
    }

    val filteredSongs by produceState(initialValue = songs, songs, searchQuery) {
        if (searchQuery.isNotBlank()) delay(180)
        value = withContext(Dispatchers.Default) {
            if (searchQuery.isBlank()) songs else songs.filter {
                it.title.contains(searchQuery, true) || it.artist.contains(searchQuery, true) || it.album.contains(searchQuery, true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("songs_screen")
    ) {
        // Header: "ALL SONGS" and track count only (sorting removed from subtitle)
        AppScreenHeader(
            title = "Your songs",
            subtitle = "${filteredSongs.size} tracks",
            trailing = {
                IconButton(onClick = onOpenSortDialog, modifier = Modifier.testTag("sort_songs_button")) {
                    Icon(Icons.Rounded.Sort, contentDescription = "Sort songs", tint = AccentPurple)
                }
            }
        )

        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onRescanLibrary, enabled = !isScanning, modifier = Modifier.weight(1f).testTag("rescan_library_button")) {
                Icon(Icons.Rounded.Refresh, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Rescan")
            }
            Button(onClick = onLibraryWebSearch, enabled = songs.isNotEmpty(), modifier = Modifier.weight(1f).testTag("library_web_search_button")) {
                Icon(Icons.Rounded.Search, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Web Search")
            }
        }

        // Active Scanning Overlay Banner
        AnimatedVisibility(
            visible = isScanning,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GlassCardBackground)
                    .border(1.dp, GlassCardBorderSubtle, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = AccentPurple
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Scanning your music...",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = TextPrimary
                )
            }
        }

        // Quick Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter songs or artists...", color = TextSecondary) },
            leadingIcon = {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = TextSecondary)
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkCard,
                unfocusedContainerColor = DarkCard,
                focusedBorderColor = AccentCyan,
                unfocusedBorderColor = DarkElevated,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = AccentCyan
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .testTag("songs_filter_input")
        )

        // Action Row: Play All & Shuffle
        if (filteredSongs.isNotEmpty()) {
            CollectionPlaybackActions(
                onPlay = { onPlaySong(filteredSongs.first(), filteredSongs) },
                onShuffle = { val shuffled = filteredSongs.shuffled(); shuffled.firstOrNull()?.let { onPlaySong(it, shuffled) } },
                playLabel = "Play all",
                modifier = Modifier.testTag("songs_playback_actions")
            )
        }

        // List
        if (filteredSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No songs matching \"$searchQuery\"" else "No music found",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(filteredSongs, key = { it.id }) { song ->
                    val isCurrent = currentSong?.id == song.id
                    val isThisPlaying = isPlaying && isCurrent
                    SongListItem(
                        song = song,
                        isPlaying = isThisPlaying,
                        isCurrentSong = isCurrent,
                        onClick = { onPlaySong(song, filteredSongs) },
                        onToggleFavorite = { onToggleFavorite(song) },
                        onAddToQueue = { onAddToQueue(song) },
                        onPlayNext = { onPlayNext(song) },
                        onAddToPlaylist = { onAddToPlaylist(song) },
                        onShowInfo = { onShowSongInfo(song) },
                        onSearchWeb = onSearchWeb?.let { { it(song) } },
                        onIdentifyUsingLink = onIdentifyUsingLink?.let { { it(song) } },
                        onEditMetadata = onEditMetadata?.let { { it(song) } },
                        onOpenLyrics = onOpenLyrics?.let { { it(song) } },
                        onDeleteSong = { onDeleteSong(song) }
                    )
                }
            }
        }
    }
}
