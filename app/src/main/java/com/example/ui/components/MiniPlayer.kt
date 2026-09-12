package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.Song
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DividerColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MiniPlayer(currentSong: Song?, isPlaying: Boolean, currentPositionMs: Long, durationMs: Long,
    onExpandNowPlaying: () -> Unit, onTogglePlayPause: () -> Unit, onPlayNext: () -> Unit,
    onToggleFavorite: (Song) -> Unit, modifier: Modifier = Modifier) {
    AnimatedVisibility(currentSong != null, enter = slideInVertically { it }, exit = slideOutVertically { it }, modifier = modifier) {
        val song = currentSong ?: return@AnimatedVisibility
        val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
        Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).testTag("mini_player_bar")) {
            HorizontalDivider(color = DividerColor)
            BoxWithConstraints(Modifier.fillMaxWidth().height(68.dp).clickable { onExpandNowPlaying() }) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AlbumArtImage(song, Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)), cornerRadius = 10.dp, fallbackIconSize = 20.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(song.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(song.artist, style = MaterialTheme.typography.labelMedium, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(onClick = onTogglePlayPause, modifier = Modifier.size(48.dp).testTag("mini_player_play_pause_button")) {
                        Icon(if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, if (isPlaying) "Pause" else "Play", tint = AccentPurple, modifier = Modifier.size(28.dp))
                    }
                    if (this@BoxWithConstraints.maxWidth >= 360.dp) IconButton(onClick = onPlayNext, modifier = Modifier.size(48.dp).testTag("mini_player_next_button")) {
                        Icon(Icons.Rounded.SkipNext, "Next song", tint = TextPrimary, modifier = Modifier.size(26.dp))
                    }
                }
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(2.dp).align(Alignment.BottomCenter), color = AccentPurple, trackColor = DividerColor)
            }
        }
    }
}
