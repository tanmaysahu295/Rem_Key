package dev.remkey.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import dev.remkey.relay.PointerButton
import dev.remkey.relay.RelayCommand
import dev.remkey.touchpad.TouchpadGestures
import dev.remkey.ui.theme.Space
import dev.remkey.ui.theme.connected
import kotlinx.coroutines.CancellationException

@Composable
fun TouchpadScreen(
    desktopLabel: String,
    gestures: TouchpadGestures,
    onCommand: (RelayCommand) -> Unit,
    onDisconnect: () -> Unit,
    onNavigateToKeyboard: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = Space.m, vertical = Space.s),
    ) {
        Header(desktopLabel, onDisconnect, onNavigateToKeyboard)
        Spacer(Modifier.height(Space.s))
        Touchpad(gestures, Modifier.weight(1f).fillMaxWidth())
        Spacer(Modifier.height(Space.s))
        Row(horizontalArrangement = Arrangement.spacedBy(Space.s)) {
            HoldButton("Left", PointerButton.Left, onCommand, Modifier.weight(1f))
            HoldButton("Right", PointerButton.Right, onCommand, Modifier.weight(1f))
        }
    }
}

@Composable
private fun Header(
    desktopLabel: String,
    onDisconnect: () -> Unit,
    onNavigateToKeyboard: () -> Unit,
) {
    Row(Modifier.fillMaxWidth().padding(start = Space.s), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.connected))
        Spacer(Modifier.width(Space.s + Space.xs))
        Text(
            desktopLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onNavigateToKeyboard) {
            Text("Keyboard", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TextButton(onClick = onDisconnect) {
            Text("Disconnect", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Touchpad(gestures: TouchpadGestures, modifier: Modifier) {
    Box(
        modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(gestures) {
                awaitEachGesture {
                    try {
                        do {
                            val event = awaitPointerEvent()
                            for (change in event.changes) {
                                val x = change.position.x / density
                                val y = change.position.y / density
                                val id = change.id.value
                                when {
                                    change.changedToDownIgnoreConsumed() -> gestures.down(id, x, y, change.uptimeMillis)
                                    change.changedToUpIgnoreConsumed() -> gestures.up(id, change.uptimeMillis)
                                    change.positionChanged() -> gestures.move(id, x, y)
                                }
                                change.consume()
                            }
                        } while (event.changes.any { it.pressed })
                    } catch (e: CancellationException) {
                        gestures.cancel()
                        throw e
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "Move with one finger\nTap to click · two fingers to scroll",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** Pressed for as long as the finger is down, so it can be held while dragging on the pad. */
@Composable
private fun HoldButton(
    label: String,
    button: PointerButton,
    onCommand: (RelayCommand) -> Unit,
    modifier: Modifier,
) {
    var pressed by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    Box(
        modifier
            .height(56.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(if (pressed) colors.outline else colors.surfaceVariant)
            .pointerInput(button) {
                awaitEachGesture {
                    awaitFirstDown().consume()
                    pressed = true
                    onCommand(RelayCommand.Button(button, pressed = true))
                    try {
                        waitForUpOrCancellation()?.consume()
                    } finally {
                        pressed = false
                        onCommand(RelayCommand.Button(button, pressed = false))
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
    }
}
