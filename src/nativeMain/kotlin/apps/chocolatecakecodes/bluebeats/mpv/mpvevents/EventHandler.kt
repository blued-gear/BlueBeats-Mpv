package apps.chocolatecakecodes.bluebeats.mpv.mpvevents

import apps.chocolatecakecodes.bluebeats.mpv.PlaylistRunner
import apps.chocolatecakecodes.bluebeats.mpv.PluginContext
import apps.chocolatecakecodes.bluebeats.mpv.exception.MpvException
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalForeignApi::class)
internal class EventHandler(
    private val pluginContext: PluginContext
) {

    /*TODO
        write a EventConsumer interface. it lists the event which should be listened to and this class registers and delivers them
        also the interface defines a terminate() function which is called at stopLoop()
        PlaylistRunner is a consumer
     */

    private var running = false

    fun startLoop() {
        running = true
        registerEvents()
        runLoop()
    }

    fun stopLoop() {
        running = false
    }

    private fun registerEvents() {
        mpv.mpv_hook_add(pluginContext.mpvCtx, 0UL, "on_load_fail", 1)
            .checkReturnCode("register hook on_load_fail")
    }

    private fun runLoop() {
        val looper = CoroutineScope(Dispatchers.Default).launch {
            while(running)
                processEvent()
        }
        runBlocking {
            looper.join()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun processEvent() {
        val event = mpv.mpv_wait_event(pluginContext.mpvCtx, -1.0)!!
        when(event.pointed.event_id) {
            mpv.MPV_EVENT_HOOK -> {
                val hookEvent = event.pointed.data as CPointer<mpv.mpv_event_hook>
                when(hookEvent.pointed.name!!.toKString()) {
                    "on_load_fail" -> handleOnLoadFail(hookEvent)
                }
            }
        }
    }

    private suspend fun handleOnLoadFail(event: CPointer<mpv.mpv_event_hook>) {
        try {
            val filePath = mpv.mpv_get_property_string(pluginContext.mpvCtx, "path")?.toKString()
            println("#### got failed file: $filePath")
            if(filePath != null && filePath.endsWith(".bbdp"))
                PlaylistRunner(pluginContext, filePath).run()
        } finally {
            mpv.mpv_hook_continue(pluginContext.mpvCtx, event.pointed.id)
        }
    }

    private fun Int.checkReturnCode(context: String) {
        if(this != 0)
            throw MpvException("EventHandler: $context resulted in return-code $this")
    }
}
