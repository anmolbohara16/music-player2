package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.Song
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun SongListItem(
    song: Song,
    isPlaying: Boolean,
    isCurrentSong: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToQueue: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onShowInfo: () -> Unit,
    onSearchWeb: (() -> Unit)? = null,
    onIdentifyUsingLink: (() -> Unit)? = null,
    onEditMetadata: (() -> Unit)? = null,
    onOpenLyrics: (() -> Unit)? = null,
    onDeleteSong: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }

    val backgroundColor = if (isCurrentSong) {
        DarkElevated.copy(alpha = 0.7f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("song_item_${song.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail with Playing Indicator Overlay
        Box(
            modifier = Modifier.size(50.dp),
            contentAlignment = Alignment.Center
        ) {
            AlbumArtImage(
                song = song,
                modifier = Modifier.size(50.dp),
                cornerRadius = 10.dp,
                fallbackIconSize = 22.dp
            )

            if (isCurrentSong) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPlaying) {
                        WaveformVisualizer(
                            isPlaying = true,
                            barCount = 3,
                            barHeight = 16.dp,
                            barWidth = 2.5.dp,
                            activeColor = AccentCyan
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Playing" else "Paused",
                            tint = AccentCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title and Artist / Album
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isCurrentSong) com.example.ui.theme.AccentPurple else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${song.artist} • ${song.formattedDuration}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (trailingContent != null) {
            trailingContent()
        } else {
            // Favorite Button
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(48.dp).semantics {
                    stateDescription = if (song.isFavorite) "Favorited" else "Not favorited"
                }
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = if (song.isFavorite) "Favorited" else "Favorite",
                    tint = if (song.isFavorite) AccentPink else TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // More Options Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "More options",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(DarkSurfaceVariant)
                ) {
                    // Search on Web
                    if (onSearchWeb != null) {
                        DropdownMenuItem(
                            text = { Text(if (song.isIdentified) "Search Web Again" else "Search on Web", color = TextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = AccentPurple)
                            },
                            onClick = {
                                showMenu = false
                                onSearchWeb()
                            }
                        )
                    }

                    // Identify using Link
                    if (onIdentifyUsingLink != null) {
                        DropdownMenuItem(
                            text = { Text("Identify using Link", color = TextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Link, contentDescription = null, tint = AccentCyan)
                            },
                            onClick = {
                                showMenu = false
                                onIdentifyUsingLink()
                            }
                        )
                    }

                    // Edit Metadata
                    if (onEditMetadata != null) {
                        DropdownMenuItem(
                            text = { Text("Edit Metadata", color = TextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Edit, contentDescription = null, tint = AccentGold)
                            },
                            onClick = {
                                showMenu = false
                                onEditMetadata()
                            }
                        )
                    }

                    // Lyrics
                    if (onOpenLyrics != null) {
                        DropdownMenuItem(
                            text = { Text("Lyrics", color = TextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Lyrics, contentDescription = null, tint = AccentPink)
                            },
                            onClick = {
                                showMenu = false
                                onOpenLyrics()
                            }
                        )
                    }

                    HorizontalDivider(color = DarkElevated, modifier = Modifier.padding(vertical = 4.dp))

                    DropdownMenuItem(
                        text = { Text("Play Next", color = TextPrimary) },
                        leadingIcon = {
                            Icon(Icons.Rounded.SkipNext, contentDescription = null, tint = AccentCyan)
                        },
                        onClick = {
                            showMenu = false
                            onPlayNext()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add to Queue", color = TextPrimary) },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Rounded.QueueMusic, contentDescription = null, tint = AccentPurple)
                        },
                        onClick = {
                            showMenu = false
                            onAddToQueue()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add to Playlist", color = TextPrimary) },
                        leadingIcon = {
                            Icon(Icons.Rounded.PlaylistAdd, contentDescription = null, tint = AccentPink)
                        },
                        onClick = {
                            showMenu = false
                            onAddToPlaylist()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Track Details", color = TextPrimary) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Info, contentDescription = null, tint = TextSecondary)
                        },
                        onClick = {
                            showMenu = false
                            onShowInfo()
                        }
                    )
                    if (onDeleteSong != null) {
                        HorizontalDivider(color = DarkElevated, modifier = Modifier.padding(vertical = 4.dp))
                        DropdownMenuItem(
                            text = { Text("Delete", color = AccentPink) },
                            leadingIcon = {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = AccentPink)
                            },
                            onClick = {
                                showMenu = false
                                onDeleteSong()
                            }
                        )
                    }
                }
            }
        }
    }
}
