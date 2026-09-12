package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.SongMetadataEntity
import com.example.model.Song

@Composable
fun EditMetadataDialog(song: Song, onSave: (SongMetadataEntity) -> Unit, onReset: () -> Unit, onDismiss: () -> Unit) {
    var title by remember(song.id) { mutableStateOf(song.title) }
    var artist by remember(song.id) { mutableStateOf(song.artist) }
    var album by remember(song.id) { mutableStateOf(song.album) }
    var albumArtist by remember(song.id) { mutableStateOf(song.albumArtist.orEmpty()) }
    var genre by remember(song.id) { mutableStateOf(song.genre) }
    var year by remember(song.id) { mutableStateOf(song.releaseYear.orEmpty()) }
    var track by remember(song.id) { mutableStateOf(song.trackNumber.takeIf { it > 0 }?.toString().orEmpty()) }
    var disc by remember(song.id) { mutableStateOf(song.discNumber.toString()) }
    var artwork by remember(song.id) { mutableStateOf(song.albumArtUri.orEmpty()) }
    var lyrics by remember(song.id) { mutableStateOf(song.lyrics.orEmpty()) }
    var synced by remember(song.id) { mutableStateOf(song.syncedLyrics.orEmpty()) }
    val validTrack = track.isBlank() || (track.toIntOrNull() ?: 0) > 0
    val validDisc = (disc.toIntOrNull() ?: 0) > 0
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.width((LocalConfiguration.current.screenWidthDp.dp - 40.dp).coerceAtMost(560.dp)).testTag("edit_metadata_dialog"),
        title = { Text("Edit Metadata") },
        text = {
            LazyColumn(
                Modifier.fillMaxWidth().heightIn(max = (LocalConfiguration.current.screenHeightDp * 0.68f).dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Text("Manual editing · saved in this app. Your audio file stays untouched.") }
                item { MetadataField("Title", title, { title = it }, tag = "edit_meta_title") }
                item { MetadataField("Artist", artist, { artist = it }, tag = "edit_meta_artist") }
                item { MetadataField("Album", album, { album = it }, tag = "edit_meta_album") }
                item { MetadataField("Album artist", albumArtist, { albumArtist = it }) }
                item { MetadataField("Genre", genre, { genre = it }) }
                item { MetadataField("Release year", year, { year = it }, number = true) }
                item { MetadataField("Track number", track, { track = it }, number = true, error = !validTrack) }
                item { MetadataField("Disc number", disc, { disc = it }, number = true, error = !validDisc) }
                item { MetadataField("Artwork URL or URI", artwork, { artwork = it }) }
                item { MetadataField("Plain lyrics", lyrics, { lyrics = it }, multiline = true) }
                item { MetadataField("Synced lyrics (LRC)", synced, { synced = it }, multiline = true) }
                if (song.isManuallyEdited || song.isIdentified) item {
                    TextButton(onClick = onReset) { Text("Reset to audio file tags", color = MaterialTheme.colorScheme.error) }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = title.isNotBlank() && validTrack && validDisc, modifier = Modifier.testTag("save_metadata_button"), onClick = {
                onSave(SongMetadataEntity(songId = song.id, title = title.trim(), artist = artist.trim().ifBlank { null },
                    album = album.trim().ifBlank { null }, albumArtist = albumArtist.trim().ifBlank { null },
                    genre = genre.trim().ifBlank { null }, releaseYear = year.trim().ifBlank { null },
                    trackNumber = track.toIntOrNull(), discNumber = disc.toIntOrNull(), artworkUri = artwork.trim().ifBlank { null },
                    lyrics = lyrics.trim().ifBlank { null }, syncedLyrics = synced.trim().ifBlank { null },
                    isrc = song.isrc, musicBrainzId = song.musicBrainzId, spotifyId = song.spotifyId, youtubeUrl = song.youtubeUrl,
                    isIdentified = song.isIdentified, isManuallyEdited = true))
            }) { Text("Save changes") }
        }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun MetadataField(label: String, value: String, onChange: (String) -> Unit, tag: String = "", number: Boolean = false,
    multiline: Boolean = false, error: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Text(label, style = MaterialTheme.typography.labelLarge)
    OutlinedTextField(value, onChange, modifier = Modifier.fillMaxWidth().testTag(tag).semantics { contentDescription = label },
        singleLine = !multiline, minLines = if (multiline) 3 else 1, maxLines = if (multiline) 6 else 1,
        isError = error, supportingText = if (error) { { Text("Enter a positive whole number") } } else null,
        keyboardOptions = KeyboardOptions(keyboardType = if (number) KeyboardType.Number else KeyboardType.Text),
        shape = MaterialTheme.shapes.small)
    }
}
