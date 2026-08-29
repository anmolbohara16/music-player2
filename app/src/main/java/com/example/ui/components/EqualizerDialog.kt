package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class EqualizerPreset(
    val name: String,
    val description: String,
    val curve: List<Float> // 5-band gain factors (-1.0 to 1.0)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerDialog(
    currentPreset: String,
    onDismiss: () -> Unit,
    onSelectPreset: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val presets = listOf(
        EqualizerPreset("Balanced", "Studio flat profile with true natural harmonics", listOf(0f, 0f, 0f, 0f, 0f)),
        EqualizerPreset("Bass Boost", "Deep punchy sub-bass & warmth for modern beats", listOf(0.75f, 0.5f, 0f, 0.1f, 0.2f)),
        EqualizerPreset("Synthwave", "Enhanced analog low-mids and shimmering high frequencies", listOf(0.6f, 0.3f, 0.1f, 0.5f, 0.7f)),
        EqualizerPreset("Vocal Clarity", "Clear midrange focus for acoustic & vocal lyrics", listOf(-0.2f, 0.3f, 0.8f, 0.4f, 0.1f)),
        EqualizerPreset("Chill Lo-Fi", "Warm vinyl saturation with rolled-off treble", listOf(0.4f, 0.5f, 0.2f, -0.2f, -0.4f)),
        EqualizerPreset("Rock & Metal", "High dynamic range with driven guitars & crisp drums", listOf(0.5f, 0.2f, -0.1f, 0.4f, 0.6f))
    )

    val activePreset = presets.find { it.name == currentPreset } ?: presets.first()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        scrimColor = DarkBackground.copy(alpha = 0.75f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DarkElevated)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 24.dp)
                .testTag("equalizer_sheet")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Equalizer,
                    contentDescription = null,
                    tint = AccentPink,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Audio Equalizer",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Hardware DSP audio sound profile",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5-Band Graphic Preview Visualizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkCard)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val bandLabels = listOf("60Hz", "230Hz", "910Hz", "3.6kHz", "14kHz")
                    activePreset.curve.forEachIndexed { i, factor ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            val barHeight = ((factor + 1f) / 2f).coerceIn(0.15f, 0.95f)
                            Box(
                                modifier = Modifier
                                    .width(22.dp)
                                    .fillMaxHeight(barHeight)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(AccentPurple, AccentCyan)
                                        )
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = bandLabels[i],
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.forEach { preset ->
                    val isSelected = preset.name == currentPreset
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) DarkElevated else DarkCard)
                            .clickable { onSelectPreset(preset.name) }
                            .padding(14.dp)
                            .testTag("eq_preset_${preset.name.replace(" ", "_")}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) AccentCyan else TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = preset.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
