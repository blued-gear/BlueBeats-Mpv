package apps.chocolatecakecodes.bluebeats.mpv.editor

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import apps.chocolatecakecodes.bluebeats.mpv.editor.utils.Logger
import io.github.vinceglb.filekit.FileKit
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.log.Logger as ILogger

fun main() {
    application {
        ILogger.Slot.INSTANCE = Logger

        FileKit.init("BlueBeats-MPV_DynPl_Editor")

        Window(
            onCloseRequest = ::exitApplication,
            title = "BlueBeats MPV DynPl Editor",
        ) {
            App()
        }
    }
}
