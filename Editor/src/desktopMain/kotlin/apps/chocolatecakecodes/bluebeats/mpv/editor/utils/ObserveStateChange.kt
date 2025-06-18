package apps.chocolatecakecodes.bluebeats.mpv.editor.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State

/**
 * runs `block(oldState, newState)` whenever state changed (so not before first change)
 */
@Composable
internal inline fun <T> observerStateChange(state: State<T>, crossinline block: (T, T) -> Unit) {
    DisposableEffect(state.value) {
        val lastValue = state.value
        this.onDispose {
            block(lastValue, state.value)
        }
    }
}
