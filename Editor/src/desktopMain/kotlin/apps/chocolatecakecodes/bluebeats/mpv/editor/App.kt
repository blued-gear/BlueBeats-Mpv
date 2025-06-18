package apps.chocolatecakecodes.bluebeats.mpv.editor

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import apps.chocolatecakecodes.bluebeats.mpv.editor.views.EditView
import apps.chocolatecakecodes.bluebeats.mpv.editor.views.StartView

@Composable
internal fun App() {
    MaterialTheme {
        var view by remember { mutableStateOf(View.START) }
        when(view) {
            View.START -> {
                StartView { view = View.EDIT }
            }
            View.EDIT -> {
                EditView()
            }
        }
    }
}

private enum class View {
    START,
    EDIT
}
