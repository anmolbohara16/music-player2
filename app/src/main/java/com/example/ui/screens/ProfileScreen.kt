package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import com.example.data.ExcludedSongEntity
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Playlist
import com.example.model.Song
import com.example.ui.components.AlbumArtImage
import com.example.ui.components.SongListItem
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleDarkText
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlassCardBackground
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassCardBorderSubtle
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun ProfileScreen(
    allSongs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    playlists: List<Playlist>,
    favoriteSongs: List<Song>,
    recentlyPlayed: List<Song>,
    excludedSongs: List<ExcludedSongEntity>,
    currentSong: Song?,
    isPlaying: Boolean,
    isBatchSearching: Boolean,
    isScanning: Boolean,
    onPlaySong: (Song, List<Song>) -> Unit,
    onNavigateToSongs: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToPlaylists: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onNavigateToMostPlayed: () -> Unit,
    onLibraryWebSearch: () -> Unit,
    onRescanLibrary: () -> Unit,
    onRestoreExcludedSong: (Long) -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onShowSongInfo: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier.fillMaxSize().testTag("profile_screen"), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Library", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                    Text("${allSongs.size} songs · ${albums.size} albums · ${artists.size} artists", color = TextSecondary)
                }
                IconButton(onClick = onOpenSettings) { Icon(Icons.Rounded.Settings, "Settings", tint = TextPrimary) }
            }
        }
        item {
            LibrarySection("Collection") {
                LibraryAction("Favorites", "${favoriteSongs.size} saved tracks", Icons.Rounded.Favorite, onNavigateToFavorites, AccentPink)
                LibraryAction("Playlists", "${playlists.size} personal mixes", Icons.AutoMirrored.Rounded.QueueMusic, onNavigateToPlaylists, AccentCyan)
                LibraryAction("Most played", "Tracks you return to", Icons.Rounded.History, onNavigateToMostPlayed, AccentPurple)
                LibraryAction("All songs", "Browse your device audio", Icons.Rounded.MusicNote, onNavigateToSongs)
            }
        }
        if (recentlyPlayed.isNotEmpty()) {
            item { Text("Recently played", style = MaterialTheme.typography.titleLarge, color = TextPrimary) }
            items(recentlyPlayed.take(5), key = { "recent_${it.id}" }) { song ->
                SongListItem(song, isPlaying && currentSong?.id == song.id, currentSong?.id == song.id,
                    { onPlaySong(song, recentlyPlayed) }, { onToggleFavorite(song) }, { onAddToQueue(song) },
                    { onPlayNext(song) }, { onAddToPlaylist(song) }, { onShowSongInfo(song) })
            }
        }
        item {
            LibrarySection("Playback & audio") {
                LibraryAction("Equalizer", "Shape your sound", Icons.Rounded.Equalizer, onOpenEqualizer)
                LibraryAction("Sleep timer", "Let the music wind down", Icons.Rounded.Bedtime, onOpenSleepTimer)
            }
        }
        item {
            LibrarySection("Library & metadata") {
                LibraryAction(if (isScanning) "Scanning…" else "Rescan library", "Find audio on this device", Icons.Rounded.Refresh, onRescanLibrary)
                LibraryAction(if (isBatchSearching) "Web Search in progress" else "Web Search", "Identify recordings and review changes", Icons.Rounded.AutoAwesome, onLibraryWebSearch)
                Text("Manual edits stay protected. Online changes are saved in this app.", Modifier.padding(16.dp), color = TextSecondary)
            }
        }
        if (excludedSongs.isNotEmpty()) {
            item { Text("Hidden songs", style = MaterialTheme.typography.titleLarge, color = TextPrimary) }
            items(excludedSongs, key = { "hidden_${it.songId}" }) { excluded ->
                LibraryAction(excluded.songTitle.ifBlank { "Hidden audio" }, "Restore to library", Icons.Rounded.Restore, onClick = { onRestoreExcludedSong(excluded.songId) })
            }
        }
        item {
            LibrarySection("App information") {
                Text("Music Player · ${com.example.BuildConfig.VERSION_NAME}", Modifier.padding(top = 8.dp), color = TextPrimary)
                Text("Your music stays on this device.", color = TextSecondary)
            }
        }
    }
}

@Composable
private fun LibraryStat(label: String, value: String, modifier: Modifier) {
    androidx.compose.material3.Surface(modifier, color = DarkCard, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = AccentPurple)
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun LibrarySection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        Column { content() }
    }
}

@Composable
private fun LibraryAction(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit, tint: Color = AccentPurple) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(tint.copy(alpha = .16f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp)) }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}
