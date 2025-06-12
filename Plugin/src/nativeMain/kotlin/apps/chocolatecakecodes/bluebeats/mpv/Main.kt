@file:OptIn(ExperimentalNativeApi::class, ExperimentalForeignApi::class)

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.TimeSpanItemPlayerController
import apps.chocolatecakecodes.bluebeats.mpv.PluginContext
import apps.chocolatecakecodes.bluebeats.mpv.mpvevents.AsyncCommand
import apps.chocolatecakecodes.bluebeats.mpv.mpvevents.EventHandler
import apps.chocolatecakecodes.bluebeats.mpv.mpvevents.PlaylistInitiator
import apps.chocolatecakecodes.bluebeats.mpv.player.TimeSpanItemPlayerControllerImpl
import apps.chocolatecakecodes.bluebeats.mpv.utils.Logger
import cnames.structs.mpv_handle
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.experimental.ExperimentalNativeApi
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.log.Logger as ILogger

@OptIn(ExperimentalSerializationApi::class)
@CName("mpv_open_cplugin")
@Suppress("unused")
fun main(mpvCtx: CPointer<mpv_handle>): Int {
    Logger.info("Main", "init")

    setupBBLibSlots()

    val ctx = PluginContext(mpvCtx)
    ctx.usePinned { ctx ->
        try {
            CoroutineScope(Dispatchers.Default).launch {
                val ctxRef = ctx.get()
                EventHandler(ctxRef).also {
                    ctxRef.eventHandler = it

                    it.registerEventConsumer(AsyncCommand.init(ctxRef))
                    it.registerEventConsumer(PlaylistInitiator(ctxRef))
                }.runLoop()
            }.let {
                runBlocking {
                    it.join()
                }
            }
        } catch(e: Throwable) {
            Logger.error("Main", "uncaught exception", e)
            return -1
        }
    }

    return 0
}

private fun setupBBLibSlots() {
    ILogger.Slot.INSTANCE = Logger
    TimeSpanItemPlayerController.Factory.Slot.INSTANCE = TimeSpanItemPlayerControllerImpl.Factory
}
