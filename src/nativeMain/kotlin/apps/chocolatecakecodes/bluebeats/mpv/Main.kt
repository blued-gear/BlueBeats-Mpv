@file:OptIn(ExperimentalNativeApi::class, ExperimentalForeignApi::class)

import apps.chocolatecakecodes.bluebeats.mpv.PluginContext
import apps.chocolatecakecodes.bluebeats.mpv.mpvevents.EventHandler
import cnames.structs.mpv_handle
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.usePinned
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalSerializationApi::class)
@CName("mpv_open_cplugin")
@Suppress("unused")
fun main(mpvCtx: CPointer<mpv_handle>): Int {
    println("### plugin starting")

    val ctx = PluginContext(mpvCtx)
    ctx.usePinned { ctx ->
        try {
            EventHandler(ctx.get()).startLoop()
        } catch(e: Throwable) {
            println("### plugin error:")
            e.printStackTrace()
            println()
            return -1
        }
    }

    return 0
}
