package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.Song

@Composable
fun AlbumArtImage(song: Song?, modifier: Modifier = Modifier, cornerRadius: Dp = 12.dp, fallbackIconSize: Dp = 24.dp) {
    Artwork(song?.albumArtUri, song?.albumArtRes, song?.fallbackArtworkUri, song?.title ?: "Album", modifier, cornerRadius, fallbackIconSize)
}

@Composable
fun AlbumArtFromData(artRes: Int?, artUri: String?, modifier: Modifier = Modifier, cornerRadius: Dp = 12.dp, fallbackIconSize: Dp = 24.dp) {
    Artwork(artUri, artRes, null, "Album", modifier, cornerRadius, fallbackIconSize)
}

@Composable
private fun Artwork(uri: String?, resource: Int?, fallbackUri: String?, label: String, modifier: Modifier, radius: Dp, iconSize: Dp) {
    var failed by remember(uri) { mutableStateOf(false) }
    val data = if (failed) fallbackUri?.takeIf { it != uri } ?: resource else uri?.takeIf { it.isNotBlank() } ?: resource
    val context = LocalContext.current
    val request = remember(context, data) { ImageRequest.Builder(context).data(data).crossfade(180).build() }
    Box(modifier.clip(RoundedCornerShape(radius)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
        Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(iconSize))
        if (data != null) AsyncImage(model = request, contentDescription = "$label artwork", modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop, onError = { failed = true })
    }
}
