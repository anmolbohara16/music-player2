package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.RepeatMode
import com.example.model.Song
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TextSecondary

@Composable
fun NowPlayingSheet(
    song: Song?, isPlaying: Boolean, currentPositionMs: Long, durationMs: Long,
    isShuffle: Boolean, repeatMode: RepeatMode, onClose: () -> Unit, onTogglePlayPause: () -> Unit,
    onPlayNext: () -> Unit, onPlayPrevious: () -> Unit, onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit, onToggleRepeat: () -> Unit, onToggleFavorite: (Song) -> Unit,
    onOpenOptions: () -> Unit, onOpenQueue: () -> Unit = {}, onOpenLyrics: () -> Unit = {}, modifier: Modifier = Modifier
) {
    if (song == null) return
    Surface(modifier.fillMaxSize().testTag("now_playing_screen"), color = DarkBackground) {
        BoxWithConstraints(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp)) {
            val landscape = maxWidth > maxHeight
            val artworkSize = if (landscape) (maxHeight - 72.dp).coerceAtLeast(100.dp) else minOf(maxWidth - 24.dp, maxHeight * .43f)
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().height(52.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClose, Modifier.testTag("now_playing_close_button")) { Icon(Icons.Rounded.KeyboardArrowDown, "Collapse player") }
                    Text("NOW PLAYING", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    IconButton(onOpenOptions, Modifier.testTag("now_playing_more_button")) { Icon(Icons.Rounded.MoreVert, "More options") }
                }
                val controls: @Composable () -> Unit = {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(if (landscape) 2.dp else 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(song.title, style = MaterialTheme.typography.headlineSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(song.artist, style = MaterialTheme.typography.bodyLarge, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton({ onToggleFavorite(song) }, Modifier.testTag("now_playing_favorite_button")) {
                                Icon(if (song.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, "Favorite", tint = AccentPurple)
                            }
                        }
                        ModernPlayerSeekBar(currentPositionMs, durationMs, onSeekTo)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onToggleShuffle, Modifier.testTag("playback_shuffle_button").semantics { stateDescription = if (isShuffle) "On" else "Off" }) { Icon(Icons.Rounded.Shuffle, if (isShuffle) "Shuffle on" else "Shuffle off", tint = if (isShuffle) AccentPurple else TextSecondary) }
                            IconButton(onPlayPrevious, Modifier.testTag("playback_prev_button")) { Icon(Icons.Rounded.SkipPrevious, "Previous", Modifier.size(32.dp)) }
                            androidx.compose.material3.FilledIconButton(onTogglePlayPause, Modifier.size(if (landscape) 56.dp else 68.dp).testTag("playback_play_pause_fab")) {
                                Icon(if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, if (isPlaying) "Pause" else "Play", Modifier.size(36.dp))
                            }
                            IconButton(onPlayNext, Modifier.testTag("playback_next_button")) { Icon(Icons.Rounded.SkipNext, "Next", Modifier.size(32.dp)) }
                            IconButton(onToggleRepeat, Modifier.testTag("playback_repeat_button").semantics { stateDescription = repeatMode.name }) { Icon(if (repeatMode == RepeatMode.ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat, "Repeat ${repeatMode.name.lowercase()}", tint = if (repeatMode == RepeatMode.OFF) TextSecondary else AccentPurple) }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            TextButton(onOpenQueue) { Text("Queue") }
                            TextButton(onOpenLyrics) { Text("Lyrics") }
                        }
                    }
                }
                if (landscape) {
                    Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                        AlbumArtImage(song, Modifier.size(artworkSize).testTag("now_playing_album_art"), cornerRadius = 20.dp, fallbackIconSize = 64.dp)
                        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) { controls() }
                    }
                } else {
                    Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        AlbumArtImage(song, Modifier.size(artworkSize).testTag("now_playing_album_art"), cornerRadius = 20.dp, fallbackIconSize = 80.dp)
                        controls()
                    }
                }
            }
        }
    }
}

@Composable
fun ModernPlayerSeekBar(currentPositionMs: Long, durationMs: Long, onSeekTo: (Long) -> Unit, modifier: Modifier = Modifier) {
    val duration = durationMs.coerceAtLeast(1)
    var pending by remember { mutableStateOf<Float?>(null) }
    val progress = pending ?: (currentPositionMs.toFloat() / duration).coerceIn(0f, 1f)
    Column(modifier.fillMaxWidth()) {
        androidx.compose.material3.Slider(value = progress,
            onValueChange = { pending = it },
            onValueChangeFinished = { pending?.let { onSeekTo((it * duration).toLong()) }; pending = null },
            modifier = Modifier.fillMaxWidth().testTag("playback_seek_slider"))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatTime((progress * duration).toLong()), color = TextSecondary,
                style = MaterialTheme.typography.labelSmall, modifier = Modifier.testTag("playback_seek_time_current"))
            Text("−" + formatTime((duration - progress * duration).toLong().coerceAtLeast(0)), color = TextSecondary,
                style = MaterialTheme.typography.labelSmall, modifier = Modifier.testTag("playback_seek_time_total"))
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
