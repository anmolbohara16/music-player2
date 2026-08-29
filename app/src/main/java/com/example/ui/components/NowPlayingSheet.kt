package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RepeatMode
import com.example.model.Song
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleDarkText
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GlassBadgeBg
import com.example.ui.theme.GlassCardBackground
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassCardBorderSubtle
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingSheet(
    song: Song?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    onClose: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onOpenOptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (song == null) return

    val currentDuration = durationMs.coerceAtLeast(1L)

    val fabScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.04f else 1f,
        animationSpec = tween(300),
        label = "fab_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 35) {
                        onClose()
                    }
                }
            }
            .testTag("now_playing_screen"),
        color = DarkBackground
    ) {
        // Ambient background gradient glow
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                AccentPurple.copy(alpha = 0.18f),
                                DarkBackground.copy(alpha = 0.85f),
                                DarkBackground
                            )
                        )
                    )
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                val availableHeight = maxHeight

                // Dynamic responsive sizing for controls
                val playButtonSize = (availableHeight * 0.088f).coerceIn(58.dp, 68.dp)
                val playIconSize = (playButtonSize * 0.52f).coerceIn(30.dp, 36.dp)
                val skipButtonSize = (playButtonSize * 0.72f).coerceIn(42.dp, 48.dp)
                val skipIconSize = (skipButtonSize * 0.75f).coerceIn(30.dp, 36.dp)
                val modeButtonSize = (playButtonSize * 0.62f).coerceIn(38.dp, 44.dp)
                val modeIconSize = (modeButtonSize * 0.58f).coerceIn(22.dp, 26.dp)

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Action Bar with Dismiss Handle & More Options Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GlassCardBackground)
                                .border(1.dp, GlassCardBorderSubtle, CircleShape)
                                .testTag("now_playing_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "Collapse player",
                                tint = TextPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Text(
                            text = "NOW PLAYING",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 2.sp
                            ),
                            color = TextSecondary
                        )

                        IconButton(
                            onClick = onOpenOptions,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GlassCardBackground)
                                .border(1.dp, GlassCardBorderSubtle, CircleShape)
                                .testTag("now_playing_more_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "More Options",
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Hero Album Art in Center with Frosted Glass Frame - Responsive square inside available weight
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f, matchHeightConstraintsFirst = true)
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Soft pulsating ambient glow under artwork
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(0.85f)
                                    .shadow(
                                        elevation = 28.dp,
                                        shape = RoundedCornerShape(24.dp),
                                        ambientColor = AccentPurple.copy(alpha = 0.45f),
                                        spotColor = AccentPurple.copy(alpha = 0.60f)
                                    )
                            )

                            // Album Art container
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(24.dp))
                                    .border(1.dp, GlassCardBorder, RoundedCornerShape(24.dp))
                            ) {
                                AlbumArtImage(
                                    song = song,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(24.dp))
                                        .testTag("now_playing_album_art"),
                                    cornerRadius = 24.dp,
                                    fallbackIconSize = 80.dp
                                )
                            }

                            // Visualizer badge overlay at bottom right of art
                            if (isPlaying) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(14.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GlassBadgeBg)
                                        .border(1.dp, GlassCardBorderSubtle, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    WaveformVisualizer(
                                        isPlaying = true,
                                        barCount = 4,
                                        barHeight = 12.dp,
                                        barWidth = 3.dp,
                                        activeColor = AccentPurple
                                    )
                                }
                            }
                        }
                    }

                    // Track Information Section (Title, Artist, Format badge & Favorite)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = song.artist,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = AccentPurple,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Audio Format Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GlassBadgeBg)
                                .border(1.dp, GlassCardBorderSubtle, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = song.format,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = AccentEmerald
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Favorite Action Heart
                        IconButton(
                            onClick = { onToggleFavorite(song) },
                            modifier = Modifier
                                .size(42.dp)
                                .testTag("now_playing_favorite_button")
                        ) {
                            Icon(
                                imageVector = if (song.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = if (song.isFavorite) "Favorited" else "Favorite",
                                tint = if (song.isFavorite) AccentPink else TextSecondary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Progress / Seek Bar
                    ModernPlayerSeekBar(
                        currentPositionMs = currentPositionMs,
                        durationMs = currentDuration,
                        onSeekTo = onSeekTo,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Main Playback Controls: Shuffle, Previous, Responsive Play/Pause, Next, Repeat
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shuffle Button
                        IconButton(
                            onClick = onToggleShuffle,
                            modifier = Modifier
                                .size(modeButtonSize)
                                .testTag("playback_shuffle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (isShuffle) AccentPurple else TextTertiary,
                                modifier = Modifier.size(modeIconSize)
                            )
                        }

                        // Previous Track Button
                        IconButton(
                            onClick = onPlayPrevious,
                            modifier = Modifier
                                .size(skipButtonSize)
                                .testTag("playback_prev_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipPrevious,
                                contentDescription = "Previous Track",
                                tint = TextPrimary,
                                modifier = Modifier.size(skipIconSize)
                            )
                        }

                        // Large Responsive Play/Pause Button
                        Box(
                            modifier = Modifier
                                .scale(fabScale)
                                .size(playButtonSize)
                                .shadow(
                                    elevation = 16.dp,
                                    shape = CircleShape,
                                    ambientColor = AccentPurple.copy(alpha = 0.5f),
                                    spotColor = AccentPurple
                                )
                                .clip(CircleShape)
                                .background(AccentPurple)
                                .clickable { onTogglePlayPause() }
                                .testTag("playback_play_pause_fab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = AccentPurpleDarkText,
                                modifier = Modifier.size(playIconSize)
                            )
                        }

                        // Next Track Button
                        IconButton(
                            onClick = onPlayNext,
                            modifier = Modifier
                                .size(skipButtonSize)
                                .testTag("playback_next_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = "Next Track",
                                tint = TextPrimary,
                                modifier = Modifier.size(skipIconSize)
                            )
                        }

                        // Repeat Mode Button
                        IconButton(
                            onClick = onToggleRepeat,
                            modifier = Modifier
                                .size(modeButtonSize)
                                .testTag("playback_repeat_button")
                        ) {
                            val icon = if (repeatMode == RepeatMode.ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat
                            val tint = if (repeatMode != RepeatMode.OFF) AccentPurple else TextTertiary
                            Icon(
                                imageVector = icon,
                                contentDescription = "Repeat: $repeatMode",
                                tint = tint,
                                modifier = Modifier.size(modeIconSize)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Modern, thin, and elegant music player seek bar with a small circular thumb,
 * responsive dragging, and clear time indicators.
 */
@Composable
fun ModernPlayerSeekBar(
    currentPositionMs: Long,
    durationMs: Long,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDuration = durationMs.coerceAtLeast(1L)
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val actualProgress = (currentPositionMs.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
    val displayProgress = if (isDragging) dragProgress else actualProgress
    val displayTimeMs = (displayProgress * totalDuration).toLong()

    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) 1.25f else 1.0f,
        animationSpec = tween(150),
        label = "thumb_scale"
    )

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Touch-interactive Bar Area with 36dp height for easy touch and drag
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .testTag("playback_seek_slider")
                .pointerInput(totalDuration) {
                    detectTapGestures { offset ->
                        val newProgress = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onSeekTo((newProgress * totalDuration).toLong())
                    }
                }
                .pointerInput(totalDuration) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragProgress = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            isDragging = false
                            onSeekTo((dragProgress * totalDuration).toLong())
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            dragProgress = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            val widthPx = constraints.maxWidth.toFloat()

            // Inactive Track (Remaining Audio) - sleek frosted translucent track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0x38FFFFFF))
            )

            // Active Track (Played Audio) - vibrant lavender gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth(displayProgress)
                    .height(3.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFA58BFF),
                                AccentPurple,
                                Color(0xFFEADBFF)
                            )
                        )
                    )
            )

            // Circular Thumb Handle with frosted center and purple rim
            val thumbDiameter = 13.dp
            val thumbOffsetXDp = with(LocalDensity.current) {
                ((widthPx * displayProgress) - (thumbDiameter.toPx() / 2)).toDp()
            }.coerceIn(0.dp, with(LocalDensity.current) { (widthPx - thumbDiameter.toPx()).toDp() })

            Box(
                modifier = Modifier
                    .offset(x = thumbOffsetXDp)
                    .scale(thumbScale)
                    .size(thumbDiameter)
                    .shadow(
                        elevation = if (isDragging) 6.dp else 2.dp,
                        shape = CircleShape,
                        spotColor = AccentPurple,
                        ambientColor = Color.White
                    )
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, AccentPurple, CircleShape)
            )
        }

        // Time Indicators below seek bar: current position on the left, total duration on the right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTime(displayTimeMs),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.4.sp
                ),
                color = if (isDragging) AccentPurple else TextSecondary,
                modifier = Modifier.testTag("playback_seek_time_current")
            )
            Text(
                text = formatTime(totalDuration),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.4.sp
                ),
                color = TextTertiary,
                modifier = Modifier.testTag("playback_seek_time_total")
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
