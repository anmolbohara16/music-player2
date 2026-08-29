package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var playlistName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Playlist",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Give your playlist a name:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    placeholder = { Text("e.g. Chill Session, Roadtrip Hits", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = DarkElevated,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = AccentCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("playlist_name_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (playlistName.isNotBlank()) {
                        onCreate(playlistName.trim())
                    }
                },
                enabled = playlistName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentPurple,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_create_playlist_button")
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_create_playlist_button")
            ) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkCard,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.testTag("create_playlist_dialog")
    )
}
