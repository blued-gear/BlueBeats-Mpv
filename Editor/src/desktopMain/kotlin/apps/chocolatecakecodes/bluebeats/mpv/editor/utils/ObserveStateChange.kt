package apps.chocolatecakecodes.bluebeats.mpv.editor.utils

import androidx.compose.runtime.*

/**
 * Runs `block(oldState, newState)` whenever state changed (so not before first change).
 */
@Composable
internal inline fun <T> observeStateChange(state: State<T>, crossinline block: (T, T) -> Unit) {
    var expectedStateRef by remember { mutableStateOf(state) }
    var lastValue by remember { mutableStateOf(state.value) }

    LaunchedEffect(state.value, state) {
        if(state === expectedStateRef) {
            block(lastValue, state.value)
        } else {
            expectedStateRef = state
        }
        lastValue = state.value
    }
}
