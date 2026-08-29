package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlassCardBorderSubtle
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerDialog(
    sleepTimerSecondsLeft: Long?,
    onDismiss: () -> Unit,
    onSetTimerMinutes: (Int) -> Unit,
    onCancelTimer: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val predefinedOptions = listOf(5, 10, 15)

    var showCustomInput by remember { mutableStateOf(false) }
    var customMinutesText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                .padding(bottom = 28.dp)
                .testTag("sleep_timer_sheet")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Bedtime,
                    contentDescription = null,
                    tint = AccentEmerald,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Sleep Timer",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    if (sleepTimerSecondsLeft != null) {
                        val min = sleepTimerSecondsLeft / 60
                        val sec = sleepTimerSecondsLeft % 60
                        Text(
                            text = "Stopping playback in %02d:%02d".format(min, sec),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AccentEmerald
                        )
                    } else {
                        Text(
                            text = "Automatically stop playback after duration",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Exactly 3 Predefined Options: 5, 10, 15 minutes
                predefinedOptions.forEach { minutes ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkCard)
                            .border(1.dp, GlassCardBorderSubtle, RoundedCornerShape(12.dp))
                            .clickable {
                                onSetTimerMinutes(minutes)
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .testTag("timer_option_${minutes}_min"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$minutes minutes",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Set",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = AccentCyan
                        )
                    }
                }

                // Custom Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (showCustomInput) AccentPurple.copy(alpha = 0.15f) else DarkCard)
                        .border(
                            1.dp,
                            if (showCustomInput) AccentPurple else GlassCardBorderSubtle,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            showCustomInput = !showCustomInput
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .testTag("timer_option_custom"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null,
                            tint = AccentPurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Custom",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = if (showCustomInput) "Close" else "Enter duration",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = AccentPurple
                    )
                }

                // Custom Input Expandable Section
                AnimatedVisibility(
                    visible = showCustomInput,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkElevated)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Enter duration in minutes:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = customMinutesText,
                                onValueChange = { newValue ->
                                    if (newValue.all { it.isDigit() } && newValue.length <= 4) {
                                        customMinutesText = newValue
                                        errorMessage = null
                                    }
                                },
                                placeholder = { Text("e.g. 7, 20, 45", color = TextSecondary.copy(alpha = 0.5f)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        val minutes = customMinutesText.toIntOrNull()
                                        if (minutes != null && minutes > 0) {
                                            onSetTimerMinutes(minutes)
                                        } else {
                                            errorMessage = "Please enter valid minutes"
                                        }
                                    }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentPurple,
                                    unfocusedBorderColor = GlassCardBorderSubtle,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = AccentPurple
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("custom_timer_input")
                            )

                            Button(
                                onClick = {
                                    val minutes = customMinutesText.toIntOrNull()
                                    if (minutes != null && minutes > 0) {
                                        onSetTimerMinutes(minutes)
                                    } else {
                                        errorMessage = "Please enter valid minutes"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("set_custom_timer_button")
                            ) {
                                Text("Set", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = AccentPink
                            )
                        }
                    }
                }

                // Turn Off Timer if active
                if (sleepTimerSecondsLeft != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentPink.copy(alpha = 0.15f))
                            .clickable { onCancelTimer() }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .testTag("cancel_timer_button"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Turn Off Timer",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = AccentPink
                        )
                        Icon(
                            imageVector = Icons.Rounded.Cancel,
                            contentDescription = null,
                            tint = AccentPink
                        )
                    }
                }
            }
        }
    }
}
