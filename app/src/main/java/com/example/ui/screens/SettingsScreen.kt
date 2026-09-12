package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.components.AppScreenHeader

@Composable
fun SettingsScreen(
    equalizerPreset: String,
    sleepTimerSecondsLeft: Long?,
    isScanning: Boolean,
    isBatchSearching: Boolean,
    songCount: Int,
    onBack: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onRescanLibrary: () -> Unit,
    onOpenWebSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            AppScreenHeader(
                title = "Settings",
                subtitle = "Tune the way your library and player behave",
                modifier = Modifier.padding(horizontal = 0.dp),
                trailing = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, contentDescription = "Back") } }
            )
        }
        item { SettingsGroup("Playback") {
            SettingsAction("Equalizer", equalizerPreset, Icons.Rounded.Equalizer, onOpenEqualizer)
            SettingsAction("Sleep timer", sleepTimerLabel(sleepTimerSecondsLeft), Icons.Rounded.Bedtime, onOpenSleepTimer)
        } }
        item { SettingsGroup("Library") {
            SettingsAction("Rescan library", "$songCount songs indexed", Icons.Rounded.Refresh, onRescanLibrary, enabled = !isScanning)
            SettingsAction("Web Search", if (isBatchSearching) "Search in progress" else "Identify and review library metadata", Icons.Rounded.AutoAwesome, onOpenWebSearch, enabled = !isBatchSearching)
        } }
        item { SettingsGroup("Appearance") {
            Text("Light lavender and dark plum follow the device appearance.", Modifier.padding(16.dp), color = TextSecondary)
        } }
        item { SettingsGroup("Metadata and lyrics") {
            Text("Manual edits stay in the app database. Online searches never rewrite your audio files.", Modifier.padding(16.dp), color = TextSecondary)
            Text("Lyrics can be searched from the player and synchronized when timed LRC data is available.", Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), color = TextSecondary)
        } }
        item { SettingsGroup("About") {
            Text("Music Player", Modifier.padding(start = 16.dp, top = 16.dp), fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text("Local playback with reviewable metadata enrichment.", Modifier.padding(16.dp), color = TextSecondary)
        } }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainer) {
            Column(Modifier.fillMaxWidth()) { content() }
        }
    }
}

@Composable
private fun SettingsAction(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, enabled: Boolean = true) {
    TextButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = AccentPurple)
            Column(Modifier.weight(1f).padding(start = 14.dp), horizontalAlignment = Alignment.Start) {
                Text(title, color = TextPrimary)
                Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun sleepTimerLabel(seconds: Long?): String = when {
    seconds == null -> "Off"
    seconds < 60 -> "${seconds}s remaining"
    else -> "${seconds / 60}m remaining"
}