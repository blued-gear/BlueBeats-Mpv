package apps.chocolatecakecodes.bluebeats.mpv.player

import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.BasicPlayer
import apps.chocolatecakecodes.bluebeats.blueplaylists.interfaces.media.TimeSpanItemPlayerController
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.items.TimeSpanItem
import apps.chocolatecakecodes.bluebeats.mpv.mpvevents.EventConsumer
import apps.chocolatecakecodes.bluebeats.mpv.utils.MpvPropertyHelper
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*
import mpv.mpv_event

@OptIn(ExperimentalForeignApi::class)
internal class TimeSpanItemPlayerControllerImpl : TimeSpanItemPlayerController, EventConsumer {

    object Factory : TimeSpanItemPlayerController.Factory {
        override fun create(): TimeSpanItemPlayerController {
            return TimeSpanItemPlayerControllerImpl()
        }
    }

    override val requestedEvents: Set<UInt> = setOf(mpv.MPV_EVENT_FILE_LOADED)

    private lateinit var item: TimeSpanItem
    private lateinit var player: PlayerControl
    private var job: Job? = null
    private var fileLoaded = false

    override fun init(item: TimeSpanItem, player: BasicPlayer) {
        this.item = item
        this.player = player as PlayerControl
    }

    override fun register(callback: () -> Unit) {
        job = CoroutineScope(Dispatchers.Default).launch {
            player.pluginContext.eventHandler.registerEventConsumer(this@TimeSpanItemPlayerControllerImpl)
            callback()

            runLoop()
        }
    }

    override suspend fun unregister() {
        job?.cancel("stopped")
        player.pluginContext.eventHandler.deregisterEventConsumer(this)
    }

    override suspend fun onEvent(eventId: UInt, event: CPointer<mpv_event>) {
        when(eventId) {
            mpv.MPV_EVENT_FILE_LOADED -> {
                val currentFilePath = MpvPropertyHelper.getString(player.pluginContext.mpvCtx, "path")
                if(currentFilePath != item.file.path) {
                    unregister()
                } else {
                    fileLoaded = true
                    player.setTimePos(item.startMs)
                }
            }
        }
    }

    override suspend fun terminate() {
        job?.cancel("terminated")
    }

    private suspend fun runLoop() {
        while(true) {
            delay(100)
            if(!fileLoaded) continue

            val curTime = player.getTimePos()
            if(curTime >= item.endMs) {
                player.playNextMedia()
                unregister()
                break
            }
        }
    }
}
