package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.SongMetadataEntity
import com.example.model.Song
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlassCardBorderSubtle
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun EditMetadataDialog(
    song: Song,
    onSave: (SongMetadataEntity) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(song.title) }
    var artist by remember { mutableStateOf(if (song.artist != "Unknown" && song.artist != "<unknown>") song.artist else "") }
    var album by remember { mutableStateOf(if (song.album != "Unknown" && song.album != "<unknown>") song.album else "") }
    var albumArtist by remember { mutableStateOf(song.albumArtist ?: "") }
    var genre by remember { mutableStateOf(if (song.genre != "Unknown" && song.genre != "<unknown>") song.genre else "") }
    var releaseYear by remember { mutableStateOf(song.releaseYear ?: "") }
    var trackNumber by remember { mutableStateOf(if (song.trackNumber > 0) song.trackNumber.toString() else "") }
    var discNumber by remember { mutableStateOf(if (song.discNumber > 1) song.discNumber.toString() else "1") }
    var artworkUrl by remember { mutableStateOf(song.albumArtUri ?: "") }
    var lyrics by remember { mutableStateOf(song.lyrics ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, GlassCardBorderSubtle, RoundedCornerShape(24.dp))
                .testTag("edit_metadata_dialog"),
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
                                .background(AccentCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Edit Metadata",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Saved locally on device",
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

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable form fields
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetadataTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "Song Title",
                        testTag = "edit_meta_title"
                    )

                    MetadataTextField(
                        value = artist,
                        onValueChange = { artist = it },
                        label = "Artist",
                        testTag = "edit_meta_artist"
                    )

                    MetadataTextField(
                        value = album,
                        onValueChange = { album = it },
                        label = "Album",
                        testTag = "edit_meta_album"
                    )

                    MetadataTextField(
                        value = albumArtist,
                        onValueChange = { albumArtist = it },
                        label = "Album Artist (Optional)"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetadataTextField(
                            value = genre,
                            onValueChange = { genre = it },
                            label = "Genre",
                            modifier = Modifier.weight(1.2f)
                        )

                        MetadataTextField(
                            value = releaseYear,
                            onValueChange = { releaseYear = it },
                            label = "Year",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(0.8f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetadataTextField(
                            value = trackNumber,
                            onValueChange = { trackNumber = it },
                            label = "Track #",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )

                        MetadataTextField(
                            value = discNumber,
                            onValueChange = { discNumber = it },
                            label = "Disc #",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    MetadataTextField(
                        value = artworkUrl,
                        onValueChange = { artworkUrl = it },
                        label = "Artwork Image URL (Optional)"
                    )

                    MetadataTextField(
                        value = lyrics,
                        onValueChange = { lyrics = it },
                        label = "Lyrics (Plain or Synced)",
                        singleLine = false,
                        maxLines = 6,
                        modifier = Modifier.heightIn(min = 90.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (song.isIdentified || song.isManuallyEdited) {
                        OutlinedButton(
                            onClick = onReset,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentPink)
                        ) {
                            Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset")
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val entity = SongMetadataEntity(
                                songId = song.id,
                                title = title.trim().ifBlank { null },
                                artist = artist.trim().ifBlank { null },
                                album = album.trim().ifBlank { null },
                                albumArtist = albumArtist.trim().ifBlank { null },
                                genre = genre.trim().ifBlank { null },
                                releaseYear = releaseYear.trim().ifBlank { null },
                                trackNumber = trackNumber.toIntOrNull(),
                                discNumber = discNumber.toIntOrNull() ?: 1,
                                artworkUri = artworkUrl.trim().ifBlank { null },
                                lyrics = lyrics.trim().ifBlank { null },
                                syncedLyrics = song.syncedLyrics,
                                isrc = song.isrc,
                                musicBrainzId = song.musicBrainzId,
                                spotifyId = song.spotifyId,
                                youtubeUrl = song.youtubeUrl,
                                isIdentified = song.isIdentified,
                                isManuallyEdited = true
                            )
                            onSave(entity)
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("save_metadata_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                    ) {
                        Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Changes", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    testTag: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentCyan,
            unfocusedBorderColor = DarkElevated,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = AccentCyan,
            focusedContainerColor = DarkCard,
            unfocusedContainerColor = DarkCard
        ),
        modifier = modifier
            .fillMaxWidth()
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
    )
}
