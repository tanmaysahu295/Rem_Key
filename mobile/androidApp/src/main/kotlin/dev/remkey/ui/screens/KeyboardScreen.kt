package dev.remkey.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.remkey.relay.EditingKey
import dev.remkey.relay.RelayCommand
import dev.remkey.ui.theme.Space
import dev.remkey.ui.theme.connected

/** Dedicated keyboard screen: Gboard (system keyboard) handles all printable text;
 * this screen only provides committed-text send plus special keys.
 */
@Composable
fun KeyboardScreen(
    desktopLabel: String,
    onCommand: (RelayCommand) -> Unit,
    onDisconnect: () -> Unit,
    onNavigateToTouchpad: () -> Unit,
) {
    var text by rememberSaveable { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = Space.m, vertical = Space.s),
    ) {
        Header(desktopLabel, onDisconnect, onNavigateToTouchpad)
        Spacer(Modifier.height(Space.s))

        // Full-screen text field — Gboard appears on focus.
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            androidx.compose.material3.TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Type…", style = MaterialTheme.typography.bodyLarge) },
                singleLine = false,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                    autoCorrectEnabled = true,
                ),
                keyboardActions = KeyboardActions(onSend = {
                    if (text.isNotBlank()) {
                        onCommand(RelayCommand.CommitText(text))
                        text = ""
                    }
                }),
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Space.l, vertical = Space.l),
            )
        }

        Spacer(Modifier.height(Space.m))

        // Special keys — tap = press+release.
        SpecialKeyRow(onCommand)
    }
}

@Composable
private fun Header(
    desktopLabel: String,
    onDisconnect: () -> Unit,
    onNavigateToTouchpad: () -> Unit,
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
        TextButton(onClick = onNavigateToTouchpad) {
            Text("Touchpad", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TextButton(onClick = onDisconnect) {
            Text("Disconnect", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private val specialKeys = listOf(
    "esc" to EditingKey.Escape,
    "tab" to EditingKey.Tab,
    "⌫" to EditingKey.Backspace,
    "⏎" to EditingKey.Enter,
    "↑" to EditingKey.ArrowUp,
    "↓" to EditingKey.ArrowDown,
    "←" to EditingKey.ArrowLeft,
    "→" to EditingKey.ArrowRight,
    "home" to EditingKey.Home,
    "end" to EditingKey.End,
    "pgup" to EditingKey.PageUp,
    "pgdn" to EditingKey.PageDown,
)

@Composable
private fun SpecialKeyRow(onCommand: (RelayCommand) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Space.xs),
    ) {
        for ((label, key) in specialKeys) {
            TextButton(
                onClick = {
                    onCommand(RelayCommand.Key(key, pressed = true))
                    onCommand(RelayCommand.Key(key, pressed = false))
                },
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}
