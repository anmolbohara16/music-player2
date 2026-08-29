package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Song
import com.example.ui.components.SongListItem
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun FavoritesScreen(
    favoriteSongs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("favorites_screen")
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = null,
                    tint = AccentPink,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Favorites",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
            Text(
                text = "${favoriteSongs.size} favorite songs saved offline",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        if (favoriteSongs.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (favoriteSongs.isNotEmpty()) {
                            onPlaySong(favoriteSongs.first(), favoriteSongs)
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
                    Text("Play Favorites")
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

        if (favoriteSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.FavoriteBorder,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No favorite songs yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap the heart icon on any song to add it here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(favoriteSongs, key = { it.id }) { song ->
                    val isCurrent = currentSong?.id == song.id
                    val isThisPlaying = isPlaying && isCurrent
                    SongListItem(
                        song = song,
                        isPlaying = isThisPlaying,
                        isCurrentSong = isCurrent,
                        onClick = { onPlaySong(song, favoriteSongs) },
                        onToggleFavorite = { onToggleFavorite(song) },
                        onAddToQueue = { onAddToQueue(song) },
                        onPlayNext = { onPlayNext(song) },
                        onAddToPlaylist = { onAddToPlaylist(song) },
                        onShowInfo = { onShowSongInfo(song) },
                        onDeleteSong = { onDeleteSong(song) }
                    )
                }
            }
        }
    }
}
