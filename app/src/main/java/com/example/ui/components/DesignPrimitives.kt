package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.OnPrimaryAction
import com.example.ui.theme.PrimaryAction
import com.example.ui.theme.SupportingSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun AppScreenHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun AppSectionHeader(
    title: String,
    count: Int? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        if (count != null) Text("  $count", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
        Spacer(Modifier.weight(1f))
        if (actionLabel != null && onAction != null) {
            androidx.compose.material3.TextButton(onClick = onAction) { Text(actionLabel, color = PrimaryAction) }
        }
    }
}

@Composable
fun CollectionPlaybackActions(
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    playLabel: String = "Play all",
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = onPlay,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAction, contentColor = OnPrimaryAction),
            shape = MaterialTheme.shapes.small
        ) {
            Icon(Icons.Rounded.PlayArrow, contentDescription = null, Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(playLabel, fontWeight = FontWeight.SemiBold)
        }
        OutlinedButton(
            onClick = onShuffle,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryAction),
            shape = MaterialTheme.shapes.small
        ) {
            Icon(Icons.Rounded.Shuffle, contentDescription = null, Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Shuffle", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun EmptyState(title: String, message: String? = null, icon: ImageVector, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        if (!message.isNullOrBlank()) Text(message, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
    }
}