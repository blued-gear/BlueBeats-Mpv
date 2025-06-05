@file:OptIn(ExperimentalNativeApi::class, ExperimentalForeignApi::class)

import apps.chocolatecakecodes.bluebeats.mpv.PluginContext
import apps.chocolatecakecodes.bluebeats.mpv.mpvevents.EventHandler
import apps.chocolatecakecodes.bluebeats.mpv.utils.Logger
import cnames.structs.mpv_handle
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.usePinned
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.experimental.ExperimentalNativeApi
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.log.Logger as ILogger

@OptIn(ExperimentalSerializationApi::class)
@CName("mpv_open_cplugin")
@Suppress("unused")
fun main(mpvCtx: CPointer<mpv_handle>): Int {
    println("### main()")

    setupBBLibSlots()

    val ctx = PluginContext(mpvCtx)
    ctx.usePinned { ctx ->
        try {
            EventHandler(ctx.get()).startLoop()
        } catch(e: Throwable) {
            Logger.error("Main", "uncaught exception", e)
            return -1
        }
    }

    return 0
}

private fun setupBBLibSlots() {
    ILogger.Slot.INSTANCE = Logger
}
