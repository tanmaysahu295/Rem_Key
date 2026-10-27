package dev.remkey

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.remkey.relay.RelayCommand
import dev.remkey.relay.RelayConnectionState
import dev.remkey.touchpad.TouchpadGestures
import dev.remkey.ui.screens.ConnectScreen
import dev.remkey.ui.screens.KeyboardScreen
import dev.remkey.ui.screens.TouchpadScreen
import dev.remkey.ui.theme.RemKeyTheme

sealed class Screen {
    object Touchpad : Screen()
    object Keyboard : Screen()
    object Voice : Screen()
}

class MainActivity : ComponentActivity() {
    private val relay: RelayViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RemKeyTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val state by relay.state.collectAsStateWithLifecycle()
                    val desktop by relay.pairedDesktop.collectAsStateWithLifecycle()
                    val codeError by relay.pairingCodeError.collectAsStateWithLifecycle()
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Touchpad) }

                    Box(Modifier.safeDrawingPadding()) {
                        Crossfade(
                            targetState = state == RelayConnectionState.Connected,
                            label = "screen",
                        ) { connected ->
                            if (connected) {
                                ConnectedContent(
                                    currentScreen = currentScreen,
                                    onScreenSelected = { currentScreen = it },
                                    desktopLabel = desktop?.label.orEmpty(),
                                    gestures = relay.gestures,
                                    onCommand = relay::send,
                                    onDisconnect = relay::disconnect,
                                )
                            } else {
                                ConnectScreen(
                                    state = state,
                                    desktop = desktop,
                                    pairingCodeError = codeError,
                                    onConnect = relay::connect,
                                    onPair = relay::pair,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectedContent(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    desktopLabel: String,
    gestures: TouchpadGestures,
    onCommand: (RelayCommand) -> Unit,
    onDisconnect: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        when (currentScreen) {
            Screen.Touchpad -> TouchpadScreen(
                desktopLabel = desktopLabel,
                gestures = gestures,
                onCommand = onCommand,
                onDisconnect = onDisconnect,
                onNavigateToKeyboard = { onScreenSelected(Screen.Keyboard) },
            )
            Screen.Keyboard -> KeyboardScreen(
                desktopLabel = desktopLabel,
                onCommand = onCommand,
                onDisconnect = onDisconnect,
                onNavigateToTouchpad = { onScreenSelected(Screen.Touchpad) },
            )
            Screen.Voice -> {
                // Voice screen placeholder — implemented in Phase 2.
                Text("Voice input coming soon", style = MaterialTheme.typography.bodyLarge)
            }
        }

        NavigationBar(
            modifier = Modifier.align(alignment = Alignment.BottomCenter),
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            NavigationBarItem(
                selected = currentScreen == Screen.Touchpad,
                onClick = { onScreenSelected(Screen.Touchpad) },
                icon = { Box(Modifier.size(24.dp)) },
                label = { Text("Touchpad") },
            )
            NavigationBarItem(
                selected = currentScreen == Screen.Keyboard,
                onClick = { onScreenSelected(Screen.Keyboard) },
                icon = { Box(Modifier.size(24.dp)) },
                label = { Text("Keyboard") },
            )
            NavigationBarItem(
                selected = currentScreen == Screen.Voice,
                onClick = { onScreenSelected(Screen.Voice) },
                icon = { Box(Modifier.size(24.dp)) },
                label = { Text("Voice") },
            )
        }
    }
}
