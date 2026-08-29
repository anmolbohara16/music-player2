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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Song
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlassCardBorderSubtle
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DeleteSongDialog(
    song: Song?,
    onDismiss: () -> Unit,
    onRemoveFromApp: (Song) -> Unit,
    onPermanentlyDelete: (Song) -> Unit
) {
    if (song == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("delete_song_dialog"),
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Remove Song?",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Compact Song Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkCard)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AlbumArtImage(
                        song = song,
                        modifier = Modifier.size(36.dp),
                        cornerRadius = 6.dp,
                        fallbackIconSize = 18.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Choice 1: Remove from App (Temporary removal from this app)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, GlassCardBorderSubtle, RoundedCornerShape(12.dp))
                        .clickable { onRemoveFromApp(song) }
                        .testTag("remove_from_app_button"),
                    color = DarkElevated
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RemoveCircleOutline,
                            contentDescription = null,
                            tint = AccentPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Remove from App",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Temporary removal from this app",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Choice 2: Permanently Delete (Permanently delete the actual file)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, AccentPink.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { onPermanentlyDelete(song) }
                        .testTag("permanently_delete_button"),
                    color = AccentPink.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteForever,
                            contentDescription = null,
                            tint = AccentPink,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Permanently Delete",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = AccentPink
                            )
                            Text(
                                text = "Permanently delete the actual file",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentPink.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("delete_dialog_cancel_button")
            ) {
                Text(
                    text = "Cancel",
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}
