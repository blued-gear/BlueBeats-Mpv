package apps.chocolatecakecodes.bluebeats.mpv

import apps.chocolatecakecodes.bluebeats.mpv.mpvevents.EventHandler
import cnames.structs.mpv_handle
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
internal class PluginContext(
    val mpvCtx: CPointer<mpv_handle>,
) {
    lateinit var eventHandler: EventHandler
}
