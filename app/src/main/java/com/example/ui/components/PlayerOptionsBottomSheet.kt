package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.Song
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlassCardBorderSubtle
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerOptionsBottomSheet(
    song: Song,
    isFavorite: Boolean,
    sleepTimerSecondsLeft: Long?,
    equalizerPreset: String,
    queueCount: Int,
    onDismiss: () -> Unit,
    onSearchWeb: () -> Unit,
    onIdentifyUsingLink: () -> Unit,
    onEditMetadata: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenSongInfo: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenAddToPlaylist: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenEqualizer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(TextTertiary.copy(alpha = 0.5f))
            )
        },
        modifier = modifier.testTag("player_options_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Track Summary Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AlbumArtImage(
                    song = song,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, GlassCardBorderSubtle, RoundedCornerShape(12.dp)),
                    cornerRadius = 12.dp,
                    fallbackIconSize = 24.dp
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${song.artist} • ${song.album}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (song.isIdentified) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentEmerald.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Identified",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = AccentEmerald
                        )
                    }
                }
            }

            HorizontalDivider(
                color = DarkElevated,
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 1. Search on Web / Search Web Again
            PlayerOptionItem(
                icon = Icons.Rounded.AutoAwesome,
                iconTint = AccentPurple,
                title = if (song.isIdentified) "Search Web Again" else "Search on Web",
                subtitle = "Identify track & fetch album art, year, and lyrics",
                onClick = {
                    onDismiss()
                    onSearchWeb()
                },
                testTag = "option_search_web"
            )

            // 2. Identify using Link
            PlayerOptionItem(
                icon = Icons.Rounded.Link,
                iconTint = AccentCyan,
                title = "Identify using Link",
                subtitle = "Use a YouTube or Spotify track URL",
                onClick = {
                    onDismiss()
                    onIdentifyUsingLink()
                },
                testTag = "option_identify_link"
            )

            // 3. Edit Metadata
            PlayerOptionItem(
                icon = Icons.Rounded.Edit,
                iconTint = AccentGold,
                title = "Edit Metadata",
                subtitle = "Manually change title, artist, album & tags",
                onClick = {
                    onDismiss()
                    onEditMetadata()
                },
                testTag = "option_edit_metadata"
            )

            // 4. Lyrics
            PlayerOptionItem(
                icon = Icons.Rounded.Lyrics,
                iconTint = AccentPink,
                title = "Lyrics",
                subtitle = if (song.lyrics != null || song.syncedLyrics != null) "View saved lyrics" else "Check or search lyrics",
                onClick = {
                    onDismiss()
                    onOpenLyrics()
                },
                testTag = "option_lyrics"
            )

            // 5. Song Info
            PlayerOptionItem(
                icon = Icons.Rounded.Info,
                iconTint = AccentCyan,
                title = "Song Info",
                subtitle = "${song.format} • ${song.bitRate} • ${song.sampleRate}",
                onClick = {
                    onDismiss()
                    onOpenSongInfo()
                },
                testTag = "option_song_info"
            )

            // 6. Up Next / Queue
            PlayerOptionItem(
                icon = Icons.AutoMirrored.Rounded.QueueMusic,
                iconTint = AccentPurple,
                title = "Up Next / Queue",
                subtitle = "$queueCount tracks in queue",
                onClick = {
                    onDismiss()
                    onOpenQueue()
                },
                testTag = "option_queue"
            )

            // 7. Add to Playlist
            PlayerOptionItem(
                icon = Icons.Rounded.PlaylistAdd,
                iconTint = AccentPurple,
                title = "Add to Playlist",
                subtitle = "Save to custom collection",
                onClick = {
                    onDismiss()
                    onOpenAddToPlaylist()
                },
                testTag = "option_add_playlist"
            )

            // 8. Favorite Toggle
            PlayerOptionItem(
                icon = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                iconTint = if (isFavorite) AccentPink else TextSecondary,
                title = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                subtitle = if (isFavorite) "Currently in your favorites" else "Save to your favorites",
                onClick = {
                    onToggleFavorite()
                },
                testTag = "option_favorite"
            )

            // 9. Sleep Timer
            val timerSubtitle = if (sleepTimerSecondsLeft != null) {
                "${sleepTimerSecondsLeft / 60}m remaining"
            } else {
                "Off"
            }
            PlayerOptionItem(
                icon = Icons.Rounded.Bedtime,
                iconTint = if (sleepTimerSecondsLeft != null) AccentEmerald else TextSecondary,
                title = "Sleep Timer",
                subtitle = timerSubtitle,
                onClick = {
                    onDismiss()
                    onOpenSleepTimer()
                },
                testTag = "option_sleep_timer"
            )

            // 10. Equalizer
            PlayerOptionItem(
                icon = Icons.Rounded.Equalizer,
                iconTint = AccentPink,
                title = "Equalizer & Sound Effects",
                subtitle = "Preset: $equalizerPreset",
                onClick = {
                    onDismiss()
                    onOpenEqualizer()
                },
                testTag = "option_equalizer"
            )
        }
    }
}

@Composable
private fun PlayerOptionItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(DarkCard)
                .border(1.dp, GlassCardBorderSubtle, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = TextPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
