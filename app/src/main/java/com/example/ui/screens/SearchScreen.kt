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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.Song
import com.example.ui.components.SongListItem
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun SearchScreen(
    query: String,
    results: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onPlaySong: (Song, List<Song>) -> Unit,
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
            .testTag("search_screen")
    ) {
        // Search Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("search_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search songs, artists, albums...", color = TextSecondary) },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = AccentCyan)
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
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
                    .weight(1f)
                    .padding(end = 8.dp)
                    .testTag("global_search_input")
            )
        }

        if (query.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Search your offline music collection",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            }
        } else if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No songs matching \"$query\"",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(results, key = { it.id }) { song ->
                    val isCurrent = currentSong?.id == song.id
                    val isThisPlaying = isPlaying && isCurrent
                    SongListItem(
                        song = song,
                        isPlaying = isThisPlaying,
                        isCurrentSong = isCurrent,
                        onClick = { onPlaySong(song, results) },
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
