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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Playlist
import com.example.model.Song
import com.example.ui.components.AlbumArtImage
import com.example.ui.components.SongListItem
import com.example.ui.components.AppSectionHeader
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleDarkText
import com.example.ui.theme.AccentPurpleDeep
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlassActiveNav
import com.example.ui.theme.GlassBadgeBg
import com.example.ui.theme.GlassCardBackground
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassCardBorderSubtle
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun HomeScreen(
    allSongs: List<Song>,
    recentlyPlayed: List<Song>,
    recentlyAdded: List<Song>,
    favoriteSongs: List<Song>,
    playlists: List<Playlist>,
    currentSong: Song?,
    isPlaying: Boolean,
    onPlaySong: (Song, List<Song>) -> Unit,
    onShuffleAll: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenProfile: () -> Unit = {},
    onOpenPlaylist: (Playlist) -> Unit,
    onCreatePlaylist: () -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onShowSongInfo: (Song) -> Unit,
    onDeleteSong: (Song) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        // Frosted Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good music.\nYour space.",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = TextPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Glass Search Icon Button
                    IconButton(
                        onClick = onOpenSearch,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(GlassCardBackground)
                            .border(1.dp, GlassCardBorder, CircleShape)
                            .testTag("home_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Avatar / Profile Pill in Light Purple
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AccentPurple)
                            .clickable(onClick = onOpenProfile),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "User profile",
                            tint = AccentPurpleDarkText,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Frosted Glass Bento Section: Quick Access
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val heroSong = recentlyPlayed.firstOrNull() ?: recentlyAdded.firstOrNull()
                androidx.compose.material3.Surface(shape = MaterialTheme.shapes.medium, color = GlassCardBackground,
                    modifier = Modifier.fillMaxWidth().testTag("hero_quick_play_card")) {
                    Row(Modifier.fillMaxWidth().clickable(enabled = heroSong != null) {
                        heroSong?.let { onPlaySong(it, if (recentlyPlayed.isNotEmpty()) recentlyPlayed else allSongs) }
                    }.padding(20.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(if (recentlyPlayed.isNotEmpty()) "PICK UP THE MOOD" else "MADE FOR YOUR MUSIC",
                                style = MaterialTheme.typography.labelSmall, color = AccentPurple)
                            Text(heroSong?.title ?: "Your next favorite is already yours.", style = MaterialTheme.typography.headlineSmall,
                                color = TextPrimary, maxLines = 3, overflow = TextOverflow.Ellipsis)
                            Text(heroSong?.artist ?: "Add audio to your device, then rescan your library.", color = TextSecondary)
                            if (heroSong != null) Text("Listen again  →", color = AccentPurple, style = MaterialTheme.typography.labelLarge)
                        }
                        if (heroSong != null) AlbumArtImage(heroSong, Modifier.size(108.dp), cornerRadius = 18.dp)
                    }
                }

                // Bento Grid Row (Favorites & Playlists / Shuffle)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Bento: Favorites
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 128.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(AccentPurpleDeep.copy(alpha = 0.35f))
                            .border(1.dp, GlassCardBorder, MaterialTheme.shapes.medium)
                            .clickable {
                                val favs = favoriteSongs
                                if (favs.isNotEmpty()) {
                                    onPlaySong(favs.first(), favs)
                                }
                            }
                            .padding(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Favorite,
                                contentDescription = "Favorites",
                                tint = AccentPurple,
                                modifier = Modifier.size(26.dp)
                            )
                            Column {
                                Text(
                                    text = "Favorites",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${favoriteSongs.size} tracks",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // Right Bento: Playlists
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 128.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(GlassCardBackground)
                            .border(1.dp, GlassCardBorder, RoundedCornerShape(24.dp))
                            .clickable { onCreatePlaylist() }
                            .padding(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.QueueMusic,
                                contentDescription = "Playlists",
                                tint = TextSecondary,
                                modifier = Modifier.size(26.dp)
                            )
                            Column {
                                Text(
                                    text = "Playlists",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${playlists.size} playlists",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Recently Played Carousel
        if (recentlyPlayed.isNotEmpty()) {
            item {
                AppSectionHeader(title = "Recently played", count = recentlyPlayed.size)
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(recentlyPlayed) { song ->
                        val isThisPlaying = isPlaying && currentSong?.id == song.id
                        RecentSongCard(
                            song = song,
                            isPlaying = isThisPlaying,
                            onClick = { onPlaySong(song, recentlyPlayed) }
                        )
                    }
                }
            }
        }

        // Section: Playlists Carousel
        item {
            AppSectionHeader(
                title = "Playlists",
                count = playlists.size,
                actionLabel = "+ New",
                onAction = onCreatePlaylist
            )
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                // New Playlist Glass Card
                item {
                    Box(
                        modifier = Modifier
                            .width(135.dp)
                            .height(135.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(GlassCardBackground)
                            .border(1.dp, GlassCardBorder, RoundedCornerShape(22.dp))
                            .clickable { onCreatePlaylist() }
                            .testTag("home_add_playlist_card"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(GlassActiveNav),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "New Playlist",
                                    tint = AccentPurple,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "New Playlist",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                        }
                    }
                }

                items(playlists, key = { it.id }) { playlist ->
                    Box(
                        modifier = Modifier
                            .width(135.dp)
                            .height(135.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(GlassCardBackground)
                            .border(1.dp, GlassCardBorder, RoundedCornerShape(22.dp))
                            .clickable { onOpenPlaylist(playlist) }
                            .padding(14.dp)
                            .testTag("home_playlist_${playlist.id}")
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GlassActiveNav),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.QueueMusic,
                                    contentDescription = null,
                                    tint = AccentPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = playlist.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Playlist",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Recently Added Music List
        if (recentlyAdded.isNotEmpty()) {
            item {
                AppSectionHeader(title = "Recently added", count = recentlyAdded.size)
            }
            items(recentlyAdded.take(10), key = { it.id }) { song ->
                val isThisPlaying = isPlaying && currentSong?.id == song.id
                val isCurrent = currentSong?.id == song.id
                SongListItem(
                    song = song,
                    isPlaying = isThisPlaying,
                    isCurrentSong = isCurrent,
                    onClick = { onPlaySong(song, recentlyAdded) },
                    onToggleFavorite = { onToggleFavorite(song) },
                    onAddToQueue = { onAddToQueue(song) },
                    onPlayNext = { onPlayNext(song) },
                    onAddToPlaylist = { onAddToPlaylist(song) },
                    onShowInfo = { onShowSongInfo(song) },
                    onDeleteSong = { onDeleteSong(song) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    count: Int? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.3).sp
                ),
                color = TextPrimary
            )
            if (count != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "($count)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        if (actionText != null && onAction != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = AccentPurple,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onAction() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun RecentSongCard(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(135.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(GlassCardBackground)
            .border(1.dp, GlassCardBorder, RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .testTag("recent_song_${song.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            ) {
                AlbumArtImage(
                    song = song,
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = 0.dp,
                    fallbackIconSize = 40.dp
                )

                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(AccentPurple)
                            .padding(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Playing",
                            tint = AccentPurpleDarkText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

