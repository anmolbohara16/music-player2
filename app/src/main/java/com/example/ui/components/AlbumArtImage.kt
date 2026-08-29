package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.model.Song
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkElevated

@Composable
fun AlbumArtImage(
    song: Song?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    fallbackIconSize: Dp = 24.dp
) {
    val shape = RoundedCornerShape(cornerRadius)
    val fallbackColors = when ((song?.id ?: 0L) % 3) {
        0L -> listOf(AccentPurple, AccentCyan)
        1L -> listOf(AccentPink, AccentPurple)
        else -> listOf(AccentCyan, AccentPurple)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(DarkElevated),
        contentAlignment = Alignment.Center
    ) {
        if (song?.albumArtRes != null && song.albumArtRes != 0) {
            AsyncImage(
                model = song.albumArtRes,
                contentDescription = "${song.title} artwork",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (!song?.albumArtUri.isNullOrEmpty()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(song?.albumArtUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "${song?.title} artwork",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = {
                    FallbackArt(fallbackColors, fallbackIconSize)
                },
                loading = {
                    FallbackArt(fallbackColors, fallbackIconSize)
                }
            )
        } else {
            FallbackArt(fallbackColors, fallbackIconSize)
        }
    }
}

@Composable
fun AlbumArtFromData(
    artRes: Int?,
    artUri: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    fallbackIconSize: Dp = 24.dp
) {
    val shape = RoundedCornerShape(cornerRadius)
    val fallbackColors = listOf(AccentPurple, AccentCyan)

    Box(
        modifier = modifier
            .clip(shape)
            .background(DarkElevated),
        contentAlignment = Alignment.Center
    ) {
        if (artRes != null && artRes != 0) {
            AsyncImage(
                model = artRes,
                contentDescription = "Album art",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (!artUri.isNullOrEmpty()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(artUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Album art",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = {
                    FallbackArt(fallbackColors, fallbackIconSize)
                },
                loading = {
                    FallbackArt(fallbackColors, fallbackIconSize)
                }
            )
        } else {
            FallbackArt(fallbackColors, fallbackIconSize)
        }
    }
}

@Composable
private fun FallbackArt(colors: List<Color>, iconSize: Dp) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(colors)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.MusicNote,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(iconSize)
        )
    }
}
