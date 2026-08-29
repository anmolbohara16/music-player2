package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Album
import com.example.model.Song
import com.example.ui.components.AlbumArtFromData
import com.example.ui.components.AlbumGridItem
import com.example.ui.components.SongListItem
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun AlbumsScreen(
    albums: List<Album>,
    onOpenAlbum: (Album) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("albums_screen")
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Albums",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = "${albums.size} albums in your library",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        if (albums.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Album,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No albums found",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(albums) { album ->
                    AlbumGridItem(
                        album = album,
                        onClick = { onOpenAlbum(album) }
                    )
                }
            }
        }
    }
}

@Composable
fun AlbumDetailScreen(
    album: Album,
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onPlaySong: (Song, List<Song>) -> Unit,
    onShuffleAll: () -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onShowSongInfo: (Song) -> Unit,
    onDeleteSong: (Song) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val albumSongs = songs.filter { it.album == album.name }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("album_detail_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Top Bar with Back button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("album_detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Album Details",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
        }

        // Hero Album Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .shadow(16.dp, RoundedCornerShape(24.dp))
                ) {
                    AlbumArtFromData(
                        artRes = album.albumArtRes,
                        artUri = album.albumArtUri,
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = 24.dp,
                        fallbackIconSize = 64.dp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = album.name,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${album.artist} • ${albumSongs.size} tracks",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AccentCyan
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Play / Shuffle Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (albumSongs.isNotEmpty()) {
                                onPlaySong(albumSongs.first(), albumSongs)
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
                        Text("Play Album")
                    }

                    Button(
                        onClick = {
                            val shuffled = albumSongs.shuffled()
                            shuffled.firstOrNull()?.let { onPlaySong(it, shuffled) }
                        },
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

        // Track List
        items(albumSongs) { song ->
            val isCurrent = currentSong?.id == song.id
            val isThisPlaying = isPlaying && isCurrent
            SongListItem(
                song = song,
                isPlaying = isThisPlaying,
                isCurrentSong = isCurrent,
                onClick = { onPlaySong(song, albumSongs) },
                onToggleFavorite = { onToggleFavorite(song) },
                onAddToQueue = { onAddToQueue(song) },
                onPlayNext = { onPlayNext(song) },
                onAddToPlaylist = { onAddToPlaylist(song) },
                onShowInfo = { onShowSongInfo(song) },
                onDeleteSong = { onDeleteSong(song) },
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
