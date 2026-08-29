package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Song
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassCardBorderSubtle
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MiniPlayer(
    currentSong: Song?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onExpandNowPlaying: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onPlayNext: () -> Unit,
    onToggleFavorite: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = currentSong != null,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        if (currentSong == null) return@AnimatedVisibility

        val progress = if (durationMs > 0) {
            (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f

        val pillShape = RoundedCornerShape(28.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = pillShape,
                    ambientColor = Color.Black,
                    spotColor = AccentPurple.copy(alpha = 0.25f)
                )
                .clip(pillShape)
                .background(DarkElevated.copy(alpha = 0.94f))
                .border(1.dp, GlassCardBorder, pillShape)
                .clickable { onExpandNowPlaying() }
                .testTag("mini_player_bar")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album art thumbnail with subtle border
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, GlassCardBorderSubtle, RoundedCornerShape(16.dp))
                    ) {
                        AlbumArtImage(
                            song = currentSong,
                            modifier = Modifier.size(48.dp),
                            cornerRadius = 16.dp,
                            fallbackIconSize = 22.dp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title & Artist with sleek typography
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentSong.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (isPlaying) {
                                Spacer(modifier = Modifier.width(6.dp))
                                WaveformVisualizer(
                                    isPlaying = true,
                                    barCount = 3,
                                    barHeight = 12.dp,
                                    barWidth = 2.dp,
                                    activeColor = AccentPurple
                                )
                            }
                        }
                        Text(
                            text = currentSong.artist.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Favorite Button
                    IconButton(
                        onClick = { onToggleFavorite(currentSong) },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("mini_player_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (currentSong.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = if (currentSong.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (currentSong.isFavorite) AccentPink else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Play/Pause Button
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("mini_player_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = TextPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Next Button
                    IconButton(
                        onClick = onPlayNext,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("mini_player_next_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Next song",
                            tint = TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Frosted Glass Accent Progress Bar docked at bottom edge of pill
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = AccentPurple,
                    trackColor = Color.Transparent
                )
            }
        }
    }
}
