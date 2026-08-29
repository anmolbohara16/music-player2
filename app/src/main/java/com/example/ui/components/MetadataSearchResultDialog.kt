package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.metadata.model.MatchConfidence
import com.example.metadata.model.OnlineSongMetadata
import com.example.model.Song
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlassCardBorderSubtle
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun MetadataSearchResultDialog(
    song: Song,
    isSearching: Boolean,
    searchResult: OnlineSongMetadata?,
    searchError: String?,
    onApplyMetadata: (OnlineSongMetadata, selectedFields: Set<String>) -> Unit,
    onRetrySearch: () -> Unit,
    onOpenIdentifyByLink: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, GlassCardBorderSubtle, RoundedCornerShape(24.dp))
                .testTag("metadata_search_dialog"),
            color = DarkSurface,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(AccentPurple.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = AccentPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (song.isIdentified) "Search Web Again" else "Online Metadata Search",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Web identification & enrichment",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content state
                when {
                    isSearching -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = AccentCyan,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "Searching online databases...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Querying MusicBrainz, iTunes & lyrics providers",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    searchResult != null -> {
                        SearchResultContent(
                            currentSong = song,
                            onlineResult = searchResult,
                            onApply = onApplyMetadata,
                            onOpenIdentifyByLink = onOpenIdentifyByLink,
                            onDismiss = onDismiss
                        )
                    }

                    else -> {
                        // No match found
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(DarkElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "We couldn't confidently identify this song.",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = searchError ?: "No matching online record found. Try searching again or paste a link directly.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onRetrySearch,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                                ) {
                                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Try Again")
                                }

                                Button(
                                    onClick = onOpenIdentifyByLink,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                                ) {
                                    Icon(Icons.Rounded.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Use Link", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultContent(
    currentSong: Song,
    onlineResult: OnlineSongMetadata,
    onApply: (OnlineSongMetadata, Set<String>) -> Unit,
    onOpenIdentifyByLink: () -> Unit,
    onDismiss: () -> Unit
) {
    // Keep track of which fields the user wants to apply
    val selectedFields = remember(onlineResult) {
        mutableStateMapOf(
            "title" to true,
            "artist" to true,
            "album" to (onlineResult.album.isNotBlank()),
            "albumArtist" to (onlineResult.albumArtist.isNotBlank()),
            "genre" to (onlineResult.genre.isNotBlank()),
            "year" to (onlineResult.releaseYear.isNotBlank()),
            "trackNumber" to (onlineResult.trackNumber > 0),
            "artwork" to (onlineResult.artworkUrl != null),
            "lyrics" to (onlineResult.lyrics != null || onlineResult.syncedLyrics != null)
        )
    }

    val (confidenceColor, confidenceBg) = when (onlineResult.confidence) {
        MatchConfidence.HIGH -> Pair(AccentEmerald, AccentEmerald.copy(alpha = 0.15f))
        MatchConfidence.MEDIUM -> Pair(AccentGold, AccentGold.copy(alpha = 0.15f))
        MatchConfidence.LOW -> Pair(AccentPink, AccentPink.copy(alpha = 0.15f))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 500.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Confidence Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(confidenceBg)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (onlineResult.confidence) {
                        MatchConfidence.HIGH -> Icons.Rounded.CheckCircle
                        MatchConfidence.MEDIUM -> Icons.Rounded.Info
                        MatchConfidence.LOW -> Icons.Rounded.Warning
                    },
                    contentDescription = null,
                    tint = confidenceColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = onlineResult.confidence.label,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = confidenceColor
                    )
                    if (onlineResult.confidenceReason.isNotBlank()) {
                        Text(
                            text = onlineResult.confidenceReason,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Compare Artwork & Basic Info Preview Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkCard)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Online artwork preview
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkElevated)
                    .border(1.dp, GlassCardBorderSubtle, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!onlineResult.artworkUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = onlineResult.artworkUrl,
                        contentDescription = "Online Artwork",
                        modifier = Modifier.size(68.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = onlineResult.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = onlineResult.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AccentCyan,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (onlineResult.album.isNotBlank()) {
                    Text(
                        text = onlineResult.album + if (onlineResult.releaseYear.isNotBlank()) " (${onlineResult.releaseYear})" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Field Comparison",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Comparison Items
        ComparisonRow(
            fieldKey = "title",
            label = "Title",
            currentVal = currentSong.title,
            onlineVal = onlineResult.title,
            selected = selectedFields["title"] == true,
            onToggle = { selectedFields["title"] = !(selectedFields["title"] ?: true) }
        )

        ComparisonRow(
            fieldKey = "artist",
            label = "Artist",
            currentVal = currentSong.artist,
            onlineVal = onlineResult.artist,
            selected = selectedFields["artist"] == true,
            onToggle = { selectedFields["artist"] = !(selectedFields["artist"] ?: true) }
        )

        if (onlineResult.album.isNotBlank() || currentSong.album != "Unknown") {
            ComparisonRow(
                fieldKey = "album",
                label = "Album",
                currentVal = currentSong.album,
                onlineVal = onlineResult.album,
                selected = selectedFields["album"] == true,
                onToggle = { selectedFields["album"] = !(selectedFields["album"] ?: true) }
            )
        }

        if (onlineResult.genre.isNotBlank() || currentSong.genre != "Unknown") {
            ComparisonRow(
                fieldKey = "genre",
                label = "Genre",
                currentVal = currentSong.genre,
                onlineVal = onlineResult.genre,
                selected = selectedFields["genre"] == true,
                onToggle = { selectedFields["genre"] = !(selectedFields["genre"] ?: true) }
            )
        }

        if (onlineResult.releaseYear.isNotBlank()) {
            ComparisonRow(
                fieldKey = "year",
                label = "Year",
                currentVal = currentSong.releaseYear ?: "Not set",
                onlineVal = onlineResult.releaseYear,
                selected = selectedFields["year"] == true,
                onToggle = { selectedFields["year"] = !(selectedFields["year"] ?: true) }
            )
        }

        if (onlineResult.artworkUrl != null) {
            ComparisonRow(
                fieldKey = "artwork",
                label = "Artwork",
                currentVal = if (currentSong.albumArtUri != null) "Local Artwork" else "No Artwork",
                onlineVal = "High-Res Artwork Found",
                selected = selectedFields["artwork"] == true,
                onToggle = { selectedFields["artwork"] = !(selectedFields["artwork"] ?: true) }
            )
        }

        if (onlineResult.lyrics != null || onlineResult.syncedLyrics != null) {
            val lyricsType = if (onlineResult.syncedLyrics != null) "Synced & Plain Lyrics" else "Plain Lyrics"
            ComparisonRow(
                fieldKey = "lyrics",
                label = "Lyrics",
                currentVal = if (currentSong.lyrics != null) "Existing Lyrics" else "None",
                onlineVal = lyricsType,
                selected = selectedFields["lyrics"] == true,
                onToggle = { selectedFields["lyrics"] = !(selectedFields["lyrics"] ?: true) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons
        Button(
            onClick = {
                val accepted = selectedFields.filterValues { it }.keys
                onApply(onlineResult, accepted)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("apply_metadata_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
        ) {
            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Use This Information", fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onOpenIdentifyByLink,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Icon(Icons.Rounded.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Identify with Link", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Text("Cancel", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ComparisonRow(
    fieldKey: String,
    label: String,
    currentVal: String,
    onlineVal: String,
    selected: Boolean,
    onToggle: () -> Unit
) {
    val isDifferent = currentVal.trim().lowercase() != onlineVal.trim().lowercase()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) DarkCard else DarkElevated.copy(alpha = 0.5f))
            .clickable(onClick = onToggle)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (selected) AccentPurple else DarkElevated)
                .border(1.dp, if (selected) AccentPurple else TextTertiary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )
                if (isDifferent) {
                    Text(
                        text = "Updated",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = AccentCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            if (isDifferent) {
                Text(
                    text = "Current: $currentVal",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Online: $onlineVal",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (selected) AccentCyan else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = onlineVal,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
