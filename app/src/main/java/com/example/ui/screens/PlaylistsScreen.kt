package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.Playlist
import com.example.model.Song
import com.example.ui.components.SongListItem
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleDarkText
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.GlassActiveNav
import com.example.ui.theme.GlassCardBackground
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    onOpenPlaylist: (Playlist) -> Unit,
    onCreatePlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("playlists_screen"),
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreatePlaylist,
                containerColor = AccentPurple,
                contentColor = AccentPurpleDarkText,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 70.dp)
                    .testTag("create_playlist_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Create Playlist")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Playlists",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "${playlists.size} custom playlists",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = onCreatePlaylist,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentPurple,
                        contentColor = AccentPurpleDarkText
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New", fontWeight = FontWeight.Bold)
                }
            }

            if (playlists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.QueueMusic,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No playlists created yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Create a playlist to organize your tracks",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextTertiary
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(playlists) { playlist ->
                        PlaylistItemRow(
                            playlist = playlist,
                            onClick = { onOpenPlaylist(playlist) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistItemRow(
    playlist: Playlist,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassCardBackground)
            .border(1.dp, GlassCardBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(14.dp)
            .testTag("playlist_row_${playlist.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(GlassActiveNav),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.QueueMusic,
                contentDescription = null,
                tint = AccentPurple,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Personal Playlist",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onPlaySong: (Song, List<Song>) -> Unit,
    onShuffleAll: () -> Unit,
    onDeletePlaylist: () -> Unit,
    onRemoveSong: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onShowSongInfo: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("playlist_detail_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Back Top Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("playlist_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Playlist",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                IconButton(
                    onClick = onDeletePlaylist,
                    modifier = Modifier.testTag("delete_playlist_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete Playlist",
                        tint = AccentPink
                    )
                }
            }
        }

        // Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(DarkElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.QueueMusic,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${songs.size} tracks",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AccentCyan
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (songs.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (songs.isNotEmpty()) {
                                    onPlaySong(songs.first(), songs)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentPurple,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play All")
                        }

                        Button(
                            onClick = onShuffleAll,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkElevated,
                                contentColor = AccentCyan
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Shuffle")
                        }
                    }
                }
            }
        }

        if (songs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "This playlist is empty.\nAdd tracks using the song options menu.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(songs) { song ->
                val isCurrent = currentSong?.id == song.id
                val isThisPlaying = isPlaying && isCurrent
                SongListItem(
                    song = song,
                    isPlaying = isThisPlaying,
                    isCurrentSong = isCurrent,
                    onClick = { onPlaySong(song, songs) },
                    onToggleFavorite = { onToggleFavorite(song) },
                    onAddToQueue = { onAddToQueue(song) },
                    onPlayNext = { onPlayNext(song) },
                    onAddToPlaylist = { onAddToPlaylist(song) },
                    onShowInfo = { onShowSongInfo(song) },
                    trailingContent = {
                        IconButton(
                            onClick = { onRemoveSong(song) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Remove from playlist",
                                tint = TextTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}
