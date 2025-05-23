package apps.chocolatecakecodes.bluebeats.mpv

import cnames.structs.mpv_handle
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
internal class PluginContext(
    val mpvCtx: CPointer<mpv_handle>
)
