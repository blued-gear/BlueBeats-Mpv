package apps.chocolatecakecodes.bluebeats.mpv.mpvevents

import apps.chocolatecakecodes.bluebeats.mpv.PlaylistRunner
import apps.chocolatecakecodes.bluebeats.mpv.PluginContext
import apps.chocolatecakecodes.bluebeats.mpv.utils.MpvPropertyHelper
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import mpv.mpv_event_hook

@OptIn(ExperimentalForeignApi::class)
internal class PlaylistInitiator(
    private val pluginContext: PluginContext,
) : EventConsumer {

    override val requestedHooks: Set<String> = setOf("on_load_fail")

    override suspend fun onHook(hookId: String, event: CPointer<mpv_event_hook>) {
        if(hookId != "on_load_fail") return

        val filePath = MpvPropertyHelper.getString(pluginContext.mpvCtx, "path")
        if(filePath != null && filePath.endsWith(".bbdp")) {
            PlaylistRunner(pluginContext, filePath).start()
        }
    }
}
